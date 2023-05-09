package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A helper (factory) to produce {@link ObjectFactory object factory} instances or its components.
 * <p>
 * Created by @ssz on 19.01.2019.
 */
public class Factories {

    @SafeVarargs
    public static ObjectFactory createFrom(OntFinder finder, Class<? extends OntObject>... types) {
        return createFrom(finder, Arrays.stream(types));
    }

    public static ObjectFactory createFrom(OntFinder finder, Stream<Class<? extends OntObject>> types) {
        return createMulti(finder, types.map(WrappedFactoryImpl::new));
    }

    public static ObjectFactory createCommon(Class<? extends OntObjectImpl> impl,
                                             OntFinder finder,
                                             OntFilter filter,
                                             OntFilter... additional) {
        return createCommon(new OntMaker.Default(impl), finder, filter, additional);
    }

    public static ObjectFactory createCommon(OntMaker maker, OntFinder finder, OntFilter primary, OntFilter... additional) {
        return new CommonFactoryImpl(Objects.requireNonNull(maker, "Null maker"),
                Objects.requireNonNull(finder, "Null finder"),
                Objects.requireNonNull(primary, "Null filter").accumulate(additional));
    }

    public static ObjectFactory createCommon(Class<? extends OntObject> type,
                                             OntMaker maker,
                                             OntFinder finder,
                                             OntFilter filter) {
        Objects.requireNonNull(type, "Null type");
        return new CommonFactoryImpl(Objects.requireNonNull(maker, "Null maker"),
                Objects.requireNonNull(finder, "Null finder"),
                Objects.requireNonNull(filter, "Null filter")) {

            @Override
            public String toString() {
                return String.format("ObjectFactory[%s]", OntObjectImpl.viewAsString(type));
            }
        };
    }

    public static OntFinder createFinder(Resource... types) {
        return createFinder(FrontsNode::asNode, types);
    }

    @SafeVarargs
    public static <R> OntFinder createFinder(Function<R, Node> asNode, R... types) {
        return eg -> Iterators.distinct(listTriplesForTypes(eg.asGraph(), asNode, types).mapWith(Triple::getSubject));
    }

    private static ObjectFactory createMulti(OntFinder finder, Stream<ObjectFactory> factories) {
        return new MultiFactoryImpl(Objects.requireNonNull(finder, "Null finder"), null,
                factories.peek(x -> Objects.requireNonNull(x, "Null component-factory")).toArray(ObjectFactory[]::new));
    }

    @SafeVarargs
    private static <R> ExtendedIterator<Triple> listTriplesForTypes(Graph g, Function<R, Node> asNode, R... types) {
        return Iterators.flatMap(Iterators.of(types).mapWith(asNode), t -> g.find(Node.ANY, RDF.Nodes.type, t));
    }
}
