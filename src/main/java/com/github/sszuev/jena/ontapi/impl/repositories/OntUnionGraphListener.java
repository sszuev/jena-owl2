package com.github.sszuev.jena.ontapi.impl.repositories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.impl.GraphListenerBase;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OntUnionGraphListener extends GraphListenerBase implements UnionGraph.EventManager {

    private final List<GraphListener> inactive = new ArrayList<>();
    final OntUnionGraphRepository repository;

    protected OntUnionGraphListener(OntUnionGraphRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void off() {
        inactive.addAll(listeners);
        listeners.clear();
    }

    @Override
    public void on() {
        listeners.addAll(inactive);
        inactive.clear();
    }

    @Override
    public void onAddSubGraph(UnionGraph graph, Graph subGraph) {
        if (Graphs.isOntGraph(Graphs.getBase(subGraph))) {
            Node ontology = Graphs.findOntologyNameNode(Graphs.getBase(subGraph))
                    .orElseThrow(() -> new OntJenaException.IllegalArgument("Unnamed or misconfigured graph is specified"));
            if (!ontology.isURI()) {
                throw new OntJenaException.IllegalArgument("Anonymous graph specified");
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.onAddSubGraph(graph, subGraph));
    }

    @Override
    public void notifySubGraphAdded(UnionGraph graph, Graph subGraph) {
        if (Graphs.isOntGraph(Graphs.getBase(subGraph))) {
            UnionGraph ontSubGraph = repository.put(subGraph);
            if (subGraph != ontSubGraph) {
                Graph justAddedGraph = graph.subGraphs().filter(it -> it == subGraph).findFirst()
                        .orElseThrow(() -> new IllegalStateException("Where is just added graph?"));
                // addSubGraph is a recursive method, so off listening
                try {
                    graph.getEventManager().off();
                    graph.removeSubGraph(justAddedGraph).addSubGraph(ontSubGraph);
                } finally {
                    graph.getEventManager().on();
                }
            }
            Graph ontSubGraphBase = ontSubGraph.getBaseGraph();
            Node ontSubGraphIri = Graphs.findOntologyNameNode(ontSubGraphBase)
                    .filter(Node::isURI)
                    .orElseThrow(() -> new IllegalStateException("Expected to be named"));
            Graph thisOntBaseGraph = graph.getBaseGraph();
            thisOntBaseGraph.add(
                    Graphs.getOrCreateOntologyName(thisOntBaseGraph, null),
                    OWL.imports.asNode(),
                    ontSubGraphIri
            );
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySubGraphAdded(graph, subGraph));
    }

    @Override
    public void onRemoveSubGraph(UnionGraph graph, Graph subGraph) {
        listeners(UnionGraph.Listener.class).forEach(it -> it.onRemoveSubGraph(graph, graph));
    }

    @Override
    public void notifySubGraphRemoved(UnionGraph graph, Graph subGraph) {
        if (subGraph instanceof UnionGraph && Graphs.isOntGraph(((UnionGraph) subGraph).getBaseGraph())) {
            Graph ontSubGraphBase = ((UnionGraph) subGraph).getBaseGraph();
            Node ontSubGraphIri = Graphs.findOntologyNameNode(ontSubGraphBase)
                    .filter(Node::isURI).orElse(null);
            if (ontSubGraphIri != null) {
                Graph thisOntBaseGraph = graph.getBaseGraph();
                thisOntBaseGraph.delete(
                        Graphs.getOrCreateOntologyName(thisOntBaseGraph, null),
                        OWL.imports.asNode(),
                        ontSubGraphIri
                );
                List<Graph> toDetach = graph.subGraphs()
                        .filter(it -> it instanceof UnionGraph)
                        .filter(
                                it -> Graphs.findOntologyNameNode(((UnionGraph) it).getBaseGraph())
                                        .filter(Node::isURI)
                                        .filter(ontSubGraphIri::equals)
                                        .isPresent()
                        )
                        .collect(Collectors.toList());
                try {
                    graph.getEventManager().off();
                    toDetach.forEach(graph::removeSubGraph);
                } finally {
                    graph.getEventManager().on();
                }
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySubGraphRemoved(graph, graph));
    }


    @Override
    protected void addTripleEvent(Graph g, Triple t) {
        if (t.getObject().isURI() && OWL.imports.asNode().equals(t.getPredicate())) {
            UnionGraph thisGraph = (UnionGraph) g;
            Node subject = Graphs.findOntologyNameNode(thisGraph.getBaseGraph()).orElse(null);
            if (!t.getSubject().equals(subject)) {
                return;
            }
            try {
                UnionGraph add = repository.get(t.getObject().getURI());
                thisGraph.addSubGraphIfAbsent(add);
            } catch (Exception ex) {
                // rollback the addition of an import statement
                thisGraph.getBaseGraph().delete(t);
                throw ex;
            }
        }
    }

    @Override
    protected void deleteTripleEvent(Graph g, Triple t) {
        if (t.getObject().isURI() && OWL.imports.asNode().equals(t.getPredicate())) {
            UnionGraph thisGraph = (UnionGraph) g;
            Node subject = Graphs.findOntologyNameNode(thisGraph.getBaseGraph()).orElse(null);
            if (!t.getSubject().equals(subject)) {
                return;
            }
            UnionGraph toRemove = repository.get(t.getObject().getURI());
            thisGraph.removeSubGraph(toRemove);
        }
    }

    @Override
    public void notifyAddGraph(Graph g, Graph other) {
        // TODO:
        super.notifyAddGraph(g, other);
    }

    @Override
    public void notifyDeleteGraph(Graph g, Graph other) {
        // TODO:
        super.notifyDeleteGraph(g, other);
    }

    @Override
    public void notifyEvent(Graph source, Object event) {
        // TODO:
        super.notifyEvent(source, event);
    }

}
