package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;

/**
 * A technical interface to set new cardinality value.
 * <p>
 * Created by @ssz on 09.05.2019.
 *
 * @param <R> - return type, a subtype of {@link OntClass.CardinalityRestrictionCE}
 * @see HasCardinality
 */
interface SetCardinality<R extends OntClass.CardinalityRestrictionCE<?, ?>> {

    /**
     * Sets a new cardinality value.
     *
     * @param cardinality, int, a non-negative integer value
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException in case of wrong input
     * @see XSD#nonNegativeInteger
     */
    R setCardinality(int cardinality);
}
