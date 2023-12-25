package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import java.util.Set;

/**
 * This is a resource type mapper.
 * It is a technical interface that is included into the {@link OntPersonality}.
 * Note: all its methods must return a IRIs (as {@code String}s), not literals or blank-nodes.
 * <p>
 * Created by @ssz on 16.01.2019.
 *
 * @param <T> any subtype of {@link Resource}
 * @see com.github.sszuev.jena.ontapi.OntVocabulary
 */
public interface ResourceVocabulary<T extends Resource> {

    /**
     * Returns a {@code Set} of {@link Node Jena Graph Node}s for the given {@code Class}-type.
     *
     * @param type {@link Class}, any subtype of {@link T}
     * @return Set of {@link Node node}s (immutable!), can be empty (if no mapping or type is not supported)
     */
    Set<Node> get(Class<? extends T> type) throws OntJenaException;

    /**
     * Answers {@code true} if the given type is supported by the vocabulary.
     */
    boolean supports(Class<? extends T> type);

}
