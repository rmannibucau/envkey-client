package com.github.rmannibucau.envkey;

import com.github.rmannibucau.envkey.impl.BouncyCastleOpenGPG;
import com.github.rmannibucau.envkey.impl.JSONPReader;
import com.github.rmannibucau.envkey.impl.JVMHttpClient;
import com.github.rmannibucau.envkey.spi.HttpClient;
import com.github.rmannibucau.envkey.spi.JsonReader;
import com.github.rmannibucau.envkey.spi.OpenGPG;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class EnvKey {
    private String base = "https://env-service.herokuapp.com/";
    private Proxy proxy = Proxy.NO_PROXY;
    private Charset charset = StandardCharsets.UTF_8;
    private String key;
    private boolean ignoreIfNoKey = true;
    private JsonReader jsonReader;
    private HttpClient httpClient;
    private OpenGPG openGPG;

    public EnvKey setIgnoreIfNoKey(final boolean ignoreIfNoKey) {
        this.ignoreIfNoKey = ignoreIfNoKey;
        return this;
    }

    public EnvKey setBase(final String base) {
        this.base = base;
        return this;
    }

    public EnvKey setProxy(final Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public EnvKey setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public EnvKey setKey(final String key) {
        this.key = key;
        return this;
    }

    public EnvKey setJsonReader(final JsonReader jsonReader) {
        this.jsonReader = jsonReader;
        return this;
    }

    public EnvKey setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public EnvKey setOpenGPG(final OpenGPG openGPG) {
        this.openGPG = openGPG;
        return this;
    }

    public Map<String, String> load() {
        init();
        if (key == null && ignoreIfNoKey) {
            return emptyMap();
        }

        validate();

        final String[] segments = key.split("-");
        final HttpClient.Response response = httpClient.get(base + segments[0], proxy);
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException("Invalid response: " + response.getStatus());
        }
        final Map<String, String> data = jsonReader.read(response.getPayload(), charset);
        return jsonReader.read(new ByteArrayInputStream(
                openGPG.decrypt(
                        requireNonNull(data.get("env"), "No env defined"),
                        requireNonNull(data.get("encrypted_privkey"), "no priv key defined"),
                        segments[1]).getBytes(charset)), charset);
    }

    private void init() {
        if (jsonReader == null) {
            jsonReader = new JSONPReader();
        }
        if (httpClient == null) {
            httpClient = new JVMHttpClient();
        }
        if (openGPG == null) {
            openGPG = new BouncyCastleOpenGPG();
        }
        if (key == null) {
            key = Optional.of(new File(System.getProperty("user.home"), ".env"))
                    .filter(File::exists)
                    .map(f -> {
                        try (final BufferedReader reader = new BufferedReader(new FileReader(f))) {
                            return reader.lines().collect(Collectors.joining()).trim();
                        } catch (IOException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }).orElse(null);
        }
    }

    private void validate() {
        if (key == null || !key.contains("-")) {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    private Map<String, String> loadJson() {
        final HttpClient.Response response = httpClient.get(base + key.split("-")[0], proxy);
        return jsonReader.read(response.getPayload(), charset);
    }
}
