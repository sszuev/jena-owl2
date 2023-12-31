package com.github.sszuev.jena.ontapi.utils;

import com.github.sszuev.jena.ontapi.OntModelFactory;
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
     * Answers {@code true} if the graph specified is {@code InfGraph}.
     *
     * @param graph {@link Graph}
     * @return {@code boolean}
     */
    public static boolean isGraphInf(Graph graph) {
        return graph instanceof InfGraph;
    }

    /**
     * Lists all indivisible graphs extracted from the composite or wrapper graph
     * including the base as flat stream of non-composite (primitive) graphs.
     * For a well-formed ontological {@code Graph} the returned stream must
     * correspond the result of the method {@link OntModels#importsClosure(OntModel)}.
     *
     * @param graph {@link Graph}
     * @return {@code Stream} of {@link Graph}s
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
            return OntModelFactory.wrapAsUnionGraph(graph);
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
        UnionGraph res = OntModelFactory.wrapAsUnionGraph(base);
        repository.stream()
                .filter(x -> !isSameBase(base, x))
                .filter(g -> imports.contains(getOntologyIRI(g)))
                .forEach(g -> res.addGraph(makeUnion(g, repository)));
        return res;
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
        Optional<Node> id = ontologyNode(getBase(graph));
        if (id.isEmpty()) {
            return ANONYMOUS_ONTOLOGY_IDENTIFIER;
        }
        ExtendedIterator<String> versions = graph.find(id.get(), OWL.versionIRI.asNode(), Node.ANY)
                .mapWith(Triple::getObject).mapWith(Node::toString);
        try {
            Set<String> res = versions.toSet();
            if (res.isEmpty()) {
                return String.format("<%s>", id.get());
            }
            return String.format("<%s%s>", id.get(), res);
        } finally {
            versions.close();
        }
    }

    /**
     * Finds and returns the primary node within the given graph,
     * which is the subject in the {@code _:x rdf:type owl:Ontology} statement.
     * If there are both uri and blank ontological nodes simultaneously in the graph, then it prefers uri one.
     * If several ontological nodes of the same kind, it chooses the most bulky.
     * Note: it works with any graph, not necessarily with the base;
     * for a valid composite ontology graph a lot of ontological nodes are expected.
     *
     * @param graph {@link Graph}
     * @return {@link Optional} around the {@link Node} which could be uri or blank
     */
    public static Optional<Node> ontologyNode(Graph graph) {
        List<Node> res = getOntologyNodes(graph);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    /**
     * Finds and returns the List of nodes
     * that are the subject in the {@code _:x rdf:type owl:Ontology} statement.
     * If there are both uri and blank ontological nodes simultaneously in the graph, then it prefers uri one.
     * If several ontological nodes of the same kind, it chooses the most bulky.
     * Note: it works with any graph, not necessarily with the base;
     * for a valid composite ontology graph a lot of ontological nodes are expected.
     *
     * @param graph {@link Graph}
     * @return {@link List} of {@link Node nodes} which could be uri or blank.
     */
    public static List<Node> getOntologyNodes(Graph graph) {
        ExtendedIterator<Node> ontologyNodes = graph.find(Node.ANY, RDF.Nodes.type, OWL.Ontology.asNode())
                .mapWith(t -> {
                    Node n = t.getSubject();
                    return n.isURI() || n.isBlank() ? n : null;
                }).filterDrop(Objects::isNull);
        try {
            List<Node> res = ontologyNodes.toList();
            if (res.isEmpty()) return List.of();
            res.sort(rootNodeComparator(graph));
            return List.copyOf(res);
        } finally {
            ontologyNodes.close();
        }
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
     * In the case of composite graph, imports are listed transitively.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return unordered Set of uris from the whole graph (it may be composite)
     * @see Graphs#getOntologyIRI(Graph)
     */
    public static Set<String> getImports(Graph graph) {
        ExtendedIterator<String> imports = listImports(graph);
        try {
            return imports.toSet();
        } finally {
            imports.close();
        }
    }

    /**
     * Returns an {@code ExtendedIterator} over all URIs from the {@code _:x owl:imports _:uri} statements.
     * In the case of composite graph, imports are listed transitively.
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
     * Collects a prefixes' library from the collection of the graphs.
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
     * @throws OutOfMemoryError may occur while iterating, e.g.when the graph is huge
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
     * <p>
     * Impl note: depending on the type of the underlying graph, it may or may not be advantageous
     * to get all types at once, or ask many separate queries.
     * Heuristically, we assume that fine-grain queries to an inference graph are preferable,
     * and all-at-once for other types, including persistent stores.
     *
     * @param node  {@link Node} to test
     * @param graph {@link Graph}
     * @param types Set of {@link Node}-types
     * @return boolean
     */
    public static boolean hasOneOfType(Node node, Graph graph, Set<Node> types) {
        if (types.isEmpty()) {
            return false;
        }
        if (types.size() == 1) {
            return graph.contains(node, RDF.Nodes.type, types.iterator().next());
        }
        if (isGraphInf(graph)) {
            for (Node type : types) {
                if (graph.contains(node, RDF.Nodes.type, type)) {
                    return true;
                }
            }
            return false;
        }
        return Iterators.anyMatch(graph.find(node, RDF.Nodes.type, Node.ANY), triple -> types.contains(triple.getObject()));
    }

    /**
     * Answers {@code true}, if there is a declaration {@code node rdf:type $type},
     * where $type is from the white types list, but not from the black types list.
     * <p>
     * Impl note: depending on the type of the underlying graph, it may or may not be advantageous
     * to get all types at once, or ask many separate queries.
     * Heuristically, we assume that fine-grain queries to an inference graph are preferable,
     * and all-at-once for other types, including persistent stores.
     *
     * @param node       {@link Node} to test
     * @param graph      {@link Graph}
     * @param whiteTypes Set of {@link Node}-types
     * @param blackTypes Set of {@link Node}-types
     * @return boolean
     */
    public static boolean testTypes(Node node, Graph graph, Set<Node> whiteTypes, Set<Node> blackTypes) {
        if (isGraphInf(graph)) {
            return testTypesUsingContains(node, graph, whiteTypes, blackTypes);
        }
        Set<Node> allTypes;
        ExtendedIterator<Node> findTypes = graph.find(node, RDF.Nodes.type, Node.ANY).mapWith(Triple::getObject);
        try {
            allTypes = findTypes.toSet();
        } finally {
            findTypes.close();
        }
        boolean hasWhiteType = false;
        for (Node type : allTypes) {
            if (blackTypes.contains(type)) {
                return false;
            }
            if (whiteTypes.contains(type)) {
                hasWhiteType = true;
            }
        }
        return hasWhiteType;
    }

    public static boolean testTypesUsingContains(Node node, Graph g, Set<Node> whiteTypes, Set<Node> blackTypes) {
        boolean hasWhiteType = false;
        boolean hasBlackType = false;
        if (whiteTypes.size() > blackTypes.size()) {
            for (Node type : whiteTypes) {
                if (g.contains(node, RDF.Nodes.type, type)) {
                    hasWhiteType = true;
                    break;
                }
            }
            if (!hasWhiteType) {
                return false;
            }
            for (Node type : blackTypes) {
                if (g.contains(node, RDF.Nodes.type, type)) {
                    hasBlackType = true;
                    break;
                }
            }
            return !hasBlackType;
        } else {
            for (Node type : blackTypes) {
                if (g.contains(node, RDF.Nodes.type, type)) {
                    hasBlackType = true;
                    break;
                }
            }
            if (hasBlackType) {
                return false;
            }
            for (Node type : whiteTypes) {
                if (g.contains(node, RDF.Nodes.type, type)) {
                    hasWhiteType = true;
                    break;
                }
            }
            return hasWhiteType;
        }
    }
}
