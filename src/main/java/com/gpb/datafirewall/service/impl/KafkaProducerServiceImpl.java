package com.gpb.datafirewall.service.impl;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.cef.SvoiLogger;
import com.gpb.datafirewall.dto.CacheUpdateMessage;
import com.gpb.datafirewall.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "spring.kafka.cache-update",
        name = "enabled",
        havingValue = "true"
)
public class KafkaProducerServiceImpl implements KafkaProducerService {
    private final SvoiLogger svoiCustomLogger;
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, CacheUpdateMessage> kafkaTemplate;

    @Value("${spring.kafka.topics.cache-update}")
    private String topic;

    public void send(String cacheName, Integer version) {
        CacheUpdateMessage message = new CacheUpdateMessage(cacheName, version);
        kafkaTemplate.send(topic, cacheName, message);

        Map<String, Object> props = kafkaProperties.buildProducerProperties();

        String bootstrapServers = resolveBootstrapServers();
        String securityProtocol = stringValue(props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        String mechanism = stringValue(props.get(SaslConfigs.SASL_MECHANISM));
        String username = resolveUsername(props);
        int port = resolvePort();

        String messageLog = String.format("securityProtocol: %s, mechanism: %s", securityProtocol, mechanism);

        svoiCustomLogger.sendKafkaMessage(
            messageLog,
            username,
            bootstrapServers,
            bootstrapServers,
            port
        );
    }

    private String resolveBootstrapServers() {
        List<String> servers = kafkaProperties.getBootstrapServers();

        return servers.stream()
                .map(this::extractHost)
                .collect(Collectors.joining(", "));
    }

    private String extractHost(String bootstrapServer) {
        int colonIndex = bootstrapServer.indexOf(':');

        if (colonIndex == -1) {
            return bootstrapServer;
        }

        return bootstrapServer.substring(0, colonIndex);
    }

    private int resolvePort() {
        List<String> servers = kafkaProperties.getBootstrapServers();
        String hostname = servers.get(0);
        int colonIndex = hostname.indexOf(':');

        if (colonIndex == -1) {
            return colonIndex;
        }

        return Integer.parseInt(hostname.substring(colonIndex + 1));
    }

    private String resolveUsername(Map<String, Object> props) {
        /*
         * SASL/PLAIN, SCRAM обычно содержит username в sasl.jaas.config.
         */
        String jaasConfig = stringValue(props.get(SaslConfigs.SASL_JAAS_CONFIG));

        if (jaasConfig != null) {
            String username = extractJaasValue(jaasConfig, "username");
            if (username != null) {
                return username;
            }

            String principal = extractJaasValue(jaasConfig, "principal");
            if (principal != null) {
                return principal;
            }
        }

        return "UNDEFINED";
    }

    private String extractJaasValue(String jaasConfig, String key) {
        Pattern pattern = Pattern.compile(key + "\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jaasConfig);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
