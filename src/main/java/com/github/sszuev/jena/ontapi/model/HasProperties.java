package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;

/**
 * A technical interface to access {@link P} properties from a []-list
 * on predicate {@link OWL#onProperties owl:onProperties}.
 * <p>
 * Created by @ssz on 09.05.2019.
 *
 * @param <P> - any subtype of {@link OntRealProperty} in general case,
 *            but in the current model it can only be {@link OntDataProperty}
 * @see SetProperties
 */
interface HasProperties<P extends OntRealProperty> extends HasRDFNodeList<P>, HasProperty<P> {

    /**
     * Gets the first property from {@code owl:onProperties} []-list.
     * Currently in OWL2, a []-list from n-ary Restrictions may contain one and only one (data) property.
     *
     * @return {@link P}
     * @see OntDataRange#arity()
     */
    @Override
    default P getProperty() {
        return getList().first().orElseThrow(OntJenaException.IllegalState::new);
    }
}
