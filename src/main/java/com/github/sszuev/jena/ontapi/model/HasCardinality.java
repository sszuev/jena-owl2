package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;

/**
 * A technical interface to provide a non-negative integer value, that is a restriction cardinality.
 *
 * @see SetCardinality
 */
interface HasCardinality {

    /**
     * Returns a cardinality number.
     *
     * @return int, a non-negative integer value
     * @see XSD#nonNegativeInteger
     */
    int getCardinality();

    /**
     * Determines if this restriction is qualified.
     * Qualified cardinality restrictions are defined to be cardinality restrictions
     * that have fillers which aren't TOP ({@link OWL#Thing owl:Thing} or
     * {@link org.apache.jena.vocabulary.RDFS#Literal rdfs:Literal}).
     * An object restriction is unqualified if it has a filler that is {@code owl:Thing}.
     * A data restriction is unqualified
     * if it has a filler which is the top data type ({@code rdfs:Literal}).
     *
     * @return {@code true} if this restriction is qualified, or {@code false} if this restriction is unqualified
     */
    boolean isQualified();
}
