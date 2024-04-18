package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to provide working with {@link OntList Ontology []-list}.
 *
 * @param <V> - {@link RDFNode}, a list's item type
 * @param <R> - {@link OntObject}, a return type
 * @see WithOntList
 * @see HasRDFNodeList
 */
interface SetComponents<V extends RDFNode, R extends OntObject> extends WithOntList<V> {

    /**
     * Replaces the existing []-list content with the specified one, that is given in the form of vararg array.
     *
     * @param values an {@code Array} of the type {@link V}
     * @return <b>this</b> instance to allow cascading calls
     */
    @SuppressWarnings("unchecked")
    default R setComponents(V... values) {
        return setComponents(Arrays.asList(values));
    }

    /**
     * Replaces the existing []-list content with the specified one, that is given in the form of {@link Collection}.
     * Nulls and self-references are not allowed.
     *
     * @param components a {@code Collection} of the type {@link V}
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException in case of wrong input
     */
    @SuppressWarnings("unchecked")
    default R setComponents(Collection<V> components) {
        if (components.stream().peek(OntJenaException::notNull).anyMatch(SetComponents.this::equals)) {
            throw new OntJenaException.IllegalArgument();
        }
        getList().clear().addAll(components);
        return (R) this;
    }
}
