package com.arbit.app.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    private final String publicBasePath;
    private final Path uploadRoot;

    public WebResourceConfig(@Value("${storage.local.upload-dir:uploads}") String uploadDir,
                             @Value("${storage.local.public-base-path:/uploads}") String publicBasePath) {
        this.publicBasePath = publicBasePath;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicBasePath + "/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }
}
