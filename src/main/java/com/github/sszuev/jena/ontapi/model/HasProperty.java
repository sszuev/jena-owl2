package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;

/**
 * A technical interface to access {@link P} property
 * on predicate {@link OWL#onProperty owl:onProperty}.
 * <p>
 * Created by @ssz on 09.05.2019.
 *
 * @param <P> - {@link OntRelationalProperty Data or Object} property expression
 * @see SetProperty
 */
interface HasProperty<P extends OntRelationalProperty> {
    /**
     * Gets the property,
     * that this {@link OntClass.UnaryRestriction} has inside its RDF structure
     * on predicate {@link OWL#onProperty owl:onProperty}.
     *
     * @return {@link P}
     */
    P getProperty();

}
