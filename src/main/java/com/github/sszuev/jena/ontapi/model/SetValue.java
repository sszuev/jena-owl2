package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A technical generic interface to provide a possibility to assign {@link RDFNode} value (so-called filler)
 * into a class expression.
 * A value can be either {@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 * or {@link org.apache.jena.rdf.model.Literal}, depending on a concrete {@link OntClass} or {@link OntDataRange} type.
 * This interface is used to construct {@link OntClass class expression}s and {@link OntDataRange data range}s as a base.
 *
 * @param <V> - any subtype of {@link RDFNode} ({@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 *            or {@link org.apache.jena.rdf.model.Literal}).
 * @param <R> - return type, a subtype of {@link OntClass} or {@link OntDataRange}
 * @see HasValue
 */
interface SetValue<V extends RDFNode, R extends OntObject> {
    /**
     * Sets the specified value (a filler in OWL-API terms)
     * into this {@link OntClass class} or {@link OntDataRange data range} expression.
     * <p>
     * A {@code value} can be {@code null} if this is a Cardinality Restriction
     * (the null-filler is considered as {@link OWL#Thing owl:Thing}
     * for an object restriction and as {@link org.apache.jena.vocabulary.RDFS#Literal} for a data restriction).
     *
     * @param value {@link V}, possible {@code null} in case of Cardinality Restriction
     * @return <b>this</b> instance to allow cascading calls
     * @see HasValue#getValue()
     */
    R setValue(V value);
}
