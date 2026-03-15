package com.ecommerce.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    /**
     * Bean for ModelMapper
     * Used for DTO <-> Entity mapping
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}