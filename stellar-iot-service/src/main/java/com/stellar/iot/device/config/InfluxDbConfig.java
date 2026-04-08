package com.stellar.iot.device.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "influxdb")
public class InfluxDbConfig {
    private String url;

    private String token;

    private String org;

    private String bucket;

    private String username;

    private String password;

    @Bean
    public InfluxDBClient influxDBClient() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .bucket(bucket)
                .org(org)
                .url(url)
                .build();
        return InfluxDBClientFactory.create(options);
    }
}
