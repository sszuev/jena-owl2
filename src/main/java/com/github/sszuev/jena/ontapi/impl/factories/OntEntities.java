package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDataPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntNamedDataRangeImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimpleClassImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is an enumeration of all entities (configurable-)factories.
 *
 * @see OntEntity
 */
final class OntEntities {

    public static EnhNodeFactory createNamedObjectPropertyFactory(OntConfig config) {
        Set<Resource> objectPropertyTypes = new LinkedHashSet<>();
        objectPropertyTypes.add(OWL.ObjectProperty);
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE)) {
            objectPropertyTypes.add(OWL.InverseFunctionalProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.ReflexiveProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.IrreflexiveProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL.SymmetricProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL.AsymmetricProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.TransitiveProperty);
        }
        return createOntEntityFactory(
                OntObjectProperty.Named.class,
                OntObjectPropertyImpl.NamedImpl.class,
                OntObjectPropertyImpl.NamedImpl::new,
                OntPersonality.Builtins::getObjectProperties,
                OntPersonality.Punnings::getObjectProperties,
                OWL.ObjectProperty,
                objectPropertyTypes.toArray(new Resource[0])
        );
    }

    public static EnhNodeFactory createDataPropertyFactory() {
        return createOntEntityFactory(
                OntDataProperty.class,
                OntDataPropertyImpl.class,
                OntDataPropertyImpl::new,
                OntPersonality.Builtins::getDatatypeProperties,
                OntPersonality.Punnings::getDatatypeProperties,
                OWL.DatatypeProperty
        );
    }

    public static EnhNodeFactory createAnnotationPropertyFactory() {
        return createOntEntityFactory(
                OntAnnotationProperty.class,
                OntAnnotationPropertyImpl.class,
                OntAnnotationPropertyImpl::new,
                OntPersonality.Builtins::getAnnotationProperties,
                OntPersonality.Punnings::getAnnotationProperties,
                OWL.AnnotationProperty
        );
    }

    public static EnhNodeFactory createOWL2NamedClassFactory() {
        return createOntEntityFactory(
                OntClass.Named.class,
                OntSimpleClassImpl.NamedImpl.class,
                OntSimpleClassImpl.NamedImpl::new,
                OntPersonality.Builtins::getNamedClasses,
                OntPersonality.Punnings::getNamedClasses,
                OWL.Class
        );
    }

    public static EnhNodeFactory createOWL2RLNamedClassFactory() {
        return createOntEntityFactory(
                OntClass.Named.class,
                OntSimpleClassImpl.RLNamedImpl.class,
                OntSimpleClassImpl.RLNamedImpl::new,
                OntPersonality.Builtins::getNamedClasses,
                OntPersonality.Punnings::getNamedClasses,
                OWL.Class
        );
    }

    public static Function<OntConfig, EnhNodeFactory> createOWL1NamedClassFactory() {
        Set<Node> compatibleTypes = Stream.of(OWL.Class, RDFS.Class, RDFS.Datatype)
                .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
        return config -> {
            Function<OntPersonality.Punnings, Set<Node>> punnings = OntPersonality.Punnings::getNamedClasses;
            Function<OntPersonality.Builtins, Set<Node>> builtins = OntPersonality.Builtins::getNamedClasses;
            boolean useLegacyClassTesting = config.getBoolean(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY);
            EnhNodeFinder finder = new EnhNodeFinder.ByType(OWL.Class);
            EnhNodeFilter filter = (n, g) -> OntClasses.canBeNamedClass(n, g, useLegacyClassTesting);
            EnhNodeProducer maker =
                    new EnhNodeProducer.WithType(OntSimpleClassImpl.NamedImpl.class, OWL.Class, OntSimpleClassImpl.NamedImpl::new)
                            .restrict(createIllegalPunningsFilter(punnings));
            return OntEnhNodeFactories.createCommon(OntClass.Named.class, maker, finder, filter);
        };
    }

    public static EnhNodeFactory createOWL2NamedDataRangeFactory() {
        return createOntEntityFactory(
                OntDataRange.Named.class,
                OntNamedDataRangeImpl.class,
                OntNamedDataRangeImpl::new,
                OntPersonality.Builtins::getDatatypes,
                OntPersonality.Punnings::getDatatypes,
                RDFS.Datatype
        );
    }

    public static EnhNodeFactory createOWL1NamedDataRangeFactory() {
        // In OWL1 only builtins
        return OntEnhNodeFactories.createCommon(OntDataRange.class,
                new EnhNodeProducer.Default(OntNamedDataRangeImpl.class, OntNamedDataRangeImpl::new),
                EnhNodeFinder.NOTHING,
                EnhNodeFilter.URI.and(createBuiltinsFilter(OntPersonality.Builtins::getDatatypes))
        );
    }

    public static EnhNodeFactory createNamedIndividualFactory() {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(OWL.NamedIndividual);
        EnhNodeFilter filter = (n, g) -> n.isURI() && testNamedIndividualType(n, g);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(
                OntIndividualImpl.NamedImpl.class, OWL.NamedIndividual, OntIndividualImpl.NamedImpl::new
        ).restrict(createIllegalPunningsFilter(OntPersonality.Punnings::getNamedClasses));
        return OntEnhNodeFactories.createCommon(OntIndividual.Named.class, maker, finder, filter);
    }

    private static boolean testNamedIndividualType(Node n, EnhGraph g) {
        OntPersonality personality = OntEnhGraph.asPersonalityModel(g).getOntPersonality();
        if (personality.getBuiltins().getNamedIndividuals().contains(n)) { // just in case
            return true;
        }
        Set<Node> forbidden = personality.getPunnings().getNamedIndividuals();
        List<Node> candidates = new ArrayList<>();
        boolean hasDeclaration = false;
        ExtendedIterator<Triple> it = g.asGraph().find(n, RDF.Nodes.type, Node.ANY);
        try {
            while (it.hasNext()) {
                Node type = it.next().getObject();
                if (forbidden.contains(type)) {
                    return false;
                }
                if (OWL.NamedIndividual.asNode().equals(type)) {
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

    public static EnhNodeFactory createOntEntityFactory(Class<? extends OntEntity> classType,
                                                        Class<? extends OntObjectImpl> impl,
                                                        BiFunction<Node, EnhGraph, EnhNode> producer,
                                                        Function<OntPersonality.Builtins, Set<Node>> builtins,
                                                        Function<OntPersonality.Punnings, Set<Node>> punnings,
                                                        Resource resourceType,
                                                        Resource... alternativeResourceTypes) {
        Set<Resource> resourceTypes = Stream.concat(Stream.of(resourceType), Stream.of(alternativeResourceTypes))
                .collect(Collectors.toUnmodifiableSet());
        EnhNodeFinder finder = new EnhNodeFinder.ByTypes(resourceTypes);
        EnhNodeFilter filter = createPrimaryEntityFilter(resourceTypes, builtins, punnings);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType, producer)
                .restrict(createIllegalPunningsFilter(punnings));
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }

    static EnhNodeFilter createPrimaryEntityFilter(
            Set<Resource> types,
            Function<OntPersonality.Builtins, Set<Node>> builtins,
            Function<OntPersonality.Punnings, Set<Node>> punnings
    ) {
        Set<Node> whiteList = types.stream().map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
        return (n, eg) -> {
            if (!n.isURI()) {
                return false;
            }
            OntPersonality personality = OntEnhGraph.asPersonalityModel(eg).getOntPersonality();
            if (builtins.apply(personality.getBuiltins()).contains(n)) {
                return true;
            }
            Set<Node> blackList = punnings.apply(personality.getPunnings());
            return Graphs.testTypes(n, eg.asGraph(), whiteList, blackList);
        };
    }

    static EnhNodeFilter createIllegalPunningsFilter(Function<OntPersonality.Punnings, Set<Node>> punnings) {
        return (n, eg) -> !hasIllegalPunnings(n, eg, punnings);
    }

    static EnhNodeFilter createBuiltinsFilter(Function<OntPersonality.Builtins, Set<Node>> extractNodeSet) {
        return (n, g) -> isBuiltIn(n, g, extractNodeSet);
    }

    static boolean hasIllegalPunnings(Node n, EnhGraph eg, Function<OntPersonality.Punnings, Set<Node>> extractNodeSet) {
        Set<Node> punnings = extractNodeSet.apply(OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getPunnings());
        return Graphs.hasOneOfType(n, eg.asGraph(), punnings);
    }

    static boolean isBuiltIn(Node n, EnhGraph eg, Function<OntPersonality.Builtins, Set<Node>> extractNodeSet) {
        return extractNodeSet.apply(OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins()).contains(n);
    }
}
