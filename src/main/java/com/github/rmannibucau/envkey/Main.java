package com.github.rmannibucau.envkey;

public final class Main {
    private Main() {
        // no-op
    }

    public static void main(final String[] args) {
        System.out.println(new EnvKey().setKey(args[0]).load());
    }
}
