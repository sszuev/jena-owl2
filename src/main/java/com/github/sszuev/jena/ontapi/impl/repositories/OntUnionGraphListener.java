package com.github.sszuev.jena.ontapi.impl.repositories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.impl.GraphListenerBase;
import com.github.sszuev.jena.ontapi.impl.OntModelEvents;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OntUnionGraphListener extends GraphListenerBase implements UnionGraph.EventManager {

    private final List<GraphListener> inactive = new ArrayList<>();
    final OntUnionGraphRepository ontUnionGraphRepository;

    protected OntUnionGraphListener(OntUnionGraphRepository ontUnionGraphRepository) {
        this.ontUnionGraphRepository = Objects.requireNonNull(ontUnionGraphRepository);
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
    public void notifySubGraphAdded(UnionGraph thisGraph, Graph subGraph) {
        if (Graphs.isOntGraph(Graphs.getBase(subGraph))) {
            Graph ontSubGraphBase = OntUnionGraphRepository.getBase(subGraph);
            Node ontSubGraphIri = Graphs.findOntologyNameNode(ontSubGraphBase)
                    .filter(Node::isURI)
                    .orElseThrow(() -> new IllegalStateException("Expected to be named"));
            Graph thisOntBaseGraph = thisGraph.getBaseGraph();
            Node ontology = Graphs.ontologyNode(thisOntBaseGraph)
                    .orElseGet(() -> Graphs.createOntologyHeaderNode(thisOntBaseGraph, null));
            thisOntBaseGraph.add(ontology, OWL.imports.asNode(), ontSubGraphIri);

            UnionGraph ontSubGraph = ontUnionGraphRepository.put(subGraph);
            if (subGraph != ontSubGraph) {
                Graph justAddedGraph = thisGraph.subGraphs()
                        .filter(it -> OntUnionGraphRepository.graphEquals(it, subGraph))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Where is just added graph?"));
                // addSubGraph is a recursive method, so off listening
                try {
                    thisGraph.getEventManager().off();
                    thisGraph.removeSubGraph(justAddedGraph).addSubGraph(ontSubGraph);
                } finally {
                    thisGraph.getEventManager().on();
                }
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySubGraphAdded(thisGraph, subGraph));
    }

    @Override
    public void notifySuperGraphAdded(UnionGraph graph, UnionGraph superGraph) {
        Node superGraphOntology = Graphs.ontologyNode(superGraph.getBaseGraph()).orElse(null);
        if (superGraphOntology != null) {
            Node graphName = Graphs.findOntologyNameNode(graph.getBaseGraph()).filter(Node::isURI).orElse(null);
            if (graphName != null) {
                superGraph.getBaseGraph().add(superGraphOntology, OWL.imports.asNode(), graphName);
                ontUnionGraphRepository.put(superGraph);
            }
        }
        listeners(UnionGraph.Listener.class).forEach(it -> it.notifySuperGraphAdded(graph, superGraph));
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
                Node ontology = Graphs.ontologyNode(thisOntBaseGraph)
                        .orElseGet(() -> Graphs.createOntologyHeaderNode(thisOntBaseGraph, null));
                thisOntBaseGraph.delete(ontology, OWL.imports.asNode(), ontSubGraphIri);
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
        UnionGraph thisGraph = (UnionGraph) g;
        if (isNameTriple(t)) {
            ontUnionGraphRepository.remap(thisGraph);
        } else if (isImportTriple(t, thisGraph.getBaseGraph())) {
            UnionGraph.EventManager manager = thisGraph.getEventManager();
            try {
                manager.off();
                UnionGraph add = ontUnionGraphRepository.get(t.getObject());
                thisGraph.addSubGraphIfAbsent(add);
            } catch (Exception ex) {
                // rollback the addition of an import statement
                thisGraph.getBaseGraph().delete(t);
                throw ex;
            } finally {
                manager.on();
            }
        }
    }

    @Override
    protected void deleteTripleEvent(Graph g, Triple t) {
        UnionGraph thisGraph = (UnionGraph) g;
        if (isNameTriple(t)) {
            ontUnionGraphRepository.remap(thisGraph);
        } else if (isImportTriple(t, thisGraph.getBaseGraph())) {
            UnionGraph.EventManager manager = thisGraph.getEventManager();
            try {
                manager.off();
                UnionGraph toRemove = ontUnionGraphRepository.get(t.getObject());
                thisGraph.removeSubGraph(toRemove);
            } finally {
                manager.on();
            }
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
        if (OntModelEvents.NOTIFY_ONT_ID_CHANGED.equals(event)) {
            ontUnionGraphRepository.remap(source);
        }
        // TODO:
        super.notifyEvent(source, event);
    }

    private boolean isNameTriple(Triple t) {
        return t.getPredicate().equals(RDF.type.asNode()) && t.getObject().equals(OWL.Ontology.asNode()) ||
                t.getPredicate().equals(OWL.versionIRI.asNode()) && t.getObject().isURI();
    }

    private boolean isImportTriple(Triple t, Graph g) {
        if (!t.getObject().isURI() || !OWL.imports.asNode().equals(t.getPredicate())) {
            return false;
        }
        Node subject = Graphs.ontologyNode(g).orElse(null);
        return t.getSubject().equals(subject);
    }
}
