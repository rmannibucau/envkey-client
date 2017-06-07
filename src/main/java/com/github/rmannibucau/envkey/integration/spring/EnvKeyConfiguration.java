package com.github.rmannibucau.envkey.integration.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class EnvKeyConfiguration {
    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public EnvKeyPropertySource envKeyPropertySource() {
        final EnvKeyPropertySource source = new EnvKeyPropertySource();
        environment.getPropertySources().addFirst(source);
        return source;
    }
}
