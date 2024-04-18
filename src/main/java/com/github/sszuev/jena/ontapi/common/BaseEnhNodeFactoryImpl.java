package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

/**
 * An extended {@link Implementation} factory,
 * the base class for any {@link EnhNodeFactory factories} to produce
 * {@link OntObject Ontology Object}s.
 * Used to bind implementation (node) and interface.
 * Also, in addition to the standard jena methods,
 * this implementation includes nodes' search and graph transformation functionality.
 */
public abstract class BaseEnhNodeFactoryImpl extends Implementation implements EnhNodeFactory {

    protected static EnhNode safeWrap(Node n, EnhGraph g, Iterable<EnhNodeFactory> factories) {
        for (EnhNodeFactory f : factories) {
            EnhNode r = safeWrap(n, g, f);
            if (r != null) return r;
        }
        return null;
    }

    protected static EnhNode safeWrap(Node n, EnhGraph g, EnhNodeFactory f) {
        try {
            return f.wrap(n, g);
        } catch (OntJenaException.Conversion c) {
            return null;
        }
    }

    protected static boolean canWrap(Node node, EnhGraph eg, EnhNodeFactory... factories) {
        for (EnhNodeFactory f : factories) {
            if (f.canWrap(node, eg)) return true;
        }
        return false;
    }

    protected static boolean canWrap(Node node, EnhGraph eg, Iterable<EnhNodeFactory> factories) {
        for (EnhNodeFactory f : factories) {
            if (f.canWrap(node, eg)) return true;
        }
        return false;
    }

    protected static EnhNode wrap(Node node, EnhGraph eg, OntJenaException.Conversion ex, EnhNodeFactory... factories) {
        for (EnhNodeFactory f : factories) {
            try {
                return f.wrap(node, eg);
            } catch (OntJenaException.Conversion c) {
                ex.addSuppressed(c);
            }
        }
        throw ex;
    }

    protected static EnhNode wrap(Node node, EnhGraph eg, OntJenaException.Conversion ex, Iterable<EnhNodeFactory> factories) {
        for (EnhNodeFactory f : factories) {
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
