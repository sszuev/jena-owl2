package com.github.sszuev.jena.ontapi.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Vocabulary definition for the standard RDF.
 * See <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#">schema</a>.
 */
public class RDF extends org.apache.jena.vocabulary.RDF {

    /**
     * This property is used explicitly in facet restrictions.
     * Also, it can be used as a literal type
     * (e.g., {@code 'test'^^rdf:PlainLiteral}) in old ontologies based on RDF-1.0
     *
     * @see <a href="https://www.w3.org/TR/rdf-plain-literal">rdf:PlainLiteral: A Datatype for RDF Plain Literals (Second Edition)</a>
     */
    public final static Resource PlainLiteral = resource("PlainLiteral");

    /**
     * This property is used in facet restrictions.
     * The facet {@code rdf:langRange} can be used to refer to a subset of strings containing the language tag.
     *
     * @see <a href="https://www.w3.org/TR/rdf-plain-literal/#langRange">rdf:langRange</a>
     */
    public static final Property langRange = property("langRange");

}
