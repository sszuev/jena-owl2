package com.github.sszuev.jena.ontapi.model;

/**
 * Provides a "sugar" functionality to represent an expression as an entity.
 * Just in convenience sake.
 * Created by @ssz on 09.03.2020.
 *
 * @param <E> - an {@link OntEntity} instance
 */
interface AsNamed<E extends OntEntity> {

    /**
     * Represents this OWL expression as a named OWL entity if it is possible, otherwise throws an exception.
     * Effectively equivalent to the expression {@code this.as(Named.class)}.
     *
     * @return {@link E}, never {@code null}
     * @throws org.apache.jena.enhanced.UnsupportedPolymorphismException if the expression is not named OWL entity
     * @see org.apache.jena.rdf.model.RDFNode#as(Class)
     */
    E asNamed();
}
