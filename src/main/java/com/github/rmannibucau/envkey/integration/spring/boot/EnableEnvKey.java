package com.github.rmannibucau.envkey.integration.spring.boot;

import com.github.rmannibucau.envkey.integration.spring.EnvKeyConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Import(EnvKeyConfiguration.class)
public @interface EnableEnvKey {
}
