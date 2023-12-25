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
import java.util.function.BiFunction;

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
    EnhNode newInstance(Node node, EnhGraph eg);

    /**
     * Changes the {@link EnhGraph} according to the encapsulated rules.
     *
     * @param node {@link Node}
     * @param eg   {@link EnhGraph}
     */
    default void doInsert(Node node, EnhGraph eg) {
        throw new OntJenaException.IllegalCall();
    }

    /**
     * Answers {@code true} if the given {@code node} can be created in graph
     */
    default boolean canInsert(Node node, EnhGraph eg) {
        return false;
    }

    /**
     * Returns a view string representation.
     */
    String targetName();

    default EnhNodeProducer restrict(EnhNodeFilter filter) {
        OntJenaException.notNull(filter, "Null restriction filter.");
        return new EnhNodeProducer() {

            @Override
            public void doInsert(Node node, EnhGraph eg) {
                EnhNodeProducer.this.doInsert(node, eg);
            }

            @Override
            public boolean canInsert(Node node, EnhGraph eg) {
                return EnhNodeProducer.this.canInsert(node, eg) && filter.test(node, eg);
            }

            @Override
            public EnhNode newInstance(Node node, EnhGraph eg) {
                return EnhNodeProducer.this.newInstance(node, eg);
            }

            @Override
            public String targetName() {
                return EnhNodeProducer.this.targetName();
            }

            @Override
            public String toString() {
                return "Producer[" + targetName() + "]Restriction[" + filter + "]";
            }
        };
    }

    /**
     * The base maker implementation for our project.
     * <p>
     * Creation in graph is disabled for this maker
     */
    class Default implements EnhNodeProducer {
        private final BiFunction<Node, EnhGraph, EnhNode> producer;
        private final String targetName;

        /**
         * Class must be public and have a public constructor with parameters {@link Node} and {@link EnhGraph}.
         *
         * @param impl     {@link OntObject} implementation
         * @param producer factory to create new instance, if {@code null} reflection is used
         */
        public Default(Class<? extends EnhNode> impl, BiFunction<Node, EnhGraph, EnhNode> producer) {
            this.targetName = OntJenaException.notNull(impl, "Null implementation class.").getName()
                    .replace(impl.getPackageName() + ".", "");
            this.producer = OntJenaException.notNull(producer);
        }

        public Default(Class<? extends EnhNode> impl) {
            this.targetName = OntJenaException.notNull(impl, "Null implementation class.")
                    .getName().replace(impl.getPackageName() + ".", "");
            this.producer = (node, graph) -> newInstance(node, graph, impl, targetName);
        }

        @Override
        public void doInsert(Node node, EnhGraph eg) {
            throw new OntJenaException.IllegalCall("Creation is not allowed for node " +
                    node + " and target " + targetName());
        }

        @Override
        public EnhNode newInstance(Node node, EnhGraph eg) {
            return producer.apply(node, eg);
        }

        private static EnhNode newInstance(Node node,
                                           EnhGraph eg,
                                           Class<? extends EnhNode> impl,
                                           String targetName) {
            try {
                return impl.getDeclaredConstructor(Node.class, EnhGraph.class).newInstance(node, eg);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new OntJenaException("Can't create instance of " + targetName, e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof JenaException) throw (JenaException) e.getCause();
                throw new OntJenaException("Can't init " + targetName, e);
            }
        }

        @Override
        public String targetName() {
            return targetName;
        }
    }

    /**
     * to create a triple representing declaration.
     */
    class WithType extends Default {
        protected final Node type;

        public WithType(Class<? extends OntObjectImpl> impl,
                        Resource type,
                        BiFunction<Node, EnhGraph, EnhNode> producer) {
            super(impl, producer);
            this.type = OntJenaException.notNull(type, "Null type.").asNode();
        }

        public WithType(Class<? extends OntObjectImpl> impl, Resource type) {
            super(impl);
            this.type = OntJenaException.notNull(type, "Null type.").asNode();
        }

        @Override
        public void doInsert(Node node, EnhGraph eg) {
            eg.asGraph().add(Triple.create(node, RDF.type.asNode(), type));
        }

        @Override
        public boolean canInsert(Node node, EnhGraph eg) {
            return true;
        }
    }
}
