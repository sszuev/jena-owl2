package com.github.sszuev.jena.ontapi.impl;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.shared.JenaException;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link Model model} implementation which encapsulates {@link UnionGraph union graph}.
 * <p>
 * Created by @ssz on 12.04.2018.
 */
@SuppressWarnings("WeakerAccess")
public class UnionModel extends ModelCom {

    // to control graph recursion while casting a node to an RDF view
    private final ThreadLocal<Set<Node>> visited = ThreadLocal.withInitial(HashSet::new);

    public UnionModel(Graph base, Personality<RDFNode> personality) {
        super(asUnionGraph(base), personality);
    }

    /**
     * Creates an {@link UnionGraph} instance if it is needed.
     *
     * @param graph {@link Graph} to wrap or return as is
     * @return {@link UnionGraph} (fresh or given)
     */
    public static UnionGraph asUnionGraph(Graph graph) {
        return graph instanceof UnionGraph ? (UnionGraph) graph : new UnionGraph(graph);
    }

    @Override
    public UnionGraph getGraph() {
        return (UnionGraph) super.getGraph();
    }

    public Graph getBaseGraph() {
        return getGraph().getBaseGraph();
    }

    public Model getBaseModel() {
        return new ModelCom(getBaseGraph());
    }

    @Override
    public UnionModel write(Writer writer) {
        getBaseModel().write(writer);
        return this;
    }

    @Override
    public UnionModel write(Writer writer, String lang) {
        getBaseModel().write(writer, lang);
        return this;
    }

    @Override
    public UnionModel write(Writer writer, String lang, String base) {
        getBaseModel().write(writer, lang, base);
        return this;
    }

    @Override
    public UnionModel write(OutputStream out) {
        getBaseModel().write(out);
        return this;
    }

    @Override
    public UnionModel write(OutputStream out, String lang) {
        getBaseModel().write(out, lang);
        return this;
    }

    @Override
    public UnionModel write(OutputStream out, String lang, String base) {
        getBaseModel().write(out, lang, base);
        return this;
    }

    /**
     * Answers {@code true} if the given statement belongs to the base graph.
     *
     * @param s {@link Statement}, not {@code null}
     * @return boolean
     */
    public boolean isLocal(Statement s) {
        return isLocal(OntJenaException.notNull(s, "Null statement.").getSubject(), s.getPredicate(), s.getObject());
    }

    /**
     * Answers {@code true} if the given SPO belongs to the base graph.
     *
     * @param s {@link Resource}, not {@code null}
     * @param p {@link Property}, not {@code null}
     * @param o {@link RDFNode}, not {@code null}
     * @return boolean
     */
    public boolean isLocal(Resource s, Property p, RDFNode o) {
        return getBaseGraph().contains(s.asNode(), p.asNode(), o.asNode());
    }

    /**
     * Returns a {@link RDFNode} for the given type and, if the result is present, caches it node at the model level.
     * The method works silently: normally no exception is expected.
     *
     * @param node {@link Node}
     * @param type {@link Class}-type
     * @param <N>  any subtype of {@link RDFNode}
     * @return {@link RDFNode} or {@code null}
     * @throws RuntimeException unexpected misconfiguration (RDF recursion, wrong input, personality mismatch, etc)
     * @see #getNodeAs(Node, Class)
     */
    public <N extends RDFNode> N findNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAs(node, type);
        } catch (OntJenaException.Conversion ignore) {
            // ignore
            return null;
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node, cannot be {@code null}
     * @throws OntJenaException unable to construct new RDF view for whatever reasons
     * @throws RuntimeException unexpected misconfiguration (wrong inputs, personality mismatch)
     */
    @Override
    public <N extends RDFNode> N getNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAsInternal(node, type);
        } catch (OntJenaException e) {
            throw e;
        } catch (JenaException e) {
            throw new OntJenaException.Conversion(String.format("Failed to convert node <%s> to <%s>",
                    node, OntObjectImpl.viewAsString(type)), e);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type,
     * taking into account possible graph recursions.
     * For internal usage only.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node or {@code null} if no match found
     * @throws OntJenaException.Recursion if a graph recursion is indicated
     * @throws RuntimeException           unexpected misconfiguration
     * @see #getNodeAs(Node, Class)
     */
    public <N extends RDFNode> N fetchNodeAs(Node node, Class<N> type) {
        // If node has already been seen up the stack, then a graph recursion is detected.
        // Although, in general case, using Map<Class, Set<Node>> seems to be more suitable and careful checking,
        // but it is also a little more expensive.
        // It seems, in the case of ONT-API ObjectFactory implementations, a Set is quite enough.
        Set<Node> nodes = visited.get();
        try {
            if (nodes.add(node)) {
                return getNodeAsInternal(node, type);
            }
            throw new OntJenaException.Recursion("Can't cast to " + OntObjectImpl.viewAsString(type) + ": " +
                    "graph contains a recursion for node <" + node + ">");
        } catch (OntJenaException.Recursion r) {
            throw r;
        } catch (JenaException e) {
            return null;
        } finally {
            nodes.remove(node);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node
     * @throws org.apache.jena.enhanced.PersonalityConfigException if personality is misconfigured
     *                                                             or the given {@code type} is absent in it;
     *                                                             normally this should not happen
     * @throws NullPointerException                                if any input is {@code null}
     * @throws JenaException                                       unable to construct a new RDF view
     */
    protected <N extends RDFNode> N getNodeAsInternal(Node node, Class<N> type) {
        return super.getNodeAs(Objects.requireNonNull(node, "Null node"),
                Objects.requireNonNull(type, "Null class view."));
    }

    @Override
    public String toString() {
        return String.format("UnionModel{%s}", Graphs.getName(getBaseGraph()));
    }
}
