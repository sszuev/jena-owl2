package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.WrappedFactoryImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Property Expression base impl-class.
 * No functionality, just a collection of factories related to all OWL property-expressions.
 * <p>
 * Created by @ssz on 08.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntPEImpl extends OntObjectImpl implements OntProperty {

    public static final EnhNodeFinder NAMED_PROPERTY_FINDER = OntEnhNodeFactories.createFinder(
            OWL.AnnotationProperty,
            OWL.ObjectProperty,
            OWL.DatatypeProperty
    );

    public OntPEImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static EnhNodeFactory createObjectPropertyExpressionFactory() {
        return new HasAnonymous() {
            private final EnhNodeFactory named = WrappedFactoryImpl.of(OntObjectProperty.Named.class);

            @Override
            public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
                ExtendedIterator<EnhNode> res = eg.asGraph().find(Node.ANY, RDF.Nodes.type, OWL.ObjectProperty.asNode())
                        .filterKeep(t -> t.getSubject().isURI())
                        .mapWith(t -> named.createInstance(t.getSubject(), eg));
                return Iterators.concat(res, anonymous.iterator(eg));
            }

            @Override
            public boolean canWrap(Node node, EnhGraph eg) {
                if (node.isURI()) {
                    return named.canWrap(node, eg);
                }
                return anonymous.canWrap(node, eg);
            }

            @Override
            public EnhNode createInstance(Node node, EnhGraph eg) {
                if (node.isURI()) {
                    return named.createInstance(node, eg);
                }
                return anonymous.createInstance(node, eg);
            }

            @Override
            public EnhNode wrap(Node node, EnhGraph eg) {
                if (node.isURI())
                    return named.wrap(node, eg);
                if (node.isBlank())
                    return anonymous.wrap(node, eg);
                throw new OntJenaException.Conversion("Can't convert node " + node + " to Object Property Expression.");
            }
        };
    }

    public static Stream<OntClass> declaringClasses(OntRealProperty property, boolean direct) {
        Set<OntClass> domains = property.domains()
                .flatMap(clazz -> Stream.concat(Stream.of(clazz), clazz.subClasses(false)))
                .filter(OntCEImpl::isNotBuiltin)
                .collect(Collectors.toSet());
        if (domains.isEmpty()) {
            Stream<OntClass> res = property.getModel().ontObjects(OntClass.class).filter(OntCEImpl::isNotBuiltin);
            if (!direct) {
                return res;
            } else {
                return res.filter(OntClass::isHierarchyRoot);
            }
        }
        return domains.stream().filter(clazz -> clazz.hasDeclaredProperty(property, direct));
    }

    static <X extends OntProperty> Stream<X> subProperties(X property, Class<X> type, boolean direct) {
        return treeNodes(property,
                x -> actualAdjacentSubProperties(x, type, false),
                x -> explicitSubProperties(x, type),
                direct);
    }

    static <X extends OntProperty> Stream<X> superProperties(X property, Class<X> type, boolean direct) {
        return treeNodes(property,
                x -> actualAdjacentSubProperties(x, type, true),
                x -> explicitSuperProperties(x, type),
                direct);
    }

    static <X extends OntProperty> Stream<X> actualAdjacentSubProperties(X property, Class<X> type, boolean inverse) {
        Set<X> equivalents = equivalentsBySubPropertyOf(property, type).collect(Collectors.toSet());
        equivalents.add(property);
        return equivalents.stream()
                .flatMap(x -> inverse ? explicitSuperProperties(x, type) : explicitSubProperties(x, type))
                .filter(x -> !equivalents.contains(x))
                .flatMap(x -> Stream.concat(Stream.of(x), equivalentsBySubPropertyOf(x, type)))
                .distinct();
    }

    static <X extends OntProperty> Stream<X> explicitSubProperties(X property, Class<X> type) {
        return subjects(RDFS.subPropertyOf, property, type);
    }

    static <X extends OntProperty> Stream<X> explicitSuperProperties(X property, Class<X> type) {
        return property.objects(RDFS.subPropertyOf, type);
    }

    static <X extends OntProperty> Stream<X> equivalentsBySubPropertyOf(X property, Class<X> type) {
        return explicitSubProperties(property, type).filter(x -> x.getModel().contains(property, RDFS.subPropertyOf, x));
    }

    @Override
    public Property asProperty() {
        if (!isURIResource()) throw new OntJenaException.IllegalState();
        return as(Property.class);
    }

    public static class PropertiesFactory extends HasAnonymous {
        final List<Factory> factories = new ArrayList<>();

        public PropertiesFactory add(Resource declaration, Class<? extends OntProperty> type) {
            factories.add(new Factory(declaration.asNode(), type));
            return this;
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            Graph g = eg.asGraph();
            ExtendedIterator<EnhNode> res = Iterators.distinct(Iterators.flatMap(Iterators.create(factories),
                    f -> g.find(Node.ANY, RDF.Nodes.type, f.nt)
                            .mapWith(t -> BaseEnhNodeFactoryImpl.safeWrap(t.getSubject(), eg, f.f))
                            .filterDrop(Objects::isNull)));
            return Iterators.concat(res, anonymous.iterator(eg));
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (!node.isURI()) {
                return anonymous.canWrap(node, eg);
            }
            for (Factory f : factories) {
                if (f.f.canWrap(node, eg)) return true;
            }
            return false;
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (!node.isURI()) {
                return anonymous.createInstance(node, eg);
            }
            for (Factory f : factories) {
                EnhNode res = safeWrap(node, eg, f.f);
                if (res != null) return res;
            }
            return null;
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isBlank())
                return anonymous.wrap(node, eg);
            OntJenaException.Conversion ex = new OntJenaException.Conversion("Can't convert node " +
                    node + " to Property Expression");
            if (!node.isURI())
                throw ex;
            for (Factory f : factories) {
                try {
                    return f.f.wrap(node, eg);
                } catch (OntJenaException.Conversion c) {
                    ex.addSuppressed(c);
                }
            }
            throw ex;
        }

        private static class Factory {
            private final Node nt;
            private final EnhNodeFactory f;

            private Factory(Node nodeType, Class<? extends OntProperty> classType) {
                this.nt = Objects.requireNonNull(nodeType);
                this.f = WrappedFactoryImpl.of(classType);
            }
        }
    }

    protected static abstract class HasAnonymous extends BaseEnhNodeFactoryImpl {
        protected final EnhNodeFactory anonymous = WrappedFactoryImpl.of(OntObjectProperty.Inverse.class);
    }

    public static class AnonymousObjectPropertyFactory extends BaseEnhNodeFactoryImpl {
        private static final Node OWL_INVERSE_OF = OWL.inverseOf.asNode();
        protected final EnhNodeFactory named = WrappedFactoryImpl.of(OntObjectProperty.Named.class);

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
            return new OntOPEImpl.InversePropertyImpl(node, eg);
        }

        private ExtendedIterator<Triple> listTriples(Node node, EnhGraph eg) {
            // "_:x owl:inverseOf PN":
            return eg.asGraph().find(node, OWL_INVERSE_OF, Node.ANY).filterKeep(x -> named.canWrap(x.getObject(), eg));
        }
    }
}
