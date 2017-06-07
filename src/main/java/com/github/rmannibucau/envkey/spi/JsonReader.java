package com.github.rmannibucau.envkey.spi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

@FunctionalInterface
public interface JsonReader {
    Map<String, String> read(InputStream stream, Charset charset);
}
