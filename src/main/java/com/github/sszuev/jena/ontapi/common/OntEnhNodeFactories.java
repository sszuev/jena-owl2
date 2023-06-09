package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A helper (factory) to produce {@link EnhNodeFactory EnhNode factories} instances or its components.
 * <p>
 * Created by @ssz on 19.01.2019.
 */
public class OntEnhNodeFactories {

    @SafeVarargs
    public static EnhNodeFactory createFrom(EnhNodeFinder finder, Class<? extends OntObject>... types) {
        return createFrom(finder, Arrays.stream(types));
    }

    public static EnhNodeFactory createFrom(EnhNodeFinder finder, Stream<Class<? extends OntObject>> types) {
        return createMulti(finder, types.map(WrappedFactoryImpl::new));
    }

    public static EnhNodeFactory createCommon(Class<? extends EnhNode> impl,
                                              EnhNodeFinder finder,
                                              EnhNodeFilter filter,
                                              EnhNodeFilter... additional) {
        return createCommon(new EnhNodeProducer.Default(impl), finder, filter, additional);
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
        return new CompositeEnhNodeFactoryImpl(Objects.requireNonNull(finder, "Null finder"), null,
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
    public static String viewAsString(Class<? extends RDFNode> type) {
        return type.getName().replace(OntObject.class.getPackage().getName() + ".", "");
    }
}
