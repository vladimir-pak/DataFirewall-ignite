package com.gpb.datafirewall.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ignite.thin")
public class IgniteThinClientProperties {

    /**
     * Список server nodes с client connector портом, например:
     * 10.10.1.11:10800,10.10.1.12:10800
     */
    private String addresses;

    private Long connectTimeoutMs = 5000L;
    private boolean sslEnabled = false;
    private String username;
    private String password;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public Long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(Long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
