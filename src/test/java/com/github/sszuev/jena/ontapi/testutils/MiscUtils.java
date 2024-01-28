package com.github.sszuev.jena.ontapi.testutils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MiscUtils {
    @SafeVarargs
    public static <X> Set<X> hashSetOf(X... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    public static Path save(String resource, Path dir) {
        try {
            String path = resource.startsWith("/") ? resource : "/" + resource;
            String name = resource.startsWith("/") ? resource.substring(1) : resource;
            try (InputStream in = Objects.requireNonNull(MiscUtils.class.getResourceAsStream(path))) {
                Path res = Files.createTempFile(dir, name + "-", ".tmp");
                Reader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                try (Writer w = Files.newBufferedWriter(res)) {
                    IOUtils.copy(in, w, StandardCharsets.UTF_8);
                }
                return res;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public static <X> Set<X> toFlatSet(Collection<? extends X>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }
}
