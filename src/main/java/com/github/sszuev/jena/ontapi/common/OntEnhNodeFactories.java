package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.StdModels;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper (factory) to produce {@link EnhNodeFactory EnhNode factories} instances or its components.
 * <p>
 * Created by @ssz on 19.01.2019.
 */
public class OntEnhNodeFactories {

    @SafeVarargs
    public static EnhNodeFactory createFrom(EnhNodeFinder finder,
                                            Class<? extends OntObject> type,
                                            Class<? extends OntObject>... types) {
        return createFrom(finder,
                Stream.concat(Stream.of(type), Arrays.stream(types)).collect(Collectors.toList()).stream());
    }

    public static EnhNodeFactory createFrom(EnhNodeFinder finder,
                                            Stream<Class<? extends OntObject>> types) {
        return createMulti(Objects.requireNonNull(finder, "Null finder"), types.map(WrappedEnhNodeFactory::new));
    }

    public static EnhNodeFactory createFrom(EnhNodeFinder finder,
                                            EnhNodeFactory factory,
                                            EnhNodeFactory... factories) {
        return createMulti(Objects.requireNonNull(finder, "Null finder"),
                Stream.concat(Stream.of(factory), Arrays.stream(factories)).collect(Collectors.toList()).stream());
    }

    public static EnhNodeFactory createFrom(EnhNodeFactory factory,
                                            EnhNodeFactory... factories) {
        return createMulti(null,
                Stream.concat(Stream.of(factory), Arrays.stream(factories)).collect(Collectors.toList()).stream());
    }

    public static EnhNodeFactory createCommon(Class<? extends EnhNode> impl,
                                              EnhNodeFinder finder,
                                              EnhNodeFilter filter,
                                              EnhNodeFilter... additional) {
        return createCommon(new EnhNodeProducer.Default(impl), finder, filter, additional);
    }

    public static EnhNodeFactory createCommon(Class<? extends EnhNode> impl,
                                              BiFunction<Node, EnhGraph, EnhNode> producer,
                                              EnhNodeFinder finder,
                                              EnhNodeFilter filter) {
        return createCommon(new EnhNodeProducer.Default(impl, producer), finder, filter);
    }

    public static EnhNodeFactory createCommon(EnhNodeProducer maker, EnhNodeFinder finder, EnhNodeFilter primary, EnhNodeFilter... additional) {
        return new CommonEnhNodeFactoryImpl(Objects.requireNonNull(maker, "Null maker"),
                Objects.requireNonNull(finder, "Null finder"),
                Objects.requireNonNull(primary, "Null filter").accumulate(additional));
    }

    public static EnhNodeFactory createCommon(Class<? extends OntObject> type,
                                              EnhNodeProducer maker,
                                              EnhNodeFinder finder,
                                              EnhNodeFilter filter) {
        Objects.requireNonNull(type, "Null type");
        return new CommonEnhNodeFactoryImpl(Objects.requireNonNull(maker, "Null maker"),
                Objects.requireNonNull(finder, "Null finder"),
                Objects.requireNonNull(filter, "Null filter")) {

            @Override
            public String toString() {
                return String.format("ObjectFactory[%s]", viewAsString(type));
            }
        };
    }

    public static EnhNodeFinder createFinder(Resource... types) {
        return createFinder(FrontsNode::asNode, types);
    }

    @SafeVarargs
    public static <R> EnhNodeFinder createFinder(Function<R, Node> asNode, R... types) {
        return eg -> Iterators.distinct(listTriplesForTypes(eg.asGraph(), asNode, types).mapWith(Triple::getSubject));
    }

    private static EnhNodeFactory createMulti(EnhNodeFinder finder, Stream<EnhNodeFactory> factories) {
        return new CompositeEnhNodeFactoryImpl(finder, null,
                factories.peek(x -> Objects.requireNonNull(x, "Null component-factory")).toArray(EnhNodeFactory[]::new));
    }

    @SafeVarargs
    private static <R> ExtendedIterator<Triple> listTriplesForTypes(Graph g, Function<R, Node> asNode, R... types) {
        return Iterators.flatMap(Iterators.of(types).mapWith(asNode), t -> g.find(Node.ANY, RDF.Nodes.type, t));
    }

    /**
     * Answers a short form of the given class-type.
     *
     * @param type {@code Class}-type, not {@code null}
     * @return String
     */
    public static String viewAsString(Class<?> type) {
        return type.getName().replace(OntObject.class.getPackage().getName() + ".", "");
    }

    static String toPrintString(Node node, EnhGraph graph) {
        if (!(graph instanceof Model)) {
            return node.toString(PrefixMapping.Standard);
        }
        Model m = ((Model) graph);
        if (m.getGraph() instanceof InfGraph) {
            return node.toString(PrefixMapping.Standard);
        }
        RDFNode rdfNode = m.asRDFNode(node);
        if (!rdfNode.isResource()) {
            return node.toString(PrefixMapping.Standard);
        }
        PrefixMapping pm = PrefixMapping.Factory.create()
                .setNsPrefixes((PrefixMapping) graph)
                .setNsPrefixes(PrefixMapping.Standard);
        StringBuilder sb = new StringBuilder("\n");
        rdfNode.asResource().listProperties().forEach(s -> sb.append(StdModels.toString(s, pm)).append("\n"));
        return sb.toString();
    }
}
