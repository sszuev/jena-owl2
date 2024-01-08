package com.github.sszuev.jena.ontapi.utils;

import com.github.sszuev.jena.ontapi.UnionGraph;
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
import org.apache.jena.util.iterator.NullIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
            return ((UnionGraph) graph).subGraphs();
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
     * @see InfGraph
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
            if (g instanceof InfGraph) {
                candidates.add(((InfGraph) g).getRawGraph());
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
     *
     * @param graph {@link Graph}
     * @return {@code Stream} of {@link Graph}s
     */
    public static Stream<Graph> dataGraphs(Graph graph) {
        if (graph == null) {
            return Stream.empty();
        }
        if (isGraphMem(graph)) {
            return Stream.of(graph);
        }
        Set<Graph> res = new LinkedHashSet<>();
        Deque<Graph> queue = new ArrayDeque<>();
        Set<Graph> seen = new HashSet<>();
        queue.add(graph);
        while (!queue.isEmpty()) {
            Graph g = queue.removeFirst();
            if (!seen.add(g)) {
                continue;
            }
            Graph bg = getBase(g);
            res.add(bg);
            directSubGraphs(g).forEach(queue::add);
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
            return u.isDistinct() || !u.hasSubGraph() && isDistinct(getBase(u));
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
     * Creates an ontology {@link UnionGraph} from the specified {@code graph} of arbitrary nature.
     * The method can be used, for example,
     * to transform the legacy {@link org.apache.jena.graph.compose.MultiUnion MultiUnion} Graph to {@link UnionGraph}.
     *
     * @param graph       {@link Graph}
     * @param wrapAsUnion {@link Function} to produce new instance {@link UnionGraph} from {@link Graph}
     * @return {@link UnionGraph}
     */
    public static UnionGraph makeOntUnionFrom(Graph graph, Function<Graph, UnionGraph> wrapAsUnion) {
        if (graph instanceof UnionGraph) {
            return (UnionGraph) graph;
        }
        if (isGraphMem(graph)) {
            return wrapAsUnion.apply(graph);
        }
        return makeOntUnion(getBase(graph), dataGraphs(graph).collect(Collectors.toSet()), wrapAsUnion);
    }

    /**
     * Assembles the hierarchical ontology {@link UnionGraph Union Graph} from the specified components
     * in accordance with their {@code owl:imports} and {@code owl:Ontology} declarations.
     * Irrelevant graphs are ignored.
     *
     * @param graph       {@link Graph}
     * @param repository  a {@code Collection} of {@link Graph graph}s to search in for missed dependencies
     * @param wrapAsUnion {@link Function} to produce new instance {@link UnionGraph} from {@link Graph}
     * @return {@link UnionGraph}
     */
    public static UnionGraph makeOntUnion(Graph graph,
                                          Collection<Graph> repository,
                                          Function<Graph, UnionGraph> wrapAsUnion) {
        Deque<Graph> graphs = new ArrayDeque<>();
        graphs.add(graph);
        Map<String, UnionGraph> res = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        while (!graphs.isEmpty()) {
            Graph next = graphs.removeFirst();
            Node ontology = findOntologyNameNode(next).orElse(null);
            if (ontology == null) {
                continue;
            }
            String name = ontology.toString();
            if (name == null || !seen.add(name)) {
                continue;
            }
            Set<String> imports = Iterators.addAll(listImports(ontology, next), new HashSet<>());
            if (imports.isEmpty()) {
                continue;
            }
            UnionGraph parent = res.computeIfAbsent(name, s -> wrapAsUnion.apply(next));
            repository.stream().filter(it -> !imports.isEmpty()).forEach(candidate -> {
                String candidateIri = findOntologyNameNode(candidate)
                        .filter(Node::isURI)
                        .map(Node::getURI)
                        .orElse(null);
                if (imports.contains(candidateIri)) {
                    UnionGraph child = res.computeIfAbsent(candidateIri, s -> wrapAsUnion.apply(candidate));
                    parent.addSubGraph(child);
                    graphs.add(child);
                    imports.remove(candidateIri);
                }
            });
        }
        return res.isEmpty() ? wrapAsUnion.apply(graph) : res.values().iterator().next();
    }

    /**
     * Lists all graphs in the tree which is specified as {@code UnionGraph}.
     *
     * @param graph {@link UnionGraph}
     * @return {@code Stream} of {@link UnionGraph}s
     * @see UnionGraph#superGraphs()
     * @see UnionGraph#subGraphs()
     */
    public static Stream<UnionGraph> flatTree(UnionGraph graph) {
        Objects.requireNonNull(graph);
        Set<UnionGraph> res = new LinkedHashSet<>();
        Deque<UnionGraph> queue = new ArrayDeque<>();
        queue.add(graph);
        while (!queue.isEmpty()) {
            UnionGraph next = queue.removeFirst();
            if (res.add(next)) {
                next.subGraphs().filter(it -> it instanceof UnionGraph).map(it -> (UnionGraph) it).forEach(queue::add);
                next.superGraphs().forEach(queue::add);
            }
        }
        return res.stream();
    }

    /**
     * Checks whether the specified graph is ontological, that is, has an OWL header.
     * This method does not check the graph for validity; it may still be misconfigured (if there are several headers).
     *
     * @param graph {@link Graph}
     * @return boolean
     */
    public static boolean isOntGraph(Graph graph) {
        return graph.contains(Node.ANY, RDF.type.asNode(), OWL.Ontology.asNode());
    }

    /**
     * Checks whether the specified graph is ontological, that is,
     * has a hierarchy synchronized with the {@code owl:imports} & {@code owl:Ontology} relationships.
     *
     * @param graph {@link UnionGraph}
     * @return boolean
     */
    public static boolean isOntUnionGraph(UnionGraph graph) {
        Node id = findOntologyNameNode(graph.getBaseGraph()).orElse(null);
        if (id == null) {
            return false;
        }
        Set<UnionGraph> res = new LinkedHashSet<>();
        Map<Node, UnionGraph> queue = new LinkedHashMap<>();
        queue.put(id, graph);
        Set<Node> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            Node nextId = queue.keySet().iterator().next();
            UnionGraph nextGraph = queue.remove(nextId);
            if (!seen.add(nextId)) {
                continue;
            }
            Set<String> nextImports = getImports(nextGraph.getBaseGraph());
            Iterator<UnionGraph> children = nextGraph.subGraphs()
                    .filter(it -> it instanceof UnionGraph)
                    .map(it -> (UnionGraph) it)
                    .iterator();
            while (children.hasNext()) {
                UnionGraph g = children.next();
                Node gid = findOntologyNameNode(g.getBaseGraph()).orElse(null);
                if (gid == null || !gid.isURI() || !nextImports.contains(gid.getURI())) {
                    return false;
                }
                queue.put(gid, g);
            }
        }
        return true;
    }

    /**
     * Creates a new ontology header ({@code uriOrBlank rdf:type owl:Ontology}) if it is not present in the graph.
     * According to the OWL specification,
     * each non-composite ontology graph must contain one and only one ontology header.
     * If a well-formed header already exists, the method returns it unchanged.
     * If there are multiple other headers,
     * the graph is considered misconfigured, and
     * any extra headers will be removed,
     * and the content will be moved to a new generated anonymous header.
     *
     * @param graph {@link Graph}
     * @param uri   ontology header IRI,
     *              if {@code null} then the method returns either existing anonymous header
     *              or generates new anonymous (blank) header
     * @return existing or new header
     */
    public static Node createOntologyHeaderNode(Graph graph, String uri) {
        Node header = ontologyNode(graph).orElse(null);
        if (header != null) {
            if (uri != null && header.isURI() && header.getURI().equals(uri)) {
                return header;
            }
            if (uri == null && header.isBlank()) {
                return header;
            }
        }
        List<Triple> prev = Iterators.addAll(Iterators.flatMap(
                graph.find(Node.ANY, RDF.type.asNode(), OWL.Ontology.asNode()),
                it -> graph.find(it.getSubject(), Node.ANY, Node.ANY)), new ArrayList<>());

        prev.forEach(graph::delete);
        Node newOntology = createNode(uri);
        graph.add(newOntology, RDF.type.asNode(), OWL.Ontology.asNode());
        prev.forEach(triple -> graph.add(newOntology, triple.getPredicate(), triple.getObject()));
        return newOntology;
    }

    /**
     * Returns OWL Ontology ID
     * (either object in {@code any owl:versionIRI <uri>} statement or subject in {@code <uri> rdf:type owl:Ontology} statement).
     *
     * @param graph {@link Graph}
     * @return {@code Optional} with {@link Node} blank or URI,
     * or {@code Optional#empty} if the ontology Node cannot be uniquely defined or absent in the {@code graph}
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Ontology_Documents'>3.2 Ontology Documents</a>
     */
    public static Optional<Node> findOntologyNameNode(Graph graph) {
        if (graph.isClosed()) {
            throw new IllegalArgumentException("Graph is closed");
        }
        Node ontologyIri = ontologyNode(graph).orElse(null);
        if (ontologyIri == null) {
            return Optional.empty();
        }
        Set<Node> versionNodes = Iterators.takeAsSet(
                graph.find(ontologyIri, OWL.versionIRI.asNode(), Node.ANY)
                        .mapWith(Triple::getObject)
                        .filterKeep(Node::isURI), 2);
        if (versionNodes.size() == 1) {
            return Optional.of(versionNodes.iterator().next());
        }
        return Optional.of(ontologyIri);
    }

    /**
     * Returns the primary Ontology Node (the subject in the {@code _:x rdf:type owl:Ontology} statement)
     * within the given graph if it can be uniquely determined.
     * Note: it works with any graph, not necessarily with the base;
     * for a valid composite ontology graph a lot of ontological nodes are expected.
     *
     * @param graph {@link Graph}
     * @return {@link Optional} around the {@link Node} which could be uri or blank
     */
    public static Optional<Node> ontologyNode(Graph graph) {
        ExtendedIterator<Node> ontologyNodes = listOntologyNodes(graph);
        Set<Node> ontologyNodesSet = Iterators.takeAsSet(ontologyNodes, 2);
        if (ontologyNodesSet.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(ontologyNodesSet.iterator().next());
    }

    /**
     * Lists all subjects from {@code uriOrBlankNode rdf:type owl:Ontology} statements.
     *
     * @param graph {@code Graph}
     * @return {@link ExtendedIterator} of {@link Node}
     */
    public static ExtendedIterator<Node> listOntologyNodes(Graph graph) {
        return graph.find(Node.ANY, RDF.Nodes.type, OWL.Ontology.asNode())
                .mapWith(t -> {
                    Node n = t.getSubject();
                    return n.isURI() || n.isBlank() ? n : null;
                }).filterDrop(Objects::isNull);
    }

    /**
     * Returns all uri-objects from the {@code _:x owl:imports _:uri} statements.
     * In the case of composite graph, imports are listed transitively.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return unordered Set of uris from the whole graph (it may be composite)
     */
    public static Set<String> getImports(Graph graph) {
        return Set.copyOf(Iterators.addAll(listImports(graph), new HashSet<>()));
    }

    /**
     * Answers {@code true} if the given uri is present in the import closure.
     *
     * @param graph {@link Graph}, not {@code null}
     * @param uri   to test
     * @return boolean
     */
    public static boolean hasImports(Graph graph, String uri) {
        Objects.requireNonNull(uri);
        return Iterators.findFirst(listImports(graph).filterKeep(uri::equals)).isPresent();
    }

    /**
     * Returns an {@code ExtendedIterator} over all URIs from the {@code _:x owl:imports _:uri} statements.
     * In the case of composite graph, imports are listed transitively.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return {@link ExtendedIterator} of {@code String}-URIs
     */
    public static ExtendedIterator<String> listImports(Graph graph) {
        Node ontology = ontologyNode(Objects.requireNonNull(graph)).orElse(null);
        if (ontology == null) {
            return NullIterator.instance();
        }
        return listImports(ontology, graph);
    }

    private static ExtendedIterator<String> listImports(Node ontology, Graph graph) {
        return graph.find(ontology, OWL.imports.asNode(), Node.ANY).mapWith(t -> {
            Node n = t.getObject();
            return n.isURI() ? n.getURI() : null;
        }).filterDrop(Objects::isNull);
    }

    /**
     * Lists all triples which related to ontology header somehow.
     *
     * @param graph {@link Graph}
     * @return {@link ExtendedIterator} of {@link Triple}s
     */
    public static ExtendedIterator<Triple> listOntHeaderTriples(Graph graph) {
        return Iterators.concat(
                graph.find(Node.ANY, RDF.type.asNode(), OWL.Ontology.asNode()),
                graph.find(Node.ANY, OWL.imports.asNode(), Node.ANY),
                graph.find(Node.ANY, OWL.versionIRI.asNode(), Node.ANY)
        );
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
