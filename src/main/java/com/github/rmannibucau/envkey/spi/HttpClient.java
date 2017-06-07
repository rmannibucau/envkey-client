package com.github.rmannibucau.envkey.spi;

import java.io.InputStream;
import java.net.Proxy;

@FunctionalInterface
public interface HttpClient {
    Response get(String url, Proxy proxy);

    class Response {
        private final int status;
        private final InputStream payload;

        public Response(final int status, final InputStream payload) {
            this.status = status;
            this.payload = payload;
        }

        public int getStatus() {
            return status;
        }

        public InputStream getPayload() {
            return payload;
        }
    }
}
