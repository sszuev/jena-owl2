package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * An abstraction for any Ontology Property Expression.
 * In OWL2 there are four such property expressions:
 * Data Property, Object Property (OWL Entity and InverseOf) and Annotation Property.
 * <p>
 * Created by @ssz on 02.11.2016.
 *
 * @see <a href='https://www.w3.org/TR/owl2-quick-reference/'>2.2 Properties</a>
 * @see OntObjectProperty
 * @see OntAnnotationProperty
 * @see OntDataProperty
 */
public interface OntProperty extends OntObject {

    /**
     * Answers a {@code Stream} over all the properties that are declared to be super-properties of this property.
     * Each element of the {@code Stream} will have the same type as this property instance:
     * if it is datatype property the method will return only data properties, etc.
     * The parameter {@code direct} controls selectivity over the properties that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *  :A rdfs:subPropertyOf :B .
     *  :A rdfs:subPropertyOf :C .
     *  :C rdfs:subPropertyOf :D .
     * } </pre>
     * If the flag {@code direct} is {@code true}, then the output will contain only direct super properties:
     * {@code B} and {@code C}. In this case the method is almost equivalent to the method {@link #superProperties()}.
     * If the flag {@code direct} is {@code false}, then the output will contain three properties:
     * {@code B}, {@code C} and {@code D} (indirectly).
     * This property instance is not included into the output in any case.
     *
     * @param direct if {@code true}, only answers the directly adjacent properties in the property hierarchy:
     *               i.e. eliminate any property for which there is a longer route
     *               to reach that child under the super-property relation
     * @return <b>distinct</b> {@code Stream} of properties with the same type as this property
     * @see #superProperties()
     * @see #subProperties(boolean)
     */
    Stream<? extends OntProperty> superProperties(boolean direct);

    /**
     * Answers a {@code Stream} over all the properties that are declared to be sub-properties of this property.
     * Each element of the {@code Stream} will have the same type as this property instance:
     * if it is datatype property the method will return only data properties, etc.
     * The parameter {@code direct} controls selectivity over the properties that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *  :D rdfs:subPropertyOf :C .
     *  :C rdfs:subPropertyOf :A .
     *  :B rdfs:subPropertyOf :A .
     * } </pre>
     * If the flag {@code direct} is {@code true}, then the output contains only direct sub properties:
     * {@code B} and {@code C}.
     * If the flag {@code direct} is {@code false}, then the output contains three properties:
     * {@code B}, {@code C} and {@code D}.
     * This property instance is not included into the output in any case.
     *
     * @param direct if {@code true}, only answers the directly adjacent properties in the property hierarchy:
     *               i.e. eliminate any property for which there is a longer route
     *               to reach that child under the super-property relation
     * @return <b>distinct</b> {@code Stream} of properties with the same type as this property
     * @see #superProperties(boolean)
     */
    Stream<? extends OntProperty> subProperties(boolean direct);

    /**
     * Lists all direct super properties for this property expression.
     * The pattern: {@code P1 rdfs:subPropertyOf P2}.
     * Note: the return elements have the same type as this instance.
     *
     * @return {@code Stream} of {@link Resource jena resource}s
     * @see OntAnnotationProperty#superProperties()
     * @see OntRealProperty#superProperties()
     */
    Stream<? extends OntProperty> superProperties();

    /**
     * Lists all property domains.
     *
     * @return {@code Stream} of {@link Resource}s
     * @see OntAnnotationProperty#domains()
     * @see OntObjectProperty#domains()
     * @see OntDataProperty#domains()
     */
    Stream<? extends Resource> domains();

    /**
     * Lists all property ranges.
     *
     * @return {@code Stream} of {@link Resource}s
     * @see OntAnnotationProperty#ranges()
     * @see OntRealProperty#ranges()
     */
    Stream<? extends Resource> ranges();

    /**
     * Returns a named part of this property expression.
     *
     * @return {@link Property}
     */
    Property asProperty();

    /**
     * Removes the specified domain resource (predicate is {@link RDFS#domain rdfs:domain}),
     * including the corresponding statement's annotations.
     * No-op in case no such domain found.
     * Removes all domains if {@code null} is specified.
     *
     * @param domain {@link Resource}, or {@code null} to remove all domains
     * @return <b>this</b> instance to allow cascading calls
     */
    OntProperty removeDomain(Resource domain);

    /**
     * Removes the specified range resource (predicate is {@link RDFS#range rdfs:range}),
     * including the corresponding statement's annotations.
     * No-op in case no such range is found.
     * Removes all ranges if {@code null} is specified.
     *
     * @param range {@link Resource}, or {@code null} to remove all ranges
     * @return <b>this</b> instance to allow cascading calls
     */
    OntProperty removeRange(Resource range);

    /**
     * Removes the specified super property (predicate is {@link RDFS#subPropertyOf rdfs:subPropertyOf}),
     * including the corresponding statement's annotations.
     * No-op in case no such super-property is found.
     * Removes all triples with predicate {@code rdfs:subPropertyOf} if {@code null} is specified.
     *
     * @param property {@link Resource} or {@code null} to remove all direct super properties
     * @return <b>this</b> instance to allow cascading calls
     */
    OntProperty removeSuperProperty(Resource property);

}
