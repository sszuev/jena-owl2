package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.impl.HierarchySupport;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.StdModels;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A base class for any class-expression implementation.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntClassImpl extends OntObjectImpl implements OntClass {

    public OntClassImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static boolean isQualified(OntObject c) {
        return c != null &&
                OntGraphModelImpl.configValue(c.getModel(), OntModelControls.USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE)
                && !(OWL.Thing.equals(c) || RDFS.Literal.equals(c));
    }

    protected static CardinalityType getCardinalityType(Class<? extends CardinalityRestriction<?, ?>> view) {
        if (ObjectMinCardinality.class.equals(view) || DataMinCardinality.class.equals(view)) {
            return CardinalityType.MIN;
        }
        if (ObjectMaxCardinality.class.equals(view) || DataMaxCardinality.class.equals(view)) {
            return CardinalityType.MAX;
        }
        return CardinalityType.EXACTLY;
    }

    protected static Literal createNonNegativeIntegerLiteral(int n) {
        if (n < 0) throw new OntJenaException.IllegalArgument("Can't accept negative value: " + n);
        return ResourceFactory.createTypedLiteral(String.valueOf(n), XSDDatatype.XSDnonNegativeInteger);
    }

    private static Resource createRestriction(OntModel model) {
        return model.createResource(OWL.Restriction);
    }

    protected static Resource createOnPropertyRestriction(OntGraphModelImpl model, OntProperty onProperty) {
        OntJenaException.notNull(onProperty, "Null property.");
        return createRestriction(model).addProperty(OWL.onProperty, onProperty);
    }

    public static <CE extends ComponentRestriction<?, ?>> CE createComponentRestrictionCE(OntGraphModelImpl model,
                                                                                          Class<CE> view,
                                                                                          OntProperty onProperty,
                                                                                          RDFNode other,
                                                                                          Property predicate) {
        OntJenaException.notNull(other, "Null expression.");
        Resource res = createOnPropertyRestriction(model, onProperty).addProperty(predicate, other);
        return model.getNodeAs(res.asNode(), view);
    }

    public static <CE extends CardinalityRestriction<?, ?>> CE createCardinalityRestrictionCE(OntGraphModelImpl model,
                                                                                              Class<CE> view,
                                                                                              OntProperty onProperty,
                                                                                              int cardinality,
                                                                                              OntObject object) {
        Literal value = createNonNegativeIntegerLiteral(cardinality);
        Resource res = createOnPropertyRestriction(model, onProperty);
        boolean qualified = isQualified(object);
        model.add(res, getCardinalityType(view).getPredicate(qualified), value);
        if (qualified) {
            model.add(res, onProperty instanceof OntObjectProperty ? OWL.onClass : OWL.onDataRange, object);
        }
        return model.getNodeAs(res.asNode(), view);
    }

    public static <CE extends NaryRestriction<?, ?>> CE createNaryRestrictionCE(OntGraphModelImpl model,
                                                                                Class<CE> type,
                                                                                OntDataRange dr,
                                                                                Collection<OntDataProperty> properties) {
        NaryRestrictionImpl.validateArity(dr, properties);
        Property predicate = NaryDataAllValuesFrom.class.equals(type) ? OWL.allValuesFrom : OWL.someValuesFrom;
        Resource res = createRestriction(model)
                .addProperty(predicate, dr)
                .addProperty(OWL.onProperties, model.createList(properties.iterator()));
        return model.getNodeAs(res.asNode(), type);
    }

    public static <CE extends CollectionOf<?>, R extends OntObject> CE createComponentsCE(OntGraphModelImpl model,
                                                                                          Class<CE> returnType,
                                                                                          Class<R> componentType,
                                                                                          Property predicate,
                                                                                          Stream<R> components) {
        OntJenaException.notNull(components, "Null components stream.");
        RDFList items = model.createList(components
                .peek(x -> OntJenaException.notNull(x,
                        OntEnhNodeFactories.viewAsString(returnType) + ": null " + OntEnhNodeFactories.viewAsString(componentType) + " member"))
                .iterator());
        Resource res = model.createResource(OWL.Class).addProperty(predicate, items);
        return model.getNodeAs(res.asNode(), returnType);
    }

    public static HasSelf createHasSelf(OntGraphModelImpl model, OntObjectProperty onProperty) {
        Resource res = createOnPropertyRestriction(model, onProperty).addProperty(OWL.hasSelf, StdModels.TRUE);
        return model.getNodeAs(res.asNode(), HasSelf.class);
    }

    public static ComplementOf createComplementOf(OntGraphModelImpl model, OntClass other) {
        OntJenaException.notNull(other, "Null class expression.");
        Resource res = model.createResource(OWL.Class).addProperty(OWL.complementOf, other);
        return model.getNodeAs(res.asNode(), ComplementOf.class);
    }

    public static OntIndividual.Anonymous createAnonymousIndividual(OntGraphModelImpl model, OntClass source) {
        OntGraphModelImpl.checkFeature(model, OntModelControls.ALLOW_ANONYMOUS_INDIVIDUALS, "anonymous-individuals");
        OntJenaException.checkSupported(source.asAssertionClass() != null,
                "Class " + OntEnhNodeFactories.viewAsString(source.getClass()) + " cannot have individuals. " +
                        "Profile: " + model.getOntPersonality().getName());
        return model.getNodeAs(model.createResource(source).asNode(), OntIndividual.Anonymous.class);
    }

    public static OntIndividual.Named createNamedIndividual(OntGraphModelImpl model, OntClass source, String uri) {
        OntJenaException.notNull(uri, "Null uri");
        OntJenaException.checkSupported(source.asAssertionClass() != null,
                "Class " + OntEnhNodeFactories.viewAsString(source.getClass()) + " cannot have individuals. " +
                        "Profile: " + model.getOntPersonality().getName());
        Resource res = model.createResource(uri, source);
        if (OntGraphModelImpl.configValue(model, OntModelControls.USE_OWL2_NAMED_INDIVIDUAL_DECLARATION_FEATURE)) {
            res.addProperty(RDF.type, OWL.NamedIndividual);
        }
        return res.as(OntIndividual.Named.class);
    }

    public static OntList<OntRelationalProperty> createHasKey(OntGraphModelImpl m,
                                                              OntClass clazz,
                                                              Stream<? extends OntRelationalProperty> collection) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE, "owl:hasKey");
        return m.createOntList(clazz, OWL.hasKey, OntRelationalProperty.class,
                collection.distinct().map(OntRelationalProperty.class::cast).iterator());
    }

    public static Stream<OntList<OntRelationalProperty>> listHasKeys(OntGraphModelImpl m, OntClass clazz) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE)) {
            return Stream.empty();
        }
        return OntListImpl.stream(m, clazz, OWL.hasKey, OntRelationalProperty.class);
    }

    public static void removeHasKey(OntGraphModelImpl m,
                                    OntClass clazz,
                                    RDFNode rdfList) throws OntJenaException.IllegalArgument {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE, "owl:hasKey");
        m.deleteOntList(clazz, OWL.hasKey, clazz.findHasKey(rdfList).orElse(null));
    }

    public static Stream<OntClass> disjointClasses(OntGraphModelImpl m, OntClass clazz) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE)) {
            return Stream.empty();
        }
        if (clazz.asDisjointClass() == null) {
            return Stream.empty();
        }
        return Stream.of(
                        clazz.objects(OWL.disjointWith, OntClass.class),
                        m.statements(null, OWL.disjointWith, clazz)
                                .filter(it -> it.getSubject().canAs(OntClass.class))
                                .map(it -> it.getSubject(OntClass.class)),
                        clazz.disjoints().flatMap(OntDisjoint::members)
                )
                .flatMap(it -> it)
                .filter(it -> !it.equals(clazz))
                .map(OntClass::asDisjointClass)
                .filter(Objects::nonNull)
                .distinct();
    }

    public static void addDisjoint(OntGraphModelImpl m, OntClass clazz, OntClass other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE, "owl:disjointWith");
        OntJenaException.checkSupported(clazz.asDisjointClass() != null && other.asDisjointClass() != null,
                "Classes " + OntEnhNodeFactories.viewAsString(clazz.getClass()) + " and " + OntEnhNodeFactories.viewAsString(other.getClass()) +
                        " cannot be disjoint. Profile " + m.getOntPersonality().getName()
        );
        clazz.addStatement(OWL.disjointWith, other);
    }

    public static void removeDisjoint(OntGraphModelImpl m, OntClass clazz, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE, "owl:disjointWith");
        Objects.requireNonNull(clazz).remove(OWL.disjointWith, other);
    }

    public static OntStatement addDisjointWithStatement(OntGraphModelImpl m, OntClass clazz, OntClass other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE, "owl:disjointWith");
        OntJenaException.checkSupported(clazz.asDisjointClass() != null && other.asDisjointClass() != null,
                "Classes " + OntEnhNodeFactories.viewAsString(clazz.getClass()) + " and " + OntEnhNodeFactories.viewAsString(other.getClass()) +
                        " cannot be disjoint. Profile " + m.getOntPersonality().getName()
        );
        return clazz.addStatement(OWL.disjointWith, other);
    }

    public static Stream<OntClass> equivalentClasses(OntGraphModelImpl m, OntClass clazz) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE)) {
            return Stream.empty();
        }
        if (clazz.asEquivalentClass() == null) {
            return Stream.empty();
        }
        return Stream.of(clazz.objects(OWL.equivalentClass, OntClass.class),
                        m.statements(null, OWL.equivalentClass, clazz)
                                .filter(it -> it.getSubject().canAs(OntClass.class))
                                .map(it -> it.getSubject(OntClass.class))
                )
                .flatMap(it -> it)
                .filter(it -> !it.equals(clazz))
                .map(OntClass::asEquivalentClass)
                .filter(Objects::nonNull)
                .distinct();
    }

    public static OntStatement addEquivalentClass(OntGraphModelImpl m, OntClass clazz, OntClass other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        OntJenaException.checkSupported(clazz.asEquivalentClass() != null && other.asEquivalentClass() != null,
                "Classes " + OntEnhNodeFactories.viewAsString(clazz.getClass()) + " and " + OntEnhNodeFactories.viewAsString(other.getClass()) +
                        " cannot be equivalent. Profile " + m.getOntPersonality().getName()
        );
        return clazz.addStatement(OWL.equivalentClass, other);
    }

    public static void removeEquivalentClass(OntGraphModelImpl m, OntClass clazz, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        clazz.remove(OWL.equivalentClass, other);
    }

    public static Stream<OntProperty> declaredProperties(OntClass clazz, boolean direct) {
        OntModel m = clazz.getModel();
        Stream<OntProperty> properties = OntPersonalities.isRDFS(OntEnhGraph.asPersonalityModel(m).getOntPersonality())
                ? m.ontObjects(OntProperty.class)
                : Stream.of(m.objectProperties(), m.dataProperties(), m.annotationProperties()).flatMap(it -> it);
        Map<OntClass, Set<OntClass>> indirectSuperclasses = new HashMap<>();
        Function<OntClass, Set<OntClass>> getIndirectSuperclasses = it ->
                indirectSuperclasses.computeIfAbsent(it, x -> x.superClasses(false).collect(Collectors.toSet()));
        return properties.distinct().filter(p -> p != null && testDomain(clazz, p, direct, getIndirectSuperclasses));
    }

    /**
     * Answers {@code true} if the {@code clazz} is a domain of {@code property}.
     *
     * @param clazz    {@link OntClass}
     * @param property {@link OntProperty}
     * @param direct   {@code boolean}
     * @return {@code boolean}
     */
    public static boolean testDomain(OntClass clazz, OntProperty property, boolean direct) {
        return testDomain(clazz, property, direct, x -> x.superClasses(false).collect(Collectors.toSet()));
    }

    protected static boolean testDomain(OntClass clazz,
                                        OntProperty property,
                                        boolean direct,
                                        Function<OntClass, Set<OntClass>> getIndirectSuperClasses) {
        if (isReservedOrBuiltin(property)) {
            return false;
        }
        AtomicBoolean isGlobal = new AtomicBoolean(true);
        AtomicBoolean seenDirect = new AtomicBoolean(false);
        try (Stream<? extends Resource> domains = property.domains()) {
            if (!domains.allMatch(domain -> {
                if (domain.equals(OWL.Thing) || domain.equals(RDFS.Resource)) {
                    // there are some well-known values we ignore
                    return true;
                }
                isGlobal.set(false);
                if (clazz.equals(domain)) {
                    // if this class is actually in the domain (as opposed to one of this class's
                    // superclasses), then we've detected the direct property case
                    seenDirect.set(true);
                } else {
                    // there is a class in the domain of p that is not a superclass of this class
                    return canProveSuperClass(clazz, domain, getIndirectSuperClasses);
                }
                return true;
            })) {
                return false;
            }
            if (direct) {
                // if we're looking for direct props, we must either have seen the direct case
                // or it's a global prop and this is a root class
                return seenDirect.get() || (isGlobal.get() && clazz.isHierarchyRoot());
            }
            // not direct, we must either found a global or a superclass prop
            // otherwise the 'return false' above would have kicked in
            return true;
        }
    }

    /**
     * Answers true
     * if we can demonstrate
     * that the class specified as first parameter class has the second parameter as a superclass.
     * If this model has a reasoner, this is equivalent to asking if the subclass relation holds.
     * Otherwise, we simulate basic reasoning by searching upwards through the class hierarchy.
     *
     * @param clazz                   potential subclass
     * @param candidate               for superclass of {@code clazz}
     * @param getIndirectSuperClasses a function to get indirect superclasses
     * @return {@code true} if we can show that {@code candidate} is a superclass of {@code clazz}
     */
    protected static boolean canProveSuperClass(OntClass clazz,
                                                Resource candidate,
                                                Function<OntClass, Set<OntClass>> getIndirectSuperClasses) {
        Set<OntClass> seen = new HashSet<>();
        Deque<OntClass> queue = new ArrayDeque<>();
        queue.add(clazz);
        while (!queue.isEmpty()) {
            OntClass current = queue.removeFirst();
            if (seen.contains(current)) {
                continue;
            }
            seen.add(current);
            if (current.equals(candidate)) {
                return true;
            }
            for (OntClass next : getIndirectSuperClasses.apply(current)) {
                if (next.equals(candidate)) {
                    return true;
                }
                queue.add(next);
            }
        }
        return false;
    }

    public static boolean isHierarchyRoot(OntClass clazz) {
        if (OWL.Nothing.equals(clazz)) {
            return false;
        }
        try (Stream<OntClass> superClasses = clazz.superClasses(true)) {
            return superClasses.allMatch(s -> s.equals(OWL.Thing) || s.equals(RDFS.Resource) || s.equals(clazz));
        }
    }

    public static boolean isDisjoint(OntClass clazz, Resource candidate) {
        if (!candidate.canAs(OntClass.class)) {
            return false;
        }
        OntClass other = candidate.as(OntClass.class);
        try (Stream<OntClass> disjoints = other.disjointClasses()) {
            if (disjoints.anyMatch(clazz::equals)) {
                return true;
            }
        }
        try (Stream<OntClass> disjoints = clazz.disjointClasses()) {
            if (disjoints.anyMatch(other::equals)) {
                return true;
            }
        }
        try (Stream<OntDisjoint.Classes> disjoints = clazz.getModel().ontObjects(OntDisjoint.Classes.class)) {
            return disjoints.anyMatch(d -> {
                Set<OntClass> members = d.members().collect(Collectors.toSet());
                return members.contains(clazz) && members.contains(other);
            });
        }
    }

    static Stream<OntIndividual> individuals(OntClass clazz, boolean direct) {
        if (clazz.asAssertionClass() == null) {
            return Stream.empty();
        }
        if (OntGraphModelImpl.configValue(clazz.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)) {
            // TODO: optimize
            return clazz.getModel().individuals().filter(i -> i.hasOntClass(clazz, direct));
        }
        return subjects(RDF.type, clazz, OntIndividual.class).filter(i -> i.hasOntClass(clazz, direct));
    }

    public static Stream<OntClass> subClasses(OntClass clazz, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(clazz.getModel(), RDFS.subClassOf);
            if (reasonerProperty != null) {
                return explicitSubClasses(reasonerProperty, clazz).filter(x -> !clazz.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                clazz,
                it -> explicitSubClasses(RDFS.subClassOf, it),
                direct,
                OntGraphModelImpl.configValue(clazz.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static Stream<OntClass> superClasses(OntClass clazz, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(clazz.getModel(), RDFS.subClassOf);
            if (reasonerProperty != null) {
                return explicitSuperClasses(reasonerProperty, clazz).filter(x -> !clazz.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                clazz,
                it -> explicitSuperClasses(RDFS.subClassOf, it),
                direct,
                OntGraphModelImpl.configValue(clazz.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    static Stream<OntClass> explicitSuperClasses(Property predicate, OntObject clazz) {
        return clazz.objects(predicate, OntClass.class).map(OntClass::asSuperClass).filter(Objects::nonNull);
    }

    static Stream<OntClass> explicitSubClasses(OntClass clazz) {
        return explicitSubClasses(RDFS.subClassOf, clazz);
    }

    static Stream<OntClass> explicitSubClasses(Property predicate, OntClass clazz) {
        return subjects(predicate, clazz, OntClass.class).map(OntClass::asSubClass).filter(Objects::nonNull);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, OWL.Class);
    }

    @Override
    public abstract Class<? extends OntClass> objectType();

    @Override
    public OntIndividual.Anonymous createIndividual() {
        return createAnonymousIndividual(getModel(), this);
    }

    @Override
    public OntIndividual.Named createIndividual(String uri) {
        return createNamedIndividual(getModel(), this, uri);
    }

    @Override
    public OntList<OntRelationalProperty> createHasKey(Collection<OntObjectProperty> ope, Collection<OntDataProperty> dpe) {
        return createHasKey(getModel(), this, Stream.of(ope, dpe).flatMap(Collection::stream));
    }

    @Override
    public OntStatement addHasKeyStatement(OntRelationalProperty... properties) {
        return createHasKey(getModel(), this, Arrays.stream(properties)).getMainStatement();
    }

    @Override
    public Stream<OntList<OntRelationalProperty>> hasKeys() {
        return listHasKeys(getModel(), this);
    }

    @Override
    public OntClassImpl removeHasKey(Resource list) throws OntJenaException.IllegalArgument {
        removeHasKey(getModel(), this, list);
        return this;
    }

    @Override
    public boolean isDisjoint(Resource candidate) {
        return isDisjoint(this, candidate);
    }

    @Override
    public Stream<OntClass> disjointClasses() {
        return disjointClasses(getModel(), this);
    }

    @Override
    public OntClass addDisjointClass(OntClass other) {
        addDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public OntStatement addDisjointWithStatement(OntClass other) {
        return addDisjointWithStatement(getModel(), this, other);
    }

    @Override
    public OntClass removeDisjointClass(Resource other) {
        removeDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public Stream<OntClass> equivalentClasses() {
        return equivalentClasses(getModel(), this);
    }

    @Override
    public OntStatement addEquivalentClassStatement(OntClass other) {
        return addEquivalentClass(getModel(), this, other);
    }

    @Override
    public OntClass removeEquivalentClass(Resource other) {
        removeEquivalentClass(getModel(), this, other);
        return this;
    }

    @Override
    public Stream<OntClass> superClasses(boolean direct) {
        return superClasses(this, direct);
    }

    @Override
    public Stream<OntClass> subClasses(boolean direct) {
        return subClasses(this, direct);
    }

    @Override
    public Stream<OntIndividual> individuals(boolean direct) {
        return individuals(this, direct);
    }

    @Override
    public boolean hasDeclaredProperty(OntProperty property, boolean direct) {
        return testDomain(this, property, direct);
    }

    @Override
    public Stream<OntProperty> declaredProperties(boolean direct) {
        return declaredProperties(this, direct);
    }

    @Override
    public boolean isHierarchyRoot() {
        return isHierarchyRoot(this);
    }

    public enum CardinalityType {
        EXACTLY(OWL.qualifiedCardinality, OWL.cardinality),
        MAX(OWL.maxQualifiedCardinality, OWL.maxCardinality),
        MIN(OWL.minQualifiedCardinality, OWL.minCardinality);
        static final RDFDatatype NON_NEGATIVE_INTEGER = XSDDatatype.XSDnonNegativeInteger;
        static final Node CLASS_REFERENCE = OWL.onClass.asNode();
        static final Node RANGE_REFERENCE = OWL.onDataRange.asNode();
        private final Property qualifiedPredicate, predicate;

        CardinalityType(Property qualifiedPredicate, Property predicate) {
            this.qualifiedPredicate = qualifiedPredicate;
            this.predicate = predicate;
        }

        private static boolean isObjectOfType(EnhGraph g, Node n, Class<? extends RDFNode> type) {
            return OntEnhGraph.canAs(type, n, g);
        }

        public static boolean isNonNegativeInteger(Node n) {
            return n.isLiteral() && NON_NEGATIVE_INTEGER.equals(n.getLiteralDatatype());
        }

        public Property getPredicate(boolean isQualified) {
            return isQualified ? qualifiedPredicate : predicate;
        }

        public boolean isQualified(Node s, EnhGraph g, Class<? extends RDFNode> objectType) {
            if (!hasCardinality(s, qualifiedPredicate, g)) {
                return false;
            }
            Node p;
            if (objectType == OntClass.class) {
                p = CLASS_REFERENCE;
            } else if (objectType == OntDataRange.class) {
                p = RANGE_REFERENCE;
            } else {
                return false;
            }
            return Iterators.findFirst(g.asGraph().find(s, p, Node.ANY)
                    .filterKeep(t -> isObjectOfType(g, t.getObject(), objectType))).isPresent();
        }

        public boolean isNonQualified(Node s, EnhGraph g) {
            return hasCardinality(s, predicate, g);
        }

        private boolean hasCardinality(Node s, Property p, EnhGraph g) {
            return Iterators.findFirst(g.asGraph().find(s, p.asNode(), Node.ANY)
                    .filterKeep(t -> isNonNegativeInteger(t.getObject()))).isPresent();
        }
    }

    public static class QLObjectSomeValuesFromImpl extends ObjectSomeValuesFromImpl {
        public QLObjectSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return OWL.Thing.equals(getValue()) ? this : null;
        }

        @Override
        public OntClass asSuperClass() {
            return getValue().isURIResource() ? this : null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return asSubClass();
        }

        @Override
        public OntClass asDisjointClass() {
            return asSubClass();
        }

        @Override
        public OntClass asAssertionClass() {
            return null;
        }
    }

    public static class RLObjectSomeValuesFromImpl extends ObjectSomeValuesFromImpl {
        public RLObjectSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSuperClass() {
            return null;
        }
    }

    public static class ObjectSomeValuesFromImpl
            extends ComponentRestrictionImpl<OntClass, OntObjectProperty, ObjectSomeValuesFromImpl> implements ObjectSomeValuesFrom {
        public ObjectSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.someValuesFrom, OntClass.class, OntObjectProperty.class);
        }

        @Override
        public Class<ObjectSomeValuesFrom> objectType() {
            return ObjectSomeValuesFrom.class;
        }
    }

    public static class QLDataSomeValuesFromImpl extends DataSomeValuesFromImpl {
        public QLDataSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asAssertionClass() {
            return null;
        }
    }

    public static class RLDataSomeValuesFromImpl extends DataSomeValuesFromImpl {

        public RLDataSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSuperClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

    }

    public static class DataSomeValuesFromImpl
            extends ComponentRestrictionImpl<OntDataRange, OntDataProperty, DataSomeValuesFromImpl> implements DataSomeValuesFrom {
        public DataSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.someValuesFrom, OntDataRange.class, OntDataProperty.class);
        }

        @Override
        public Class<DataSomeValuesFrom> objectType() {
            return DataSomeValuesFrom.class;
        }
    }

    public static class RLObjectAllValuesFromImpl extends ObjectAllValuesFromImpl {

        public RLObjectAllValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }
    }

    public static class ObjectAllValuesFromImpl
            extends ComponentRestrictionImpl<OntClass, OntObjectProperty, ObjectAllValuesFromImpl> implements ObjectAllValuesFrom {
        public ObjectAllValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.allValuesFrom, OntClass.class, OntObjectProperty.class);
        }

        @Override
        public Class<ObjectAllValuesFrom> objectType() {
            return ObjectAllValuesFrom.class;
        }
    }

    public static class RLDataAllValuesFromImpl extends DataAllValuesFromImpl {

        public RLDataAllValuesFromImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }
    }

    public static class DataAllValuesFromImpl
            extends ComponentRestrictionImpl<OntDataRange, OntDataProperty, DataAllValuesFromImpl> implements DataAllValuesFrom {
        public DataAllValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.allValuesFrom, OntDataRange.class, OntDataProperty.class);
        }

        @Override
        public Class<DataAllValuesFrom> objectType() {
            return DataAllValuesFrom.class;
        }
    }

    public static class ObjectHasValueImpl
            extends ComponentRestrictionImpl<OntIndividual, OntObjectProperty, ObjectHasValueImpl> implements ObjectHasValue {
        public ObjectHasValueImpl(Node n, EnhGraph m) {
            super(n, m, OWL.hasValue, OntIndividual.class, OntObjectProperty.class);
        }

        @Override
        public Class<ObjectHasValue> objectType() {
            return ObjectHasValue.class;
        }
    }

    public static class DataHasValueImpl
            extends ComponentRestrictionImpl<Literal, OntDataProperty, DataHasValueImpl> implements DataHasValue {
        public DataHasValueImpl(Node n, EnhGraph m) {
            super(n, m, OWL.hasValue, Literal.class, OntDataProperty.class);
        }

        @Override
        public Class<DataHasValue> objectType() {
            return DataHasValue.class;
        }
    }

    public static class RLUnionOfImpl extends UnionOfImpl {
        public RLUnionOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSuperClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return asSubClass();
        }

        @Override
        public Stream<OntClass> components() {
            return getList().members().map(OntClass::asSubClass).filter(Objects::nonNull);
        }
    }

    public static class UnionOfImpl extends CollectionOfImpl<OntClass> implements UnionOf {
        public UnionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.unionOf, OntClass.class);
        }

        @Override
        public Class<UnionOf> objectType() {
            return UnionOf.class;
        }
    }

    public static class QLIntersectionOfImpl extends IntersectionOfImpl {
        public QLIntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asAssertionClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }

        @Override
        public Stream<OntClass> components() {
            return getList().members().map(OntClass::asSuperClass).filter(Objects::nonNull);
        }
    }

    /**
     * @see <a href="https://www.w3.org/TR/owl2-profiles/#Class_Expressions_3">RL: Class Expressions</a>
     */
    public static class RLIntersectionOfImpl extends IntersectionOfImpl {
        public RLIntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            Collection<OntClass> res = collectSubClasses();
            return res.isEmpty() ? null : new RLIntersectionOfImpl(this.node, this.enhGraph) {
                @Override
                public Stream<OntClass> components() {
                    return res.stream();
                }
            };
        }

        @Override
        public OntClass asSuperClass() {
            Collection<OntClass> res = collectSuperClasses();
            return res.isEmpty() ? null : new RLIntersectionOfImpl(this.node, this.enhGraph) {
                @Override
                public Stream<OntClass> components() {
                    return res.stream();
                }
            };
        }

        @Override
        public OntClass asEquivalentClass() {
            Collection<OntClass> res = collectEquivalentClasses();
            return res.isEmpty() ? null : new RLIntersectionOfImpl(this.node, this.enhGraph) {
                @Override
                public Stream<OntClass> components() {
                    return res.stream();
                }
            };
        }

        @Override
        public OntClass asAssertionClass() {
            return asSuperClass();
        }

        @Override
        public OntClass asDisjointClass() {
            return asSubClass();
        }

        private Collection<OntClass> collectSubClasses() {
            Set<OntClass> res = new LinkedHashSet<>();
            getList().members().forEach(it -> {
                OntClass sub = it.asSubClass();
                if (sub != null) {
                    res.add(sub);
                }
            });
            return res.size() > 1 ? res : List.of();
        }

        private Collection<OntClass> collectSuperClasses() {
            Set<OntClass> res = new LinkedHashSet<>();
            getList().members().forEach(it -> {
                OntClass sub = it.asSuperClass();
                if (sub != null) {
                    res.add(sub);
                }
            });
            return res.size() > 1 ? res : List.of();
        }

        private Collection<OntClass> collectEquivalentClasses() {
            Set<OntClass> res = new LinkedHashSet<>();
            getList().members().forEach(it -> {
                OntClass sub = it.asEquivalentClass();
                if (sub != null) {
                    res.add(sub);
                }
            });
            return res.size() > 1 ? res : List.of();
        }
    }

    public static class IntersectionOfImpl extends CollectionOfImpl<OntClass> implements IntersectionOf {
        public IntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.intersectionOf, OntClass.class);
        }

        @Override
        public Class<IntersectionOf> objectType() {
            return IntersectionOf.class;
        }
    }

    public static class RLOneOfImpl extends OneOfImpl {
        public RLOneOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSuperClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }
    }

    public static class OneOfImpl extends CollectionOfImpl<OntIndividual> implements OneOf {
        public OneOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.oneOf, OntIndividual.class);
        }

        @Override
        public Class<OneOf> objectType() {
            return OneOf.class;
        }
    }

    public static class DataMinCardinalityImpl
            extends CardinalityRestrictionImpl<OntDataRange, OntDataProperty, DataMinCardinalityImpl> implements DataMinCardinality {
        public DataMinCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onDataRange, OntDataRange.class, OntDataProperty.class, CardinalityType.MIN);
        }

        @Override
        public Class<DataMinCardinality> objectType() {
            return DataMinCardinality.class;
        }
    }

    public static class ObjectMinCardinalityImpl
            extends CardinalityRestrictionImpl<OntClass, OntObjectProperty, ObjectMinCardinalityImpl> implements ObjectMinCardinality {
        public ObjectMinCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onClass, OntClass.class, OntObjectProperty.class, CardinalityType.MIN);
        }

        @Override
        public Class<ObjectMinCardinality> objectType() {
            return ObjectMinCardinality.class;
        }
    }

    public static class RLDataMaxCardinalityImpl extends DataMaxCardinalityImpl {

        public RLDataMaxCardinalityImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }
    }

    public static class DataMaxCardinalityImpl
            extends CardinalityRestrictionImpl<OntDataRange, OntDataProperty, DataMaxCardinalityImpl> implements DataMaxCardinality {
        public DataMaxCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onDataRange, OntDataRange.class, OntDataProperty.class, CardinalityType.MAX);
        }

        @Override
        public Class<DataMaxCardinality> objectType() {
            return DataMaxCardinality.class;
        }
    }

    public static class RLObjectMaxCardinalityImpl extends ObjectMaxCardinalityImpl {

        public RLObjectMaxCardinalityImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }
    }

    public static class ObjectMaxCardinalityImpl
            extends CardinalityRestrictionImpl<OntClass, OntObjectProperty, ObjectMaxCardinalityImpl> implements ObjectMaxCardinality {
        public ObjectMaxCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onClass, OntClass.class, OntObjectProperty.class, CardinalityType.MAX);
        }

        @Override
        public Class<ObjectMaxCardinality> objectType() {
            return ObjectMaxCardinality.class;
        }
    }

    public static class DataCardinalityImpl
            extends CardinalityRestrictionImpl<OntDataRange, OntDataProperty, DataCardinalityImpl> implements DataCardinality {
        public DataCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onDataRange, OntDataRange.class, OntDataProperty.class, CardinalityType.EXACTLY);
        }

        @Override
        public Class<DataCardinality> objectType() {
            return DataCardinality.class;
        }
    }

    public static class ObjectCardinalityImpl
            extends CardinalityRestrictionImpl<OntClass, OntObjectProperty, ObjectCardinalityImpl> implements ObjectCardinality {
        public ObjectCardinalityImpl(Node n, EnhGraph m) {
            super(n, m, OWL.onClass, OntClass.class, OntObjectProperty.class, CardinalityType.EXACTLY);
        }

        @Override
        public Class<ObjectCardinality> objectType() {
            return ObjectCardinality.class;
        }
    }

    public static class HasSelfImpl extends OnPropertyRestrictionImpl<OntObjectProperty, HasSelfImpl> implements HasSelf {
        public HasSelfImpl(Node n, EnhGraph m) {
            super(n, m, OntObjectProperty.class);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(OWL.hasSelf));
        }

        @Override
        public Class<HasSelf> objectType() {
            return HasSelf.class;
        }
    }

    public static class RLQLComplementOfImpl extends ComplementOfImpl {
        public RLQLComplementOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntClass asSubClass() {
            return null;
        }

        @Override
        public OntClass asAssertionClass() {
            return null;
        }

        @Override
        public OntClass asEquivalentClass() {
            return null;
        }

        @Override
        public OntClass asDisjointClass() {
            return null;
        }
    }

    public static class ComplementOfImpl extends OntClassImpl implements ComplementOf {
        public ComplementOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(OWL.complementOf));
        }

        @Override
        public Class<ComplementOf> objectType() {
            return ComplementOf.class;
        }

        @Override
        public OntClass getValue() {
            return getRequiredObject(OWL.complementOf, OntClass.class);
        }

        @Override
        public ComplementOfImpl setValue(OntClass c) {
            Objects.requireNonNull(c, "Null component");
            clear();
            addProperty(OWL.complementOf, c);
            return this;
        }

        void clear() {
            removeAll(OWL.complementOf);
        }
    }

    public static class NaryDataAllValuesFromImpl
            extends NaryRestrictionImpl<OntDataRange, OntDataProperty, NaryDataAllValuesFromImpl> implements NaryDataAllValuesFrom {

        public NaryDataAllValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.allValuesFrom, OntDataRange.class, OntDataProperty.class);
        }

        @Override
        public Class<? extends OntClass> objectType() {
            return NaryDataAllValuesFrom.class;
        }
    }

    public static class NaryDataSomeValuesFromImpl
            extends NaryRestrictionImpl<OntDataRange, OntDataProperty, NaryDataSomeValuesFromImpl> implements NaryDataSomeValuesFrom {

        public NaryDataSomeValuesFromImpl(Node n, EnhGraph m) {
            super(n, m, OWL.someValuesFrom, OntDataRange.class, OntDataProperty.class);
        }

        @Override
        public Class<? extends OntClass> objectType() {
            return NaryDataSomeValuesFrom.class;
        }
    }

    /**
     * An abstract superclass for {@link IntersectionOf}, {@link OneOf}, {@link UnionOf}.
     *
     * @param <O> {@link OntObject}
     */
    protected static abstract class CollectionOfImpl<O extends OntObject>
            extends OntClassImpl implements CollectionOf<O> {
        protected final Property predicate;
        protected final Class<O> type;

        protected CollectionOfImpl(Node n, EnhGraph m, Property predicate, Class<O> type) {
            super(n, m);
            this.predicate = OntJenaException.notNull(predicate, "Null predicate.");
            this.type = OntJenaException.notNull(type, "Null view.");
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), getList().listContent());
        }

        @Override
        public OntListImpl<O> getList() {
            return getModel().asOntList(getRequiredObject(predicate, RDFList.class), this, predicate, true, null, type);
        }
    }

    /**
     * Base for all {@link Restriction} impls.
     */
    public static class RestrictionImpl extends OntClassImpl implements Restriction {

        public RestrictionImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getRequiredRootStatement(this, OWL.Restriction);
        }

        @Override
        public Class<? extends OntClass> objectType() {
            return OntClass.Restriction.class;
        }
    }

    /**
     * Abstract implementation for any restriction with {@code owl:onProperty} predicate.
     * Can be used instantiate directly if config settings allow.
     *
     * @param <P> a subtype of {@link OntRelationalProperty Data or Object Property Expression}
     * @param <R> return type for {@link OWL#onProperty} setter
     */
    public static class OnPropertyRestrictionImpl<P extends OntRelationalProperty, R extends OntClassImpl>
            extends RestrictionImpl implements UnaryRestriction<P> {
        protected final Class<P> propertyView;

        /**
         * @param n            {@link Node}
         * @param m            {@link EnhGraph}
         * @param propertyType Class-type for {@link OntRelationalProperty}
         */
        public OnPropertyRestrictionImpl(Node n, EnhGraph m, Class<P> propertyType) {
            super(n, m);
            this.propertyView = propertyType;
        }

        @Override
        public Class<? extends OntClass> objectType() {
            return OntClass.UnaryRestriction.class;
        }

        @Override
        public P getProperty() {
            return getRequiredObject(OWL.onProperty, propertyView);
        }

        @SuppressWarnings("unchecked")
        public R setProperty(P property) {
            Objects.requireNonNull(property, "Null " + OntEnhNodeFactories.viewAsString(propertyView));
            removeAll(OWL.onProperty).addProperty(OWL.onProperty, property);
            return (R) this;
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(OWL.onProperty));
        }
    }

    /**
     * Abstract base component-restriction class.
     * It's for CE which has owl:onProperty and some component also
     * (with predicate owl:dataRange, owl:onClass, owl:someValuesFrom, owl:allValuesFrom)
     *
     * @param <O> a class-type of {@link RDFNode rdf-node}
     * @param <P> a class-type of {@link OntRelationalProperty data or object property-expression}
     * @param <R> a subtype of {@link ComponentRestrictionImpl}
     */
    public static abstract class ComponentRestrictionImpl<O extends RDFNode,
            P extends OntRelationalProperty,
            R extends ComponentRestrictionImpl<?, ?, ?>>
            extends OnPropertyRestrictionImpl<P, R> implements ComponentRestriction<O, P> {
        protected final Property predicate;
        protected final Class<O> objectView;

        /**
         * @param n            Node
         * @param m            EnhGraph
         * @param predicate    predicate for value
         * @param objectView   Class
         * @param propertyView Class
         */
        protected ComponentRestrictionImpl(Node n,
                                           EnhGraph m,
                                           Property predicate,
                                           Class<O> objectView,
                                           Class<P> propertyView) {
            super(n, m, propertyView);
            this.predicate = OntJenaException.notNull(predicate, "Null predicate.");
            this.objectView = OntJenaException.notNull(objectView, "Null object view.");
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return listSpec(true);
        }

        protected ExtendedIterator<OntStatement> listSpec(boolean requireObject) {
            return requireObject ? Iterators.concat(super.listSpec(), listRequired(predicate)) : super.listSpec();
        }

        @Override
        public O getValue() {
            return getModel().getNodeAs(getRequiredProperty(predicate).getObject().asNode(), objectView);
        }

        @SuppressWarnings("unchecked")
        public R setValue(O c) {
            Objects.requireNonNull(c, "Null filler");
            removeAll(predicate).addProperty(predicate, c);
            return (R) this;
        }
    }

    /**
     * Abstraction for any cardinality restriction.
     *
     * @param <O> either {@link OntClass} (predicate {@link OWL#onClass owl:onClass}) or {@link OntDataRange}
     *            (predicate: {@link OWL#onDataRange owl:onDataRange})
     * @param <P> either {@link OntObjectProperty} or {@link OntDataProperty}
     * @param <R> subtype of {@link CardinalityRestrictionImpl}
     */
    public static abstract class CardinalityRestrictionImpl<O extends OntObject,
            P extends OntRelationalProperty,
            R extends CardinalityRestrictionImpl<?, ?, ?>>
            extends ComponentRestrictionImpl<O, P, R> implements CardinalityRestriction<O, P> {
        protected final CardinalityType cardinalityType;

        /**
         * @param n               {@link Node}
         * @param m               {@link EnhGraph}
         * @param predicate       either {@code owl:onDataRange} or {@code owl:onClass}
         * @param objectView      interface of class expression or data range
         * @param propertyView    interface, property expression
         * @param cardinalityType type of cardinality.
         */
        protected CardinalityRestrictionImpl(Node n,
                                             EnhGraph m,
                                             Property predicate,
                                             Class<O> objectView,
                                             Class<P> propertyView,
                                             CardinalityType cardinalityType) {
            super(n, m, predicate, objectView, propertyView);
            this.cardinalityType = cardinalityType;
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            // note: object value <O> is null for non-qualified restrictions.
            boolean q;
            return Iterators.concat(super.listSpec(q = isQualified()), listRequired(getCardinalityPredicate(q)));
        }

        @Override
        public O getValue() { // null for non-qualified restrictions:
            return object(predicate, objectView).orElseGet(this::getUnqualifiedValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public R setValue(O value) {
            Literal c = getCardinalityLiteral();
            removeAll(predicate);
            if (!isQualified(value)) { // null, owl:Thing, rdfs:Label
                removeAll(getCardinalityPredicate(true))
                        .addProperty(getCardinalityPredicate(false), c);
            } else {
                removeAll(getCardinalityPredicate(false))
                        .addProperty(getCardinalityPredicate(true), c)
                        .addProperty(predicate, value);
            }
            return (R) this;
        }

        private O getUnqualifiedValue() {
            OntModel m = getModel();
            return (O) (OntClass.class.isAssignableFrom(objectView) ? m.getOWLThing() : m.getRDFSLiteral());
        }

        @Override
        public int getCardinality() {
            return getCardinalityLiteral().getInt();
        }

        @SuppressWarnings("unchecked")
        public R setCardinality(int cardinality) {
            Literal value = createNonNegativeIntegerLiteral(cardinality);
            Property property = getCardinalityPredicate();
            removeAll(property).addLiteral(property, value);
            return (R) this;
        }

        private Literal getCardinalityLiteral() {
            return getRequiredObject(getCardinalityPredicate(), Literal.class);
        }

        protected Property getCardinalityPredicate() {
            return getCardinalityPredicate(isQualified());
        }

        private Property getCardinalityPredicate(boolean q) {
            return cardinalityType.getPredicate(q);
        }

        @Override
        public boolean isQualified() {
            return isQualified(getValue()) && hasProperty(cardinalityType.getPredicate(true));
        }
    }

    public static abstract class NaryRestrictionImpl<O extends OntObject,
            P extends OntRelationalProperty, R extends NaryRestrictionImpl<?, ?, ?>>
            extends OntClassImpl implements NaryRestriction<O, P> {
        protected final Property predicate;
        protected final Class<O> objectType; // always OntDR
        protected final Class<P> propertyType;

        protected NaryRestrictionImpl(Node n,
                                      EnhGraph m,
                                      Property predicate,
                                      Class<O> objectType,
                                      Class<P> propertyType) {
            super(n, m);
            this.predicate = predicate;
            this.objectType = objectType;
            this.propertyType = propertyType;
        }

        public static void validateArity(OntDataRange dr, Collection<OntDataProperty> properties) {
            properties.forEach(x -> OntJenaException.notNull(x, "Null data property"));
            if (dr.arity() == properties.size()) return;
            throw new OntJenaException.IllegalArgument("The number of data properties (" + properties.size() + ") " +
                    "must be equal to the data range arity (" + dr.arity() + ").");
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getRequiredRootStatement(this, OWL.Restriction);
        }

        @Override
        public O getValue() {
            return getRequiredObject(predicate, objectType);
        }

        @SuppressWarnings("unchecked")
        public R setValue(O value) {
            Objects.requireNonNull(value);
            removeAll(predicate).addProperty(predicate, value);
            return (R) this;
        }

        @SuppressWarnings("unchecked")
        public R setComponents(Collection<P> properties) {
            validateArity((OntDataRange) getValue(), (Collection<OntDataProperty>) properties);
            getList().clear().addAll(properties);
            return (R) this;
        }

        @Override
        public Class<? extends OntClass> objectType() {
            return NaryRestriction.class;
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(predicate), getList().listContent());
        }

        @Override
        public OntListImpl<P> getList() {
            return getModel().asOntList(getRequiredObject(OWL.onProperties, RDFList.class),
                    this, OWL.onProperties, propertyType);
        }
    }

}
