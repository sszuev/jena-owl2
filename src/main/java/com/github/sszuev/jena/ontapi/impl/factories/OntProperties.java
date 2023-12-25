package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.WrappedEnhNodeFactory;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimplePropertyImpl;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntProperties {
    public static final EnhNodeFinder NEGATIVE_PROPERTY_ASSERTION_FINDER = new EnhNodeFinder.ByType(OWL.NegativePropertyAssertion);
    public static final EnhNodeFilter NEGATIVE_PROPERTY_ASSERTION_FILTER = EnhNodeFilter.ANON
            .and(new EnhNodeFilter.HasPredicate(OWL.sourceIndividual))
            .and(new EnhNodeFilter.HasPredicate(OWL.assertionProperty));
    private static final EnhNodeFactory NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Named.class);
    private static final EnhNodeFactory ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Inverse.class);

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

    /**
     * Generic factory for any OntProperty including {@code rdf:Property}.
     * It does not care about punnings.
     */
    public static class AnyOntPropertyFactory extends BaseEnhNodeFactoryImpl {

        private static final List<Node> OWL2_OBJECT_PROPERTY_TYPES = List.of(
                OWL.ObjectProperty.asNode(),
                OWL.InverseFunctionalProperty.asNode(),
                OWL.ReflexiveProperty.asNode(),
                OWL.IrreflexiveProperty.asNode(),
                OWL.SymmetricProperty.asNode(),
                OWL.AsymmetricProperty.asNode(),
                OWL.TransitiveProperty.asNode()
        );

        private static final List<Node> OWL1_OBJECT_PROPERTY_TYPES = List.of(
                OWL.ObjectProperty.asNode(),
                OWL.InverseFunctionalProperty.asNode(),
                OWL.SymmetricProperty.asNode(),
                OWL.TransitiveProperty.asNode()
        );

        private static final List<Node> OWL2_PROPERTY_TYPES = Stream.concat(Stream.of(
                RDF.Property.asNode(),
                OWL.DatatypeProperty.asNode(),
                OWL.AnnotationProperty.asNode(),
                OWL.FunctionalProperty.asNode()
        ), OWL2_OBJECT_PROPERTY_TYPES.stream()).collect(Collectors.toUnmodifiableList());

        private static final List<Node> OWL1_PROPERTY_TYPES = Stream.concat(Stream.of(
                RDF.Property.asNode(),
                OWL.DatatypeProperty.asNode(),
                OWL.AnnotationProperty.asNode(),
                OWL.FunctionalProperty.asNode()
        ), OWL1_OBJECT_PROPERTY_TYPES.stream()).collect(Collectors.toUnmodifiableList());

        private final boolean forOWL2;

        public AnyOntPropertyFactory(boolean forOWL2) {
            this.forOWL2 = forOWL2;
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            ExtendedIterator<Node> named = Iterators.distinct(
                    Iterators.flatMap(
                            WrappedIterator.create((forOWL2 ? OWL2_PROPERTY_TYPES : OWL1_PROPERTY_TYPES).iterator()),
                            type -> eg.asGraph().find(Node.ANY, RDF.type.asNode(), type)
                    ).mapWith(Triple::getSubject).filterKeep(Node::isURI)
            );
            if (!forOWL2) {
                return named.mapWith(it -> createInstance(it, eg));
            }
            ExtendedIterator<Node> anonymous = Iterators.distinct(
                    eg.asGraph().find(Node.ANY, OWL.inverseOf.asNode(), Node.ANY).mapWith(triple -> {
                        if (!triple.getSubject().isBlank()) {
                            return null;
                        }
                        if (!isNamedObjectProperty(triple.getObject(), eg)) {
                            return null;
                        }
                        return triple.getSubject();
                    }).filterKeep(Objects::nonNull)
            );
            return Iterators.concat(named, anonymous).mapWith(it -> createInstance(it, eg));
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntSimplePropertyImpl(node, eg);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                for (Node type : (forOWL2 ? OWL2_PROPERTY_TYPES : OWL1_PROPERTY_TYPES)) {
                    if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                        return true;
                    }
                }
            }
            if (forOWL2 && node.isBlank()) {
                // "_:x owl:inverseOf PN":
                return isInverseObjectProperty(node, eg);
            }
            return false;
        }

        private boolean isInverseObjectProperty(Node node, EnhGraph eg) {
            return Iterators.findFirst(
                    eg.asGraph().find(node, OWL.inverseOf.asNode(), Node.ANY)
                            .mapWith(Triple::getObject)
                            .filterKeep(it -> isNamedObjectProperty(it, eg))
            ).isPresent();
        }

        private boolean isNamedObjectProperty(Node node, EnhGraph eg) {
            if (!node.isURI()) {
                return false;
            }
            for (Node type : (forOWL2 ? OWL2_PROPERTY_TYPES : OWL1_PROPERTY_TYPES)) {
                if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                    return true;
                }
            }
            return false;
        }
    }
}
