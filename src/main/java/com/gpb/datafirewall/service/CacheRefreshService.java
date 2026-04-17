package com.gpb.datafirewall.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.dto.RefreshResult;
import com.gpb.datafirewall.properties.SyncProperties;
import com.gpb.datafirewall.service.impl.DqChecksCacheRefreshServiceImpl;
import com.gpb.datafirewall.service.impl.PoliticsCacheRefreshServiceImpl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheRefreshService {

    private final DqChecksCacheRefreshServiceImpl dqChecksRefreshService;
    private final PoliticsCacheRefreshServiceImpl politicsCacheRefreshServiceImpl;
    private final SyncProperties syncProperties;

    @Qualifier("cacheRefreshExecutor")
    private final Executor cacheRefreshExecutor;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean scheduledEnabled = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        scheduledEnabled.set(syncProperties.isEnabled());
    }

    @Scheduled(fixedDelayString = "${sync.interval:60000}")
    public void refreshCacheScheduled() {
        if (!scheduledEnabled.get()) {
            log.debug("Автоматическое обновление кэша отключено");
            return;
        }

        RefreshResult result = runRefresh("scheduler");

        if (!result.started()) {
            log.debug("Плановый запуск пропущен: обновление уже выполняется");
        }
    }

    public RefreshResult refreshCacheNow() {
        return runRefresh("manual");
    }

    public void enableScheduledRefresh() {
        scheduledEnabled.set(true);
        log.info("Обновление кэша по расписанию включено");
    }

    public void disableScheduledRefresh() {
        scheduledEnabled.set(false);
        log.info("Обновление кэша по расписанию выключено");
    }

    public boolean isScheduledRefreshEnabled() {
        return scheduledEnabled.get();
    }

    private RefreshResult runRefresh(String trigger) {
        if (!running.compareAndSet(false, true)) {
            return RefreshResult.running();
        }

        long startedAt = System.currentTimeMillis();
        long timeoutMs = syncProperties.getTimeoutMs();

        CompletableFuture<Void> f1 = null;
        CompletableFuture<Void> f2 = null;

        try {
            log.info("=== Запуск обновления кэша. trigger={} timeoutMs={} ===", trigger, timeoutMs);

            f1 = CompletableFuture.runAsync(
                    dqChecksRefreshService::refreshCaches,
                    cacheRefreshExecutor
            );

            f2 = CompletableFuture.runAsync(
                    politicsCacheRefreshServiceImpl::refreshCaches,
                    cacheRefreshExecutor
            );

            CompletableFuture.allOf(f1, f2).get(timeoutMs, TimeUnit.MILLISECONDS);

            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("=== Обновление кэша завершено успешно. trigger={} durationMs={} ===", trigger, durationMs);

            return RefreshResult.success(durationMs);

        } catch (TimeoutException e) {
            log.error("Превышен timeout обновления кэша. trigger={} timeoutMs={}", trigger, timeoutMs, e);

            cancelFuture("dqChecks", f1);
            cancelFuture("politics", f2);

            return RefreshResult.timeout(timeoutMs);

        } catch (ExecutionException e) {
            log.error("Ошибка в асинхронной задаче обновления кэша. trigger={}", trigger, e.getCause());

            cancelFuture("dqChecks", f1);
            cancelFuture("politics", f2);

            return RefreshResult.failed(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Поток обновления кэша был прерван. trigger={}", trigger, e);

            cancelFuture("dqChecks", f1);
            cancelFuture("politics", f2);

            return RefreshResult.failed("Refresh interrupted");
        } catch (Exception e) {
            log.error("Ошибка при обновлении кэша. trigger={}", trigger, e);

            cancelFuture("dqChecks", f1);
            cancelFuture("politics", f2);

            return RefreshResult.failed(e.getMessage());
        } finally {
            running.set(false);
        }
    }

    private void cancelFuture(String name, CompletableFuture<Void> future) {
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            log.warn("Задача {} отменена: {}", name, cancelled);
        }
    }
}
