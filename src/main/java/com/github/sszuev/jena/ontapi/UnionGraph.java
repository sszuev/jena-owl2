package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.utils.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * UnionGraph.
 * <p>
 * It consists of two parts: a {@link #getBaseGraph() base graph} and an {@link SubGraphs sub-graphs} collection.
 * Unlike {@link org.apache.jena.graph.compose.MultiUnion MultiUnion} this implementation explicitly requires primary (base) graph.
 * Underlying sub-graphs are only used for searching; modify operations are performed only on the base graph.
 * This graph allows building graph hierarchy which can be used to link different models.
 * Also, it allows recursion, that is, it can contain itself somewhere in the hierarchy.
 * The {@link PrefixMapping} of this graph is taken from the base graph,
 * and, therefore, any changes in it reflect both the base and this graph.
 */
@SuppressWarnings({"WeakerAccess"})
public interface UnionGraph extends Graph {

    /**
     * Answers the ont event manager for this graph.
     *
     * @return {@link OntEventManager}, not {@code null}
     */
    @Override
    OntEventManager getEventManager();

    /**
     * Answers {@code true} iff this graph is distinct,
     * which means that the method {@link Graph#find(Triple)} does not produce duplicates.
     *
     * @return boolean
     */
    boolean isDistinct();

    /**
     * Returns the base (primary) graph.
     *
     * @return {@link Graph}, not {@code null}
     */
    Graph getBaseGraph();

    /**
     * Returns the underlying graph, possible empty.
     *
     * @return {@link SubGraphs}, not {@code null}
     */
    SubGraphs getUnderlying();

    /**
     * Adds the specified graph to the underlying graph collection.
     * Note: for a well-formed ontological {@code UnionGraph}
     * the input {@code graph} must be also a {@code UnionGraph}, even it has no hierarchy structure.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    default UnionGraph addGraph(Graph graph) {
        getUnderlying().add(graph);
        return this;
    }

    /**
     * Removes the specified graph from the underlying graph collection.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    default UnionGraph removeGraph(Graph graph) {
        getUnderlying().remove(graph);
        return this;
    }

    /**
     * A container to hold all sub-graphs, that make up the hierarchy.
     * Such a representation of sub-graphs collection in the form of separate class allows
     * sharing its instance among different {@code UnionGraph} instances
     * to impart whole hierarchy structure when it is needed.
     */
    interface SubGraphs {

        /**
         * Removes the given graph from the underlying collection.
         * Maybe overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        void remove(Graph graph);

        /**
         * Adds the given graph into the underlying collection.
         * Maybe overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        void add(Graph graph);

        /**
         * Lists all sub-graphs.
         *
         * @return {@link ExtendedIterator} of sub-{@link Graph graph}s
         */
        ExtendedIterator<Graph> listGraphs();

        /**
         * Lists all sub-graphs.
         *
         * @return {@code Stream} of sub-{@link Graph graph}s
         */
        default Stream<Graph> graphs() {
            return Iterators.asStream(listGraphs());
        }

        /**
         * Answers {@code true} iff this container is empty.
         *
         * @return boolean
         */
        boolean isEmpty();

    }

    /**
     * An extended {@link org.apache.jena.graph.GraphEventManager Jena Graph Event Manager},
     * a holder for {@link GraphListener}s.
     */
    interface OntEventManager extends GraphEventManager {

        /**
         * Lists all encapsulated listeners.
         *
         * @return Stream of {@link GraphListener}s
         */
        Stream<GraphListener> listeners();

        /**
         * Answers {@code true} if there is a {@link GraphListener listener}
         * which is a subtype of the specified class-type.
         *
         * @param view {@code Class}-type of {@link GraphListener}
         * @return boolean
         */
        default boolean hasListeners(Class<? extends GraphListener> view) {
            return listeners().anyMatch(l -> view.isAssignableFrom(l.getClass()));
        }
    }
}
