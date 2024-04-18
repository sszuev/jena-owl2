package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A technical generic interface to provide {@link RDFNode} value,
 * which can be either {@link OntClass}, {@link OntDataRange}, {@link OntIndividual} or {@link org.apache.jena.rdf.model.Literal}.
 * This interface is used to construct {@link OntClass class expression}s and {@link OntDataRange data range}s as a base.
 *
 * @param <V> a subtype of {@link RDFNode}: {@link OntClass}, {@link OntDataRange}, {@link OntIndividual}
 *            or {@link org.apache.jena.rdf.model.Literal}
 * @see SetValue
 */
interface HasValue<V extends RDFNode> {

    /**
     * Gets an RDF-value (a filler in OWL-API terms) encapsulated by this expression
     * (that can be either {@link OntClass class} or {@link OntDataRange data range} expression).
     * <p>
     * The result is not {@code null} even if it is an Unqualified Cardinality Restriction,
     * that has no explicit filler in RDF
     * (the filler is expected to be either {@link OWL#Thing owl:Thing}
     * for object restriction or {@link org.apache.jena.vocabulary.RDFS#Literal} for data restriction).
     *
     * @return {@link V}, not {@code null}
     * @see SetValue#setValue(RDFNode)
     */
    V getValue();
}
