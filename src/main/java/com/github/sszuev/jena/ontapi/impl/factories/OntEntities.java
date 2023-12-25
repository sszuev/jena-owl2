package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is an enumeration of all entities (configurable-)factories.
 * <p>
 * Created @ssz on 03.11.2016.
 *
 * @see OntEntity
 */
final class OntEntities {

    public static EnhNodeFactory createOWL2ObjectPropertyFactory() {
        return createOntEntityFactory(
                OntObjectProperty.Named.class,
                OntObjectPropertyImpl.NamedImpl.class,
                OntObjectPropertyImpl.NamedImpl::new,
                OntPersonality.Builtins::getObjectProperties,
                OntPersonality.Punnings::getObjectProperties,
                OWL.ObjectProperty,
                OWL.InverseFunctionalProperty,
                OWL.ReflexiveProperty,
                OWL.IrreflexiveProperty,
                OWL.SymmetricProperty,
                OWL.AsymmetricProperty,
                OWL.TransitiveProperty
        );
    }

    public static EnhNodeFactory createOWL1ObjectPropertyFactory() {
        return createOntEntityFactory(
                OntObjectProperty.Named.class,
                OntObjectPropertyImpl.NamedImpl.class,
                OntObjectPropertyImpl.NamedImpl::new,
                OntPersonality.Builtins::getObjectProperties,
                OntPersonality.Punnings::getObjectProperties,
                OWL.ObjectProperty,
                OWL.InverseFunctionalProperty,
                OWL.SymmetricProperty,
                OWL.TransitiveProperty
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

    public static EnhNodeFactory createNamedClassFactory() {
        return createOntEntityFactory(
                OntClass.Named.class,
                OntSimpleClassImpl.NamedImpl.class,
                OntSimpleClassImpl.NamedImpl::new,
                OntPersonality.Builtins::getNamedClasses,
                OntPersonality.Punnings::getNamedClasses,
                OWL.Class
        );
    }

    public static EnhNodeFactory createNamedDataRangeFactory() {
        return createOntEntityFactory(
                OntDataRange.Named.class,
                OntNamedDataRangeImpl.class,
                OntNamedDataRangeImpl::new,
                OntPersonality.Builtins::getDatatypes,
                OntPersonality.Punnings::getDatatypes,
                RDFS.Datatype
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
        EnhNodeFilter modelEntity = new EnhNodeFilter.HasOneOfType(types).and(createIllegalPunningsFilter(punnings));
        EnhNodeFilter entity = createBuiltinsFilter(builtins).or(modelEntity);
        return EnhNodeFilter.URI.and(entity);
    }

    static EnhNodeFilter createIllegalPunningsFilter(Function<OntPersonality.Punnings, Set<Node>> extractNodeSet) {
        return (n, eg) -> {
            Set<Node> punnings = extractNodeSet.apply(OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getPunnings());
            Graph g = eg.asGraph();
            for (Node t : punnings) {
                if (g.contains(n, RDF.Nodes.type, t)) {
                    return false;
                }
            }
            return true;
        };
    }

    static EnhNodeFilter createBuiltinsFilter(Function<OntPersonality.Builtins, Set<Node>> extractNodeSet) {
        return (n, g) -> extractNodeSet.apply(OntEnhGraph.asPersonalityModel(g).getOntPersonality().getBuiltins()).contains(n);
    }
}
