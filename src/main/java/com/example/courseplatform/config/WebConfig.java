package com.example.courseplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle standard static resources
        registry.addResourceHandler(
                "/favicon.ico",
                "/css/**",
                "/js/**", 
                "/images/**")
                .addResourceLocations(
                        "classpath:/static/favicon.ico",
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/images/")
                .setCachePeriod(3600);
        
        // Handle everything in /static/** path
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
} 