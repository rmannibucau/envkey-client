package com.github.rmannibucau.envkey.impl;

import com.github.rmannibucau.envkey.spi.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.util.stream.Collectors.joining;

public class JVMHttpClient implements HttpClient {
    @Override
    public Response get(final String url, final Proxy proxy) {
        try {
            final URL jvmUrl = new URL(url);
            final HttpURLConnection connection = HttpURLConnection.class.cast(jvmUrl.openConnection(proxy));
            return new Response(connection.getResponseCode(), connection.getInputStream());
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private String slurp(final InputStream slurp) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(slurp, StandardCharsets.UTF_8))) {
            return reader.lines().collect(joining());
        }
    }
}
