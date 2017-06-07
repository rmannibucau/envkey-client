package com.github.rmannibucau.envkey.integration.spring.boot;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@Ignore("don't forget to set -Dcom.github.rmannibucau.envkey=${envkey}")
@SpringBootTest(classes = EnableEnvKeyTest.App.class)
@RunWith(SpringRunner.class)
public class EnableEnvKeyTest {
    @Autowired
    private Environment environment;

    @Test
    public void checkEnv() {
        assertEquals("VALUE_TEST", environment.getProperty("KEY_TEST"));
    }

    @EnableEnvKey
    @SpringBootApplication
    public static class App {
    }
}
