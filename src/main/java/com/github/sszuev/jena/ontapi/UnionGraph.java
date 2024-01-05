package com.github.sszuev.jena.ontapi;

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
 * It consists of two parts:
 * a {@link #getBaseGraph() base graph} and an {@link #listSubGraphs() sub-graphs} collection.
 * Underlying sub-graphs are only used for searching; modify operations are performed only on the base graph.
 * This graph allows building graph hierarchy which can be used to link different models.
 * Also, it allows recursion, that is, it can contain itself somewhere in the hierarchy.
 * The {@link PrefixMapping} of this graph is taken from the base graph,
 * and, therefore, any changes in it reflect both the base and this graph.
 */
@SuppressWarnings({"WeakerAccess"})
public interface UnionGraph extends Graph {

    @Override
    EventManager getEventManager();

    /**
     * Answers {@code true} iff this graph is distinct,
     * which means that the method {@link Graph#find(Triple)} does not produce duplicates.
     *
     * @return boolean
     */
    boolean isDistinct();

    /**
     * Returns the base (primary) data graph.
     *
     * @return {@link Graph}, not {@code null}
     */
    Graph getBaseGraph();

    /**
     * Answers {@code true} iff this {@code UnionGraph} has sub-graphs.
     *
     * @return boolean
     */
    boolean hasSubGraph();

    /**
     * Lists all sub-graphs.
     * The {@link #getBaseGraph() base graph} is not included in the result.
     *
     * @return {@link ExtendedIterator} of sub-{@link Graph graph}s
     */
    ExtendedIterator<Graph> listSubGraphs();

    /**
     * Lists all sub-graphs.
     * The {@link #getBaseGraph() base graph} is not included in the result.
     * Note: in a well-formed ontological {@code UnionGraph}
     * the result will contain only {@code UnionGraph}s.
     *
     * @return {@link ExtendedIterator} of sub-{@link Graph graph}s
     */
    Stream<Graph> subGraphs();

    /**
     * Adds the specified graph to the underlying graph collection.
     * Note: for a well-formed ontological {@code UnionGraph}
     * the input {@code graph} is expected to also be a {@code UnionGraph}.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    UnionGraph addSubGraph(Graph graph);

    /**
     * Removes the specified graph from the underlying graph collection.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    UnionGraph removeSubGraph(Graph graph);

    /**
     * Adds the specified graph to the underlying graph collection if it is absent.
     * Note: for a well-formed ontological {@code UnionGraph}
     * the input {@code graph} is expected to also be a {@code UnionGraph}.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    default UnionGraph addSubGraphIfAbsent(Graph graph) {
        if (!contains(graph)) {
            addSubGraph(graph);
        }
        return this;
    }

    /**
     * Answers {@code true} iff this {@code UnionGraph} contains the specified graph as a sub-graph.
     *
     * @return boolean
     */
    default boolean contains(Graph graph) {
        return subGraphs().anyMatch(it -> it.equals(graph));
    }

    /**
     * An enhanced {@link GraphEventManager Jena Graph Event Manager} and {@link Listener}s.
     */
    interface EventManager extends GraphEventManager, Listener {

        /**
         * Turns off all listeners.
         */
        void off();

        /**
         * Turns on all listeners.
         */
        void on();

        /**
         * Lists all encapsulated listeners.
         *
         * @return Stream of {@link GraphListener}s
         */
        Stream<GraphListener> listeners();

        /**
         * Lists all encapsulated listeners.
         *
         * @param type {@code Class}-type of {@link GraphListener}
         * @return Stream of {@link GraphListener}s
         */
        @SuppressWarnings("unchecked")
        default <L extends GraphListener> Stream<L> listeners(Class<L> type) {
            return listeners().filter(it -> type.isAssignableFrom(it.getClass())).map(it -> (L) it);
        }
    }

    interface Listener extends GraphListener {
        /**
         * Called before {@link UnionGraph#addSubGraph(Graph)}
         *
         * @param graph    {@link UnionGraph}
         * @param subGraph {@link Graph}
         */
        void onAddSubGraph(UnionGraph graph, Graph subGraph);

        /**
         * Called after {@link UnionGraph#addSubGraph(Graph)}
         *
         * @param graph    {@link UnionGraph}
         * @param subGraph {@link Graph}
         */
        void notifySubGraphAdded(UnionGraph graph, Graph subGraph);

        /**
         * Called before {@link UnionGraph#removeSubGraph(Graph)}
         *
         * @param graph    {@link Graph}
         * @param subGraph {@link Graph}
         */
        void onRemoveSubGraph(UnionGraph graph, Graph subGraph);

        /**
         * Called after {@link UnionGraph#removeSubGraph(Graph)}
         *
         * @param graph    {@link Graph}
         * @param subGraph {@link Graph}
         */
        void notifySubGraphRemoved(UnionGraph graph, Graph subGraph);

    }
}
