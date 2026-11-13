package com.adres.urgencias.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Makes the back end serve the built front end (see ADR-0005).
 *
 * <p>The Docker build copies the Vite output into {@code classpath:/static/}, so the front end
 * ships inside the same jar as the API: one artifact, one process.
 */
@Configuration
public class StaticFrontendConfig implements WebMvcConfigurer {

    private static final String STATIC_LOCATION = "classpath:/static/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(STATIC_LOCATION)
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }
}
