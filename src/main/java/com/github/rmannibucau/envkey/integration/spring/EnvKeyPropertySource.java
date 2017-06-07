package com.github.rmannibucau.envkey.integration.spring;

import com.github.rmannibucau.envkey.EnvKey;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class EnvKeyPropertySource extends MapPropertySource {
    public EnvKeyPropertySource() {
        super("envkey", Map.class.cast(new EnvKey()
                .setKey(System.getProperty("com.github.rmannibucau.envkey.value"))
                .setIgnoreIfNoKey(!Boolean.getBoolean("com.github.rmannibucau.envkey.force"))
                .load()));
    }
}
