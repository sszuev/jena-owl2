package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;

/**
 * A technical interface to provide a possibility to assign {@link OntDataProperty} properties
 * into {@link OntClass.NaryRestriction n-ary restriction class expression}
 * on predicate {@link OWL#onProperties owl:onProperties}.
 * <p>
 * Created by @ssz on 09.05.2019.
 *
 * @param <P> - any subtype of {@link OntRelationalProperty} in general case, but in the current model it can only be {@link OntDataProperty}
 * @param <R> - return type, a subtype of {@link OntClass.NaryRestriction}
 * @see HasProperties
 */
interface SetProperties<P extends OntRelationalProperty, R extends OntClass.NaryRestriction<?, ?>>
        extends SetComponents<P, R>, SetProperty<P, R> {

    /**
     * Sets the given property as the only member of the []-list.
     *
     * @param property {@link P}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see HasProperties#getProperty()
     */
    @SuppressWarnings("unchecked")
    @Override
    default R setProperty(P property) {
        getList().clear().add(property);
        return (R) this;
    }
}
