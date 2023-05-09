package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Interface representing the Negative Property Assertion abstraction,
 * where predicate (property) is expected to be either ontology {@link OntDataProperty data property} ({@code R}) or
 * {@link OntObjectProperty object property exception} ({@code P}).
 * Assuming {@code _:x} is a blank node, {@code ai} is an individual and {@code v} is literal,
 * a Negative Object Property Assertion in Turtle syntax looks like this:
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion ;
 * _:x owl:sourceIndividual a1 ;
 * _:x owl:assertionProperty P ;
 * _:x owl:targetIndividual a2 .
 * }</pre>
 * In turn, a Negative Data Property Assertion looks like following:
 * <pre>{@code
 * _:x rdf:type owl:NegativePropertyAssertion ;
 * _:x owl:sourceIndividual a ;
 * _:x owl:assertionProperty R ;
 * _:x owl:targetValue v .
 * }</pre>
 *
 * <p>
 * Created by @ssz on 15.11.2016.
 *
 * @param <P> - either {@link OntObjectProperty object property expression} or {@link OntDataProperty data property}
 * @param <V> - either {@link OntIndividual} or {@link Literal}
 */
public interface OntNegativeAssertion<P extends OntRealProperty, V extends RDFNode> extends OntObject {

    /**
     * Returns the source individual.
     *
     * @return {@link OntIndividual}
     */
    OntIndividual getSource();

    /**
     * Returns the assertion property.
     *
     * @return either {@link OntObjectProperty} or {@link OntDataProperty}
     */
    P getProperty();

    /**
     * Returns the target node.
     *
     * @return either {@link OntIndividual} or {@link Literal}
     */
    V getTarget();

    /**
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Negative_Object_Property_Assertions'>9.6.5 Negative Object Property Assertions</a>
     */
    interface WithObjectProperty extends OntNegativeAssertion<OntObjectProperty, OntIndividual> {
    }

    /**
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Negative_Data_Property_Assertions'>9.6.7 Negative Data Property Assertions</a>
     */
    interface WithDataProperty extends OntNegativeAssertion<OntDataProperty, Literal> {
    }
}
