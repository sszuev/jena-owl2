package com.github.sszuev.jena.ontapi.impl.repositories;

import com.github.sszuev.jena.ontapi.GraphRepository;
import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.InfGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class OntUnionGraphRepository {
    private final GraphRepository repository;
    private final Function<Graph, UnionGraph> factory;
    private final boolean ignoreMissingImports;

    // graph ont-name to graph
    private final Map<String, UnionGraph> graphs = new HashMap<>();

    public OntUnionGraphRepository(GraphRepository repository,
                                   Function<Graph, UnionGraph> factory,
                                   boolean ignoreMissingImports) {
        this.repository = repository;
        this.factory = factory;
        this.ignoreMissingImports = ignoreMissingImports;
    }

    public UnionGraph get(String name) {
        UnionGraph res = graphs.get(name);
        if (res != null) {
            return res;
        }
        return put(getBaseGraph(name), name);
    }

    public UnionGraph put(Graph givenGraph) {
        return put(givenGraph, null);
    }

    protected UnionGraph put(Graph givenGraph, String repositoryGraphId) {
        if (givenGraph instanceof UnionGraph && graphs.containsValue((UnionGraph) givenGraph)) {
            // already here
            return (UnionGraph) givenGraph;
        }
        Graph graph = givenGraph;
        if (givenGraph instanceof InfGraph) {
            graph = ((InfGraph) givenGraph).getRawGraph();
        }
        Graph base = Graphs.getBase(graph);
        Node ontology = Graphs.findOntologyNameNode(base).orElse(null);
        if (ontology == null) {
            throw new OntJenaException.IllegalArgument(
                    "unnamed graph specified" + (repositoryGraphId != null ? ": <" + repositoryGraphId + ">" : "")
            );
        }
        String name = ontology.toString();
        OntJenaException.checkFalse(graphs.containsKey(name),
                "Another graph with name <" + name + "> is already in the hierarchy"
        );
        OntJenaException.checkTrue(repositoryGraphId == null || repositoryGraphId.equals(name),
                "Why repositoryGraphId (<" + repositoryGraphId + ">) != ontologyName (<" + name + ">)");

        repository.put(name, base);

        UnionGraph res = graph instanceof UnionGraph ? (UnionGraph) graph : factory.apply(base);
        UnionGraph.EventManager manager = res.getEventManager();
        if (manager.listeners(OntUnionGraphListener.class)
                .noneMatch(it -> it.repository == this)) {
            manager.register(new OntUnionGraphListener(this));
        }
        Set<String> imports = Graphs.getImports(base);
        // recursively add a graph tree using listener
        imports.forEach(uri -> res.addSubGraph(Graphs.getBase(getBaseGraph(uri))));

        graphs.put(name, res);
        return res;
    }

    private Graph getBaseGraph(String uri) {
        try {
            return repository.get(uri);
        } catch (Exception ex) {
            if (ignoreMissingImports) {
                Graph g = GraphMemFactory.createDefaultGraph();
                g.add(NodeFactory.createURI(uri), RDF.type.asNode(), OWL.Ontology.asNode());
                repository.put(uri, g);
                return g;
            }
            throw ex;
        }
    }

}
