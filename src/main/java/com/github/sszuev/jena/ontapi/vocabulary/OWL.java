package com.github.sszuev.jena.ontapi.vocabulary;

import org.apache.jena.rdf.model.Resource;

/**
 * Vocabulary definition for the <a href="https://www.w3.org/TR/owl2-syntax/">OWL 2 Web Ontology Language</a>
 * See <a href="http://www.w3.org/2002/07/owl#">schema(ttl)</a>
 * Note: {@code owl:real} and {@code owl:rational} are absent in the schema and standard jena vocabulary (don't know why).
 * <p>
 * Created by @ssz on 21.12.2016.
 */
public class OWL extends org.apache.jena.vocabulary.OWL2 {

    /**
     * The {@code owl:real} datatype does not directly provide any lexical forms.
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Real_Numbers.2C_Decimal_Numbers.2C_and_Integers">4.1 Real Numbers, Decimal Numbers, and Integers</a>
     */
    public final static Resource real = resource("real");

    /**
     * The {@code owl:rational} datatype supports lexical forms defined by the following grammar
     * (whitespace within the grammar MUST be ignored and MUST NOT be included in the lexical forms of owl:rational,
     * and single quotes are used to introduce terminal symbols):
     * numerator '/' denominator
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Real_Numbers.2C_Decimal_Numbers.2C_and_Integers">4.1 Real Numbers, Decimal Numbers, and Integers</a>
     */
    public final static Resource rational = resource("rational");

}
