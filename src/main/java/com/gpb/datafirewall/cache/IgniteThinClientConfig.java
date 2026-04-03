package com.gpb.datafirewall.cache;

import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.SslMode;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gpb.datafirewall.properties.IgniteSslProperties;
import com.gpb.datafirewall.properties.IgniteThinClientProperties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;

@Configuration
@EnableConfigurationProperties({
        IgniteThinClientProperties.class,
        IgniteSslProperties.class
})
public class IgniteThinClientConfig {

    @Bean(destroyMethod = "close")
    public IgniteClient igniteClient(
            IgniteThinClientProperties props,
            IgniteSslProperties sslProps
    ) throws Exception {

        if (props.getAddresses() == null || props.getAddresses().isBlank()) {
            throw new IllegalStateException("ignite.thin.addresses must be set");
        }

        String[] addresses = Arrays.stream(props.getAddresses().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
                

        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(addresses)
                .setTimeout(props.getConnectTimeoutMs().intValue());

        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            cfg.setUserName(props.getUsername());
        }

        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            cfg.setUserPassword(props.getPassword());
        }

        if (props.isSslEnabled()) {
            cfg.setSslMode(SslMode.REQUIRED);
            cfg.setSslContextFactory(() -> buildSslContext(sslProps));
        }

        return Ignition.startClient(cfg);
    }

    private SSLContext buildSslContext(IgniteSslProperties sslProps) {
        try {
            if (isBlank(sslProps.getKeyStorePath())
                    || isBlank(sslProps.getTrustStorePath())
                    || isBlank(sslProps.getKeyStorePassword())
                    || isBlank(sslProps.getTrustStorePassword())) {
                throw new IllegalStateException(
                        "SSL is enabled, but keystore/truststore settings are incomplete"
                );
            }

            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream in = new FileInputStream(sslProps.getKeyStorePath())) {
                keyStore.load(in, sslProps.getKeyStorePassword().toCharArray());
            }

            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, sslProps.getKeyStorePassword().toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream in = new FileInputStream(sslProps.getTrustStorePath())) {
                trustStore.load(in, sslProps.getTrustStorePassword().toCharArray());
            }

            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build Ignite SSL context", e);
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}
