/**
 * Configures static resource handling for the application.
 *
 * This configuration serves static files from the classpath "static" directory. If a requested resource
 * is not found or unreadable, it falls back to "index.html" to support client-side routing.
 */
package com.springboot.MyTodoList.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**") // Match all paths
                .addResourceLocations("classpath:/static/") // Serve static files from /static first
                .resourceChain(true)
                .addResolver(
                        new PathResourceResolver() {
                            @Override
                            protected Resource getResource(String resourcePath, Resource location)
                                    throws IOException {
                                Resource requestedResource = location.createRelative(resourcePath);
                                // If the requested resource exists and is accessible, serve it.
                                // Otherwise, fall back to serving index.html for client-side
                                // routing.
                                return requestedResource.exists() && requestedResource.isReadable()
                                        ? requestedResource
                                        : new ClassPathResource("/static/index.html");
                            }
                        });
    }
}
