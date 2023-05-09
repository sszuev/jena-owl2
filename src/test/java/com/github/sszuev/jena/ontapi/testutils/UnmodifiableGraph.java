package com.github.sszuev.jena.ontapi.testutils;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;

import java.util.Objects;

/**
 * Created by @ssz on 21.10.2018.
 */
public class UnmodifiableGraph extends org.apache.jena.sparql.graph.UnmodifiableGraph {

    private static final Capabilities READ_ONLY_CAPABILITIES = new Capabilities() {
        @Override
        public boolean sizeAccurate() {
            return true;
        }

        @Override
        public boolean addAllowed() {
            return false;
        }

        @Override
        public boolean deleteAllowed() {
            return false;
        }

        @Override
        public boolean handlesLiteralTyping() {
            return true;
        }
    };

    private final PrefixMapping pm;

    public UnmodifiableGraph(Graph base) {
        super(Objects.requireNonNull(base));
        pm = base.getPrefixMapping();
    }

    @Override
    public void add(Triple t) {
        throw new AddDeniedException("Read only graph: can't add triple " + t);
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        GraphUtil.remove(this, s, p, o);
    }

    @Override
    public void delete(Triple t) {
        throw new DeleteDeniedException("Read only graph: can't delete triple " + t);
    }

    @Override
    public void clear() {
        throw new AccessDeniedException("Read only graph: can't clear");
    }

    @Override
    public Capabilities getCapabilities() {
        return READ_ONLY_CAPABILITIES;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return PrefixMapping.Factory.create().setNsPrefixes(pm).lock();
    }

}
