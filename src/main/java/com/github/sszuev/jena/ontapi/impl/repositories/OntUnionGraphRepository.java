package com.github.sszuev.jena.ontapi.impl.repositories;

import com.github.sszuev.jena.ontapi.GraphRepository;
import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.InfGraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A wrapper for {@link DocumentGraphRepository} that controls imports {@link com.github.sszuev.jena.ontapi.model.OntModel} closure.
 */
public class OntUnionGraphRepository {
    private final GraphRepository repository;
    private final Function<Graph, UnionGraph> unionGraphFactory;
    private final Supplier<Graph> baseGraphFactory;
    private final boolean ignoreUnresolvedImports;

    public OntUnionGraphRepository(GraphRepository repository,
                                   Function<Graph, UnionGraph> unionGraphFactory,
                                   Supplier<Graph> baseGraphFactory,
                                   boolean ignoreUnresolvedImports) {
        this.repository = repository;
        this.unionGraphFactory = unionGraphFactory;
        this.baseGraphFactory = baseGraphFactory;
        this.ignoreUnresolvedImports = ignoreUnresolvedImports;
    }

    protected static Graph getBase(Graph graph) {
        return getRaw(graph instanceof UnionGraph ? ((UnionGraph) graph).getBaseGraph() : graph);
    }

    protected static Graph getRaw(Graph graph) {
        if (graph instanceof InfGraph) {
            return ((InfGraph) graph).getRawGraph();
        } else {
            return graph;
        }
    }

    protected static boolean graphEquals(Graph left, Graph right) {
        return left == right;
    }

    public void remap(Graph graph) {
        String newName = Graphs.findOntologyNameNode(getBase(graph)).map(Node::toString).orElse(null);
        if (newName != null && repository.contains(newName)) {
            return;
        }
        String prevName = repository.ids()
                .filter(name -> graphEquals(graph, repositoryGerOrNull(name)))
                .findFirst()
                .orElse(null);
        if (Objects.equals(newName, prevName)) {
            return;
        }
        repository.remove(prevName);
        repository.put(newName, graph);
    }

    public UnionGraph get(Node name) {
        return putGraph(repositoryGet(name), name.toString());
    }

    public UnionGraph put(Graph givenGraph) {
        return putGraph(givenGraph, null);
    }

    protected UnionGraph putGraph(Graph root, String rootGraphId) {
        Set<UnionGraph> seen = new HashSet<>();
        Deque<UnionGraph> queue = new ArrayDeque<>();

        Node ontologyName = Graphs.findOntologyNameNode(getBase(root)).orElse(null);
        if (ontologyName == null) {
            throw new OntJenaException.IllegalArgument(
                    "Unnamed graph specified" +
                            (rootGraphId != null ? ", root graph = <" + rootGraphId + ">" : "")
            );
        }
        if (rootGraphId != null && !rootGraphId.equals(ontologyName.toString())) {
            throw new OntJenaException.IllegalState(
                    "Wrong mapping. Expected <" + rootGraphId + ">, but found <" + ontologyName + ">"
            );
        }
        UnionGraph res = findOrPut(root, ontologyName);
        queue.add(res);

        while (!queue.isEmpty()) {
            UnionGraph current = queue.removeFirst();
            if (!seen.add(current)) {
                continue;
            }
            Graphs.getImports(current.getBaseGraph()).forEach(uri -> {
                Node name = NodeFactory.createURI(uri);
                Graph sub = current.subGraphs()
                        .filter(it -> Graphs.findOntologyNameNode(getBase(it)).filter(name::equals).isPresent())
                        .findFirst()
                        .orElse(null);
                UnionGraph u = findOrPut(sub, name);
                if (graphEquals(sub, u)) {
                    return;
                }
                UnionGraph.EventManager events = current.getEventManager();
                try {
                    events.off();
                    if (sub != null) {
                        current.removeSubGraph(sub);
                    }
                    current.addSubGraphIfAbsent(u);
                } finally {
                    events.on();
                }
                queue.add(u);
            });
        }
        return res;
    }

    protected UnionGraph findOrPut(Graph graph, Node ontologyName) {
        String graphId = ontologyName.toString();
        if (repository.contains(graphId)) {
            Graph found = repositoryGet(ontologyName);
            if (graph != null && !graphEquals(getBase(graph), getBase(found))) {
                throw new OntJenaException.IllegalArgument(
                        "Another graph with name <" + graphId + "> is already in the hierarchy"
                );
            }
            graph = found;
        } else if (graph == null) {
            graph = newGraph(ontologyName);
        }
        graph = getRaw(graph);
        UnionGraph union = graph instanceof UnionGraph ? (UnionGraph) graph : unionGraphFactory.apply(graph);
        attachListener(union);
        repository.put(graphId, union);
        return union;
    }

    protected Graph repositoryGet(Node name) {
        try {
            return repository.get(name.toString());
        } catch (Exception ex) {
            if (ignoreUnresolvedImports) {
                Graph res = newGraph(name);
                repository.put(name.toString(), res);
                return res;
            }
            throw ex;
        }
    }

    private Graph newGraph(Node name) {
        Graph res = baseGraphFactory.get();
        res.add(name, RDF.type.asNode(), OWL.Ontology.asNode());
        return res;
    }

    private Graph repositoryGerOrNull(String name) {
        try {
            return repository.get(name);
        } catch (Exception ex) {
            return null;
        }
    }

    protected void attachListener(UnionGraph res) {
        UnionGraph.EventManager manager = res.getEventManager();
        if (manager.listeners(OntUnionGraphListener.class).noneMatch(it -> isSameRepository(it.ontUnionGraphRepository))) {
            manager.register(new OntUnionGraphListener(this));
        }
    }

    protected boolean isSameRepository(OntUnionGraphRepository repository) {
        return this.repository == repository.repository;
    }


}
