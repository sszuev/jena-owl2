package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.common.Factories;
import com.github.sszuev.jena.ontapi.common.ObjectFactory;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntFilter;
import com.github.sszuev.jena.ontapi.common.OntFinder;
import com.github.sszuev.jena.ontapi.common.OntMaker;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.common.Vocabulary;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This is an enumeration of all entity (configurable-)factories.
 * <p>
 * Created @ssz on 03.11.2016.
 *
 * @see OntEntity
 */
public enum OWL2Entity {
    CLASS(OWL.Class, OntClass.Named.class, OntClassImpl.class, Vocabulary.Entities::getClasses),
    DATATYPE(RDFS.Datatype, OntDataRange.Named.class, OntDatatypeImpl.class, Vocabulary.Entities::getDatatypes),
    ANNOTATION_PROPERTY(OWL.AnnotationProperty, OntAnnotationProperty.class, OntAPropertyImpl.class, Vocabulary.Entities::getAnnotationProperties),
    DATA_PROPERTY(OWL.DatatypeProperty, OntDataProperty.class, OntDPropertyImpl.class, Vocabulary.Entities::getDatatypeProperties),
    OBJECT_PROPERTY(OWL.ObjectProperty, OntObjectProperty.Named.class, OntOPEImpl.NamedPropertyImpl.class, Vocabulary.Entities::getObjectProperties),
    INDIVIDUAL(OWL.NamedIndividual, OntIndividual.Named.class, OntIndividualImpl.NamedImpl.class, Vocabulary.Entities::getIndividuals) {
        @Override
        OntFilter createPrimaryFilter() {
            return (n, g) -> n.isURI() && filterType(n, g);
        }

        private boolean filterType(Node n, EnhGraph g) {
            if (builtInURIs(g).contains(n)) { // just in case
                return true;
            }
            Set<Node> forbidden = bannedTypes(g);
            List<Node> candidates = new ArrayList<>();
            boolean hasDeclaration = false;
            ExtendedIterator<Triple> it = g.asGraph().find(n, RDF.Nodes.type, Node.ANY);
            try {
                while (it.hasNext()) {
                    Node type = it.next().getObject();
                    if (forbidden.contains(type)) {
                        return false;
                    }
                    if (resourceType.asNode().equals(type)) {
                        hasDeclaration = true;
                    } else {
                        candidates.add(type);
                    }
                }
            } finally {
                it.close();
            }
            if (hasDeclaration) {
                return true;
            }
            // In general, owl:NamedIndividual declaration is optional
            for (Node c : candidates) {
                if (OntEnhGraph.canAs(OntClass.class, c, g)) return true;
            }
            return false;
        }
    },
    ;
    private static final OntFinder ENTITY_FINDER = Factories.createFinder(e -> e.getResourceType().asNode(), values());
    public static final ObjectFactory ALL = Factories.createFrom(ENTITY_FINDER,
            Arrays.stream(values()).map(OWL2Entity::getActualType));

    final Class<? extends OntObjectImpl> impl;
    final Class<? extends OntEntity> classType;
    final Resource resourceType;
    final Function<Vocabulary.Entities, Set<Node>> extractNodeSet;

    /**
     * Creates an entity enum.
     *
     * @param resourceType   {@link Resource}-type
     * @param classType      class-type of the corresponding {@link OntEntity}
     * @param impl           class-implementation
     * @param extractNodeSet to retrieve {@link Node}s
     */
    OWL2Entity(Resource resourceType,
               Class<? extends OntEntity> classType,
               Class<? extends OntObjectImpl> impl,
               Function<Vocabulary.Entities, Set<Node>> extractNodeSet) {
        this.classType = classType;
        this.resourceType = resourceType;
        this.impl = impl;
        this.extractNodeSet = extractNodeSet;
    }

    /**
     * Finds the entity by the resource-type.
     *
     * @param type {@link Resource}
     * @return {@link Optional} of {@link OWL2Entity}
     */
    public static Optional<OWL2Entity> find(Resource type) {
        return find(type.asNode());
    }

    /**
     * Finds the entity by the node-type.
     *
     * @param type {@link Node}, not {@code null}
     * @return {@link Optional} of {@link OWL2Entity}
     */
    public static Optional<OWL2Entity> find(Node type) {
        for (OWL2Entity e : values()) {
            if (Objects.equals(e.getResourceType().asNode(), type)) return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * Finds the entity by the class-type.
     *
     * @param type {@link Class}
     * @return {@link Optional} of {@link OWL2Entity}
     */
    public static Optional<OWL2Entity> find(Class<? extends OntEntity> type) {
        for (OWL2Entity e : values()) {
            if (Objects.equals(e.getActualType(), type)) return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * Returns entity class-type.
     *
     * @return {@link Class}, one of {@link OntEntity}
     */
    public Class<? extends OntEntity> getActualType() {
        return classType;
    }

    /**
     * Returns entity resource-type.
     *
     * @return {@link Resource}
     */
    public Resource getResourceType() {
        return resourceType;
    }

    private OntPersonality personality(EnhGraph g) {
        return OntEnhGraph.asPersonalityModel(g).getOntPersonality();
    }

    /**
     * Answers a {@code Set} of URI Nodes that this entity cannot have as {@code rdf:type}.
     *
     * @param g {@link EnhGraph}
     * @return Set of {@link Node}s
     */
    Set<Node> bannedTypes(EnhGraph g) {
        return extractNodeSet.apply(personality(g).getPunnings());
    }

    /**
     * Answers a {@code Set} of URI Nodes
     * that can be treated as this entity even there is no any {@code rdf:type} declarations.
     *
     * @param g {@link EnhGraph}
     * @return Set of {@link Node}s
     */
    Set<Node> builtInURIs(EnhGraph g) {
        return extractNodeSet.apply(personality(g).getBuiltins());
    }

    /**
     * Creates a factory for the entity.
     *
     * @return {@link ObjectFactory}
     */
    public ObjectFactory createFactory() {
        OntFinder finder = new OntFinder.ByType(resourceType);
        OntFilter filter = createPrimaryFilter();
        OntMaker maker = new OntMaker.WithType(impl, resourceType).restrict(createIllegalPunningsFilter());
        return Factories.createCommon(classType, maker, finder, filter);
    }

    OntFilter createIllegalPunningsFilter() {
        return (n, eg) -> {
            Graph g = eg.asGraph();
            for (Node t : bannedTypes(eg)) {
                if (g.contains(n, RDF.Nodes.type, t)) return false;
            }
            return true;
        };
    }

    OntFilter createPrimaryFilter() {
        OntFilter builtInEntity = (n, g) -> builtInURIs(g).contains(n);
        OntFilter modelEntity = new OntFilter.HasType(resourceType).and(createIllegalPunningsFilter());
        OntFilter entity = modelEntity.or(builtInEntity);
        return OntFilter.URI.and(entity);
    }

}
