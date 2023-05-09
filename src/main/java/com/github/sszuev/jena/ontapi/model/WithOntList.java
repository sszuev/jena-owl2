package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A technical interface to provide {@link OntList Ontology []-list} instance.
 * <p>
 * Created by @ssz on 08.05.2019.
 *
 * @param <E> any {@link RDFNode} - a type for list's item
 * @see HasRDFNodeList
 * @see SetComponents
 */
interface WithOntList<E extends RDFNode> extends HasRDFNodeList<E> {
    /**
     * Gets an modifiable []-list with items of the type {@link E}.
     *
     * @return {@link OntList Ontology []-list} with items of the type {@link E}
     */
    @Override
    OntList<E> getList();
}
