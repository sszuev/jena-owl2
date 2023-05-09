package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Objects;

/**
 * Default implementation of {@link ObjectFactory}.
 * This is a kind of constructor that consists of three modules:
 * <ul>
 * <li>{@link OntMaker} for initialization and physical creation a node {@link EnhNode} in the graph {@link EnhGraph}.</li>
 * <li>{@link OntFilter} to test the presence of a node in the graph.</li>
 * <li>{@link OntFinder} to search for nodes in the graph.</li>
 * </ul>
 * <p>
 * Created @ssz on 07.11.2016.
 */
public class CommonFactoryImpl extends BaseFactoryImpl {
    private final OntMaker maker;
    private final OntFinder finder;
    private final OntFilter filter;

    public CommonFactoryImpl(OntMaker maker, OntFinder finder, OntFilter filter) {
        this.maker = Objects.requireNonNull(maker, "Null maker.");
        this.finder = Objects.requireNonNull(finder, "Null finder.");
        this.filter = Objects.requireNonNull(filter, "Null filter.");
    }

    public OntMaker getMaker() {
        return maker;
    }

    public OntFinder getFinder() {
        return finder;
    }

    public OntFilter getFilter() {
        return filter;
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        if (!canWrap(node, eg))
            throw new OntJenaException.Conversion(String.format("Can't wrap node %s to %s", node, maker.getImpl()));
        return createInstance(node, eg);
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return filter.test(node, eg);
    }

    @Override
    public EnhNode createInGraph(Node node, EnhGraph eg) {
        if (!canCreateInGraph(node, eg))
            throw new OntJenaException.Creation(String.format("Can't modify graph for %s (%s)", node, maker.getImpl()));
        maker.make(node, eg);
        return createInstance(node, eg);
    }

    @Override
    public boolean canCreateInGraph(Node node, EnhGraph eg) {
        return maker.getTester().test(node, eg);
    }

    @Override
    public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
        return finder.restrict(filter).iterator(eg).mapWith(n -> maker.instance(n, eg));
    }

    @Override
    public EnhNode createInstance(Node node, EnhGraph eg) {
        return maker.instance(node, eg);
    }
}
