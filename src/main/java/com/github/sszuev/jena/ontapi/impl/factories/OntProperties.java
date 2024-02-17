package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntConfig;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class OntProperties {
    public static final EnhNodeFinder NEGATIVE_PROPERTY_ASSERTION_FINDER = new EnhNodeFinder.ByType(OWL.NegativePropertyAssertion);
    public static final EnhNodeFilter NEGATIVE_PROPERTY_ASSERTION_FILTER = EnhNodeFilter.ANON
            .and(new EnhNodeFilter.HasPredicate(OWL.sourceIndividual))
            .and(new EnhNodeFilter.HasPredicate(OWL.assertionProperty));
    private static final EnhNodeFactory NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Named.class);
    private static final EnhNodeFactory ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Inverse.class);

    public static Factory createFactory(OntConfig config) {
        List<Node> objectPropertyTypes = new ArrayList<>();
        List<Node> allPropertyTypes = new ArrayList<>();
        objectPropertyTypes.add(OWL.ObjectProperty.asNode());
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE)) {
            objectPropertyTypes.add(OWL.InverseFunctionalProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.ReflexiveProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.IrreflexiveProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL.SymmetricProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL.AsymmetricProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE)) {
            objectPropertyTypes.add(OWL.TransitiveProperty.asNode());
        }
        allPropertyTypes.add(RDF.Property.asNode());
        allPropertyTypes.add(OWL.AnnotationProperty.asNode());
        allPropertyTypes.add(OWL.DatatypeProperty.asNode());
        allPropertyTypes.add(OWL.FunctionalProperty.asNode());
        allPropertyTypes.addAll(objectPropertyTypes);
        return new Factory(
                allPropertyTypes.stream().collect(Collectors.toUnmodifiableList()),
                objectPropertyTypes.stream().collect(Collectors.toUnmodifiableList()),
                config.getBoolean(OntModelControls.USE_OWL_INVERSE_OBJECT_PROPERTY_FEATURE)
        );
    }

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
    public static class Factory extends BaseEnhNodeFactoryImpl {

        private final List<Node> propertyTypes;
        private final List<Node> objectPropertyTypes;
        private final boolean allowInverseObjectProperty;

        private Factory(List<Node> propertyTypes, List<Node> objectPropertyTypes, boolean allowInverseObjectProperty) {
            this.propertyTypes = Objects.requireNonNull(propertyTypes);
            this.objectPropertyTypes = Objects.requireNonNull(objectPropertyTypes);
            this.allowInverseObjectProperty = allowInverseObjectProperty;
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            ExtendedIterator<Node> named = Iterators.distinct(
                    Iterators.flatMap(
                            WrappedIterator.create(propertyTypes.iterator()),
                            type -> eg.asGraph().find(Node.ANY, RDF.type.asNode(), type)
                    ).mapWith(Triple::getSubject).filterKeep(Node::isURI)
            );
            if (!allowInverseObjectProperty) {
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
                for (Node type : propertyTypes) {
                    if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                        return true;
                    }
                }
            }
            if (allowInverseObjectProperty && node.isBlank()) {
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
            for (Node type : objectPropertyTypes) {
                if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                    return true;
                }
            }
            return false;
        }
    }

}
