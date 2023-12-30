package com.github.sszuev.jena.ontapi.utils;

import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Dyadic;
import org.apache.jena.graph.compose.Polyadic;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper to work with {@link Graph Jena Graph} (generally with our {@link UnionGraph}) and with its related objects:
 * {@link Triple} and {@link Node}.
 * <p>
 * Created @ssz on 06.02.2017.
 *
 * @see GraphUtil
 * @see GraphUtils
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Graphs {

    public static final String ANONYMOUS_ONTOLOGY_IDENTIFIER = "AnonymousOntology";
    public static final String RECURSIVE_GRAPH_IDENTIFIER = "Recursion";

    /**
     * Extracts and lists all top-level sub-graphs from the given composite graph-container,
     * that is allowed to be either {@link UnionGraph} or {@link Polyadic} or {@link Dyadic}.
     * If the graph is not of the list above, an empty stream is expected.
     * The base graph is not included in the resulting stream.
     * In case of {@link Dyadic}, the left graph is considered as a base and the right is a sub-graph.
     *
     * @param graph {@link Graph}
     * @return {@code Stream} of {@link Graph}s
     * @see Graphs#getBase(Graph)
     * @see UnionGraph
     * @see Polyadic
     * @see Dyadic
     */
    public static Stream<Graph> directSubGraphs(Graph graph) {
        if (graph instanceof UnionGraph) {
            return ((UnionGraph) graph).getUnderlying().graphs();
        }
        if (graph instanceof Polyadic) {
            return ((Polyadic) graph).getSubGraphs().stream();
        }
        if (graph instanceof Dyadic) {
            return Stream.of(((Dyadic) graph).getR());
        }
        return Stream.empty();
    }

    /**
     * Extracts the base (primary) base graph from a composite or wrapper graph if it is possible
     * otherwise returns the same graph.
     * If the specified graph is {@link Dyadic}, the left part is considered as base graph.
     * Note: the {@link org.apache.jena.graph.impl.WrappedGraph} is intentionally not included in the consideration:
     * any sub-instances of that class are considered as indivisible.
     *
     * @param graph {@link Graph}
     * @return {@link Graph}
     * @see Graphs#directSubGraphs(Graph)
     * @see GraphWrapper
     * @see UnionGraph
     * @see Polyadic
     * @see Dyadic
     */
    public static Graph getBase(Graph graph) {
        if (isGraphMem(graph)) {
            return graph;
        }
        Deque<Graph> candidates = new ArrayDeque<>();
        candidates.add(graph);
        Set<Graph> seen = new HashSet<>();
        while (!candidates.isEmpty()) {
            Graph g = candidates.removeFirst();
            if (!seen.add(g)) {
                continue;
            }
            if (g instanceof GraphWrapper) {
                candidates.add(((GraphWrapper) g).get());
                continue;
            }
            if (g instanceof UnionGraph) {
                candidates.add(((UnionGraph) g).getBaseGraph());
                continue;
            }
            if (g instanceof Polyadic) {
                candidates.add(((Polyadic) g).getBaseGraph());
                continue;
            }
            if (g instanceof Dyadic) {
                candidates.add(((Dyadic) g).getL());
                continue;
            }
            return g;
        }
        return graph;
    }

    /**
     * Answers {@code true} if the graph specified is {@code GraphMem}.
     *
     * @param graph {@link Graph}
     * @return {@code boolean}
     */
    @SuppressWarnings("deprecation")
    public static boolean isGraphMem(Graph graph) {
        return graph instanceof GraphMem;
    }

    /**
     * Lists all indivisible graphs extracted from the composite or wrapper graph
     * including the base as flat stream of non-composite (primitive) graphs.
     * For a well-formed ontological {@code Graph} the returned stream must
     * correspond the result of the method {@link OntModels#importsClosure(OntModel)}.
     *
     * @param graph {@link Graph}
     * @return {@code Stream} of {@link Graph}s
     * @see UnionGraph#listBaseGraphs()
     * @see OntModels#importsClosure(OntModel)
     */
    public static Stream<Graph> dataGraphs(Graph graph) {
        if (graph == null) {
            return Stream.empty();
        }
        if (isGraphMem(graph)) {
            return Stream.of(graph);
        }
        Set<Graph> res = new LinkedHashSet<>();
        Deque<Graph> candidates = new ArrayDeque<>();
        Set<Graph> seen = new HashSet<>();
        candidates.add(graph);
        while (!candidates.isEmpty()) {
            Graph g = candidates.removeFirst();
            if (!seen.add(g)) {
                continue;
            }
            Graph bg = getBase(g);
            res.add(bg);
            directSubGraphs(g).forEach(candidates::add);
        }
        return res.stream();
    }

    /**
     * Answers {@code true} iff the two input graphs are based on the same primitive graph.
     *
     * @param left  {@link Graph}
     * @param right {@link Graph}
     * @return {@code boolean}
     */
    public static boolean isSameBase(Graph left, Graph right) {
        return Objects.equals(getBase(left), getBase(right));
    }

    /**
     * Answers {@code true} iff the given graph is distinct.
     * A distinct {@code Graph} behaves like a {@code Set}:
     * for each pair of encountered triples {@code t1, t2} from any iterator, {@code !t1.equals(t2)}.
     *
     * @param graph {@link Graph} to test
     * @return {@code boolean} if {@code graph} is distinct
     * @see Spliterator#DISTINCT
     * @see UnionGraph#isDistinct()
     */
    public static boolean isDistinct(Graph graph) {
        if (isGraphMem(graph)) {
            return true;
        }
        if (graph instanceof UnionGraph) {
            UnionGraph u = (UnionGraph) graph;
            return u.isDistinct() || u.getUnderlying().isEmpty() && isDistinct(getBase(u));
        }
        return false;
    }

    /**
     * Answers {@code true} iff the given {@code graph} has known size
     * and therefore the operation {@code graph.size()} does not take significant efforts.
     * Composite graphs are considered as sized only if they relay on a single base graph,
     * since their sizes are not always a sum of part size.
     *
     * @param graph {@link Graph} to test
     * @return {@code boolean} if {@code graph} is sized
     * @see Spliterator#SIZED
     * @see Graphs#size(Graph)
     */
    public static boolean isSized(Graph graph) {
        if (isGraphMem(graph)) {
            return true;
        }
        if (directSubGraphs(graph).findFirst().isPresent()) {
            return false;
        }
        return isGraphMem(getBase(graph));
    }

    /**
     * Returns the number of triples in the {@code graph} as {@code long}.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return {@code long}
     * @see Graphs#isSized(Graph)
     */
    public static long size(Graph graph) {
        if (isGraphMem(graph)) {
            return graph.size();
        }
        if (directSubGraphs(graph).findFirst().isPresent()) {
            return Iterators.count(graph.find());
        }
        return getBase(graph).size();
    }

    /**
     * Converts the given graph to the hierarchical {@link UnionGraph Union Graph}
     * in accordance with their {@code owl:imports} declarations.
     * Irrelevant graphs are skipped from consideration.
     * If the input graph is already {@link UnionGraph} it will be returned unchanged.
     * The method can be used, for example, to get an ONT graph from the {@link org.apache.jena.ontology.OntModel}.
     * Note: it is a recursive method.
     *
     * @param graph {@link Graph}
     * @return {@link UnionGraph}
     * @throws StackOverflowError in case there is a loop in imports (i.e. a recursion in the hierarchy)
     */
    public static UnionGraph makeUnion(Graph graph) {
        if (graph instanceof UnionGraph) {
            return (UnionGraph) graph;
        }
        if (isGraphMem(graph)) {
            return new UnionGraph(graph);
        }
        return makeUnion(getBase(graph), dataGraphs(graph).collect(Collectors.toSet()));
    }

    /**
     * Builds a union-graph using the specified components.
     * Note: this is a recursive method.
     *
     * @param graph      {@link Graph} the base graph (root)
     * @param repository a {@code Collection} of {@link Graph graph}s to search in for missed dependencies
     * @return {@link UnionGraph}
     */
    public static UnionGraph makeUnion(Graph graph, Collection<Graph> repository) {
        Graph base = getBase(graph);
        Set<String> imports = getImports(base);
        UnionGraph res = new UnionGraph(base);
        repository.stream()
                .filter(x -> !isSameBase(base, x))
                .filter(g -> imports.contains(getOntologyIRI(g)))
                .forEach(g -> res.addGraph(makeUnion(g, repository)));
        return res;
    }

    /**
     * Creates a new {@code UnionGraph} with the given base {@code Graph}
     * and the same structure and settings as in the specified {@code UnionGraph}.
     *
     * @param base  {@link Graph} new base, not {@code null}
     * @param union {@link UnionGraph} to inherit settings and hierarchy, not {@code null}
     * @return {@link UnionGraph}
     */
    public static UnionGraph withBase(Graph base, UnionGraph union) {
        return new UnionGraph(base, union.getUnderlying(), union.getEventManager(), union.isDistinct());
    }

    /**
     * Gets Ontology URI from the base graph or returns {@code null}
     * if there is no {@code owl:Ontology} or it is anonymous ontology.
     *
     * @param graph {@link Graph}
     * @return String uri or {@code null}
     * @see Graphs#getImports(Graph)
     */
    public static String getOntologyIRI(Graph graph) {
        return ontologyNode(getBase(graph)).filter(Node::isURI).map(Node::getURI).orElse(null);
    }

    /**
     * Gets the "name" of the base graph: uri, blank-node-id as string or dummy string if there is no ontology at all.
     * The version IRI info is also included if it is present in the graph for the found ontology node.
     *
     * @param graph {@link Graph}
     * @return String
     * @see Graphs#getOntologyIRI(Graph)
     */
    public static String getName(Graph graph) {
        if (graph.isClosed()) {
            return "(closed)";
        }
        Optional<Node> res = ontologyNode(getBase(graph));
        if (res.isEmpty()) return ANONYMOUS_ONTOLOGY_IDENTIFIER;
        List<String> versions = graph.find(res.get(), OWL.versionIRI.asNode(), Node.ANY)
                .mapWith(Triple::getObject).mapWith(Node::toString).toList();
        if (versions.isEmpty()) {
            return String.format("<%s>", res.get());
        }
        return String.format("<%s%s>", res.get(), versions);
    }

    /**
     * Finds and returns the primary node within the given graph,
     * which is the subject in the {@code _:x rdf:type owl:Ontology} statement.
     * If there are both uri and blank ontological nodes together in the graph then it prefers uri.
     * Of several ontological nodes the same kind, it chooses the most bulky.
     * Note: it works with any graph, not necessarily with the base;
     * for a valid composite ontology graph a lot of ontological nodes are expected.
     *
     * @param graph {@link Graph}
     * @return {@link Optional} around the {@link Node} which could be uri or blank.
     */
    public static Optional<Node> ontologyNode(Graph graph) {
        List<Node> res = graph.find(Node.ANY, RDF.Nodes.type, OWL.Ontology.asNode())
                .mapWith(t -> {
                    Node n = t.getSubject();
                    return n.isURI() || n.isBlank() ? n : null;
                }).filterDrop(Objects::isNull).toList();
        if (res.isEmpty()) return Optional.empty();
        res.sort(rootNodeComparator(graph));
        return Optional.of(res.get(0));
    }

    /**
     * Returns a comparator for root nodes.
     * Tricky logic:
     * first compares roots as standalone nodes and any uri-node is considered less than any blank-node,
     * then compares roots as part of the graph using the rule 'the fewer children -&gt; the greater weight'.
     *
     * @param graph {@link Graph}
     * @return {@link Comparator}
     */
    public static Comparator<Node> rootNodeComparator(Graph graph) {
        return Comparator.comparing(Node::isURI).reversed()
                .thenComparing(Comparator.comparingInt((Node x) ->
                        graph.find(x, Node.ANY, Node.ANY).toList().size()).reversed());
    }

    /**
     * Returns all uri-objects from the {@code _:x owl:imports _:uri} statements.
     * In case of composite graph imports are listed transitively.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return unordered Set of uris from the whole graph (it may be composite)
     * @see Graphs#getOntologyIRI(Graph)
     */
    public static Set<String> getImports(Graph graph) {
        return listImports(graph).toSet();
    }

    /**
     * Returns an {@code ExtendedIterator} over all URIs from the {@code _:x owl:imports _:uri} statements.
     * In case of composite graph imports are listed transitively.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return {@link ExtendedIterator} of {@code String}-URIs
     * @see Graphs#getOntologyIRI(Graph)
     */
    public static ExtendedIterator<String> listImports(Graph graph) {
        return graph.find(Node.ANY, OWL.imports.asNode(), Node.ANY).mapWith(t -> {
            Node n = t.getObject();
            return n.isURI() ? n.getURI() : null;
        }).filterDrop(Objects::isNull);
    }

    /**
     * Prints a graph hierarchy tree.
     * For a valid ontology it should match an imports ({@code owl:imports}) tree also.
     * For debugging.
     * <p>
     * An examples of possible output:
     * <pre> {@code
     * <http://imports.test.Main.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *      <http://imports.test.D.ttl>
     * }, {@code
     * <http://imports.test.D.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *              <http://imports.test.Main.ttl>
     * } </pre>
     *
     * @param graph {@link Graph}
     * @return hierarchy tree as String
     */
    public static String importsTreeAsString(Graph graph) {
        Function<Graph, String> printDefaultGraphName = g -> g.getClass().getSimpleName() + "@" + Integer.toHexString(g.hashCode());
        return makeImportsTree(graph, g -> {
            if (g.isClosed()) return "Closed(" + printDefaultGraphName.apply(g) + ")";
            String res = getName(g);
            if (ANONYMOUS_ONTOLOGY_IDENTIFIER.equals(res)) {
                res += "(" + printDefaultGraphName.apply(g) + ")";
            }
            return res;
        }, "\t", "\t", new HashSet<>()).toString();
    }

    private static StringBuilder makeImportsTree(Graph graph,
                                                 Function<Graph, String> getName,
                                                 String indent,
                                                 String step,
                                                 Set<Graph> seen) {
        StringBuilder res = new StringBuilder();
        Graph base = getBase(graph);
        String name = getName.apply(base);
        try {
            if (!seen.add(graph)) {
                return res.append(RECURSIVE_GRAPH_IDENTIFIER).append(": ").append(name);
            }
            res.append(name).append("\n");
            directSubGraphs(graph)
                    .sorted(Comparator.comparingLong(o -> directSubGraphs(o).count()))
                    .forEach(sub -> res.append(indent)
                            .append(makeImportsTree(sub, getName, indent + step, step, seen)));
            return res;
        } finally {
            seen.remove(graph);
        }
    }

    /**
     * Collects a prefixes library from the collection of the graphs.
     *
     * @param graphs {@link Iterable} a collection of graphs
     * @return unmodifiable (locked) {@link PrefixMapping prefix mapping}
     */
    public static PrefixMapping collectPrefixes(Iterable<Graph> graphs) {
        PrefixMapping res = PrefixMapping.Factory.create();
        graphs.forEach(g -> res.setNsPrefixes(g.getPrefixMapping()));
        return res.lock();
    }

    /**
     * Answers {@code true} if the left graph depends on the right one.
     *
     * @param left  {@link Graph}
     * @param right {@link Graph}
     * @return {@code true} if the left argument graph is dependent on the right
     */
    public static boolean dependsOn(Graph left, Graph right) {
        return left == right || (left != null && left.dependsOn(right));
    }

    /**
     * Lists all unique subject nodes in the given graph.
     * Warning: the result is temporary stored in-memory!
     *
     * @param graph {@link Graph}, not {@code null}
     * @return an {@link ExtendedIterator ExtendedIterator} (<b>distinct</b>) of all subjects in the graph
     * @throws OutOfMemoryError while iterating in case the graph is too large
     *                          so that all its subjects can be placed in memory as a {@code Set}
     * @see GraphUtil#listSubjects(Graph, Node, Node)
     */
    public static ExtendedIterator<Node> listSubjects(Graph graph) {
        return Iterators.create(() -> Collections.unmodifiableSet(graph.find().mapWith(Triple::getSubject).toSet()).iterator());
    }

    /**
     * Lists all unique nodes in the given graph, which are used in a subject or an object positions.
     * Warning: the result is temporary stored in-memory!
     *
     * @param graph {@link Graph}, not {@code null}
     * @return an {@link ExtendedIterator ExtendedIterator} (<b>distinct</b>) of all subjects or objects in the graph
     * @throws OutOfMemoryError while iterating in case the graph is too large
     *                          so that all its subjects and objects can be placed in memory as a {@code Set}
     * @see GraphUtils#allNodes(Graph)
     */
    public static ExtendedIterator<Node> listSubjectsAndObjects(Graph graph) {
        return Iterators.create(() -> Collections.unmodifiableSet(Iterators.flatMap(graph.find(),
                t -> Iterators.of(t.getSubject(), t.getObject())).toSet()).iterator());
    }

    /**
     * Lists all unique nodes in the given graph.
     * Warning: the result is temporary stored in-memory!
     *
     * @param graph {@link Graph}, not {@code null}
     * @return an {@link ExtendedIterator ExtendedIterator} (<b>distinct</b>) of all nodes in the graph
     * @throws OutOfMemoryError while iterating in case the graph is too large to be placed in memory as a {@code Set}
     */
    public static ExtendedIterator<Node> listAllNodes(Graph graph) {
        return Iterators.create(() -> Collections.unmodifiableSet(Iterators.flatMap(graph.find(),
                t -> Iterators.of(t.getSubject(), t.getPredicate(), t.getObject())).toSet()).iterator());
    }

    /**
     * Makes a fresh node instance according to the given iri.
     *
     * @param iri String, an IRI to create URI-Node or {@code null} to create Blank-Node
     * @return {@link Node}, not {@code null}
     */
    public static Node createNode(String iri) {
        return iri == null ? NodeFactory.createBlankNode() : NodeFactory.createURI(iri);
    }

    /**
     * Answers {@code true} if all parts of the given RDF triple are URIs (i.e. not blank nodes or literals).
     *
     * @param triple a regular graph {@link Triple}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean isNamedTriple(Triple triple) {
        // in a valid RDF triple a predicate is a URI by definition
        return triple.getObject().isURI() && triple.getSubject().isURI();
    }

    /**
     * Inverts the given triple so that
     * the new triple has the same subject as the given object, and the same object as the given subject.
     *
     * @param triple {@code SPO} the {@link Triple}, not {@code null}
     * @return {@link Triple}, {@code OPS}
     */
    public static Triple invertTriple(Triple triple) {
        return Triple.create(triple.getObject(), triple.getPredicate(), triple.getSubject());
    }

    /**
     * Returns a {@link Spliterator} characteristics based on graph analysis.
     *
     * @param graph {@link Graph}
     * @return int
     */
    public static int getSpliteratorCharacteristics(Graph graph) {
        // a graph cannot return iterator with null-elements
        int res = Spliterator.NONNULL;
        if (isDistinct(graph)) {
            return res | Spliterator.DISTINCT;
        }
        return res;
    }

    /**
     * Answers {@code true}, if there is a declaration {@code node rdf:type $type},
     * where $type is one of the specified types.
     *
     * @param node  {@link Node} to test
     * @param g     {@link Graph}
     * @param types Set of {@link Node}-types
     * @return boolean
     */
    public static boolean hasOneOfType(Node node, Graph g, Set<Node> types) {
        if (types.isEmpty()) {
            return false;
        }
        if (types.size() == 1) {
            return g.contains(node, RDF.Nodes.type, types.iterator().next());
        }
        // depending on the type of the underlying graph, it may or may not be advantageous
        // to get all types at once, or ask many separate queries.
        // heuristically, we assume that fine-grain queries to an inference graph are preferable,
        // and all-at-once for other types, including persistent stores
        if (g instanceof InfGraph) {
            for (Node type : types) {
                if (g.contains(node, RDF.Nodes.type, type)) {
                    return true;
                }
            }
            return false;
        }
        return Iterators.anyMatch(g.find(node, RDF.Nodes.type, Node.ANY), triple -> types.contains(triple.getObject()));
    }
}
