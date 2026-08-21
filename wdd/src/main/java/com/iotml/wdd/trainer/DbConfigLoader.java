package com.iotml.wdd.trainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads spring.datasource.* from application.properties (already on the
 * classpath, since this trainer lives in the same module) - no Spring
 * context involved, just a plain Properties load.
 *
 * application.properties uses ${VAR_NAME} placeholders resolved by Spring at
 * runtime (e.g. spring.datasource.url=${DATA_SOURCE_URL}) - since this
 * loader bypasses Spring, it resolves that same placeholder pattern directly
 * against the environment, so the same env vars used to run the Spring app
 * work here too.
 */
public class DbConfigLoader {

    public DbConfig load() {
        Properties props = new Properties();

        try (InputStream in = DbConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (in == null) {
                throw new IllegalStateException(
                        "application.properties not found on classpath - is TrainerApp being run "
                                + "from the same module as the Spring app?");
            }

            props.load(in);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read application.properties", e);
        }

        String url = require(props, "spring.datasource.url");
        String user = require(props, "spring.datasource.username");
        String password = require(props, "spring.datasource.password");

        return new DbConfig(resolvePlaceholder(url), resolvePlaceholder(user), resolvePlaceholder(password));
    }

    private String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    /**
     * application.properties uses ${VAR_NAME} placeholders resolved by Spring
     * at runtime - since this loader bypasses Spring entirely, it resolves
     * the same placeholder pattern directly against the environment,
     * matching what Spring would have done.
     */
    private String resolvePlaceholder(String rawValue) {
        if (rawValue.startsWith("${") && rawValue.endsWith("}")) {
            String envVarName = rawValue.substring(2, rawValue.length() - 1);
            String resolved = System.getenv(envVarName);
            if (resolved == null) {
                throw new IllegalStateException(
                        "Environment variable " + envVarName + " is not set (required by "
                                + rawValue + " in application.properties)");
            }
            return resolved;
        }
        return rawValue;
    }

    public record DbConfig(String url, String user, String password) {
    }
}