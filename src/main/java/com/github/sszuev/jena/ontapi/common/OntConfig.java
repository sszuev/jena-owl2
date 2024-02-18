package com.github.sszuev.jena.ontapi.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration to control {@link com.github.sszuev.jena.ontapi.model.OntModel} and {@link OntPersonality} behavior.
 */
public class OntConfig {
    public static final OntConfig DEFAULT = new OntConfig();

    private final Map<String, Object> settings;

    public OntConfig() {
        this(Map.of());
    }

    protected OntConfig(Map<String, Object> settings) {
        this.settings = Map.copyOf(Objects.requireNonNull(settings));
    }

    public boolean getBoolean(Enum<?> key) {
        return getBoolean(key.name());
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Config contains value for key = " + key + ", but it is not a boolean");
    }

    public Object get(String key) {
        return settings.get(key);
    }

    public OntConfig setTrue(Enum<?> key) {
        return setBoolean(key, true);
    }

    public OntConfig setFalse(Enum<?> key) {
        return setBoolean(key, false);
    }

    public OntConfig setBoolean(Enum<?> key, boolean value) {
        return setBoolean(key.name(), value);
    }

    public OntConfig setBoolean(String key, boolean value) {
        return set(key, value);
    }

    public OntConfig set(String key, Object value) {
        Map<String, Object> settings = new HashMap<>(this.settings);
        settings.put(key, value);
        return new OntConfig(settings);
    }
}
