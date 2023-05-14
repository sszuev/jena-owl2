package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.CompositionBase;
import org.apache.jena.graph.impl.SimpleEventManager;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * UnionGraph.
 * <p>
 * It consists of two parts: a {@link #base base graph} and an {@link SubGraphs sub-graphs} collection.
 * Unlike {@link org.apache.jena.graph.compose.MultiUnion MultiUnion} this implementation explicitly requires primary (base) graph.
 * Underlying sub-graphs are only used for searching; modify operations are performed only on the base graph.
 * This graph allows to build graph hierarchy which can be used to link different models.
 * Also, it allows recursion, that is, it can contain itself somewhere in the hierarchy.
 * The {@link PrefixMapping} of this graph is taken from the base graph,
 * and, therefore, any changes in it reflects both the base and this graph.
 * <p>
 * Created @ssz on 28.10.2016.
 */
@SuppressWarnings({"WeakerAccess"})
public class UnionGraph extends CompositionBase {

    protected final Graph base;
    protected final SubGraphs subGraphs;
    protected final boolean distinct;

    /**
     * A set of parents, used to control {@link #bases}.
     * Items of this {@code Set} are removed automatically by GC
     * if there are no more strong references (a graph/model is removed, i.e. there is no its usage anymore).
     */
    protected Set<UnionGraph> parents = Collections.newSetFromMap(new WeakHashMap<>());
    /**
     * Internal cache to hold all base graphs, used while {@link Graph#find(Triple) #find(..)}.
     * This {@code Set} cannot contain {@link UnionGraph}s.
     */
    protected Set<Graph> bases;

    /**
     * Creates an instance with default settings.
     * <p>
     * Note: it results a distinct graph (i.e. its parameter {@link #distinct} is {@code true}).
     * This means that the method {@link #find(Triple)} does not produce duplicates.
     * The additional duplicate checking may lead to temporary writing
     * the whole graph or some its part into memory in the form of {@code Set},
     * and for huge ontologies it is unacceptable.
     * This checking is not performed if the graph is single (underlying part is empty).
     * <p>
     * Also notice, a top-level ontology view of in-memory graph is not sensitive to the distinct parameter
     * since it uses only base graphs to collect axiomatic data.
     *
     * @param base {@link Graph}, not {@code null}
     */
    public UnionGraph(Graph base) {
        this(base, true);
    }

    /**
     * Creates a graph for the given {@code base},
     * which will be either distinct or non-distinct, depending on the second parameter.
     * Other settings are default.
     *
     * @param base     {@link Graph}, not {@code null}
     * @param distinct if {@code true} a distinct graph is created
     */
    public UnionGraph(Graph base, boolean distinct) {
        this(base, null, null, distinct);
    }

    /**
     * The base constructor.
     * A well-formed ontology {@link UnionGraph} is expected to
     * have a plain (non-union) graph as a {@link #getBaseGraph() root}
     * and {@code UnionGraph} as {@link #getUnderlying() leaves}.
     *
     * @param base      {@link Graph}, not {@code null}
     * @param subGraphs {@link SubGraphs} or {@code null} to use default empty sub-graph container
     * @param gem       {@link OntEventManager} or {@code null} to use default fresh event manager
     * @param distinct  if {@code true}, the method {@link #find(Triple)} returns an iterator avoiding duplicates
     * @throws NullPointerException if base graph is {@code null}
     */
    public UnionGraph(Graph base, SubGraphs subGraphs, OntEventManager gem, boolean distinct) {
        this.base = Objects.requireNonNull(base, "Null base graph.");
        this.subGraphs = subGraphs == null ? new SubGraphs() : subGraphs;
        this.gem = gem == null ? new OntEventManager() : gem;
        this.distinct = distinct;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return getBaseGraph().getPrefixMapping();
    }

    /**
     * Answers the ont event manager for this graph.
     *
     * @return {@link OntEventManager}, not {@code null}
     */
    @Override
    public OntEventManager getEventManager() {
        return (OntEventManager) gem;
    }

    /**
     * Answers {@code true} iff this graph is distinct.
     * See {@link #UnionGraph(Graph)} description.
     *
     * @return boolean
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Returns the base (primary) graph.
     *
     * @return {@link Graph}, not {@code null}
     */
    public Graph getBaseGraph() {
        return base;
    }

    /**
     * Returns the underlying graph, possible empty.
     *
     * @return {@link SubGraphs}, not {@code null}
     */
    public SubGraphs getUnderlying() {
        return subGraphs;
    }

    @Override
    public void performAdd(Triple t) {
        if (!subGraphs.contains(t))
            base.add(t);
    }

    @Override
    public void performDelete(Triple t) {
        base.delete(t);
    }

    /**
     * Adds the specified graph to the underlying graph collection.
     * Note: for a well-formed ontological {@code UnionGraph}
     * the input {@code graph} must be also a {@code UnionGraph}, even it has no hierarchy structure.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    public UnionGraph addGraph(Graph graph) {
        checkOpen();
        getUnderlying().add(graph);
        addParent(graph);
        resetGraphsCache();
        return this;
    }

    protected void addParent(Graph graph) {
        if (!(graph instanceof UnionGraph)) {
            return;
        }
        ((UnionGraph) graph).parents.add(this);
    }

    /**
     * Removes the specified graph from the underlying graph collection.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    public UnionGraph removeParent(Graph graph) {
        checkOpen();
        getUnderlying().remove(graph);
        removeUnion(graph);
        resetGraphsCache();
        return this;
    }

    protected void removeUnion(Graph graph) {
        if (!(graph instanceof UnionGraph)) {
            return;
        }
        ((UnionGraph) graph).parents.remove(this);
    }

    /**
     * Clears the {@link #bases cache}.
     */
    protected void resetGraphsCache() {
        getAllLinkedUnionGraphs().forEach(x -> x.bases = null);
    }

    /**
     * Lists all indivisible (base) data {@code Graph}s
     * that are encapsulated either in the hierarchy
     * or (which is possible) inside the {@link #getBaseGraph() base} (root) graph itself.
     *
     * @return <b>distinct</b> {@link ExtendedIterator} of {@link Graph}s, including the base graph
     * @see UnionGraph#getBaseGraph()
     */
    public ExtendedIterator<Graph> listBaseGraphs() {
        return Iterators.create(bases == null ? bases = getAllBaseGraphs() : bases);
    }

    /**
     * Performs the find operation.
     * Override {@code graphBaseFind} to return an iterator that will report when a deletion occurs.
     *
     * @param m {@link Triple} the matcher to match against, not {@code null}
     * @return an {@link ExtendedIterator iterator} of all triples matching {@code m} in the union of the graphs
     * @see org.apache.jena.graph.compose.MultiUnion#graphBaseFind(Triple)
     */
    @Override
    protected final ExtendedIterator<Triple> graphBaseFind(Triple m) {
        return SimpleEventManager.notifyingRemove(this, createFindIterator(m));
    }

    /**
     * Answers {@code true} if the graph contains any triple matching {@code t}.
     *
     * @param t {@link Triple}, not {@code null}
     * @return boolean
     * @see org.apache.jena.graph.compose.MultiUnion#graphBaseContains(Triple)
     */
    @Override
    public boolean graphBaseContains(Triple t) {
        if (base.contains(t)) return true;
        if (subGraphs.isEmpty()) return false;
        Iterator<Graph> graphs = listBaseGraphs();
        while (graphs.hasNext()) {
            Graph g = graphs.next();
            if (g == base) continue;
            if (g.contains(t)) return true;
        }
        return false;
    }

    @Override
    public int graphBaseSize() {
        if (subGraphs.isEmpty()) {
            return base.size();
        }
        return super.graphBaseSize();
    }

    @Override
    public boolean isEmpty() {
        // the default implementation use size(), which is extremely ineffective in general,
        // since implies iterating over whole graph
        return Iterators.findFirst(find()).isEmpty();
    }

    /**
     * Creates an extended iterator to be used in {@link Graph#find(Triple)}.
     *
     * @param m {@link Triple} pattern, not {@code null}
     * @return {@link ExtendedIterator} of {@link Triple}s
     * @see org.apache.jena.graph.compose.Union#_graphBaseFind(Triple)
     * @see org.apache.jena.graph.compose.MultiUnion#multiGraphFind(Triple)
     */
    @SuppressWarnings("JavadocReference")
    protected ExtendedIterator<Triple> createFindIterator(Triple m) {
        if (subGraphs.isEmpty()) {
            return base.find(m);
        }
        if (!distinct) {
            return Iterators.flatMap(listBaseGraphs(), x -> x.find(m));
        }
        // The logic and the comment below have been copy-pasted from the org.apache.jena.graph.compose.Union:
        // To find in the union, find in the components, concatenate the results, and omit duplicates.
        // That last is a performance penalty,
        // but I see no way to remove it unless we know the graphs do not overlap.
        Set<Triple> seen = createSet();
        return Iterators.flatMap(listBaseGraphs(), x -> CompositionBase.recording(rejecting(x.find(m), seen), seen));
    }

    /**
     * Creates a {@code Set} to be used while {@link Graph#find()}.
     * The returned set may contain a huge number of items.
     * And that's why this method has protected access -
     * implementations are allowed to override it for better performance.
     *
     * @return Set of {@link Triple}s
     */
    protected Set<Triple> createSet() {
        return new HashSet<>();
    }

    /**
     * Closes the graph including all related graphs.
     * Caution: this is an irreversible operation,
     * once closed a graph can not be reopened.
     */
    @Override
    public void close() {
        listBaseGraphs().forEachRemaining(Graph::close);
        getAllUnderlyingUnionGraphs().forEach(x -> x.closed = true);
    }

    /**
     * Generic dependsOn, returns {@code true} iff this graph or any sub-graphs depend on the specified graph.
     *
     * @param other {@link Graph}
     * @return boolean
     */
    @Override
    public boolean dependsOn(Graph other) {
        return (other instanceof UnionGraph && getAllUnderlyingUnionGraphs().contains(other))
                || Iterators.anyMatch(listBaseGraphs(), x -> Graphs.dependsOn(x, other));
    }

    /**
     * Calculates and returns a {@code Set} of all indivisible {@link Graph graph}s
     * that are placed somewhere lower in the hierarchy.
     * The method also includes into consideration a rare possibility
     * when the {@link #getBaseGraph() base} graph is also an {@code UnionGraph}.
     * The primary indivisible part of the base graph (which is usually a base graph itself)
     * is added to the beginning of the returned collection.
     *
     * @return a {@code Set} (ordered) of {@link Graph}s
     */
    protected Set<Graph> getAllBaseGraphs() {
        Set<Graph> res = new LinkedHashSet<>();
        Set<UnionGraph> visited = new HashSet<>();
        List<Graph> queue = new LinkedList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            Graph next = queue.remove(0);
            if (next instanceof UnionGraph) {
                UnionGraph u = (UnionGraph) next;
                if (visited.add(u)) {
                    queue.add(u.base);
                    queue.addAll(u.subGraphs.graphs);
                }
            } else {
                res.add(next);
            }
        }
        return res;
    }

    /**
     * Recursively collects a {@code Set} of all {@link UnionGraph UnionGraph}s
     * that are related to this instance somehow,
     * i.e. are present in the hierarchy lower or higher.
     * This union graph instance is also included in the returned {@code Set}.
     *
     * @return Set of {@link UnionGraph}s
     */
    protected Set<UnionGraph> getAllLinkedUnionGraphs() {
        Set<UnionGraph> res = new LinkedHashSet<>();
        List<UnionGraph> queue = new LinkedList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            UnionGraph next = queue.remove(0);
            if (res.add(next)) {
                next.parents.stream().filter(res::add).forEach(queue::add);
                next.getAllUnderlyingUnionGraphs().stream().filter(res::add).forEach(queue::add);
            }

        }
        return res;
    }

    /**
     * Recursively collects all {@link UnionGraph}s that underlies this instance, inclusive.
     *
     * @return Set of {@link UnionGraph}s
     */
    protected Set<UnionGraph> getAllUnderlyingUnionGraphs() {
        Set<UnionGraph> res = new LinkedHashSet<>();
        List<UnionGraph> queue = new LinkedList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            UnionGraph next = queue.remove(0);
            if (res.add(next)) {
                next.unionSubGraphs().forEach(queue::add);
            }
        }
        return res;
    }

    private Stream<UnionGraph> unionSubGraphs() {
        return getUnderlying().graphs()
                .filter(g -> g instanceof UnionGraph)
                .map(u -> (UnionGraph) u);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)@%s", getClass().getName(), Graphs.getName(this), Integer.toHexString(hashCode()));
    }

    /**
     * A container to hold all sub-graphs, that make up the hierarchy.
     * Such a representation of sub-graphs collection in the form of separate class allows
     * to share its instance among different {@code UnionGraph} instances
     * in order to impart whole hierarchy structure when it is needed.
     */
    public static class SubGraphs {
        protected final Collection<Graph> graphs;

        protected SubGraphs() {
            this(new ArrayList<>());
        }

        protected SubGraphs(Collection<Graph> graphs) {
            this.graphs = Objects.requireNonNull(graphs);
        }

        /**
         * Lists all sub-graphs.
         *
         * @return {@link ExtendedIterator} of sub-{@link Graph graph}s
         */
        public ExtendedIterator<Graph> listGraphs() {
            return Iterators.create(graphs);
        }

        /**
         * Lists all sub-graphs.
         *
         * @return {@code Stream} of sub-{@link Graph graph}s
         */
        public Stream<Graph> graphs() {
            return graphs.stream();
        }

        /**
         * Answers {@code true} iff this container is empty.
         *
         * @return boolean
         */
        public boolean isEmpty() {
            return graphs.isEmpty();
        }

        /**
         * Removes the given graph from the underlying collection.
         * May be overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        protected void remove(Graph graph) {
            graphs.remove(graph);
        }

        /**
         * Adds the given graph into the underlying collection.
         * May be overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        protected void add(Graph graph) {
            graphs.add(Objects.requireNonNull(graph));
        }

        /**
         * Tests if the given triple belongs to any of the sub-graphs.
         *
         * @param t {@link Triple} to test
         * @return boolean
         * @see Graph#contains(Triple)
         */
        protected boolean contains(Triple t) {
            if (graphs.isEmpty()) return false;
            for (Graph g : graphs) {
                if (g.contains(t)) return true;
            }
            return false;
        }
    }

    /**
     * An extended {@link org.apache.jena.graph.GraphEventManager Jena Graph Event Manager},
     * a holder for {@link GraphListener}s.
     */
    public static class OntEventManager extends SimpleEventManager {

        /**
         * Lists all encapsulated listeners.
         *
         * @return Stream of {@link GraphListener}s
         */
        public Stream<GraphListener> listeners() {
            return listeners.stream();
        }

        /**
         * Answers {@code true} if there is a {@link GraphListener listener}
         * which is a subtype of the specified class-type.
         *
         * @param view {@code Class}-type of {@link GraphListener}
         * @return boolean
         */
        public boolean hasListeners(Class<? extends GraphListener> view) {
            return listeners().anyMatch(l -> view.isAssignableFrom(l.getClass()));
        }
    }
}
