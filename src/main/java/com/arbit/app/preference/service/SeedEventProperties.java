package com.arbit.app.preference.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seed-event")
public record SeedEventProperties(
        String baseUrl
) {
}
