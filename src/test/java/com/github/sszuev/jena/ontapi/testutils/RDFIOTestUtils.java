package com.github.sszuev.jena.ontapi.testutils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Objects;

public class RDFIOTestUtils {

    public static String asString(Model model, Lang ext) {
        return toStringWriter(model, ext).toString();
    }

    public static StringWriter toStringWriter(Model model, Lang lang) {
        StringWriter sw = new StringWriter();
        model.write(sw, lang.getName(), null);
        return sw;
    }

    public static Model loadResourceAsModel(String resource, Lang lang) {
        return readResourceToModel(ModelFactory.createDefaultModel(), resource, lang);
    }

    @SuppressWarnings("unchecked")
    public static <X extends Model> X readResourceToModel(X m, String resource, Lang lang) {
        try (InputStream in = Objects.requireNonNull(RDFIOTestUtils.class.getResourceAsStream(resource))) {
            return (X) m.read(in, null, lang.getName());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
