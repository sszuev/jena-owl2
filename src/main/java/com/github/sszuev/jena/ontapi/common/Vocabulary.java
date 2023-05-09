package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntVocabulary;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
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
 * @see OntVocabulary
 */
@FunctionalInterface
public interface Vocabulary<T extends Resource> {

    /**
     * Returns a {@code Set} of {@link Node Jena Graph Node}s for the given {@code Class}-type.
     *
     * @param type {@link Class}, any subtype of {@link T}
     * @return Set of {@link Node node}s
     * @throws OntJenaException in case the mapping is not possible
     */
    Set<Node> get(Class<? extends T> type) throws OntJenaException;

    /**
     * A technical interface to describe vocabulary for {@link OntEntity OWL Entity} types.
     * <p>
     * Created by @ssz on 18.01.2019.
     *
     * @see OntEntity#types()
     */
    interface Entities extends Vocabulary<OntObject> {

        default Set<Node> getClasses() {
            return get(OntClass.Named.class);
        }

        default Set<Node> getDatatypes() {
            return get(OntDataRange.Named.class);
        }

        default Set<Node> getObjectProperties() {
            return get(OntObjectProperty.Named.class);
        }

        default Set<Node> getDatatypeProperties() {
            return get(OntDataProperty.class);
        }

        default Set<Node> getAnnotationProperties() {
            return get(OntAnnotationProperty.class);
        }

        default Set<Node> getIndividuals() {
            return get(OntIndividual.Named.class);
        }
    }
}
