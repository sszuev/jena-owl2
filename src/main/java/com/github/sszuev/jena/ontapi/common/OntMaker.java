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
 * Used in factory ({@link CommonFactoryImpl}).
 * <p>
 * Created @ssz on 07.11.2016.
 */
public interface OntMaker {

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
    default void make(Node node, EnhGraph eg) {
        throw new OntJenaException.Unsupported();
    }

    /**
     * Returns a {@link OntFilter}, that is used as tester to decide does this maker support graph modification or not.
     *
     * @return {@link OntFilter}
     */
    default OntFilter getTester() {
        return OntFilter.FALSE;
    }

    /**
     * Returns an interface view implementation.
     *
     * @return a class-type of a concrete {@link OntObject OWL Object}.
     */
    Class<? extends EnhNode> getImpl();

    default OntMaker restrict(OntFilter filter) {
        OntJenaException.notNull(filter, "Null restriction filter.");
        return new OntMaker() {
            @Override
            public void make(Node node, EnhGraph eg) {
                OntMaker.this.make(node, eg);
            }

            @Override
            public OntFilter getTester() {
                return OntMaker.this.getTester().and(filter);
            }

            @Override
            public EnhNode instance(Node node, EnhGraph eg) {
                return OntMaker.this.instance(node, eg);
            }

            @Override
            public Class<? extends EnhNode> getImpl() {
                return OntMaker.this.getImpl();
            }
        };
    }

    /**
     * The base maker implementation for our project.
     * <p>
     * Creation in graph is disabled for this maker
     */
    class Default implements OntMaker {
        protected final Class<? extends OntObjectImpl> impl;

        /**
         * Class must be public and have a public constructor with parameters {@link Node} and {@link EnhGraph}.
         *
         * @param impl {@link OntObject} implementation.
         */
        public Default(Class<? extends OntObjectImpl> impl) {
            this.impl = OntJenaException.notNull(impl, "Null implementation class.");
        }

        @Override
        public void make(Node node, EnhGraph eg) {
            throw new OntJenaException.Unsupported("Creation is not allowed for node " +
                    node + " and class " + impl.getSimpleName());
        }

        @Override
        public EnhNode instance(Node node, EnhGraph eg) {
            try {
                return impl.getDeclaredConstructor(Node.class, EnhGraph.class).newInstance(node, eg);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new OntJenaException("Can't create instance of " + impl, e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof JenaException) throw (JenaException) e.getCause();
                throw new OntJenaException("Can't init " + impl, e);
            }
        }

        @Override
        public Class<? extends OntObjectImpl> getImpl() {
            return impl;
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
        public void make(Node node, EnhGraph eg) {
            eg.asGraph().add(Triple.create(node, RDF.type.asNode(), type));
        }

        @Override
        public OntFilter getTester() {
            return OntFilter.TRUE;
        }
    }
}
