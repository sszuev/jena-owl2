package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.WrappedFactoryImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

final class OntProperties {
    public static final EnhNodeFinder NEGATIVE_PROPERTY_ASSERTION_FINDER = new EnhNodeFinder.ByType(OWL.NegativePropertyAssertion);
    public static final EnhNodeFilter NEGATIVE_PROPERTY_ASSERTION_FILTER = EnhNodeFilter.ANON
            .and(new EnhNodeFilter.HasPredicate(OWL.sourceIndividual))
            .and(new EnhNodeFilter.HasPredicate(OWL.assertionProperty));
    private static final EnhNodeFactory NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedFactoryImpl.of(OntObjectProperty.Named.class);
    private static final EnhNodeFactory ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedFactoryImpl.of(OntObjectProperty.Inverse.class);

    public static class ObjectPropertyExpressionFactory extends BaseEnhNodeFactoryImpl {
        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return Iterators.concat(
                    NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.iterator(eg),
                    ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.iterator(eg)
            );
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(node, eg);
            }
            return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(node, eg);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.createInstance(node, eg);
            }
            return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.createInstance(node, eg);
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isURI())
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.wrap(node, eg);
            if (node.isBlank())
                return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.wrap(node, eg);
            throw new OntJenaException.Conversion("Can't convert node " + node + " to Object Property Expression.");
        }
    }

    public static class AnonymousObjectPropertyFactory extends BaseEnhNodeFactoryImpl {
        private static final Node OWL_INVERSE_OF = OWL.inverseOf.asNode();

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return listTriples(Node.ANY, eg)
                    .filterKeep(x -> x.getSubject().isBlank())
                    .mapWith(x -> createInstance(x.getSubject(), eg));
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return node.isBlank() && Iterators.findFirst(listTriples(node, eg)).isPresent();
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntObjectPropertyImpl.InversePropertyImpl(node, eg);
        }

        private ExtendedIterator<Triple> listTriples(Node node, EnhGraph eg) {
            // "_:x owl:inverseOf PN":
            return eg.asGraph().find(node, OWL_INVERSE_OF, Node.ANY)
                    .filterKeep(x -> NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(x.getObject(), eg));
        }
    }
}
