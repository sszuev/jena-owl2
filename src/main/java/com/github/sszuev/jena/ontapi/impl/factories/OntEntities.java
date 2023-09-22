package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.common.Vocabulary;
import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDataPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimpleClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimpleDataRangeImpl;
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
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This is an enumeration of all entities (configurable-)factories.
 * <p>
 * Created @ssz on 03.11.2016.
 *
 * @see OntEntity
 */
enum OntEntities {
    CLASS(OWL.Class, OntClass.Named.class, OntSimpleClassImpl.class, Vocabulary.Entities::getClasses) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntSimpleClassImpl(OntObjectImpl.checkNamed(node), graph);
        }
    },
    DATATYPE(RDFS.Datatype, OntDataRange.Named.class, OntSimpleDataRangeImpl.class, Vocabulary.Entities::getDatatypes) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntSimpleDataRangeImpl(OntObjectImpl.checkNamed(node), graph);
        }
    },
    ANNOTATION_PROPERTY(OWL.AnnotationProperty, OntAnnotationProperty.class, OntAnnotationPropertyImpl.class, Vocabulary.Entities::getAnnotationProperties) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntAnnotationPropertyImpl(OntObjectImpl.checkNamed(node), graph);
        }
    },
    DATA_PROPERTY(OWL.DatatypeProperty, OntDataProperty.class, OntDataPropertyImpl.class, Vocabulary.Entities::getDatatypeProperties) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntDataPropertyImpl(OntObjectImpl.checkNamed(node), graph);
        }
    },
    OBJECT_PROPERTY(OWL.ObjectProperty, OntObjectProperty.Named.class, OntObjectPropertyImpl.NamedPropertyImpl.class, Vocabulary.Entities::getObjectProperties) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntObjectPropertyImpl.NamedPropertyImpl(OntObjectImpl.checkNamed(node), graph);
        }
    },
    INDIVIDUAL(OWL.NamedIndividual, OntIndividual.Named.class, OntIndividualImpl.NamedImpl.class, Vocabulary.Entities::getIndividuals) {
        @Override
        EnhNode newInstance(Node node, EnhGraph graph) {
            return new OntIndividualImpl.NamedImpl(OntObjectImpl.checkNamed(node), graph);
        }

        @Override
        EnhNodeFilter createPrimaryFilter() {
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
    OntEntities(Resource resourceType,
                Class<? extends OntEntity> classType,
                Class<? extends OntObjectImpl> impl,
                Function<Vocabulary.Entities, Set<Node>> extractNodeSet) {
        this.classType = classType;
        this.resourceType = resourceType;
        this.impl = impl;
        this.extractNodeSet = extractNodeSet;
    }

    /**
     * Creates a factory for this entity.
     *
     * @return {@link EnhNodeFactory}
     */
    public EnhNodeFactory createFactory() {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(resourceType);
        EnhNodeFilter filter = createPrimaryFilter();
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType, this::newInstance)
                .restrict(createIllegalPunningsFilter());
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }

    abstract EnhNode newInstance(Node node, EnhGraph graph);

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

    EnhNodeFilter createIllegalPunningsFilter() {
        return new EnhNodeFilter() {
            @Override
            public boolean test(Node n, EnhGraph eg) {
                Graph g = eg.asGraph();
                for (Node t : OntEntities.this.bannedTypes(eg)) {
                    if (g.contains(n, RDF.Nodes.type, t)) return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return OntEntities.this.name() + "::illegalPunningsFilter";
            }
        };
    }

    EnhNodeFilter createBuiltinsFilter() {
        return new EnhNodeFilter() {
            @Override
            public boolean test(Node n, EnhGraph g) {
                return OntEntities.this.builtInURIs(g).contains(n);
            }

            @Override
            public String toString() {
                return OntEntities.this.name() + "::builtInFilter";
            }
        };
    }

    EnhNodeFilter createPrimaryFilter() {
        EnhNodeFilter modelEntity = new EnhNodeFilter.HasType(resourceType).and(createIllegalPunningsFilter());
        EnhNodeFilter entity = createBuiltinsFilter().or(modelEntity);
        return EnhNodeFilter.URI.and(entity);
    }

}
