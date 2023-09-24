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
 * Used by {@link CommonEnhNodeFactoryImpl default factory} and {@link CompositeEnhNodeFactoryImpl} implementations as a component.
 * <p>
 * Created @ssz on 07.11.2016.
 */
@FunctionalInterface
public interface EnhNodeFilter {
    EnhNodeFilter TRUE = new EnhNodeFilter() {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return true;
        }

        @Override
        public String toString() {
            return "TRUE";
        }
    };
    EnhNodeFilter FALSE = new EnhNodeFilter() {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return false;
        }

        @Override
        public String toString() {
            return "FALSE";
        }
    };
    EnhNodeFilter URI = new EnhNodeFilter() {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return n.isURI();
        }

        @Override
        public String toString() {
            return "URI";
        }
    };
    EnhNodeFilter ANON = new EnhNodeFilter() {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return n.isBlank();
        }

        @Override
        public String toString() {
            return "BLANK";
        }
    };

    /**
     * Tests if the given {@link Node node} suits the encapsulated conditions in bounds of the specified {@link EnhGraph graph}.
     *
     * @param n {@link Node}, not {@code null}
     * @param g {@link EnhGraph}, not {@code null}
     * @return boolean
     */
    boolean test(Node n, EnhGraph g);

    default EnhNodeFilter and(EnhNodeFilter other) {
        if (Objects.requireNonNull(other, "Null and-filter.").equals(TRUE)) {
            return this;
        }
        if (this.equals(TRUE)) return other;
        if (other.equals(FALSE)) return FALSE;
        if (this.equals(FALSE)) return FALSE;
        return new EnhNodeFilter() {
            @Override
            public boolean test(Node n, EnhGraph g) {
                return EnhNodeFilter.this.test(n, g) && other.test(n, g);
            }

            @Override
            public String toString() {
                return "(" + EnhNodeFilter.this + ")AND(" + other + ")";
            }
        };
    }

    default EnhNodeFilter or(EnhNodeFilter other) {
        if (Objects.requireNonNull(other, "Null or-filter.").equals(TRUE)) {
            return TRUE;
        }
        if (this.equals(TRUE)) return TRUE;
        if (other.equals(FALSE)) return this;
        if (this.equals(FALSE)) return other;
        return new EnhNodeFilter() {
            @Override
            public boolean test(Node n, EnhGraph g) {
                return EnhNodeFilter.this.test(n, g) || other.test(n, g);
            }

            @Override
            public String toString() {
                return "(" + EnhNodeFilter.this + ")OR(" + other + ")";
            }
        };
    }

    default EnhNodeFilter accumulate(EnhNodeFilter... filters) {
        EnhNodeFilter res = this;
        for (EnhNodeFilter o : filters) {
            res = res.and(o);
        }
        return res;
    }

    class HasPredicate implements EnhNodeFilter {
        protected final Node predicate;

        public HasPredicate(Property predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Null predicate.").asNode();
        }

        @Override
        public boolean test(Node n, EnhGraph g) {
            return g.asGraph().contains(n, predicate, Node.ANY);
        }

        @Override
        public String toString() {
            return "HasPredicate::" + predicate.getLocalName();
        }
    }

    class HasType implements EnhNodeFilter {
        protected final Node type;

        public HasType(Resource type) {
            this.type = Objects.requireNonNull(type, "Null type.").asNode();
        }

        @Override
        public boolean test(Node node, EnhGraph eg) {
            return eg.asGraph().contains(node, RDF.Nodes.type, type);
        }

        @Override
        public String toString() {
            return "HasType::" + type.getLocalName();
        }
    }

    class OneOf implements EnhNodeFilter {
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

        @Override
        public String toString() {
            return "OneOf::" + nodes.stream().map(Node::getLocalName).collect(Collectors.toList());
        }
    }
}
