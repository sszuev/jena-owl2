package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;

/**
 * A technical interface to provide a possibility to assign {@link OntRelationalProperty data or object} property
 * into {@link OntClass.Restriction restriction class expression}.
 *
 * @param <P> {@link OntRelationalProperty data or object} property expression
 * @param <R> - return type, a subtype of {@link OntClass.Restriction}
 * @see HasProperty
 */
interface SetProperty<P extends OntRelationalProperty, R extends OntClass.Restriction> {

    /**
     * Sets the given property into this Restriction
     * (as an object with predicate {@link OWL#onProperty owl:onProperty}
     * if it is Unary Restriction).
     *
     * @param property {@link P}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     */
    R setProperty(P property);
}
