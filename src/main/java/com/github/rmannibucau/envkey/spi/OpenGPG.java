package com.github.rmannibucau.envkey.spi;

@FunctionalInterface
public interface OpenGPG {
    String decrypt(String env, String privKey, String pwd);
}
