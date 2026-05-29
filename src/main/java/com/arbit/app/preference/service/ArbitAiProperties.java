package com.arbit.app.preference.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arbit-ai")
public record ArbitAiProperties(
        String baseUrl
) {
}
