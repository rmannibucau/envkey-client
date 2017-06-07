package com.github.rmannibucau.envkey.integration.deltaspike;

import com.github.rmannibucau.envkey.EnvKey;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.impl.config.MapConfigSource;

public class EnvKeyConfigSource extends MapConfigSource {
    public EnvKeyConfigSource() {
        super(new EnvKey()
                .setKey(ConfigResolver.getPropertyValue("com.github.rmannibucau.envkey.value"))
                .setIgnoreIfNoKey("true".equals(ConfigResolver.getPropertyValue("com.github.rmannibucau.envkey.force")))
                .load());
    }

    @Override
    public String getConfigName() {
        return "envkey";
    }
}
