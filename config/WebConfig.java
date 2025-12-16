package com.badmintonshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Web MVC Configuration
 * - CORS settings
 * - Static resources
 * - Interceptors
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * CORS Configuration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Static Resource Handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // Uploaded files (local storage)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600);

        // WebJars (if using)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/")
                .setCachePeriod(3600);
    }

    /**
     * View Controllers for simple page mappings
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect root to home
        registry.addRedirectViewController("/", "/home");
        
        // Simple static pages
        registry.addViewController("/about").setViewName("customer/about");
        registry.addViewController("/contact").setViewName("customer/contact");
        registry.addViewController("/privacy-policy").setViewName("customer/privacy-policy");
        registry.addViewController("/terms").setViewName("customer/terms");
        
        // Error pages
        registry.addViewController("/access-denied").setViewName("error/403");
    }
}
