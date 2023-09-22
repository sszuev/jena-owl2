package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.WrappedFactoryImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

final class OntClasses {
    public static final EnhNodeFinder CLASS_FINDER = new EnhNodeFinder.ByType(OWL.Class);
    public static final EnhNodeFinder RESTRICTION_FINDER = new EnhNodeFinder.ByType(OWL.Restriction);
    public static final EnhNodeFilter RESTRICTION_FILTER = EnhNodeFilter.BLANK.and(new EnhNodeFilter.HasType(OWL.Restriction));

    public static EnhNodeFactory createCEFactory(
            Class<? extends OntClassImpl> impl,
            Property predicate,
            Class<? extends RDFNode> view) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Class);
        EnhNodeFilter filter = EnhNodeFilter.BLANK.and(new EnhNodeFilter.HasType(OWL.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, predicate.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            if (OntEnhGraph.canAs(view, res.next().getObject(), g)) return true;
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createRestrictionFactory(Class<? extends OntClassImpl.CardinalityRestrictionImpl<?, ?, ?>> impl,
                                                          RestrictionType restrictionType,
                                                          ObjectRestrictionType objectType,
                                                          OntClassImpl.CardinalityType cardinalityType) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter filter = RESTRICTION_FILTER
                .and(getCardinalityFilter(cardinalityType, objectType.view()))
                .and(restrictionType.getFilter());
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createRestrictionFactory(Class<? extends OntClassImpl.ComponentRestrictionImpl<?, ?, ?>> impl,
                                                          RestrictionType propertyType,
                                                          ObjectRestrictionType objectType,
                                                          Property predicate) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter filter = RESTRICTION_FILTER
                .and(propertyType.getFilter())
                .and(objectType.getFilter(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createNaryFactory(Class<? extends OntClassImpl.NaryRestrictionImpl<?, ?, ?>> impl,
                                                   Property predicate) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter filter = RESTRICTION_FILTER
                .and(new EnhNodeFilter.HasPredicate(OWL.onProperties))
                .and(new EnhNodeFilter.HasPredicate(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFilter getCardinalityFilter(OntClassImpl.CardinalityType type, Class<? extends RDFNode> objectType) {
        return (n, g) -> type.isNonQualified(n, g) || type.isQualified(n, g, objectType);
    }

    public enum ObjectRestrictionType implements PredicateFilterProvider {
        CLASS {
            @Override
            public Class<OntClass> view() {
                return OntClass.class;
            }
        },
        DATA_RANGE {
            @Override
            public Class<OntDataRange> view() {
                return OntDataRange.class;
            }
        },
        INDIVIDUAL {
            @Override
            public Class<OntIndividual> view() {
                return OntIndividual.class;
            }
        },
        LITERAL {
            @Override
            public Class<Literal> view() {
                return Literal.class;
            }

            @Override
            public boolean testObject(Node node, EnhGraph graph) {
                return node.isLiteral();
            }
        },
    }

    public enum RestrictionType implements PredicateFilterProvider {
        DATA(OntDataProperty.class),
        OBJECT(OntObjectProperty.class),
        ;
        private final Class<? extends OntProperty> type;
        private final EnhNodeFactory propertyFactory;

        RestrictionType(Class<? extends OntProperty> type) {
            this.type = type;
            this.propertyFactory = WrappedFactoryImpl.of(type);
        }

        @Override
        public Class<? extends OntProperty> view() {
            return type;
        }

        public EnhNodeFilter getFilter() {
            return getFilter(OWL.onProperty);
        }

        @Override
        public boolean testObject(Node node, EnhGraph graph) {
            return propertyFactory.canWrap(node, graph);
        }
    }

    /**
     * Technical interface to make predicate filter for restrictions
     */
    private interface PredicateFilterProvider {

        Class<? extends RDFNode> view();

        default EnhNodeFilter getFilter(Property predicate) {
            return (node, graph) -> testObjects(predicate, node, graph);
        }

        default boolean testObjects(Property predicate, Node node, EnhGraph graph) {
            return Iterators.anyMatch(graph.asGraph().find(node, predicate.asNode(), Node.ANY),
                    t -> testObject(t.getObject(), graph));
        }

        default boolean testObject(Node node, EnhGraph graph) {
            return OntEnhGraph.canAs(view(), node, graph);
        }
    }

    /**
     * A factory to produce {@link OntClass}s.
     * <p>
     * Although it would be easy to produce this factory using {@link OntEnhNodeFactories#createFrom(EnhNodeFinder, Class, Class[])},
     * this variant with explicit methods must be a little faster,
     * since there is a reduction of number of some possible repetition calls.
     * Also, everything here is under control.
     * <p>
     * Created by @ssz on 01.09.2018.
     */
    @SuppressWarnings("WeakerAccess")
    public static class ClassExpressionFactory extends BaseEnhNodeFactoryImpl {

        protected static final Implementation LIST_FACTORY = RDFListImpl.factory;
        private static final Node ANY = Node.ANY;
        private static final Node TYPE = RDF.Nodes.type;
        private static final Node CLASS = OWL.Class.asNode();
        private static final Node RESTRICTION = OWL.Restriction.asNode();
        private static final Node ON_PROPERTY = OWL.onProperty.asNode();
        private static final Node HAS_VALUE = OWL.hasValue.asNode();
        private static final Node QUALIFIED_CARDINALITY = OWL.qualifiedCardinality.asNode();
        private static final Node CARDINALITY = OWL.cardinality.asNode();
        private static final Node MIN_QUALIFIED_CARDINALITY = OWL.minQualifiedCardinality.asNode();
        private static final Node MIN_CARDINALITY = OWL.minCardinality.asNode();
        private static final Node MAX_QUALIFIED_CARDINALITY = OWL.maxQualifiedCardinality.asNode();
        private static final Node MAX_CARDINALITY = OWL.maxCardinality.asNode();
        private static final Node SOME_VALUES_FROM = OWL.someValuesFrom.asNode();
        private static final Node ALL_VALUES_FROM = OWL.allValuesFrom.asNode();
        private static final Node ON_CLASS = OWL.onClass.asNode();
        private static final Node ON_DATA_RANGE = OWL.onDataRange.asNode();
        private static final Node HAS_SELF = OWL.hasSelf.asNode();
        private static final Node ON_PROPERTIES = OWL.onProperties.asNode();
        private static final Node INTERSECTION_OF = OWL.intersectionOf.asNode();
        private static final Node UNION_OF = OWL.unionOf.asNode();
        private static final Node ONE_OF = OWL.oneOf.asNode();
        private static final Node COMPLEMENT_OF = OWL.complementOf.asNode();
        private static final Node TRUE = NodeFactory.createLiteralByValue(Boolean.TRUE, XSDDatatype.XSDboolean);
        private static final String NON_NEGATIVE_INTEGER_URI = XSD.nonNegativeInteger.getURI();
        protected final EnhNodeFactory objectPropertyFactory = WrappedFactoryImpl.of(OntObjectProperty.class);
        protected final EnhNodeFactory dataPropertyFactory = WrappedFactoryImpl.of(OntDataProperty.class);

        public static EnhNodeFactory createFactory() {
            return new ClassExpressionFactory();
        }

        private static boolean isDataCardinality(Node n, EnhGraph eg, Node p, Node qp) {
            return isCardinality(n, eg, p) || isQualifiedCardinality(n, eg, qp, ON_DATA_RANGE, OntDataRange.class);
        }

        private static boolean isObjectCardinality(Node n, EnhGraph eg, Node p, Node qp) {
            return isCardinality(n, eg, p) || isQualifiedCardinality(n, eg, qp, ON_CLASS, OntClass.class);
        }

        private static boolean isQualifiedCardinality(Node n,
                                                      EnhGraph eg,
                                                      Node p,
                                                      Node o,
                                                      Class<? extends OntObject> t) {
            return isCardinality(n, eg, p) && isObjectOfType(n, eg, o, t);
        }

        private static boolean isCardinality(Node n, EnhGraph eg, Node p) {
            return Iterators.findFirst(listObjects(n, eg, p)
                    .filterKeep(x -> isLiteral(x.getObject(), NON_NEGATIVE_INTEGER_URI))).isPresent();
        }

        private static boolean isList(Node n, EnhGraph eg, Node p) {
            return Iterators.findFirst(listObjects(n, eg, p)
                    .filterKeep(x -> LIST_FACTORY.canWrap(x.getObject(), eg))).isPresent();
        }

        @SuppressWarnings("SameParameterValue")
        private static boolean isLiteral(Node n, String dt) {
            return n.isLiteral() && dt.equals(n.getLiteralDatatypeURI());
        }

        private static boolean isObjectOfType(Node n, EnhGraph eg, Node p, Class<? extends OntObject> t) {
            return Iterators.findFirst(listObjects(n, eg, p).filterKeep(x -> hasType(x.getObject(), eg, t))).isPresent();
        }

        private static boolean hasType(Node n, EnhGraph eg, Class<? extends OntObject> type) {
            return OntEnhGraph.canAs(type, n, eg);
        }

        private static ExtendedIterator<Triple> listObjects(Node n, EnhGraph eg, Node p) {
            return eg.asGraph().find(n, p, ANY);
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph g) {
            return g.asGraph().find(ANY, RDF.Nodes.type, CLASS)
                    .mapWith(t -> {
                        Node n = t.getSubject();
                        return n.isURI() ? safeWrap(n, g, Factory.CLASS.factory) : safeWrap(n, g, Factory.ANONYMOUS);
                    })
                    .andThen(g.asGraph().find(ANY, RDF.Nodes.type, RESTRICTION)
                            .mapWith(t -> safeWrap(t.getSubject(), g, Factory.RESTRICTIONS)))
                    .filterDrop(Objects::isNull);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            Factory f = map(node, eg);
            if (f == null) return null;
            return f.factory.createInstance(node, eg);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return map(node, eg) != null;
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) throws OntJenaException {
            Factory f = map(node, eg);
            if (f == null) {
                throw new OntJenaException.Conversion("Can't convert node " + node + " to Class Expression.");
            }
            EnhNode res = f.factory.createInstance(node, eg);
            if (res == null) {
                throw new OntJenaException.IllegalState("Can't create Class Expression for node " + node);
            }
            return res;
        }

        private Factory map(Node n, EnhGraph eg) {
            if (n.isURI()) {
                if (Factory.CLASS.factory.canWrap(n, eg)) {
                    return Factory.CLASS;
                }
                return null;
            }
            if (!n.isBlank()) {
                return null;
            }
            Graph g = eg.asGraph();
            if (g.contains(n, TYPE, RESTRICTION)) {
                ExtendedIterator<Node> props = listObjects(n, eg, ON_PROPERTY).mapWith(Triple::getObject);
                try {
                    while (props.hasNext()) {
                        Node p = props.next();
                        if (objectPropertyFactory.canWrap(p, eg)) {
                            // ObjectSomeValuesFrom, ObjectAllValuesFrom
                            if (isObjectOfType(n, eg, SOME_VALUES_FROM, OntClass.class)) {
                                return Factory.OBJECT_SOME_VALUES_FROM;
                            }
                            if (isObjectOfType(n, eg, ALL_VALUES_FROM, OntClass.class)) {
                                return Factory.OBJECT_ALL_VALUES_FROM;
                            }
                            // ObjectMinCardinality, ObjectMaxCardinality, ObjectCardinality
                            if (isObjectCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                                return Factory.OBJECT_MIN_CARDINALITY;
                            }
                            if (isObjectCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                                return Factory.OBJECT_MAX_CARDINALITY;
                            }
                            if (isObjectCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                                return Factory.OBJECT_EXACT_CARDINALITY;
                            }
                            // ObjectHasValue
                            if (isObjectOfType(n, eg, HAS_VALUE, OntIndividual.class)) {
                                return Factory.OBJECT_HAS_VALUE;
                            }
                            // HasSelf
                            if (Iterators.findFirst(listObjects(n, eg, HAS_SELF)
                                    .filterKeep(x -> TRUE.equals(x.getObject()))).isPresent()) {
                                return Factory.OBJECT_HAS_SELF;
                            }
                        }
                        if (dataPropertyFactory.canWrap(p, eg)) {
                            // DataSomeValuesFrom, DataAllValuesFrom
                            if (isObjectOfType(n, eg, SOME_VALUES_FROM, OntDataRange.class)) {
                                return Factory.DATA_SOME_VALUES_FROM;
                            }
                            if (isObjectOfType(n, eg, ALL_VALUES_FROM, OntDataRange.class)) {
                                return Factory.DATA_ALL_VALUES_FROM;
                            }
                            // DataMinCardinality, DataMaxCardinality, DataCardinality
                            if (isDataCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                                return Factory.DATA_MIN_CARDINALITY;
                            }
                            if (isDataCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                                return Factory.DATA_MAX_CARDINALITY;
                            }
                            if (isDataCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                                return Factory.DATA_EXACT_CARDINALITY;
                            }
                            // DataHasValue
                            if (Iterators.findFirst(listObjects(n, eg, HAS_VALUE)
                                    .filterKeep(x -> x.getObject().isLiteral())).isPresent()) {
                                return Factory.DATA_HAS_VALUE;
                            }
                        }
                    }
                } finally {
                    props.close();
                }

                // very simplified factories for nary-restrictions:
                if (g.contains(n, ON_PROPERTIES, ANY)) {
                    if (Iterators.findFirst(listObjects(n, eg, SOME_VALUES_FROM)).isPresent()) {
                        return Factory.DATA_NARY_SOME_VALUES_FROM;
                    }
                    if (Iterators.findFirst(listObjects(n, eg, ALL_VALUES_FROM)).isPresent()) {
                        return Factory.DATA_NARY_ALL_VALUES_FROM;
                    }
                }
            }
            if (g.contains(n, TYPE, CLASS)) {
                // first check owl:complementOf, since it is more accurately defined
                if (isObjectOfType(n, eg, COMPLEMENT_OF, OntClass.class)) {
                    return Factory.COMPLEMENT_OF;
                }
                // simplified checks for []-lists
                // todo: need more accurate check - also for content, to avoid intersections with data ranges
                if (isList(n, eg, INTERSECTION_OF)) {
                    return Factory.INTERSECTION_OF;
                }
                if (isList(n, eg, UNION_OF)) {
                    return Factory.UNION_OF;
                }
                if (isList(n, eg, ONE_OF)) {
                    return Factory.ONE_OF;
                }
            }
            return null;
        }

        enum Factory {
            CLASS(OntClass.Named.class, false),
            OBJECT_SOME_VALUES_FROM(OntClass.ObjectSomeValuesFrom.class),
            OBJECT_ALL_VALUES_FROM(OntClass.ObjectAllValuesFrom.class),
            OBJECT_MIN_CARDINALITY(OntClass.ObjectMinCardinality.class),
            OBJECT_MAX_CARDINALITY(OntClass.ObjectMaxCardinality.class),
            OBJECT_EXACT_CARDINALITY(OntClass.ObjectCardinality.class),
            OBJECT_HAS_VALUE(OntClass.ObjectHasValue.class),
            OBJECT_HAS_SELF(OntClass.HasSelf.class),

            DATA_SOME_VALUES_FROM(OntClass.DataSomeValuesFrom.class),
            DATA_ALL_VALUES_FROM(OntClass.DataAllValuesFrom.class),
            DATA_MIN_CARDINALITY(OntClass.DataMinCardinality.class),
            DATA_MAX_CARDINALITY(OntClass.DataMaxCardinality.class),
            DATA_EXACT_CARDINALITY(OntClass.DataCardinality.class),
            DATA_HAS_VALUE(OntClass.DataHasValue.class),
            DATA_NARY_SOME_VALUES_FROM(OntClass.NaryDataSomeValuesFrom.class),
            DATA_NARY_ALL_VALUES_FROM(OntClass.NaryDataAllValuesFrom.class),

            UNION_OF(OntClass.UnionOf.class, false),
            INTERSECTION_OF(OntClass.IntersectionOf.class, false),
            ONE_OF(OntClass.OneOf.class, false),
            COMPLEMENT_OF(OntClass.ComplementOf.class, false),
            ;
            private static final Collection<EnhNodeFactory> RESTRICTIONS = Arrays.stream(values())
                    .filter(x -> x.isRestriction)
                    .map(x -> x.factory)
                    .collect(Collectors.toUnmodifiableList());
            private static final Collection<EnhNodeFactory> ANONYMOUS = Arrays.stream(values())
                    .filter(x -> !x.isRestriction && CLASS != x)
                    .map(x -> x.factory)
                    .collect(Collectors.toUnmodifiableList());

            private final EnhNodeFactory factory;
            private final boolean isRestriction;

            Factory(Class<? extends OntObject> type) {
                this(type, true);
            }

            Factory(Class<? extends OntObject> type, boolean restriction) {
                this.factory = WrappedFactoryImpl.of(type);
                this.isRestriction = restriction;
            }
        }
    }

    public static class HasSelfFilter implements EnhNodeFilter {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return g.asGraph().contains(n, OWL.hasSelf.asNode(), ModelUtils.TRUE.asNode());
        }
    }

    public static class HasSelfMaker extends EnhNodeProducer.WithType {
        public HasSelfMaker() {
            super(OntClassImpl.HasSelfImpl.class, OWL.Restriction);
        }

        @Override
        public void insert(Node node, EnhGraph eg) {
            super.insert(node, eg);
            eg.asGraph().add(Triple.create(node, OWL.hasSelf.asNode(), ModelUtils.TRUE.asNode()));
        }
    }
}
