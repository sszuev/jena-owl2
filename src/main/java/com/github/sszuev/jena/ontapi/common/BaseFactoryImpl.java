package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

/**
 * An extended {@link Implementation} factory,
 * the base class for any {@link ObjectFactory factories} to produce
 * {@link OntObject Ontology Object}s.
 * Used to bind implementation (node) and interface.
 * Also, in addition to the standard jena methods,
 * this implementation includes nodes search and graph transformation functionality.
 * <p>
 * Created by @ssz on 03.11.2016.
 */
public abstract class BaseFactoryImpl extends Implementation implements ObjectFactory {

    protected static EnhNode safeWrap(Node n, EnhGraph g, Iterable<ObjectFactory> factories) {
        for (ObjectFactory f : factories) {
            EnhNode r = safeWrap(n, g, f);
            if (r != null) return r;
        }
        return null;
    }

    protected static EnhNode safeWrap(Node n, EnhGraph g, ObjectFactory f) {
        try {
            return f.wrap(n, g);
        } catch (OntJenaException.Conversion c) {
            return null;
        }
    }

    protected static boolean canWrap(Node node, EnhGraph eg, ObjectFactory... factories) {
        for (ObjectFactory f : factories) {
            if (f.canWrap(node, eg)) return true;
        }
        return false;
    }

    protected static boolean canWrap(Node node, EnhGraph eg, Iterable<ObjectFactory> factories) {
        for (ObjectFactory f : factories) {
            if (f.canWrap(node, eg)) return true;
        }
        return false;
    }

    protected static EnhNode wrap(Node node, EnhGraph eg, OntJenaException.Conversion ex, ObjectFactory... factories) {
        for (ObjectFactory f : factories) {
            try {
                return f.wrap(node, eg);
            } catch (OntJenaException.Conversion c) {
                ex.addSuppressed(c);
            }
        }
        throw ex;
    }

    protected static EnhNode wrap(Node node, EnhGraph eg, OntJenaException.Conversion ex, Iterable<ObjectFactory> factories) {
        for (ObjectFactory f : factories) {
            try {
                return f.wrap(node, eg);
            } catch (OntJenaException.Conversion c) {
                ex.addSuppressed(c);
            }
        }
        throw ex;
    }

    /**
     * Creates a new {@link EnhNode} wrapping the given {@link Node} node in the context of the graph {@link EnhGraph}.
     *
     * @param node the node to be wrapped
     * @param eg   the graph containing the node
     * @return A new enhanced node which wraps node but presents the interface(s) that this factory encapsulates.
     * @throws OntJenaException.Conversion in case wrapping is impossible
     */
    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        if (!canWrap(node, eg))
            throw new OntJenaException.Conversion("Can't wrap node " + node + ". Use direct factory.");
        return createInstance(node, eg);
    }
}
