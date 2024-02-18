package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.WrappedEnhNodeFactory;
import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSimpleClassImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.StdModels;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntClasses {
    public static final EnhNodeFinder CLASS_FINDER = new EnhNodeFinder.ByType(OWL.Class);
    public static final EnhNodeFinder RESTRICTION_FINDER = new EnhNodeFinder.ByType(OWL.Restriction);

    // legacy Jena's OntModel allows these types
    private static final Set<Node> COMPATIBLE_TYPES = Stream.of(OWL.Class, RDFS.Class, RDFS.Datatype)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());

    public static boolean canBeNamedClass(Node n, EnhGraph eg, boolean useLegacyCheck) {
        if (!n.isURI()) {
            return false;
        }
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getNamedClasses().contains(n)) {
            return true;
        }
        Graph g = eg.asGraph();
        Set<Node> punnings = OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getPunnings().getNamedClasses();
        if (!useLegacyCheck) {
            return g.contains(n, RDF.type.asNode(), OWL.Class.asNode()) && !Graphs.hasOneOfType(n, g, punnings);
        }
        if (Graphs.hasOneOfType(n, g, punnings)) {
            return false;
        }
        return canBeClass(n, g);
    }

    /**
     * This is for compatibility with legacy jena's OntModel.
     */
    public static boolean canBeClass(Node n, Graph g) {
        if (Graphs.hasOneOfType(n, g, COMPATIBLE_TYPES)) {
            return true;
        }
        return g.contains(Node.ANY, RDFS.domain.asNode(), n) || g.contains(Node.ANY, RDFS.range.asNode(), n);
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config, Type... filters) {
        return createClassExpressionFactory(config, false, filters);
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config,
                                                              boolean baseClass,
                                                              Type... filters) {
        // namedClassFactory should be specified only for the super type OntClass.
        boolean useLegacyClassTest = config.getBoolean(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY);
        BiPredicate<Node, EnhGraph> namedClassFilter = baseClass ?
                (node, graph) -> canBeNamedClass(node, graph, useLegacyClassTest) : null;
        BiPredicate<Node, EnhGraph> genericClassFilter = baseClass && config.getBoolean(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS) ?
                (node, graph) -> canBeClass(node, graph.asGraph()) : null;
        return new Factory(
                /*namedClassFactory*/ namedClassFilter,
                /*genericClassFilter*/ genericClassFilter,
                /*allowNamedClassExpressions*/ config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS),
                /*allowQualifiedCardinalityRestrictions*/ config.getBoolean(OntModelControls.USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE),
                /*restrictOneOfToSingleIndividual*/ config.getBoolean(OntModelControls.USE_OBJECT_ONE_OF_SINGLE_INDIVIDUAL_RESTRICTION),
                /*filter types*/ Arrays.asList(filters));
    }

    // Boolean Connectives and Enumeration of Individuals
    public static EnhNodeFactory createBooleanConnectivesAndIndividualEnumerationFactory(
            Class<? extends OntClassImpl> impl,
            Property predicate,
            Class<? extends RDFNode> view,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Class);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, predicate.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            if (OntEnhGraph.canAs(view, res.next().getObject(), g)) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    @SuppressWarnings("DuplicatedCode")
    public static EnhNodeFactory createOWL2OneOfEnumerationFactory(
            OntConfig config) {
        boolean restrictOneOfToSingleIndividual =
                config.getBoolean(OntModelControls.USE_OBJECT_ONE_OF_SINGLE_INDIVIDUAL_RESTRICTION);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.OneOfImpl.class, OWL.Class, OntClassImpl.OneOfImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL.Class))
                .and((n, g) -> {
                    RDFList list = Iterators.findFirst(g.asGraph().find(n, OWL.oneOf.asNode(), Node.ANY).filterKeep(
                            it -> STDObjectFactories.RDF_LIST.canWrap(it.getObject(), g)
                    ).mapWith(it -> new RDFListImpl(it.getObject(), g))).orElse(null);
                    if (list == null) {
                        return false;
                    }
                    return !restrictOneOfToSingleIndividual || Iterators.takeAsSet(list.iterator(), 2).size() == 1;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createCardinalityRestrictionFactory(
            Class<? extends OntClassImpl.CardinalityRestrictionImpl<?, ?, ?>> impl,
            RestrictionType restrictionType,
            ObjectRestrictionType objectType,
            OntClassImpl.CardinalityType cardinalityType,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL.Restriction))
                .and(getCardinalityFilter(cardinalityType,
                        objectType.view(),
                        config.getBoolean(OntModelControls.USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE)))
                .and(restrictionType.getFilter());
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createComponentRestrictionFactory(
            Class<? extends OntClassImpl.ComponentRestrictionImpl<?, ?, ?>> impl,
            RestrictionType propertyType,
            ObjectRestrictionType objectType,
            Property predicate,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL.Restriction))
                .and(propertyType.getFilter())
                .and(objectType.getFilter(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createNaryRestrictionFactory(
            Class<? extends OntClassImpl.NaryRestrictionImpl<?, ?, ?>> impl,
            Property predicate) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL.Restriction);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(OWL.Restriction))
                .and(new EnhNodeFilter.HasPredicate(OWL.onProperties))
                .and(new EnhNodeFilter.HasPredicate(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    private static EnhNodeFilter getCardinalityFilter(
            OntClassImpl.CardinalityType type,
            Class<? extends RDFNode> objectType,
            boolean qualifiedCardinalityAllowed) {
        return (n, g) -> type.isNonQualified(n, g) || (qualifiedCardinalityAllowed && type.isQualified(n, g, objectType));
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
            this.propertyFactory = WrappedEnhNodeFactory.of(type);
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

    public static class HasSelfFilter implements EnhNodeFilter {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return g.asGraph().contains(n, OWL.hasSelf.asNode(), StdModels.TRUE.asNode());
        }
    }

    public static class HasSelfMaker extends EnhNodeProducer.WithType {
        public HasSelfMaker() {
            super(OntClassImpl.HasSelfImpl.class, OWL.Restriction);
        }

        @Override
        public void doInsert(Node node, EnhGraph eg) {
            super.doInsert(node, eg);
            eg.asGraph().add(Triple.create(node, OWL.hasSelf.asNode(), StdModels.TRUE.asNode()));
        }
    }

    /**
     * A factory to produce {@link OntClass}s of any types.
     * <p>
     * Created by @ssz on 01.09.2018.
     */
    @SuppressWarnings("WeakerAccess")
    static class Factory extends BaseEnhNodeFactoryImpl {

        private static final Implementation LIST_FACTORY = STDObjectFactories.RDF_LIST;
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
        private static final Node SUB_CLASS_OF = RDFS.subClassOf.asNode();
        private static final String NON_NEGATIVE_INTEGER_URI = XSD.nonNegativeInteger.getURI();

        private static final BiFunction<Node, EnhGraph, EnhNode> NAMED_CLASS_PRODUCER = OntSimpleClassImpl.NamedImpl::new;
        private static final BiFunction<Node, EnhGraph, EnhNode> GENERIC_CLASS_PRODUCER = OntSimpleClassImpl::new;
        private static final BiFunction<Node, EnhGraph, EnhNode> GENERIC_RESTRICTION_PRODUCER = OntClassImpl.RestrictionImpl::new;

        protected final EnhNodeFactory objectPropertyFactory = WrappedEnhNodeFactory.of(OntObjectProperty.class);
        protected final EnhNodeFactory dataPropertyFactory = WrappedEnhNodeFactory.of(OntDataProperty.class);

        private final boolean allowNamedClassExpressions;
        private final boolean allowQualifiedCardinalityRestrictions;
        private final boolean restrictOneOfToSingleIndividual;

        private final Set<Type> filters;
        private final BiPredicate<Node, EnhGraph> namedClassFilter;
        private final BiPredicate<Node, EnhGraph> genericClassFilter;

        private Factory(
                BiPredicate<Node, EnhGraph> namedClassFilter,
                BiPredicate<Node, EnhGraph> genericClassFilter,
                boolean allowNamedClassExpressions,
                boolean allowQualifiedCardinalityRestrictions,
                boolean restrictOneOfToSingleIndividual,
                List<Type> filters) {
            if (genericClassFilter != null && namedClassFilter == null) {
                throw new IllegalArgumentException();
            }
            this.namedClassFilter = namedClassFilter;
            this.genericClassFilter = genericClassFilter;
            this.allowNamedClassExpressions = allowNamedClassExpressions;
            this.allowQualifiedCardinalityRestrictions = allowQualifiedCardinalityRestrictions;
            this.restrictOneOfToSingleIndividual = restrictOneOfToSingleIndividual;
            this.filters = EnumSet.copyOf(filters);
        }

        private boolean isDataCardinality(Node n,
                                          EnhGraph eg,
                                          Node p,
                                          Node qp) {
            return isCardinality(n, eg, p) || (allowQualifiedCardinalityRestrictions
                    && isQualifiedCardinality(n, eg, qp, ON_DATA_RANGE, OntDataRange.class));
        }

        private boolean isObjectCardinality(Node n,
                                            EnhGraph eg,
                                            Node p,
                                            Node qp) {
            return isCardinality(n, eg, p) || (allowQualifiedCardinalityRestrictions
                    && isQualifiedCardinality(n, eg, qp, ON_CLASS, OntClass.class));
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
        private static RDFList getList(Node n, EnhGraph eg, Node p) {
            return Iterators.findFirst(listObjects(n, eg, p)
                            .filterKeep(it -> LIST_FACTORY.canWrap(it.getObject(), eg))
                            .mapWith(triple -> new RDFListImpl(triple.getObject(), eg)))
                    .orElse(null);
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
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            ExtendedIterator<EnhNode> byOWLClass = null;
            if (filterDeclaredClassExpressions()) {
                byOWLClass = eg.asGraph().find(ANY, RDF.Nodes.type, CLASS)
                        .mapWith(t -> {
                            Node n = t.getSubject();
                            if (namedClassFilter != null && n.isURI()) {
                                return namedClassFilter.test(n, eg) ? NAMED_CLASS_PRODUCER.apply(n, eg) : null;
                            }
                            if (genericClassFilter != null) {
                                return GENERIC_CLASS_PRODUCER.apply(n, eg);
                            }
                            BiFunction<Node, EnhGraph, EnhNode> res = null;
                            if ((!n.isURI() || allowNamedClassExpressions) && filterLogicalExpressions()) {
                                res = logicalExpressionFactory(n, eg);
                            }
                            if (res != null) {
                                return res.apply(n, eg);
                            }
                            return null;
                        })
                        .filterKeep(Objects::nonNull);
            }
            ExtendedIterator<EnhNode> byOWLRestriction = null;
            if (filterRestrictions()) {
                byOWLRestriction = eg.asGraph().find(ANY, RDF.Nodes.type, RESTRICTION)
                        .mapWith(t -> {
                            Node n = t.getSubject();
                            if (n.isURI() && !allowNamedClassExpressions) {
                                return null;
                            }
                            if (genericClassFilter != null) {
                                return GENERIC_RESTRICTION_PRODUCER.apply(n, eg);
                            }
                            BiFunction<Node, EnhGraph, EnhNode> res = restrictionFactory(n, eg);
                            if (res != null) {
                                return res.apply(n, eg);
                            }
                            return null;
                        })
                        .filterKeep(Objects::nonNull);
            }
            if (byOWLClass == null && byOWLRestriction == null) {
                return NullIterator.instance();
            }
            if (byOWLClass != null && byOWLRestriction != null) {
                return byOWLClass.andThen(byOWLRestriction);
            }
            if (byOWLClass == null) {
                return byOWLRestriction;
            }
            return byOWLClass;
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            BiFunction<Node, EnhGraph, EnhNode> f = map(node, eg);
            if (f == null) return null;
            return f.apply(node, eg);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return map(node, eg) != null;
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) throws OntJenaException {
            BiFunction<Node, EnhGraph, EnhNode> f = map(node, eg);
            if (f == null) {
                throw new OntJenaException.Conversion("Can't convert node " + node + " to Class Expression.");
            }
            EnhNode res = f.apply(node, eg);
            if (res == null) {
                throw new OntJenaException.IllegalState("Can't create Class Expression for node " + node);
            }
            return res;
        }

        private BiFunction<Node, EnhGraph, EnhNode> map(Node n, EnhGraph eg) {
            if (n.isLiteral()) {
                return null;
            }
            if (namedClassFilter != null && n.isURI()) {
                // fast check for entity-class - the most common case in OWL2
                return namedClassFilter.test(n, eg) ? NAMED_CLASS_PRODUCER : null;
            }
            Graph g = eg.asGraph();
            if (filterRestrictions() && g.contains(n, TYPE, RESTRICTION)) {
                if (n.isURI() && !allowNamedClassExpressions) {
                    return null;
                }
                return restrictionFactory(n, eg);
            }
            if (filterLogicalExpressions() && g.contains(n, TYPE, CLASS)) {
                BiFunction<Node, EnhGraph, EnhNode> res = null;
                if ((!n.isURI() || allowNamedClassExpressions) && filterLogicalExpressions()) {
                    res = logicalExpressionFactory(n, eg);
                }
                if (res != null) {
                    return res;
                }
                if (genericClassFilter != null) {
                    // can’t recognize what kind of class this is,
                    // for compatibility reasons (jena OntModel) we return a “generic” factory
                    return GENERIC_CLASS_PRODUCER;
                }
                return null;
            }
            if (genericClassFilter != null) {
                return genericClassFilter.test(n, eg) ? GENERIC_CLASS_PRODUCER : null;
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> restrictionFactory(Node n, EnhGraph eg) {
            if (filterUnaryRestrictions()) {
                BiFunction<Node, EnhGraph, EnhNode> res = unaryRestrictionFactory(n, eg);
                if (res != null) {
                    return res;
                }
            }
            if (filterNaryRestrictions() && eg.asGraph().contains(n, ON_PROPERTIES, ANY)) {
                // very simplified factories for nary-restrictions:
                if (filters.contains(Type.DATA_NARY_SOME_VALUES_FROM)
                        && Iterators.findFirst(listObjects(n, eg, SOME_VALUES_FROM)).isPresent()) {
                    return Type.DATA_NARY_SOME_VALUES_FROM;
                }
                if (filters.contains(Type.DATA_NARY_ALL_VALUES_FROM)
                        && Iterators.findFirst(listObjects(n, eg, ALL_VALUES_FROM)).isPresent()) {
                    return Type.DATA_NARY_ALL_VALUES_FROM;
                }
            }
            if (genericClassFilter != null) {
                // can’t recognize what kind of Restriction this is,
                // for compatibility reasons (jena OntModel) we return a “generic” factory
                return GENERIC_RESTRICTION_PRODUCER;
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> unaryRestrictionFactory(Node n, EnhGraph eg) {
            ExtendedIterator<Node> props = listObjects(n, eg, ON_PROPERTY).mapWith(Triple::getObject);
            try {
                boolean onPropertyFound = false;
                while (props.hasNext()) {
                    onPropertyFound = true;
                    Node p = props.next();
                    if (filterObjectUnaryRestrictions() && objectPropertyFactory.canWrap(p, eg)) {
                        if (filters.contains(Type.OBJECT_SOME_VALUES_FROM)
                                && isObjectOfType(n, eg, SOME_VALUES_FROM, OntClass.class)) {
                            return Type.OBJECT_SOME_VALUES_FROM;
                        }
                        if (filters.contains(Type.OBJECT_ALL_VALUES_FROM)
                                && isObjectOfType(n, eg, ALL_VALUES_FROM, OntClass.class)) {
                            return Type.OBJECT_ALL_VALUES_FROM;
                        }
                        if (filters.contains(Type.OBJECT_HAS_VALUE)
                                && isObjectOfType(n, eg, HAS_VALUE, OntIndividual.class)) {
                            return Type.OBJECT_HAS_VALUE;
                        }
                        if (filters.contains(Type.OBJECT_MIN_CARDINALITY)
                                && isObjectCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                            return Type.OBJECT_MIN_CARDINALITY;
                        }
                        if (filters.contains(Type.OBJECT_MAX_CARDINALITY)
                                && isObjectCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                            return Type.OBJECT_MAX_CARDINALITY;
                        }
                        if (filters.contains(Type.OBJECT_EXACT_CARDINALITY)
                                && isObjectCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                            return Type.OBJECT_EXACT_CARDINALITY;
                        }
                        if (filters.contains(Type.OBJECT_HAS_SELF)
                                && Iterators.findFirst(listObjects(n, eg, HAS_SELF)
                                .filterKeep(x -> TRUE.equals(x.getObject()))).isPresent()) {
                            return Type.OBJECT_HAS_SELF;
                        }
                    }
                    if (filterDataUnaryRestrictions() && dataPropertyFactory.canWrap(p, eg)) {
                        if (filters.contains(Type.DATA_SOME_VALUES_FROM)
                                && isObjectOfType(n, eg, SOME_VALUES_FROM, OntDataRange.class)) {
                            return Type.DATA_SOME_VALUES_FROM;
                        }
                        if (filters.contains(Type.DATA_ALL_VALUES_FROM)
                                && isObjectOfType(n, eg, ALL_VALUES_FROM, OntDataRange.class)) {
                            return Type.DATA_ALL_VALUES_FROM;
                        }
                        if (filters.contains(Type.DATA_HAS_VALUE)
                                && Iterators.findFirst(listObjects(n, eg, HAS_VALUE)
                                .filterKeep(x -> x.getObject().isLiteral())).isPresent()) {
                            return Type.DATA_HAS_VALUE;
                        }
                        if (filters.contains(Type.DATA_MIN_CARDINALITY)
                                && isDataCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                            return Type.DATA_MIN_CARDINALITY;
                        }
                        if (filters.contains(Type.DATA_MAX_CARDINALITY)
                                && isDataCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                            return Type.DATA_MAX_CARDINALITY;
                        }
                        if (filters.contains(Type.DATA_EXACT_CARDINALITY)
                                && isDataCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                            return Type.DATA_EXACT_CARDINALITY;
                        }
                    }
                }
                if (onPropertyFound && genericClassFilter != null) {
                    // can’t recognize what kind of unary Restriction this is,
                    // for compatibility reasons (jena OntModel) we return a “generic” factory
                    return GENERIC_RESTRICTION_PRODUCER;
                }
            } finally {
                props.close();
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> logicalExpressionFactory(Node n, EnhGraph eg) {
            // first check owl:complementOf, since it is more accurately defined
            if (filters.contains(Type.COMPLEMENT_OF) && isObjectOfType(n, eg, COMPLEMENT_OF, OntClass.class)) {
                return Type.COMPLEMENT_OF;
            }
            if (filters.contains(Type.INTERSECTION_OF) && isList(n, eg, INTERSECTION_OF)) {
                return Type.INTERSECTION_OF;
            }
            if (filters.contains(Type.UNION_OF) && isList(n, eg, UNION_OF)) {
                return Type.UNION_OF;
            }
            if (filters.contains(Type.ONE_OF)) {
                RDFList list = getList(n, eg, ONE_OF);
                if (list != null && (!restrictOneOfToSingleIndividual || Iterators.takeAsSet(list.iterator(), 2).size() == 1)) {
                    return Type.ONE_OF;
                }
            }
            return null;
        }

        private boolean filterDeclaredClassExpressions() {
            return namedClassFilter != null || filterLogicalExpressions();
        }

        private boolean filterLogicalExpressions() {
            return filters.contains(Type.COMPLEMENT_OF) || filterCollectionOfExpressions();
        }

        private boolean filterCollectionOfExpressions() {
            return filters.contains(Type.UNION_OF) || filters.contains(Type.INTERSECTION_OF) || filters.contains(Type.ONE_OF);
        }

        private boolean filterRestrictions() {
            return filterUnaryRestrictions() || filterNaryRestrictions();
        }

        private boolean filterUnaryRestrictions() {
            return filterObjectUnaryRestrictions() || filterDataUnaryRestrictions() || filters.contains(Type.OBJECT_HAS_SELF);
        }

        private boolean filterObjectUnaryRestrictions() {
            return filters.contains(Type.OBJECT_ALL_VALUES_FROM)
                    || filters.contains(Type.OBJECT_SOME_VALUES_FROM)
                    || filters.contains(Type.OBJECT_HAS_VALUE)
                    || filters.contains(Type.OBJECT_MIN_CARDINALITY)
                    || filters.contains(Type.OBJECT_EXACT_CARDINALITY)
                    || filters.contains(Type.OBJECT_MAX_CARDINALITY)
                    || filters.contains(Type.OBJECT_HAS_SELF);
        }

        private boolean filterDataUnaryRestrictions() {
            return filters.contains(Type.DATA_ALL_VALUES_FROM)
                    || filters.contains(Type.DATA_SOME_VALUES_FROM)
                    || filters.contains(Type.DATA_HAS_VALUE)
                    || filters.contains(Type.DATA_MIN_CARDINALITY)
                    || filters.contains(Type.DATA_EXACT_CARDINALITY)
                    || filters.contains(Type.DATA_MAX_CARDINALITY);
        }

        private boolean filterNaryRestrictions() {
            return filters.contains(Type.DATA_NARY_ALL_VALUES_FROM) || filters.contains(Type.DATA_SOME_VALUES_FROM);
        }
    }

    public enum Type implements BiFunction<Node, EnhGraph, EnhNode> {
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

        UNION_OF(OntClass.UnionOf.class),
        INTERSECTION_OF(OntClass.IntersectionOf.class),
        ONE_OF(OntClass.OneOf.class),
        COMPLEMENT_OF(OntClass.ComplementOf.class),
        ;

        private final Class<? extends OntObject> type;

        Type(Class<? extends OntObject> type) {
            this.type = type;
        }

        EnhNodeFactory factory() {
            return WrappedEnhNodeFactory.of(type);
        }

        @Override
        public EnhNode apply(Node node, EnhGraph enhGraph) {
            return factory().createInstance(node, enhGraph);
        }
    }
}
