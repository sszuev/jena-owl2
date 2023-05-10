package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.Factories;
import com.github.sszuev.jena.ontapi.common.ObjectFactory;
import com.github.sszuev.jena.ontapi.common.OntFilter;
import com.github.sszuev.jena.ontapi.common.OntFinder;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.impl.UnionModel;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The base for any Ontology Object {@link Resource} implementation.
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntObjectImpl extends ResourceImpl implements OntObject {

    public static ObjectFactory objectFactory = Factories.createCommon(OntObjectImpl.class,
            OntFinder.ANY_SUBJECT, OntFilter.URI.or(OntFilter.BLANK));

    public OntObjectImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    /**
     * Finds a root statement for the given ontology object and resource type.
     * Throws an exception if the corresponding triple is not found.
     *
     * @param subject {@link OntObjectImpl}, the subject
     * @param type    URI-{@link Resource}, the type
     * @return Optional around {@link OntStatement}, which is never empty
     * @throws OntJenaException.IllegalState in case there is no root statement
     *                                       within the graph for the specified parameters
     */
    protected static Optional<OntStatement> getRequiredRootStatement(OntObjectImpl subject, Resource type)
            throws OntJenaException.IllegalState {
        // there are no built-in named individuals:
        Optional<OntStatement> res = getOptionalRootStatement(subject, type);
        if (res.isEmpty())
            throw new OntJenaException.IllegalState("Can't find " + subject.getModel().shortForm(type.getURI()) +
                    " declaration for " + subject);
        return res;
    }

    /**
     * Finds a root statement for the given ontology object and resource type.
     * Returns an empty {@code Optional} if the corresponding triple is not found.
     *
     * @param subject {@link OntObjectImpl}, the subject
     * @param type    URI-{@link Resource}, the type
     * @return Optional around {@link OntStatement} or {@code Optional.empty()} in case
     * there is no root statement within the graph for the specified parameters
     */
    protected static Optional<OntStatement> getOptionalRootStatement(OntObjectImpl subject, Resource type) {
        if (!subject.hasProperty(RDF.type, checkNamed(type))) return Optional.empty();
        return Optional.of(subject.getModel().createStatement(subject, RDF.type, type).asRootStatement());
    }

    /**
     * Answers a short form of a given class-type.
     *
     * @param type {@code Class}-type, not {@code null}
     * @return String
     */
    public static String viewAsString(Class<? extends RDFNode> type) {
        return type.getName().replace(OntObject.class.getPackage().getName() + ".", "");
    }

    /**
     * Tests the node is named.
     *
     * @param res {@link Node} to test, not {@code null}
     * @return the same node
     * @throws OntJenaException in case {@code null} or anonymous node is given
     */
    public static Node checkNamed(Node res) {
        if (OntJenaException.notNull(res, "Null node").isURI()) {
            return res;
        }
        throw new OntJenaException.IllegalArgument("Not uri node " + res);
    }

    /**
     * Tests the RDF resource is named.
     *
     * @param res {@link Resource} to test, not {@code null}
     * @return the same resource
     * @throws OntJenaException in case {@code null} or anonymous resource is given
     */
    public static Resource checkNamed(Resource res) {
        if (OntJenaException.notNull(res, "Null resource").isURIResource()) {
            return res;
        }
        throw new OntJenaException.IllegalArgument("Not uri resource " + res);
    }

    public static <X extends OntObject> Stream<X> subjects(Property predicate, OntObject object, Class<X> type) {
        return object.getModel()
                .statements(null, predicate, object)
                .map(x -> x.getSubject().getAs(type))
                .filter(Objects::nonNull);
    }

    /**
     * Lists all descendants for the specified object and the predicate.
     *
     * @param object    {@link X}
     * @param type      the class-type of {@link X}
     * @param predicate the {@link Property} whose values are required
     * @param inverse   if {@code true}, use the inverse of {@code predicate} rather than {@code predicate}
     * @param direct    if {@code true}, only returns the direct (adjacent) values
     * @param <X>       subtype of {@link OntObject}
     * @return <b>distinct</b> {@code Stream} of {@link X}s
     */
    @Deprecated // wrong logic
    public static <X extends OntObject> Stream<X> hierarchy(X object,
                                                            Class<X> type,
                                                            Property predicate,
                                                            boolean inverse,
                                                            boolean direct) {
        if (direct) {
            return getListDirect(type, predicate, inverse).apply(object);
        }
        return treeAsStream(object, getListDirect(type, predicate, inverse));
    }

    /**
     * Lists all descendants using the operation {@code children} to determine adjacent children.
     *
     * @param object   {@link X}
     * @param children a {@code Function} that returns {@code Stream} for an object of type {@link X}
     * @param direct   if {@code true}, only returns the direct (adjacent) values
     * @param <X>      subtype of {@link OntObject}
     * @return <b>distinct</b> {@code Stream} of {@link X}s
     */
    public static <X extends OntObject> Stream<X> hierarchy(X object,
                                                            Function<X, Stream<X>> children,
                                                            boolean direct) {
        if (direct) {
            return adjacentChildren(object, children);
        }
        return treeAsStream(object, children);
    }

    /**
     * Lists all descendants using the operation {@code children} to determine adjacent children.
     *
     * @param object           {@link X}
     * @param implicitChildren a {@code Function} that returns {@code Stream} of {@link X}'s children, including implicit ones (for example, bound by an equivalent operator)
     * @param explicitChildren a {@code Function} that returns {@code Stream} of {@link X}'s children, declared in RDF
     * @param direct           if {@code true}, only returns the direct (adjacent) values
     * @param <X>              subtype of {@link OntObject}
     * @return <b>distinct</b> {@code Stream} of {@link X}s
     */
    public static <X extends OntObject> Stream<X> hierarchy(X object,
                                                            Function<X, Stream<X>> implicitChildren,
                                                            Function<X, Stream<X>> explicitChildren,
                                                            boolean direct) {
        if (direct) {
            return adjacentChildren(object, implicitChildren);
        }
        return treeAsStream(object, explicitChildren);
    }

    /**
     * Gets a facility to get direct iterator.
     *
     * @param type      the class-type of {@link X}
     * @param predicate the {@link Property} whose values are required
     * @param inverse   if {@code true}, use the inverse of {@code predicate} rather than {@code predicate}
     * @param <X>       subtype of {@link OntObject} (actually {@link OntObjectImpl})
     * @return a {@code Function} that responses a {@code ExtendedIterator} over direct listed {@link X}
     */
    private static <X extends OntObject> Function<X, Stream<X>> getListDirect(Class<X> type,
                                                                              Property predicate,
                                                                              boolean inverse) {
        return inverse ? x -> subjects(predicate, x, type) : x -> x.objects(predicate, type);
    }

    /**
     * For the given object returns a {@code Set} of objects the same type,
     * that are its children which is determined by the operation {@code listChildren}.
     *
     * @param object       {@link X}
     * @param listChildren a {@code Function} that returns {@code Iterator} for an object of type {@link X}
     * @param <X>          subtype of {@link Resource}
     * @return {@code Set} of {@link X}
     */
    static <X extends Resource> Stream<X> treeAsStream(X object, Function<X, ? extends Stream<X>> listChildren) {
        return Iterators.fromSet(() -> {
            Set<X> res = new HashSet<>();
            collectIndirect(object, listChildren, res);
            res.remove(object);
            return res;
        });
    }

    /**
     * For the given object recursively collects all children determined by the operation {@code listChildren}.
     *
     * @param root         {@link X}
     * @param listChildren a {@code Function} that returns {@code Iterator} for an object of type {@link X}
     * @param res          {@code Set} to store result
     * @param <X>          any subtype of {@link Resource}
     */
    static <X extends Resource> void collectIndirect(X root,
                                                     Function<X, ? extends Stream<X>> listChildren,
                                                     Set<X> res) {
        List<X> queue = new LinkedList<>();
        queue.add(root);
        while (queue.size() != 0) {
            X next = queue.remove(0);
            try (Stream<X> children = listChildren.apply(next)) {
                children.forEach(x -> {
                    if (res.add(x)) {
                        queue.add(x);
                    }
                });
            }
        }
    }

    static <X extends Resource> Stream<X> adjacentChildren(X object, Function<X, Stream<X>> listChildren) {
        return Iterators.fromSet(() -> {
            Set<X> res = listChildren.apply(object).collect(Collectors.toSet());
            dropNodesWithSeveralPaths(object, listChildren, res);
            return res;
        });
    }

    static <X extends Resource> void dropNodesWithSeveralPaths(X root, Function<X, ? extends Stream<X>> listChildren, Set<X> res) {
        Set<X> seen = new HashSet<>();
        List<X> queue = new LinkedList<>();
        queue.add(root);
        while (queue.size() != 0) {
            X next = queue.remove(0);
            try (Stream<X> children = listChildren.apply(next)) {
                Iterator<X> it = children.iterator();
                while (it.hasNext()) {
                    X x = it.next();
                    if (seen.add(x)) {
                        queue.add(x);
                    } else {
                        res.remove(x);
                    }
                    if (res.isEmpty()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Finds a public {@link OntObject Ontology Object} class-type.
     *
     * @param o {@link OntObject}, not {@code null}
     * @return Class of the given {@link OntObject}
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends OntObject> findActualClass(OntObject o) {
        return Arrays.stream(o.getClass().getInterfaces())
                .filter(OntObject.class::isAssignableFrom)
                .map(c -> (Class<? extends OntObject>) c)
                .findFirst()
                .orElse(null);
    }

    /**
     * Answers an enhanced node that wraps the given RDF node and conforms to the given interface {@code view}.
     * The method does not change the model nodes cache,
     * but changes the object's cache if {@code view} is suitable for the given node.
     *
     * @param node {@link RDFNode}, not {@code null}
     * @param view a {@code Class}-type of the desired RDF view (interface)
     * @param <X>  any subtype of {@link RDFNode}
     * @return an instance of the type {@link X} or {@code null}
     * @see UnionModel#getNodeAs(Node, Class)
     */
    public static <X extends RDFNode> X getNodeAs(RDFNode node, Class<X> view) {
        try {
            return node.as(view);
        } catch (JenaException ignore) {
            return null;
        }
    }

    /**
     * Creates a fresh {@link OntObject} instance.
     *
     * @param node  {@link Node}, not {@code null}
     * @param model {@link EnhGraph}, not {@code null}
     * @return {@link OntObject}
     */
    public static OntObject wrapAsOntObject(Node node, EnhGraph model) {
        return new OntObjectImpl(node, model);
    }

    protected int getCharacteristics() {
        return Graphs.getSpliteratorCharacteristics(getModel().getGraph());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if the root statement belongs to the base graph
     */
    @Override
    public boolean isLocal() {
        return findRootStatement().map(OntStatement::isLocal).orElse(false);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link OntStatement}
     */
    @Override
    public final OntStatement getMainStatement() {
        return findRootStatement().orElse(null);
    }

    /**
     * {@inheritDoc}
     *
     * @return Stream of {@link OntStatement}s
     */
    @Override
    public Stream<OntStatement> spec() {
        return Iterators.asStream(listSpec(), getCharacteristics());
    }

    /**
     * {@inheritDoc}
     * Impl remarks:
     * - the method body is optimized to minimize graph querying;
     * - we use {@code RDFNode} (not {@code Resource}) as generic supertype due to the fact
     * that an {@code OntObject} can be also a {@code Literal}
     * (but in only single case when it is {@link OntSWRL.DArg}).
     */
    @Override
    public <X extends RDFNode> X getAs(Class<X> type) {
        return getNodeAs(this, type);
    }

    /**
     * Lists all object's characteristic statements according to its OWL2 specification.
     *
     * @return {@code ExtendedIterator} of {@link OntStatement}s
     */
    public ExtendedIterator<OntStatement> listSpec() {
        return findRootStatement().map(Iterators::of).orElseGet(NullIterator::instance);
    }

    /**
     * {@inheritDoc}
     *
     * @return <b>distinct</b> Stream of {@link OntStatement}s
     */
    @Override
    public Stream<OntStatement> content() {
        return Iterators.fromSet(this::getContent);
    }

    /**
     * Gets the content of the object, i.e. it's all characteristic statements (see {@link #listSpec()}),
     * plus all the additional statements in which this object is the subject,
     * excluding those of them whose predicate is an annotation property.
     *
     * @return {@code Set} of {@link OntStatement}s
     */
    protected Set<OntStatement> getContent() {
        Set<OntStatement> res = listSpec().toSet();
        listStatements().filterDrop(OntStatement::isAnnotationAssertion).forEachRemaining(res::add);
        return res;
    }

    /**
     * Finds the <b>first</b> declaration root statement.
     * The graph may contain several triples with predicate {@code rdf:type} and this ontology object as subject.
     * In this case the result is unpredictable.
     *
     * @return Optional around {@link OntStatement} that supports plain annotation assertions
     */
    public Optional<OntStatement> findRootStatement() {
        return Iterators.findFirst(listObjects(RDF.type))
                .map(o -> getModel().createStatement(this, RDF.type, o).asRootStatement());
    }

    /**
     * Adds or removes {@code @this rdf:type @type} statement.
     *
     * @param type URI-{@link Resource}, the type
     * @param add  if {@code true} the add operation is performed, otherwise the remove
     * @return <b>this</b> instance to allow cascading calls
     */
    protected OntObjectImpl changeRDFType(Resource type, boolean add) {
        if (add) {
            addStatement(RDF.type, type);
        } else {
            remove(RDF.type, type);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param property {@link Property}
     * @return Optional around {@link OntStatement}
     */
    @Override
    public Optional<OntStatement> statement(Property property) {
        return Iterators.findFirst(listStatements(property));
    }

    /**
     * {@inheritDoc}
     *
     * @param property {@link Property}, the predicate
     * @param value    {@link RDFNode}, the object
     * @return Optional around {@link OntStatement}
     */
    @Override
    public Optional<OntStatement> statement(Property property, RDFNode value) {
        return Iterators.findFirst(getModel().listOntStatements(this, property, value));
    }

    /**
     * Returns an ont-statement with the given subject and property.
     * If more than one statement that match the patter exists in the model,
     * it is undefined which will be returned.
     * If none exist, an exception is thrown.
     *
     * @param property {@link Property}, the predicate
     * @return {@link OntStatement}
     * @throws PropertyNotFoundException no statement are found
     */
    @Override
    public OntStatement getRequiredProperty(Property property) throws PropertyNotFoundException {
        return statement(property).orElseThrow(() -> new PropertyNotFoundException(property));
    }

    /**
     * Lists all statements for the given predicates and this ontology object as subject.
     *
     * @param properties Array of {@link Property properties}
     * @return {@code ExtendedIterator} of {@link OntStatement}s
     */
    protected ExtendedIterator<OntStatement> listRequired(Property... properties) {
        return Iterators.of(properties).mapWith(this::getRequiredProperty);
    }

    @Override
    public OntStatement addStatement(Property property, RDFNode value) {
        OntStatement res = getModel().createStatement(this,
                OntJenaException.notNull(property, "Null property."),
                OntJenaException.notNull(value, "Null value."));
        getModel().add(res);
        return res;
    }

    @Override
    public OntObjectImpl remove(Property property, RDFNode value) {
        OntGraphModelImpl m = getModel();
        m.listOntStatements(this, OntJenaException.notNull(property, "Null property."), value)
                .toList()
                .forEach(s -> m.remove(s.clearAnnotations()));
        return this;
    }

    @Override
    public Stream<OntStatement> statements(Property property) {
        return Iterators.asStream(listStatements(property), getCharacteristics());
    }

    @Override
    public Stream<OntStatement> statements() {
        return Iterators.asStream(listStatements(), getCharacteristics());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link StmtIterator} which contains {@link OntStatement}s
     * @see #listStatements()
     */
    @Override
    public StmtIterator listProperties() {
        return listProperties(null);
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} over all the properties of this resource.
     * The model associated with this resource is search and an iterator is
     * returned which iterates over all the statements which have this resource as a subject.
     *
     * @return {@link ExtendedIterator} over all the {@link OntStatement}s about this object
     * @see #listProperties()
     */
    public ExtendedIterator<OntStatement> listStatements() {
        return listStatements(null);
    }

    /**
     * {@inheritDoc}
     *
     * @param p {@link Property}, the predicate to search, can be {@code null}
     * @return {@link StmtIterator}
     * @see #listStatements(Property)
     */
    @Override
    public StmtIterator listProperties(Property p) {
        return Iterators.createStmtIterator(getModel().getGraph().find(asNode(), OntGraphModelImpl.asNode(p), Node.ANY),
                t -> createOntStatement(p, t));
    }

    /**
     * Lists all the values of the property {@code p}.
     * Returns an {@link ExtendedIterator Extended Iterator} over all the statements in the associated model whose
     * subject is this resource and whose predicate is {@code p}.
     *
     * @param p {@link Property}, the predicate sought, can be {@code null}
     * @return {@link ExtendedIterator} over the {@link OntStatement}s
     * @see #listStatements(Property)
     */
    public ExtendedIterator<OntStatement> listStatements(Property p) {
        return Iterators.create(getModel().getGraph().find(asNode(), OntGraphModelImpl.asNode(p), Node.ANY)
                .mapWith(t -> createOntStatement(p, t)));
    }

    /**
     * Creates a new {@link OntStatement} instance using the given {@link Triple} and {@link Property}.
     * The object and (if possible) the predicate property of the new statement are cached inside model
     * Auxiliary method.
     *
     * @param p {@link Property}, can be {@code null}
     * @param t {@link Triple}, not {@code null}
     * @return new {@link OntStatement} around the triple
     */
    protected OntStatementImpl createOntStatement(Property p, Triple t) {
        OntGraphModelImpl m = getModel();
        Property property = p == null ? m.getNodeAs(t.getPredicate(), Property.class) : p;
        RDFNode object = m.getNodeAs(t.getObject(), RDFNode.class);
        return m.createStatement(this, property, object);
    }

    /**
     * Lists all annotation property assertions (so-called plain annotations) attached to this object
     * plus all bulk annotations of the root statement.
     *
     * @return Stream of {@link OntStatement}s
     * @see #listAnnotations()
     */
    @Override
    public Stream<OntStatement> annotations() {
        return Iterators.asStream(listAnnotations(), getCharacteristics());
    }

    /**
     * Lists all related annotation assertions.
     *
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     * @see #annotations()
     */
    public ExtendedIterator<OntStatement> listAnnotations() {
        ExtendedIterator<OntStatement> res = listAssertions();
        Optional<OntStatement> main = findRootStatement();
        if (main.isEmpty()) {
            return res;
        }
        OntStatementImpl s = (OntStatementImpl) main.get();
        return res.andThen(Iterators.flatMap(s.listAnnotationResources(), a -> ((OntAnnotationImpl) a).listAssertions()));
    }

    /**
     * Lists all annotation property assertions (so-called plain annotations) attached to this object.
     *
     * @return Stream of {@link OntStatement}s
     * @see OntObjectImpl#listAssertions()
     */
    public Stream<OntStatement> assertions() {
        return Iterators.asStream(listAssertions(), getCharacteristics());
    }

    /**
     * Returns an iterator over object's annotation property assertions.
     * The annotation assertion is a statements with an {@link OntAnnotationProperty annotation property} as predicate.
     *
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     * @see #assertions()
     */
    public ExtendedIterator<OntStatement> listAssertions() {
        return listStatements().filterKeep(OntStatement::isAnnotationAssertion);
    }

    /**
     * Returns an iterator over all literal's annotations.
     *
     * @param predicate {@link OntAnnotationProperty}, not {@code null}
     * @return {@link ExtendedIterator} of {@link Literal}s
     * @see #listAnnotations()
     */
    public ExtendedIterator<Literal> listAnnotationLiterals(OntAnnotationProperty predicate) {
        return listAnnotations()
                .filterKeep(s -> Objects.equals(predicate, s.getPredicate()))
                .mapWith(Statement::getObject)
                .filterKeep(RDFNode::isLiteral)
                .mapWith(RDFNode::asLiteral);
    }

    @Override
    public Stream<String> annotationValues(OntAnnotationProperty p, String lang) {
        if (lang == null) return Iterators.asStream(listAnnotationLiterals(p).mapWith(Literal::getString));
        return Iterators.asStream(listAnnotationLiterals(p))
                .sorted(Comparator.comparing(Literal::getLanguage))
                .filter(l -> {
                    String target = l.getLanguage();
                    if (lang.isEmpty())
                        return target.isEmpty();
                    String x = target.length() > lang.length() ? target.substring(0, lang.length()) : target;
                    return lang.equalsIgnoreCase(x);
                })
                .map(Literal::getString);
    }

    /**
     * Adds an annotation assertion.
     * It could be expanded to bulk form by adding sub-annotation.
     *
     * @param property {@link OntAnnotationProperty}, Named annotation property.
     * @param value    {@link RDFNode} the value: uri-resource, literal or anonymous individual.
     * @return OntStatement for newly added annotation
     * @throws OntJenaException in case input is wrong
     */
    @Override
    public OntStatement addAnnotation(OntAnnotationProperty property, RDFNode value) {
        return findRootStatement().map(r -> r.addAnnotation(property, value))
                .orElseGet(() -> getModel().createStatement(addProperty(property, value), property, value));
    }

    /**
     * {@inheritDoc}
     *
     * @return this instance
     */
    @Override
    public OntObjectImpl clearAnnotations() {
        // for built-ins
        Iterators.peek(listAssertions(), OntStatement::clearAnnotations).toSet()
                .forEach(a -> getModel().remove(a));
        // for others
        findRootStatement().ifPresent(OntStatement::clearAnnotations);
        return this;
    }

    /**
     * Returns an object from a first found statement with specified predicate.
     * Since the order in the graph is undefined
     * in case there are more than one statement for a property the result is unpredictable.
     *
     * @param predicate {@link Property}
     * @param view      Class
     * @param <T>       {@link RDFNode} type
     * @return an object from statement
     * @throws OntJenaException in case no object by predicate has been found
     * @see #getRequiredProperty(Property)
     */
    public <T extends RDFNode> T getRequiredObject(Property predicate, Class<T> view) {
        return object(predicate, view)
                .orElseThrow(() -> new OntJenaException(
                        String.format("Can't find required object [%s @%s %s]", this, predicate, viewAsString(view))));
    }

    /**
     * Finds a <b>first</b> object with the given {@code rdf:type}
     * attached to this ontology object on the given {@code predicate}.
     * The result is unpredictable in case there more than one statement for these conditions.
     *
     * @param predicate {@link Property}
     * @param type      subclass of {@link RDFNode}
     * @param <T>       any subtype of {@link RDFNode}
     * @return Optional around {@link T}
     */
    public <T extends RDFNode> Optional<T> object(Property predicate, Class<T> type) {
        return Iterators.findFirst(listObjects(predicate, type));
    }

    /**
     * {@inheritDoc}
     *
     * @param predicate {@link Property} predicate, can be null
     * @param type      Interface to find and cast, not null
     * @param <O>       any subtype of {@link RDFNode}
     * @return Stream of {@link RDFNode node}s of the {@link O} type
     */
    @Override
    public <O extends RDFNode> Stream<O> objects(Property predicate, Class<O> type) {
        return Iterators.asStream(listObjects(predicate, type), predicate != null ? getCharacteristics() : Spliterator.NONNULL);
    }

    /**
     * Lists all objects for the given predicate and type, considering this instance in a subject relation.
     *
     * @param predicate {@link Property}, can be {@code null}
     * @param type      class-type of interface to find and cast, not {@code null}
     * @param <O>       subtype of {@link RDFNode rdf-node}
     * @return {@link ExtendedIterator} of {@link RDFNode node}s of the type {@link O}
     * @see #object(Property, Class)
     * @see #listSubjects(Property, Class)
     */
    public <O extends RDFNode> ExtendedIterator<O> listObjects(Property predicate, Class<O> type) {
        OntGraphModelImpl m = getModel();
        return listProperties(predicate)
                .mapWith(s -> m.findNodeAs(s.getObject().asNode(), type))
                .filterDrop(Objects::isNull);
    }

    /**
     * Lists all subjects for the given predicate and type, considering this instance in an object relation.
     *
     * @param predicate {@link Property}, can be {@code null}
     * @param type      class-type of interface to find and cast, not {@code null}
     * @param <S>       subtype of {@link RDFNode rdf-node}
     * @return {@link ExtendedIterator} of {@link RDFNode node}s of the type {@link S}
     * @see #listObjects(Property, Class)
     */
    public <S extends RDFNode> ExtendedIterator<S> listSubjects(Property predicate, Class<S> type) {
        OntGraphModelImpl m = getModel();
        return m.listStatements(null, predicate, this)
                .mapWith(s -> m.findNodeAs(s.getSubject().asNode(), type))
                .filterDrop(Objects::isNull);
    }

    /**
     * Lists all objects for the given predicate.
     *
     * @param predicate {@link Property}
     * @return Stream of {@link RDFNode}s
     * @see #listObjects(Property)
     */
    @Override
    public Stream<RDFNode> objects(Property predicate) {
        return Iterators.asStream(listObjects(predicate), predicate != null ? getCharacteristics() : Spliterator.NONNULL);
    }

    /**
     * Lists all objects for the given predicate.
     *
     * @param predicate {@link Property}
     * @return {@link ExtendedIterator} of {@link RDFNode}s
     * @see #objects(Property)
     */
    public ExtendedIterator<RDFNode> listObjects(Property predicate) {
        return listProperties(predicate).mapWith(Statement::getObject);
    }

    @Override
    public OntGraphModelImpl getModel() {
        return (OntGraphModelImpl) enhGraph;
    }

    /**
     * Gets a public ont-object type identifier.
     *
     * @return Class, the actual type of this object
     */
    public Class<? extends OntObject> getActualClass() {
        return findActualClass(this);
    }

    @Override
    public String toString() {
        Class<? extends RDFNode> view = getActualClass();
        return view == null ? super.toString() : String.format("[%s]%s", viewAsString(view), asNode());
    }

}
