package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * To filter resources.
 * Used by {@link CommonFactoryImpl default factory} and {@link MultiFactoryImpl} implementations as a component.
 * <p>
 * Created @ssz on 07.11.2016.
 */
@FunctionalInterface
public interface OntFilter {
    OntFilter TRUE = (n, g) -> true;
    OntFilter FALSE = (n, g) -> false;
    OntFilter URI = (n, g) -> n.isURI();
    OntFilter BLANK = (n, g) -> n.isBlank();

    /**
     * Tests if the given {@link Node node} suits the encapsulated conditions in bounds of the specified {@link EnhGraph graph}.
     *
     * @param n {@link Node}, not {@code null}
     * @param g {@link EnhGraph}, not {@code null}
     * @return boolean
     */
    boolean test(Node n, EnhGraph g);

    default OntFilter and(OntFilter other) {
        if (Objects.requireNonNull(other, "Null and-filter.").equals(TRUE)) {
            return this;
        }
        if (this.equals(TRUE)) return other;
        if (other.equals(FALSE)) return FALSE;
        if (this.equals(FALSE)) return FALSE;
        return (Node n, EnhGraph g) -> this.test(n, g) && other.test(n, g);
    }

    default OntFilter or(OntFilter other) {
        if (Objects.requireNonNull(other, "Null or-filter.").equals(TRUE)) {
            return TRUE;
        }
        if (this.equals(TRUE)) return TRUE;
        if (other.equals(FALSE)) return this;
        if (this.equals(FALSE)) return other;
        return (Node n, EnhGraph g) -> this.test(n, g) || other.test(n, g);
    }

    default OntFilter negate() {
        if (this.equals(TRUE)) return FALSE;
        if (this.equals(FALSE)) return TRUE;
        return (Node n, EnhGraph g) -> !test(n, g);
    }

    default OntFilter accumulate(OntFilter... filters) {
        OntFilter res = this;
        for (OntFilter o : filters) {
            res = res.and(o);
        }
        return res;
    }

    class HasPredicate implements OntFilter {
        protected final Node predicate;

        public HasPredicate(Property predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Null predicate.").asNode();
        }

        @Override
        public boolean test(Node n, EnhGraph g) {
            return g.asGraph().contains(n, predicate, Node.ANY);
        }
    }

    class HasType implements OntFilter {
        protected final Node type;

        public HasType(Resource type) {
            this.type = Objects.requireNonNull(type, "Null type.").asNode();
        }

        @Override
        public boolean test(Node node, EnhGraph eg) {
            return eg.asGraph().contains(node, RDF.Nodes.type, type);
        }
    }

    class OneOf implements OntFilter {
        protected final Set<Node> nodes;

        public OneOf(Collection<? extends RDFNode> types) {
            this.nodes = Objects.requireNonNull(types).stream().map(RDFNode::asNode)
                    .collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public boolean test(Node n, EnhGraph g) {
            return nodes.contains(n);
        }

        @Override
        public boolean equals(Object o) {
            if (nodes.isEmpty() && FALSE == o) return true;
            return super.equals(o);
        }
    }
}
