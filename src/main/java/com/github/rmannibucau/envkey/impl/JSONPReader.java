package com.github.rmannibucau.envkey.impl;

import com.github.rmannibucau.envkey.spi.JsonReader;

import javax.json.Json;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public class JSONPReader implements JsonReader {
    private final JsonReaderFactory factory = Json.createReaderFactory(emptyMap());

    @Override
    public Map<String, String> read(final InputStream stream, final Charset charset) {
        return factory.createReader(stream, charset).readObject().entrySet().stream()
                .filter(v -> v.getValue().getValueType() == JsonValue.ValueType.STRING)
                .collect(toMap(Map.Entry::getKey, e -> JsonString.class.cast(e.getValue()).getString()));
    }
}
