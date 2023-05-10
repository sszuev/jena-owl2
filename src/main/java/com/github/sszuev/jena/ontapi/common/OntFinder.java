package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A class-helper to perform the preliminary resource search in a model.
 * Subsequently, the search result Stream will be filtered by the {@link OntFilter} instance.
 * Used as a component in {@link CommonFactoryImpl default factory} and {@link MultiFactoryImpl} implementations
 * <p>
 * Created @ssz on 07.11.2016.
 */
@FunctionalInterface
public interface OntFinder {
    OntFinder ANY_SUBJECT = eg -> Graphs.listSubjects(eg.asGraph());
    OntFinder ANY_BLANK_SUBJECT = eg -> Iterators.distinct(eg.asGraph().find().mapWith(Triple::getSubject).filterKeep(Node::isBlank));
    OntFinder ANY_SUBJECT_AND_OBJECT = eg -> Graphs.listSubjectsAndObjects(eg.asGraph());
    OntFinder ANYTHING = eg -> Graphs.listAllNodes(eg.asGraph());
    OntFinder TYPED = new ByPredicate(RDF.type);

    /**
     * Returns an iterator over the nodes in the given model, which satisfy some criterion,
     * specific to this {@link OntFinder}.
     * It is expected that the result does not contain duplicates.
     *
     * @param eg {@link EnhGraph}, model
     * @return {@link ExtendedIterator} of {@link Node}s
     */
    ExtendedIterator<Node> iterator(EnhGraph eg);

    /**
     * Lists the nodes from the specified model by the encapsulated criterion.
     *
     * @param eg {@link EnhGraph}, model
     * @return {@link Stream} of {@link Node}s
     */
    default Stream<Node> find(EnhGraph eg) {
        return Iterators.asStream(iterator(eg));
    }

    default OntFinder restrict(OntFilter filter) {
        if (Objects.requireNonNull(filter, "Null restriction filter.").equals(OntFilter.TRUE)) return this;
        if (filter.equals(OntFilter.FALSE)) return eg -> NullIterator.instance();
        return eg -> iterator(eg).filterKeep(n -> filter.test(n, eg));
    }

    class ByType implements OntFinder {
        protected final Node type;

        public ByType(Resource type) {
            this.type = Objects.requireNonNull(type, "Null type.").asNode();
        }

        @Override
        public ExtendedIterator<Node> iterator(EnhGraph eg) {
            return eg.asGraph().find(Node.ANY, RDF.Nodes.type, type).mapWith(Triple::getSubject);
        }
    }

    class ByPredicate implements OntFinder {
        protected final Node predicate;

        public ByPredicate(Property predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Null predicate.").asNode();
        }

        @Override
        public ExtendedIterator<Node> iterator(EnhGraph eg) {
            return Iterators.distinct(eg.asGraph().find(Node.ANY, predicate, Node.ANY).mapWith(Triple::getSubject));
        }
    }
}