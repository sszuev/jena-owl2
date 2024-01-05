package com.github.sszuev.jena.ontapi.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.SimpleEventManager;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base implementation of {@link GraphListener}.
 */
public abstract class GraphListenerBase extends SimpleEventManager {

    protected abstract void addTripleEvent(Graph g, Triple t);

    protected abstract void deleteTripleEvent(Graph g, Triple t);

    public Stream<GraphListener> listeners() {
        return listeners.stream();
    }

    @Override
    public void notifyAddTriple(Graph g, Triple ts) {
        addTripleEvent(g, ts);
        super.notifyAddTriple(g, ts);
    }

    @Override
    public void notifyAddArray(Graph g, Triple[] ts) {
        for (Triple t : ts) {
            addTripleEvent(g, t);
        }
        super.notifyAddArray(g, ts);
    }

    @Override
    public void notifyAddList(Graph g, List<Triple> ts) {
        ts.forEach(t -> addTripleEvent(g, t));
        super.notifyAddList(g, ts);
    }

    @Override
    public void notifyAddIterator(Graph g, List<Triple> ts) {
        ts.forEach(t -> addTripleEvent(g, t));
        super.notifyAddIterator(g, ts);
    }

    @Override
    public void notifyAddIterator(Graph g, Iterator<Triple> ts) {
        ts.forEachRemaining(t -> addTripleEvent(g, t));
        super.notifyAddIterator(g, ts);
    }

    @Override
    public void notifyDeleteTriple(Graph g, Triple t) {
        deleteTripleEvent(g, t);
        super.notifyDeleteTriple(g, t);
    }

    @Override
    public void notifyDeleteArray(Graph g, Triple[] ts) {
        for (Triple t : ts) {
            deleteTripleEvent(g, t);
        }
        super.notifyDeleteArray(g, ts);
    }

    @Override
    public void notifyDeleteList(Graph g, List<Triple> ts) {
        ts.forEach(t -> deleteTripleEvent(g, t));
        super.notifyDeleteList(g, ts);
    }

    @Override
    public void notifyDeleteIterator(Graph g, List<Triple> ts) {
        ts.forEach(t -> deleteTripleEvent(g, t));
        super.notifyDeleteIterator(g, ts);
    }

    @Override
    public void notifyDeleteIterator(Graph g, Iterator<Triple> ts) {
        ts.forEachRemaining(t -> deleteTripleEvent(g, t));
        super.notifyDeleteIterator(g, ts);
    }

}
