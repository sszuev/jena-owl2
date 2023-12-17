package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimpleClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimplePropertyImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.function.BiFunction;

/**
 * A helper-factory to produce (OWL1) {@link EnhNodeFactory} factories;
 * for {@link com.github.sszuev.jena.ontapi.common.OntPersonality ont-personalities}
 */
public final class RDFSObjectFactories {

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(
            OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT,
            EnhNodeFilter.URI.or(EnhNodeFilter.ANON)
    );

    public static EnhNodeFactory NAMED_CLASS = createFactory(
            OntSimpleClassImpl.NamedImpl.class,
            OntClass.Named.class,
            RDFS.Class,
            OntSimpleClassImpl.NamedImpl::new,
            RDFSObjectFactories::isNamedClass
    );

    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.NamedImpl.class,
            OntIndividualImpl.NamedImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isURI),
            RDFSObjectFactories::isNamedIndividual
    );

    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            OntIndividualImpl.AnonymousImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isBlank),
            RDFSObjectFactories::isAnonymousIndividual
    );

    public static EnhNodeFactory PROPERTY = createFactory(
            OntSimplePropertyImpl.class,
            OntProperty.class,
            RDF.Property,
            OntSimplePropertyImpl::new,
            RDFSObjectFactories::isAnyProperty
    );

    public static EnhNodeFactory ANY_CLASS = createFactory(
            OntSimpleClassImpl.class,
            OntClass.class,
            RDFS.Class,
            OntSimpleClassImpl::new,
            RDFSObjectFactories::isAnyClass
    );

    public static EnhNodeFactory ANY_ENTITY = OntEnhNodeFactories.createFrom(
            OntEnhNodeFactories.createFinder(RDF.Property, RDFS.Class), NAMED_CLASS, NAMED_INDIVIDUAL
    );

    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            RDFSObjectFactories::findIndividuals,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    private static boolean isNamedClass(Node n, EnhGraph eg) {
        return n.isURI() && isAnyClass(n, eg);
    }

    private static boolean isAnyClass(Node n, EnhGraph eg) {
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getNamedClasses().contains(n)) {
            return true;
        }
        Graph g = eg.asGraph();
        return g.contains(n, RDF.type.asNode(), RDFS.Class.asNode()) ||
                g.contains(n, RDF.type.asNode(), RDFS.Datatype.asNode()) ||
                g.contains(Node.ANY, RDFS.domain.asNode(), n) ||
                g.contains(Node.ANY, RDFS.range.asNode(), n);
    }

    private static boolean isAnyProperty(Node n, EnhGraph eg) {
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getOntProperties().contains(n)) {
            return true;
        }
        return eg.asGraph().contains(n, RDF.type.asNode(), RDF.Property.asNode());
    }

    private static boolean isNamedIndividual(Node n, EnhGraph eg) {
        return n.isURI() && isIndividual(n, eg);
    }

    private static boolean isAnonymousIndividual(Node n, EnhGraph eg) {
        return !n.isURI() && isIndividual(n, eg);
    }

    private static boolean isIndividual(Node n, EnhGraph eg) {
        return Iterators.anyMatch(
                eg.asGraph().find(n, RDF.type.asNode(), Node.ANY)
                        .mapWith(Triple::getObject),
                it -> isAnyClass(it, eg)
        );
    }

    private static ExtendedIterator<Node> findIndividuals(EnhGraph eg) {
        Graph g = eg.asGraph();
        return g.find(Node.ANY, RDF.type.asNode(), Node.ANY)
                .filterKeep(t -> isAnyClass(t.getObject(), eg))
                .mapWith(Triple::getSubject);
    }

    private static EnhNodeFactory createFactory(
            Class<? extends OntObjectImpl> impl,
            Class<? extends OntObject> classType,
            Resource resourceType,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            EnhNodeFilter filter) {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(resourceType);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType, producer);
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }
}
