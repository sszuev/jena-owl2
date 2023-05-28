package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.JenaException;

import java.lang.reflect.InvocationTargetException;

/**
 * To make some preparation while creating (create main triple).
 * Also, to create new instance of the resource ({@link EnhNode}).
 * Used in factory ({@link CommonEnhNodeFactoryImpl}).
 * <p>
 * Created @ssz on 07.11.2016.
 */
public interface EnhNodeProducer {

    /**
     * Wraps the given {@code node} as a {@link EnhNode Jena RDFNode}.
     * No changes in the given {@link EnhGraph} are made.
     *
     * @param node {@link Node}
     * @param eg   {@link EnhGraph}
     * @return {@link EnhNode}
     */
    EnhNode instance(Node node, EnhGraph eg);

    /**
     * Changes the {@link EnhGraph} according to the encapsulated rules.
     *
     * @param node {@link Node}
     * @param eg   {@link EnhGraph}
     */
    default void insert(Node node, EnhGraph eg) {
        throw new OntJenaException.Unsupported();
    }

    /**
     * Returns a {@link EnhNodeFilter}, that is used as tester to decide does this maker support graph modification or not.
     *
     * @return {@link EnhNodeFilter}
     */
    default EnhNodeFilter getTester() {
        return EnhNodeFilter.FALSE;
    }

    /**
     * Returns a view string representation.
     */
    String targetName();

    default EnhNodeProducer restrict(EnhNodeFilter filter) {
        OntJenaException.notNull(filter, "Null restriction filter.");
        return new EnhNodeProducer() {
            @Override
            public void insert(Node node, EnhGraph eg) {
                EnhNodeProducer.this.insert(node, eg);
            }

            @Override
            public EnhNodeFilter getTester() {
                return EnhNodeProducer.this.getTester().and(filter);
            }

            @Override
            public EnhNode instance(Node node, EnhGraph eg) {
                return EnhNodeProducer.this.instance(node, eg);
            }

            @Override
            public String targetName() {
                return EnhNodeProducer.this.targetName();
            }
        };
    }

    /**
     * The base maker implementation for our project.
     * <p>
     * Creation in graph is disabled for this maker
     */
    class Default implements EnhNodeProducer {
        protected final Class<? extends EnhNode> impl;

        /**
         * Class must be public and have a public constructor with parameters {@link Node} and {@link EnhGraph}.
         *
         * @param impl {@link OntObject} implementation.
         */
        public Default(Class<? extends EnhNode> impl) {
            this.impl = OntJenaException.notNull(impl, "Null implementation class.");
        }

        @Override
        public void insert(Node node, EnhGraph eg) {
            throw new OntJenaException.Unsupported("Creation is not allowed for node " +
                    node + " and target " + targetName());
        }

        @Override
        public EnhNode instance(Node node, EnhGraph eg) {
            try {
                return impl.getDeclaredConstructor(Node.class, EnhGraph.class).newInstance(node, eg);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new OntJenaException("Can't create instance of " + targetName(), e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof JenaException) throw (JenaException) e.getCause();
                throw new OntJenaException("Can't init " + targetName(), e);
            }
        }

        @Override
        public String targetName() {
            return impl.getSimpleName();
        }
    }

    /**
     * to create a triple representing declaration.
     */
    class WithType extends Default {
        protected final Node type;

        public WithType(Class<? extends OntObjectImpl> impl, Resource type) {
            super(impl);
            this.type = OntJenaException.notNull(type, "Null type.").asNode();
        }

        @Override
        public void insert(Node node, EnhGraph eg) {
            eg.asGraph().add(Triple.create(node, RDF.type.asNode(), type));
        }

        @Override
        public EnhNodeFilter getTester() {
            return EnhNodeFilter.TRUE;
        }
    }
}
