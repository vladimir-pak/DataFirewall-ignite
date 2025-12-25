package com.gpb.datafirewall.cache;

import java.util.Arrays;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class IgniteConfig {

    @Value("${ignite.persistence.storagePath:/ignite-storage/db}")
    private String persistenceStoragePath;

    @Value("${ignite.persistence.walPath:/ignite-storage/wal}")
    private String persistenceWalPath;

    @Value("${ignite.persistence.walArchivePath:/ignite-storage/wal-archive}")
    private String persistenceWalArchivePath;

    @Value("${ignite.network.localAddress:127.0.0.1}")
    private String localAddress;

    /**
     * Список discovery-узлов, к которым будут подключаться клиенты/ноды.
     * Формат: host1:47500,host2:47500
     */
    @Value("${ignite.network.discoveryAddresses:127.0.0.1:47500}")
    private String discoveryAddresses;

    @Value("${ignite.network.communicationPort:47100}")
    private int communicationPort;

    @Value("${ignite.ssl.enabled:false}")  // Добавляем флаг для SSL
    private boolean sslEnabled;

    @Value("${ignite.jksPassword:#{null}}")
    private String jksPassword;

    /**
     * Порт тонкого клиента (thin client), по которому смогут подключаться внешние сервисы.
     */
    @Value("${ignite.network.clientConnectorPort:10800}")
    private int clientConnectorPort;

    @Bean(name = "igniteInstance", destroyMethod = "close")
    public Ignite igniteInstance() {
        IgniteConfiguration cfg = new IgniteConfiguration();

        // Основные настройки
        cfg.setIgniteInstanceName("datafirewall-cache");
        cfg.setPeerClassLoadingEnabled(false);
        cfg.setClientMode(false);
        cfg.setLocalHost(localAddress);

        // Конфигурация хранилища (Persistance)
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();

        DataRegionConfiguration dataRegionConfig = new DataRegionConfiguration()
                .setName("Default_Region")
                .setInitialSize(256L * 1024 * 1024) // 256 MB
                .setMaxSize(512L * 1024 * 1024)     // 512 MB
                .setPersistenceEnabled(true);

        // Устанавливаем регион по умолчанию
        storageCfg.setDefaultDataRegionConfiguration(dataRegionConfig);

        // Папка для хранения данных Ignite
        storageCfg.setStoragePath(persistenceStoragePath);
        storageCfg.setWalPath(persistenceWalPath);
        storageCfg.setWalArchivePath(persistenceWalArchivePath);

        cfg.setDataStorageConfiguration(storageCfg);

        // Discovery SPI (локальный кластер)
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.setAddresses(Arrays.asList(discoveryAddresses.split(",")));
        discoverySpi.setIpFinder(ipFinder);
        discoverySpi.setLocalAddress(localAddress);
        discoverySpi.setLocalPort(47500);
        discoverySpi.setJoinTimeout(5000);
        cfg.setDiscoverySpi(discoverySpi);

        // Communication SPI
        TcpCommunicationSpi commSpi = new TcpCommunicationSpi();
        commSpi.setLocalPort(communicationPort);
        commSpi.setTcpNoDelay(true);
        cfg.setCommunicationSpi(commSpi);

        // Коннектор тонкого клиента (thin client) — по нему будут подключаться внешние сервисы
        ClientConnectorConfiguration clientConnectorCfg = new ClientConnectorConfiguration()
                .setHost(localAddress)
                .setPort(clientConnectorPort)
                .setPortRange(10)
                .setSslEnabled(sslEnabled)
                // .setSslClientAuth(true)
                // .setSslContextFactory(sslContextFactory())
                .setThreadPoolSize(8)
                .setMaxOpenCursorsPerConnection(128);

        if (sslEnabled) {
            clientConnectorCfg.setSslClientAuth(true)
                .setSslContextFactory(sslContextFactory());
            clientConnectorCfg.setSslClientAuth(true);
        }
                        
        cfg.setClientConnectorConfiguration(clientConnectorCfg);
        
        // Аутентификация
        cfg.setAuthenticationEnabled(true);

        cfg.setNetworkTimeout(5000);
        cfg.setMetricsLogFrequency(0);

        Ignite ignite = Ignition.start(cfg);

        // ВАЖНО: активация кластера при persistence
        ignite.cluster().state(ClusterState.ACTIVE);

        return ignite;
    }

    @Bean
    @ConditionalOnProperty(name = "ignite.ssl.enabled", havingValue = "true")
    public SslContextFactory sslContextFactory() {
        if (jksPassword == null || jksPassword.isBlank() || jksPassword.isEmpty()) {
            throw new IllegalArgumentException("JKS password must be set when SSL is enabled");
        }

        SslContextFactory f = new SslContextFactory();

        char[] pass = jksPassword.toCharArray();

        f.setKeyStoreFilePath("/opt/ignite/ssl/server.jks");
        f.setKeyStorePassword(pass);

        f.setTrustStoreFilePath("/opt/ignite/ssl/trust.jks");
        f.setTrustStorePassword(pass);

        return f;
    }
}

