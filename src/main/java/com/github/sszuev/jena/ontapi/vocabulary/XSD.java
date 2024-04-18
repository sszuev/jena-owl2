package com.github.sszuev.jena.ontapi.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * XML Schema Vocabulary
 * See <a href="http://www.w3.org/2001/XMLSchema">XSD</a>
 */
public class XSD extends org.apache.jena.vocabulary.XSD {

    public static final Property length = property("length");
    public static final Property minLength = property("minLength");
    public static final Property maxLength = property("maxLength");
    public static final Property pattern = property("pattern");
    public static final Property minInclusive = property("minInclusive");
    public static final Property minExclusive = property("minExclusive");
    public static final Property maxInclusive = property("maxInclusive");
    public static final Property maxExclusive = property("maxExclusive");
    public static final Property totalDigits = property("totalDigits");
    public static final Property fractionDigits = property("fractionDigits");

    private static Property property(String name) {
        return ResourceFactory.createProperty(NS + name);
    }
}
