package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A technical interface to provide {@link RDFNodeList} instance.
 * Created by @ssz on 08.05.2019.
 *
 * @param <E> any {@link RDFNode} - a type for list's item
 * @see WithOntList
 * @see SetComponents
 */
interface HasRDFNodeList<E extends RDFNode> {

    /**
     * Gets an unmodifiable []-list with items of the type {@link E}
     *
     * @return {@link RDFNodeList}
     */
    RDFNodeList<E> getList();

}
