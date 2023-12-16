package com.github.sszuev.jena.ontapi.impl;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelConfig;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDataRangeImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntFacetRestrictionImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntListImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSWRLImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntStatementImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntID;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.PersonalityConfigException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.IteratorFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base model ONT-API implementation to work through jena only.
 * This is an analogue of {@link org.apache.jena.ontology.impl.OntModelImpl} to work in accordance with OWL2 DL specification.
 * <p>
 * Created by @ssz on 27.10.2016.
 *
 * @see UnionGraph
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused"})
public class OntGraphModelImpl extends ModelCom implements OntModel, OntEnhGraph, InfModel {

    // the model's types mapper
    protected final Map<String, RDFDatatype> dtTypes = new HashMap<>();
    // to control RDF recursion while casting a node to an RDF view, see #fetchNodeAs(Node, Class)
    private final ThreadLocal<Set<Node>> visited = ThreadLocal.withInitial(HashSet::new);
    // Cached deductions model
    private Model deductionsModel = null;
    private final Set<Class<? extends OntEntity>> supportedEntityTypes;

    /**
     * This {@link OntModel} implementation wraps
     * only {@link UnionGraph} or {@link InfGraph} which in turn wraps {@link UnionGraph}.
     *
     * @param graph       {@link Graph}
     * @param personality {@link OntPersonality}
     */
    public OntGraphModelImpl(Graph graph, OntPersonality personality) {
        super(makeGraph(graph), OntPersonality.asJenaPersonality(personality));
        this.supportedEntityTypes = OntEntity.TYPES.stream().filter(personality::supports).collect(Collectors.toSet());
    }

    protected static Graph makeGraph(Graph given) {
        if (given instanceof InfGraph) {
            Graph raw = ((InfGraph) given).getRawGraph();
            if (raw instanceof UnionGraph) {
                return given;
            }
            throw new IllegalArgumentException(
                    "The specified InfGraph does not wrap UnionGraph, instead it wraps " + raw.getClass().getSimpleName()
            );
        }
        return Graphs.makeUnion(given);
    }

    /**
     * Creates a fresh ontology resource (i.e. {@code @uri rdf:type owl:Ontology} triple)
     * and moves to it all content from existing ontology resources (if they present).
     *
     * @param model {@link Model} graph holder
     * @param uri   String an ontology iri, null for anonymous ontology
     * @return {@link Resource} in model
     * @throws OntJenaException if creation is not possible by some reason.
     */
    public static Resource createOntologyID(Model model, String uri) throws OntJenaException {
        return createOntologyID(model, uri == null ? NodeFactory.createBlankNode() : NodeFactory.createURI(uri));
    }

    /**
     * Creates a fresh ontology resource from the given {@link Node}
     * and moves to it all content from existing ontology resources (if they present).
     *
     * @param model {@link Model} graph holder, not {@code null}
     * @param node  {@link Node}, must be either uri or blank, not {@code null}
     * @return {@link Resource} in model
     * @throws OntJenaException         in case the given node is uri, and it takes part in {@code owl:imports}
     * @throws IllegalArgumentException in case the given node is not uri or blank (i.e. literal)
     */
    public static Resource createOntologyID(Model model, Node node) throws OntJenaException, IllegalArgumentException {
        if (!Objects.requireNonNull(node, "Null node").isURI() && !node.isBlank())
            throw new IllegalArgumentException("Expected uri or blank node: " + node);
        List<Statement> prev = Iterators.flatMap(model.listStatements(null, RDF.type, OWL.Ontology),
                s -> s.getSubject().listProperties()).toList();
        if (prev.stream()
                .filter(s -> OWL.imports.equals(s.getPredicate()))
                .map(Statement::getObject)
                .filter(RDFNode::isURIResource)
                .map(RDFNode::asNode).anyMatch(node::equals)) {
            throw new OntJenaException.IllegalArgument("Can't create ontology: " +
                    "the specified uri (<" + node + ">) is present in the imports.");
        }
        model.remove(prev);
        Resource res = model.wrapAsResource(node).addProperty(RDF.type, OWL.Ontology);
        prev.forEach(s -> res.addProperty(s.getPredicate(), s.getObject()));
        return res;
    }

    /**
     * Lists all {@code OntObject}s for the given {@code OntGraphModelImpl}.
     *
     * @param m    {@link OntGraphModelImpl} the impl to cache
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <M>  a subtype of {@link EnhGraph} and {@link OntEnhGraph}
     * @param <O>  subtype of {@link OntObject}
     * @return an {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    public static <M extends EnhGraph & OntEnhGraph, O extends OntObject> ExtendedIterator<O> listOntObjects(M m,
                                                                                                             Class<? extends O> type) {
        OntPersonality p = m.getOntPersonality();
        if (p.supports(type)) {
            return p.getObjectFactory(type).iterator(m).mapWith(e -> m.getNodeAs(e.asNode(), type));
        } else {
            return NullIterator.instance();
        }
    }

    /**
     * Filters {@code OntIndividual}s from the specified {@code ExtendedIterator}.
     *
     * @param model      {@link M}, not {@code null}
     * @param system     a {@code Set} of {@link Node}s,
     *                   that cannot be treated as {@link OntClass Ontology Class}es, not {@code null}
     * @param assertions {@link ExtendedIterator} of {@link Triple}s
     *                   with the {@link RDF#type rdf:type} as predicate, not {@code null}
     * @param <M>        a subtype of {@link OntModel} and {@link OntEnhGraph}
     * @return {@link ExtendedIterator} of {@link OntIndividual}s that are attached to the {@code model}
     */
    public static <M extends OntModel & OntEnhGraph> ExtendedIterator<OntIndividual> listIndividuals(M model,
                                                                                                     Set<Node> system,
                                                                                                     ExtendedIterator<Triple> assertions) {
        Set<Triple> seen = new HashSet<>();
        boolean useSimplifiedClassChecking = model.getOntPersonality()
                .getConfig().getBoolean(OntModelConfig.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS);
        boolean isRDFS = OntPersonalities.isRDFS(model.getOntPersonality());
        return assertions
                .mapWith(t -> {
                    // to speed up the process,
                    // the investigation (that includes TTO, PS, HP, GALEN, FAMILY and PIZZA ontologies),
                    // shows that the profit exists, and it is significant sometimes:
                    if (system.contains(t.getObject())) {
                        return null;
                    }

                    // skip duplicates (an individual may have several class-assertions):
                    if (seen.remove(t)) {
                        return null;
                    }
                    if (!testIsClass(model, t.getObject(), useSimplifiedClassChecking, isRDFS)) {
                        return null;
                    }
                    return model.asStatement(t);
                })
                .filterKeep(s -> {
                    if (s == null) return false;
                    // an individual may have a factory with punning restrictions,
                    // so need to check its type also.
                    // this time does not cache in a model
                    OntIndividual i = s.getSubject().getAs(OntIndividual.class);
                    if (i == null) return false;

                    // update the set with duplicates to ensure the stream is distinct
                    ((OntIndividualImpl) i).listClasses()
                            .forEachRemaining(x -> {
                                if (s.getObject().equals(x)) {
                                    // skip this statement, otherwise all individuals fall into memory
                                    return;
                                }
                                seen.add(Triple.create(i.asNode(), RDF.Nodes.type, x.asNode()));
                            });
                    return true;
                })
                .mapWith(s -> s.getSubject(OntIndividual.class));
    }

    private static <M extends OntModel & OntEnhGraph> boolean testIsClass(
            M model,
            Node candidate,
            boolean simple,
            boolean isRDFS
    ) {
        if (simple) {
            if (isRDFS) {
                return model.getGraph().contains(candidate, RDF.type.asNode(), RDFS.Class.asNode());
            }
            return model.getGraph().contains(candidate, RDF.type.asNode(), OWL.Class.asNode()) ||
                    model.getGraph().contains(candidate, RDF.type.asNode(), OWL.Restriction.asNode());
        }
        return model.findNodeAs(candidate, OntClass.class) != null;
    }

    /**
     * Creates a {@code Stream} for a graph.
     *
     * @param graph    {@link Graph} to test
     * @param it       {@code ExtendedIterator} obtained from the {@code graph}
     * @param withSize if {@code true} attempts to include graph size as an estimated size of a future {@code Stream}
     * @param <X>      type of stream items
     * @return {@code Stream} of {@link X}s
     */
    private static <X> Stream<X> asStream(Graph graph,
                                          ExtendedIterator<X> it,
                                          boolean withSize) {
        int characteristics = Graphs.getSpliteratorCharacteristics(graph);
        long size = -1;
        if (withSize && Graphs.isSized(graph)) {
            size = Graphs.size(graph);
            characteristics = characteristics | Spliterator.SIZED;
        }
        return Iterators.asStream(it, size, characteristics);
    }

    /**
     * Answers {@code true} iff the given {@code SPO} corresponds {@link Triple#ANY}.
     *
     * @param s {@link Resource}, the subject
     * @param p {@link Property}, the predicate
     * @param o {@link RDFNode}, the object
     * @return boolean
     */
    private static boolean isANY(Resource s, Property p, RDFNode o) {
        if (s != null) return false;
        if (p != null) return false;
        return o == null;
    }

    @Override
    public OntPersonality getOntPersonality() {
        return (OntPersonality) super.getPersonality();
    }

    /**
     * Returns {@link UnionGraph}.
     * This implementation requires that the underlying graph is union-graph or inf-graph.
     */
    public UnionGraph getUnionGraph() {
        Graph graph = super.getGraph();
        if (graph instanceof InfGraph) {
            Graph raw = ((InfGraph) graph).getRawGraph();
            if (raw instanceof UnionGraph) {
                return ((UnionGraph) raw);
            }
            throw new IllegalStateException(
                    "The encapsulated InfGraph does not wrap UnionGraph, instead it wraps " + raw.getClass().getSimpleName()
            );
        }
        if (graph instanceof UnionGraph) {
            return ((UnionGraph) graph);
        }
        throw new IllegalStateException(
                "The model wraps " + graph.getClass().getSimpleName() + ", that is illegal"
        );
    }

    @Override
    public Graph getBaseGraph() {
        return getUnionGraph().getBaseGraph();
    }

    @Override
    public Model getBaseModel() {
        return new ModelCom(getBaseGraph());
    }

    public <X extends OntObject> void checkType(Class<X> type) {
        if (!getOntPersonality().supports(type)) {
            throw new OntJenaException.Unsupported(
                    "Profile " + getOntPersonality().getName() + " does not support language construct " +
                            OntEnhNodeFactories.viewAsString(type)
            );
        }
    }

    @Override
    public OntID getID() {
        return getNodeAs(Graphs.ontologyNode(getBaseGraph())
                .orElseGet(() -> createResource(OWL.Ontology).asNode()), OntID.class);
    }

    @Override
    public Optional<OntID> id() {
        return Graphs.ontologyNode(getBaseGraph()).map(x -> getNodeAs(x, OntID.class));
    }

    @Override
    public OntID setID(String uri) {
        return getNodeAs(createOntologyID(getBaseModel(), uri).asNode(), OntID.class);
    }

    @Override
    public OntGraphModelImpl addImport(OntModel m) {
        if (Objects.requireNonNull(m, "Null model specified.").getID().isAnon()) {
            throw new OntJenaException.IllegalArgument("Anonymous sub models are not allowed.");
        }
        String importsURI = Objects.requireNonNull(m.getID().getImportsIRI());
        if (importsURI.equals(getID().getURI())) {
            throw new OntJenaException.IllegalArgument("Attempt to import ontology with the same name: " + importsURI);
        }
        if (hasImport(importsURI)) {
            throw new OntJenaException.IllegalArgument("Ontology <" + importsURI + "> is already in imports.");
        }
        addImportModel(m.getGraph(), importsURI);
        return this;
    }

    @Override
    public boolean hasImport(OntModel m) {
        Objects.requireNonNull(m);
        return findImport(x -> Graphs.isSameBase(x.getGraph(), m.getGraph())).isPresent();
    }

    @Override
    public boolean hasImport(String uri) {
        return findImport(x -> Objects.equals(x.getID().getImportsIRI(), uri)).isPresent();
    }

    @Override
    public OntGraphModelImpl removeImport(OntModel m) {
        Objects.requireNonNull(m);
        findImport(x -> Graphs.isSameBase(x.getGraph(), m.getGraph()))
                .ifPresent(x -> removeImportModel(x.getGraph(), x.getID().getImportsIRI()));
        return this;
    }

    @Override
    public OntGraphModelImpl removeImport(String uri) {
        findImport(x -> Objects.equals(uri, x.getID().getImportsIRI()))
                .ifPresent(x -> removeImportModel(x.getGraph(), x.getID().getImportsIRI()));
        return this;
    }

    @Override
    public Stream<OntModel> imports() {
        return imports(getOntPersonality());
    }

    /**
     * Lists all top-level sub-models built with the given {@code personality}.
     *
     * @param personality {@link OntPersonality}, not {@code null}
     * @return {@code Stream} of {@link OntModel}s
     */
    public Stream<OntModel> imports(OntPersonality personality) {
        return Iterators.asStream(listImportModels(personality));
    }

    /**
     * Finds a model impl from the internals using the given {@code filter}.
     *
     * @param filter {@code Predicate} to filter {@link OntGraphModelImpl}s
     * @return {@code Optional} around {@link OntGraphModelImpl}
     */
    protected Optional<OntGraphModelImpl> findImport(Predicate<OntGraphModelImpl> filter) {
        return Iterators.findFirst(listImportModels(getOntPersonality()).filterKeep(filter));
    }

    /**
     * Adds the graph-uri pair into the internals.
     *
     * @param g {@link Graph}, not {@code null}
     * @param u String, not {@code null}
     */
    protected void addImportModel(Graph g, String u) {
        getUnionGraph().addGraph(g);
        getID().addImport(u);
    }

    /**
     * Removes the graph-uri pair from the internals.
     *
     * @param g {@link Graph}, not {@code null}
     * @param u String, not {@code null}
     */
    protected void removeImportModel(Graph g, String u) {
        getUnionGraph().removeParent(g);
        getID().removeImport(u);
    }

    /**
     * Lists {@link OntGraphModelImpl model impl}s with the specified {@code personality}
     * from the top tier of the imports' hierarchy.
     *
     * @param personality {@link OntPersonality}, not {@code null}
     * @return <b>non-distinct</b> {@code ExtendedIterator} of {@link OntGraphModelImpl}s
     */
    public final ExtendedIterator<OntGraphModelImpl> listImportModels(OntPersonality personality) {
        return listImportGraphs().mapWith(u -> new OntGraphModelImpl(u, personality));
    }

    /**
     * Lists all top-level {@link UnionGraph}s of the model's {@code owl:import} hierarchy.
     * This model graph is not included.
     *
     * @return <b>non-distinct</b> {@code ExtendedIterator} of {@link UnionGraph}
     */
    protected final ExtendedIterator<UnionGraph> listImportGraphs() {
        return getUnionGraph().getUnderlying().listGraphs()
                .filterKeep(x -> x instanceof UnionGraph)
                .mapWith(x -> (UnionGraph) x);
    }

    /**
     * Gets the top-level {@link OntGraphModelImpl Ontology Graph Model impl}.
     * The returned model may contain import declarations, but cannot contain sub-models.
     * Be warned: any listeners, attached on the {@link #getGraph()}
     *
     * @return {@link OntGraphModelImpl}
     * @see #getBaseModel()
     */
    public OntGraphModelImpl getTopModel() {
        if (independent()) {
            return this;
        }
        OntPersonality personality = getOntPersonality();
        return new OntGraphModelImpl(getBaseGraph(), personality);
    }

    /**
     * Determines whether this model is independent.
     *
     * @return {@code true} if this model is independent of others
     */
    @Override
    public boolean independent() {
        return getUnionGraph().getUnderlying().isEmpty();
    }

    /**
     * Answers {@code true} if the given entity is built-in.
     *
     * @param e   {@link OntEntity} object impl
     * @param <E> subtype of {@link OntObjectImpl} and {@link OntEntity}
     * @return boolean
     */
    public <E extends OntObjectImpl & OntEntity> boolean isBuiltIn(E e) {
        return getOntPersonality().getBuiltins()
                .get(e.getActualClass())
                .contains(e.asNode());
    }

    /**
     * Retrieves the stream of {@link OntObject Ontology Object}s.
     * The result object will be cached inside the model.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return {@code Stream} of {@link OntObject}s
     */
    @Override
    public <O extends OntObject> Stream<O> ontObjects(Class<? extends O> type) {
        return Iterators.asStream(listOntObjects(type), Graphs.getSpliteratorCharacteristics(getGraph()));
    }

    /**
     * Lists all {@link OntObject Ontology Object}s and caches them inside this model.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return an {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    public <O extends OntObject> ExtendedIterator<O> listOntObjects(Class<? extends O> type) {
        return listOntObjects(this, type);
    }

    /**
     * The same as {@link OntGraphModelImpl#listOntObjects(Class)}, but for the base graph.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    public <O extends OntObject> ExtendedIterator<O> listLocalOntObjects(Class<? extends O> type) {
        return listOntObjects(getTopModel(), type);
    }

    @Override
    public Stream<OntEntity> ontEntities() {
        return supportedEntityTypes.stream().flatMap(this::ontObjects);
    }

    /**
     * Lists all Ontology Entities.
     * Built-ins are not included.
     *
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntEntity}s
     * @see #listLocalOntEntities()
     */
    public ExtendedIterator<OntEntity> listOntEntities() {
        return Iterators.flatMap(Iterators.create(supportedEntityTypes), this::listOntObjects);
    }

    /**
     * The same as {@link #listOntEntities()} but for the base graph.
     *
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntEntity}s
     * @see #listOntEntities()
     */
    public ExtendedIterator<OntEntity> listLocalOntEntities() {
        return Iterators.flatMap(Iterators.create(supportedEntityTypes), this::listLocalOntObjects);
    }

    /**
     * Gets 'punnings', i.e. the {@link OntEntity}s which have not only a single type.
     *
     * @param withImports if it false takes into account only the base model
     * @return {@code Stream} of {@link OntEntity}s.
     */
    public Stream<OntEntity> ambiguousEntities(boolean withImports) {
        return ontEntities().filter(e -> withImports || e.isLocal()).filter(e -> supportedEntityTypes.stream()
                .filter(view -> e.canAs(view) && (withImports || e.as(view).isLocal())).count() > 1);
    }

    @Override
    public Stream<OntIndividual> individuals() {
        return Iterators.asStream(listIndividuals(), Graphs.getSpliteratorCharacteristics(getGraph()));
    }

    /**
     * Returns an {@code ExtendedIterator} over all individuals
     * that participate in class assertion statement {@code a rdf:type C}.
     * <b>Note:</b> this method behaves differently than
     * the method {@link org.apache.jena.ontology.impl.OntModelImpl#listIndividuals()}!
     * The Jena's method does not verify that the right side of the class-assertion is indeed a valid class expression;
     * it checks only several well-known cases.
     *
     * @return {@link ExtendedIterator} of {@link OntIndividual}s
     */
    public ExtendedIterator<OntIndividual> listIndividuals() {
        return listIndividuals(this,
                getOntPersonality().forbidden(OntClass.Named.class),
                getGraph().find(Node.ANY, RDF.Nodes.type, Node.ANY));
    }

    @Override
    public Stream<OntClass> hierarchyRoots() {
        if (getOntPersonality().getConfig().getBoolean(OntModelConfig.SUPPORTS_OWL_THING)) {
            Model capabilities = getReasonerCapabilities();
            if (capabilities != null && capabilities.contains(null, ReasonerVocabulary.supportsP, ReasonerVocabulary.directSubClassOf)) {
                return statements(null, ReasonerVocabulary.directSubClassOf, OWL.Thing).map(it -> it.getSubject().as(OntClass.class));
            }
        }
        return ontObjects(OntClass.class)
                .filter(c -> !c.isURIResource() || !c.asNamed().isBuiltIn())
                .filter(OntClass::isHierarchyRoot);
    }

    @Override
    public <E extends OntEntity> E getOntEntity(Class<E> type, String uri) {
        return findNodeAs(NodeFactory.createURI(OntJenaException.notNull(uri, "Null uri.")), type);
    }

    @Override
    public <T extends OntEntity> T createOntEntity(Class<T> type, String iri) {
        try {
            return createOntObject(type, iri);
        } catch (OntJenaException.Creation e) {
            // illegal punning ?
            throw new OntJenaException.Creation(String.format("Unable to create entity [%s: <%s>].",
                    OntEnhNodeFactories.viewAsString(type), iri), e);
        }
    }

    /**
     * Creates and caches an ontology object resource by the given type and uri.
     *
     * @param type Class, object type
     * @param uri  String, URI (IRI), can be {@code null} for anonymous resource
     * @param <T>  class-type of {@link OntObject}
     * @return {@link OntObject}, new instance
     * @throws OntJenaException.Unsupported profile mismatch
     */
    public <T extends OntObject> T createOntObject(Class<T> type, String uri) {
        OntPersonality personality = getOntPersonality();
        if (!personality.supports(type)) {
            throw new OntJenaException.Unsupported(
                    "Attempt to create resource <" + uri + ">. Profile " + personality.getName() +
                            " does not support language construct " + OntEnhNodeFactories.viewAsString(type)
            );
        }
        return personality.getObjectFactory(type).createInGraph(Graphs.createNode(uri), this).as(type);
    }

    @Override
    public OntGraphModelImpl removeOntObject(OntObject obj) {
        obj.clearAnnotations().content()
                .peek(OntStatement::clearAnnotations)
                .collect(Collectors.toSet()).forEach(this::remove);
        return this;
    }

    @Override
    public OntGraphModelImpl removeOntStatement(OntStatement statement) {
        return remove(statement.clearAnnotations());
    }

    @Override
    public Stream<OntStatement> statements() {
        Graph g = getGraph();
        return asStream(g, g.find().mapWith(this::asStatement), true);
    }

    @Override
    public Stream<OntStatement> statements(Resource s, Property p, RDFNode o) {
        return asStream(getGraph(), listOntStatements(s, p, o), isANY(s, p, o));
    }

    @Override
    public Stream<OntStatement> localStatements(Resource s, Property p, RDFNode o) {
        return asStream(getBaseGraph(), listLocalStatements(s, p, o), isANY(s, p, o));
    }

    /**
     * {@inheritDoc}
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link StmtIterator} of {@link OntStatement}s
     */
    @Override
    public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
        return ModelUtils.createStmtIterator(getGraph().find(asNode(s), asNode(p), asNode(o)), this::asStatement);
    }

    /**
     * Returns an {@link ExtendedIterator extended iterator} over all the statements in the model that match a pattern.
     * The statements selected are those whose subject matches the {@code s} argument,
     * whose predicate matches the {@code p} argument
     * and whose object matches the {@code o} argument.
     * If an argument is {@code null} it matches anything.
     * The method is equivalent to the expression {@code listStatements(s, p, o).mapWith(OntStatement.class::cast)}.
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     * @see #listStatements(Resource, Property, RDFNode)
     */
    public ExtendedIterator<OntStatement> listOntStatements(Resource s, Property p, RDFNode o) {
        return Iterators.create(getGraph().find(asNode(s), asNode(p), asNode(o)).mapWith(this::asStatement));
    }

    /**
     * Lists all statements in the <b>base</b> model that match a pattern
     * in the form of {@link ExtendedIterator Extended Iterator}.
     * The method is equivalent to the expression
     * {@code listStatements(s, p, o).mapWith(OntStatement.class::cast).filterKeep(OntStatement::isLocal)}.
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s, which are local to the base graph
     * @see #listStatements(Resource, Property, RDFNode)
     */
    public ExtendedIterator<OntStatement> listLocalStatements(Resource s, Property p, RDFNode o) {
        return Iterators.create(getBaseGraph().find(asNode(s), asNode(p), asNode(o)).mapWith(this::asStatement));
    }

    @Override
    public OntStatementImpl createStatement(Resource s, Property p, RDFNode o) {
        return OntStatementImpl.createOntStatementImpl(s, p, o, this);
    }

    @Override
    public OntStatementImpl asStatement(Triple triple) {
        return OntStatementImpl.createOntStatementImpl(triple, this);
    }

    /**
     * Determines if the given {@code (s, p, o)} pattern is present in the base graph,
     * with {@code null} allowed to represent a wildcard match.
     *
     * @param s - {@link Resource} - the subject of the statement tested ({@code null} as wildcard)
     * @param p - {@link Property} - the predicate of the statement tested ({@code null} as wildcard)
     * @param o - {@link RDFNode} - the object of the statement tested ({@code null} as wildcard)
     * @return boolean
     * @see Model#contains(Resource, Property, RDFNode)
     */
    public boolean containsLocal(Resource s, Property p, RDFNode o) {
        return getBaseGraph().contains(asNode(s), asNode(p), asNode(o));
    }

    /**
     * Wraps the existing given {@link RDFList []-list} as {@link OntList ONT-list}.
     *
     * @param list      {@link RDFList}, not {@code null}
     * @param subject   {@link OntObject}, not {@code null}
     * @param predicate {@link Property}, not {@code null}
     * @param type      a {@code Class}-type for list element {@link E}, not {@code null}
     * @param <E>       any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> asOntList(RDFList list,
                                                        OntObject subject,
                                                        Property predicate,
                                                        Class<E> type) {
        return asOntList(list, subject, predicate, false, null, type);
    }

    /**
     * Wraps the existing given {@link RDFList []-list} as {@link OntList ONT-list}.
     *
     * @param list            {@link RDFList}, not {@code null}
     * @param subject         {@link OntObject}, not {@code null}
     * @param predicate       {@link Property}, not {@code null}
     * @param checkRecursions boolean, if {@code true} more careful and expensive checking for list content is performed
     * @param listType        an uri-{@link Resource}, used as an archaic RDF-type, usually this parameter should be {@code null}
     * @param elementType     a {@code Class}-type for list element {@link E}, not {@code null}
     * @param <E>             any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> asOntList(RDFList list,
                                                        OntObject subject,
                                                        Property predicate,
                                                        boolean checkRecursions,
                                                        Resource listType,
                                                        Class<E> elementType) {
        OntListImpl.checkRequiredInputs(subject, predicate, list, listType, elementType);
        return checkRecursions ?
                OntListImpl.asSafeOntList(list, this, subject, predicate, listType, elementType) :
                OntListImpl.asOntList(list, this, subject, predicate, listType, elementType);
    }

    /**
     * Creates ONT-List with given elements and other settings.
     *
     * @param subject   {@link OntObject}, not {@code null}
     * @param predicate {@link Property}, not {@code null}
     * @param type      a {@code Class}-type for element {@link E}, not {@code null}
     * @param elements  and {@code Iterator} of {@link E}-elements (the order is preserved), not {@code null}
     * @param <E>       any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> createOntList(OntObject subject,
                                                            Property predicate,
                                                            Class<E> type,
                                                            Iterator<E> elements) {
        return createOntList(subject, predicate, null, type, elements);
    }

    /**
     * Creates ONT-List with given elements and other settings.
     *
     * @param subject     {@link OntObject}, not {@code null}
     * @param predicate   {@link Property}, not {@code null}
     * @param listType    an uri-{@link Resource}, used as an archaic RDF-type, usually this parameter should be {@code null}
     * @param elementType a {@code Class}-type for element {@link E}, not {@code null}
     * @param elements    and {@code Iterator} of {@link E}-elements (the order is preserved), not {@code null}
     * @param <E>         any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> createOntList(OntObject subject,
                                                            Property predicate,
                                                            Resource listType,
                                                            Class<E> elementType,
                                                            Iterator<E> elements) {
        OntListImpl.checkRequiredInputs(subject, predicate, listType, elementType);
        return OntListImpl.create(this, subject, predicate, listType, elementType, Iterators.create(elements));
    }

    /**
     * Lists all (bulk) annotation anonymous resources for the given {@code rdf:type} and SPO.
     *
     * @param t {@link Resource} either {@link OWL#Axiom owl:Axiom} or {@link OWL#Annotation owl:Annotation}
     * @param s {@link Resource} subject
     * @param p {@link Property} predicate
     * @param o {@link RDFNode} object
     * @return {@link ExtendedIterator} of annotation {@link Resource resource}s
     */
    public ExtendedIterator<Resource> listAnnotations(Resource t, Resource s, Property p, RDFNode o) {
        return getGraph().find(Node.ANY, OWL.annotatedSource.asNode(), s.asNode())
                .mapWith(this::asStatement)
                .filterKeep(x -> (OWL.Axiom == t ? x.belongsToOWLAxiom() : x.belongsToOWLAnnotation())
                        && x.hasAnnotatedProperty(p) && x.hasAnnotatedTarget(o))
                .mapWith(Statement::getSubject);
    }

    /**
     * Deletes the specified {@code OntList} including its annotations.
     *
     * @param subject   {@link OntObject} the subject of the OntList root statement
     * @param predicate {@link Property} the predicate of the OntList root statement
     * @param object    {@link OntList} to be deleted
     * @return this model instance
     */
    @SuppressWarnings("UnusedReturnValue")
    public OntGraphModelImpl deleteOntList(OntObject subject, Property predicate, OntList<?> object) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(predicate);
        OntJenaException.notNull(object, "Null list for subject " + subject + " and predicate " + predicate);
        boolean hasNil = !object.isNil() && contains(subject, predicate, RDF.nil);
        object.getMainStatement().clearAnnotations();
        object.clear(); // now it is nil-list
        if (!hasNil) {
            return remove(subject, predicate, object);
        }
        return this;
    }

    @Override
    public OntDisjoint.Classes createDisjointClasses(Collection<OntClass> classes) {
        checkType(OntDisjoint.Classes.class);
        return OntDisjointImpl.createDisjointClasses(this, classes.stream());
    }

    @Override
    public OntDisjoint.Individuals createDifferentIndividuals(Collection<OntIndividual> individuals) {
        checkType(OntDisjoint.Individuals.class);
        return OntDisjointImpl.createDifferentIndividuals(this, individuals.stream());
    }

    @Override
    public OntDisjoint.ObjectProperties createDisjointObjectProperties(Collection<OntObjectProperty> properties) {
        checkType(OntDisjoint.ObjectProperties.class);
        return OntDisjointImpl.createDisjointObjectProperties(this, properties.stream());
    }

    @Override
    public OntDisjoint.DataProperties createDisjointDataProperties(Collection<OntDataProperty> properties) {
        checkType(OntDisjoint.DataProperties.class);
        return OntDisjointImpl.createDisjointDataProperties(this, properties.stream());
    }

    @Override
    public <T extends OntFacetRestriction> T createFacetRestriction(Class<T> view, Literal literal) {
        checkType(OntFacetRestriction.class);
        return OntFacetRestrictionImpl.create(this, view, literal);
    }

    @Override
    public OntDataRange.OneOf createDataOneOf(Collection<Literal> values) {
        checkType(OntDataRange.OneOf.class);
        return OntDataRangeImpl.createOneOf(this, values.stream());
    }

    @Override
    public OntDataRange.Restriction createDataRestriction(OntDataRange.Named datatype, Collection<OntFacetRestriction> values) {
        checkType(OntDataRange.Restriction.class);
        return OntDataRangeImpl.createRestriction(this, datatype, values.stream());
    }

    @Override
    public OntDataRange.ComplementOf createDataComplementOf(OntDataRange other) {
        checkType(OntDataRange.ComplementOf.class);
        return OntDataRangeImpl.createComplementOf(this, other);
    }

    @Override
    public OntDataRange.UnionOf createDataUnionOf(Collection<OntDataRange> values) {
        checkType(OntDataRange.UnionOf.class);
        return OntDataRangeImpl.createUnionOf(this, values.stream());
    }

    @Override
    public OntDataRange.IntersectionOf createDataIntersectionOf(Collection<OntDataRange> values) {
        checkType(OntDataRange.IntersectionOf.class);
        return OntDataRangeImpl.createIntersectionOf(this, values.stream());
    }

    @Override
    public OntClass.ObjectSomeValuesFrom createObjectSomeValuesFrom(OntObjectProperty property, OntClass ce) {
        checkType(OntClass.ObjectSomeValuesFrom.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.ObjectSomeValuesFrom.class, property, ce, OWL.someValuesFrom);
    }

    @Override
    public OntClass.DataSomeValuesFrom createDataSomeValuesFrom(OntDataProperty property, OntDataRange dr) {
        checkType(OntClass.DataSomeValuesFrom.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.DataSomeValuesFrom.class, property, dr, OWL.someValuesFrom);
    }

    @Override
    public OntClass.ObjectAllValuesFrom createObjectAllValuesFrom(OntObjectProperty property, OntClass ce) {
        checkType(OntClass.ObjectAllValuesFrom.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.ObjectAllValuesFrom.class, property, ce, OWL.allValuesFrom);
    }

    @Override
    public OntClass.DataAllValuesFrom createDataAllValuesFrom(OntDataProperty property, OntDataRange dr) {
        checkType(OntClass.DataAllValuesFrom.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.DataAllValuesFrom.class, property, dr, OWL.allValuesFrom);
    }

    @Override
    public OntClass.ObjectHasValue createObjectHasValue(OntObjectProperty property, OntIndividual individual) {
        checkType(OntClass.ObjectHasValue.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.ObjectHasValue.class, property, individual, OWL.hasValue);
    }

    @Override
    public OntClass.DataHasValue createDataHasValue(OntDataProperty property, Literal literal) {
        checkType(OntClass.DataHasValue.class);
        return OntClassImpl.createComponentRestrictionCE(this, OntClass.DataHasValue.class, property, literal, OWL.hasValue);
    }

    @Override
    public OntClass.ObjectMinCardinality createObjectMinCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectMinCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.ObjectMinCardinality.class, property, cardinality, ce);
    }

    @Override
    public OntClass.DataMinCardinality createDataMinCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataMinCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.DataMinCardinality.class, property, cardinality, dr);
    }

    @Override
    public OntClass.ObjectMaxCardinality createObjectMaxCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectMaxCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.ObjectMaxCardinality.class, property, cardinality, ce);
    }

    @Override
    public OntClass.DataMaxCardinality createDataMaxCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataMaxCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.DataMaxCardinality.class, property, cardinality, dr);
    }

    @Override
    public OntClass.ObjectCardinality createObjectCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.ObjectCardinality.class, property, cardinality, ce);
    }

    @Override
    public OntClass.DataCardinality createDataCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this, OntClass.DataCardinality.class, property, cardinality, dr);
    }

    @Override
    public OntClass.UnionOf createObjectUnionOf(Collection<OntClass> classes) {
        checkType(OntClass.UnionOf.class);
        return OntClassImpl.createComponentsCE(this, OntClass.UnionOf.class, OntClass.class, OWL.unionOf, classes.stream());
    }

    @Override
    public OntClass.IntersectionOf createObjectIntersectionOf(Collection<OntClass> classes) {
        checkType(OntClass.IntersectionOf.class);
        return OntClassImpl.createComponentsCE(this,
                OntClass.IntersectionOf.class, OntClass.class, OWL.intersectionOf, classes.stream());
    }

    @Override
    public OntClass.OneOf createObjectOneOf(Collection<OntIndividual> individuals) {
        checkType(OntClass.OneOf.class);
        return OntClassImpl.createComponentsCE(this,
                OntClass.OneOf.class, OntIndividual.class, OWL.oneOf, individuals.stream());
    }

    @Override
    public OntClass.HasSelf createHasSelf(OntObjectProperty property) {
        checkType(OntClass.HasSelf.class);
        return OntClassImpl.createHasSelf(this, property);
    }

    @Override
    public OntClass.NaryDataAllValuesFrom createDataAllValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr) {
        checkType(OntClass.NaryDataAllValuesFrom.class);
        return OntClassImpl.createNaryRestrictionCE(this, OntClass.NaryDataAllValuesFrom.class, dr, properties);
    }

    @Override
    public OntClass.NaryDataSomeValuesFrom createDataSomeValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr) {
        checkType(OntClass.NaryDataSomeValuesFrom.class);
        return OntClassImpl.createNaryRestrictionCE(this, OntClass.NaryDataSomeValuesFrom.class, dr, properties);
    }

    @Override
    public OntClass.ComplementOf createObjectComplementOf(OntClass ce) {
        checkType(OntClass.ComplementOf.class);
        return OntClassImpl.createComplementOf(this, ce);
    }

    @Override
    public OntSWRL.Variable createSWRLVariable(String uri) {
        checkType(OntSWRL.Variable.class);
        return OntSWRLImpl.createVariable(this, uri);
    }

    @Override
    public OntSWRL.Atom.WithBuiltin createBuiltInSWRLAtom(Resource predicate, Collection<OntSWRL.DArg> arguments) {
        checkType(OntSWRL.Atom.WithBuiltin.class);
        return OntSWRLImpl.createBuiltInAtom(this, predicate, arguments);
    }

    @Override
    public OntSWRL.Atom.WithClass createClassSWRLAtom(OntClass clazz, OntSWRL.IArg arg) {
        checkType(OntSWRL.Atom.WithClass.class);
        return OntSWRLImpl.createClassAtom(this, clazz, arg);
    }

    @Override
    public OntSWRL.Atom.WithDataRange createDataRangeSWRLAtom(OntDataRange range, OntSWRL.DArg arg) {
        checkType(OntSWRL.Atom.WithDataRange.class);
        return OntSWRLImpl.createDataRangeAtom(this, range, arg);
    }

    @Override
    public OntSWRL.Atom.WithDataProperty createDataPropertySWRLAtom(OntDataProperty dataProperty,
                                                                    OntSWRL.IArg firstArg,
                                                                    OntSWRL.DArg secondArg) {
        checkType(OntSWRL.Atom.WithDataProperty.class);
        return OntSWRLImpl.createDataPropertyAtom(this, dataProperty, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithObjectProperty createObjectPropertySWRLAtom(OntObjectProperty dataProperty,
                                                                        OntSWRL.IArg firstArg,
                                                                        OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithObjectProperty.class);
        return OntSWRLImpl.createObjectPropertyAtom(this, dataProperty, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithDifferentIndividuals createDifferentIndividualsSWRLAtom(OntSWRL.IArg firstArg,
                                                                                    OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithDifferentIndividuals.class);
        return OntSWRLImpl.createDifferentIndividualsAtom(this, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithSameIndividuals createSameIndividualsSWRLAtom(OntSWRL.IArg firstArg,
                                                                          OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithSameIndividuals.class);
        return OntSWRLImpl.createSameIndividualsAtom(this, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Imp createSWRLImp(Collection<OntSWRL.Atom<?>> head,
                                     Collection<OntSWRL.Atom<?>> body) {
        checkType(OntSWRL.Atom.Imp.class);
        return OntSWRLImpl.createImp(this, head, body);
    }

    public RDFDatatype getRDFDatatype(String uri) {
        return dtTypes.computeIfAbsent(uri, u -> {
            RDFDatatype res = TypeMapper.getInstance().getTypeByName(u);
            return res == null ? new BaseDatatype(u) : res;
        });
    }

    public PrefixMapping getPrefixMapping() {
        return getGraph().getPrefixMapping();
    }

    @Override
    public OntGraphModelImpl setNsPrefix(String prefix, String uri) {
        getPrefixMapping().setNsPrefix(prefix, uri);
        return this;
    }

    @Override
    public OntGraphModelImpl removeNsPrefix(String prefix) {
        getPrefixMapping().removeNsPrefix(prefix);
        return this;
    }

    @Override
    public OntGraphModelImpl clearNsPrefixMap() {
        getPrefixMapping().clearNsPrefixMap();
        return this;
    }

    @Override
    public OntGraphModelImpl setNsPrefixes(PrefixMapping pm) {
        getPrefixMapping().setNsPrefixes(pm);
        return this;
    }

    @Override
    public OntGraphModelImpl setNsPrefixes(Map<String, String> map) {
        getPrefixMapping().setNsPrefixes(map);
        return this;
    }

    @Override
    public OntGraphModelImpl withDefaultMappings(PrefixMapping other) {
        getPrefixMapping().withDefaultMappings(other);
        return this;
    }

    @Override
    public OntGraphModelImpl lock() {
        getPrefixMapping().lock();
        return this;
    }

    @Override
    public OntGraphModelImpl add(Statement s) {
        super.add(s);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Statement s) {
        super.remove(s);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, RDFNode o) {
        super.add(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Resource s, Property p, RDFNode o) {
        super.remove(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Model m) {
        super.add(m);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Model m) {
        super.remove(m);
        return this;
    }

    @Override
    public OntGraphModelImpl add(StmtIterator iter) {
        super.add(iter);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(StmtIterator iter) {
        super.remove(iter);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Statement[] statements) {
        super.add(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Statement[] statements) {
        super.remove(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl add(List<Statement> statements) {
        super.add(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(List<Statement> statements) {
        super.remove(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl removeAll(Resource s, Property p, RDFNode o) {
        super.removeAll(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl removeAll() {
        super.removeAll();
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, boolean v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, long v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, int v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, char v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, float v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, double v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, Literal o) {
        super.addLiteral(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex) {
        super.add(s, p, lex);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex, RDFDatatype datatype) {
        super.add(s, p, lex, datatype);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex, boolean wellFormed) {
        super.add(s, p, lex, wellFormed);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex, String lang) {
        super.add(s, p, lex, lang);
        return this;
    }


    @Override
    public OntGraphModelImpl read(String url) {
        super.read(url);
        return this;
    }

    @Override
    public OntGraphModelImpl read(Reader reader, String base) {
        super.read(reader, base);
        return this;
    }

    @Override
    public OntGraphModelImpl read(InputStream reader, String base) {
        super.read(reader, base);
        return this;
    }

    @Override
    public OntGraphModelImpl read(String url, String lang) {
        super.read(url, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl read(String url, String base, String lang) {
        super.read(url, base, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl read(Reader reader, String base, String lang) {
        super.read(reader, base, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl read(InputStream reader, String base, String lang) {
        super.read(reader, base, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl write(Writer writer) {
        getBaseModel().write(writer);
        return this;
    }

    @Override
    public OntGraphModelImpl write(Writer writer, String lang) {
        getBaseModel().write(writer, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl write(Writer writer, String lang, String base) {
        getBaseModel().write(writer, lang, base);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out) {
        getBaseModel().write(out);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out, String lang) {
        getBaseModel().write(out, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out, String lang, String base) {
        getBaseModel().write(out, lang, base);
        return this;
    }

    @Override
    public OntAnnotationProperty getRDFSComment() {
        return findNodeAs(RDFS.Nodes.comment, OntAnnotationProperty.class);
    }

    @Override
    public OntAnnotationProperty getRDFSLabel() {
        return findNodeAs(RDFS.Nodes.label, OntAnnotationProperty.class);
    }

    @Override
    public OntClass.Named getOWLThing() {
        return findNodeAs(OWL.Thing.asNode(), OntClass.Named.class);
    }

    @Override
    public OntDataRange.Named getRDFSLiteral() {
        return findNodeAs(RDFS.Literal.asNode(), OntDataRange.Named.class);
    }

    @Override
    public OntClass.Named getOWLNothing() {
        return findNodeAs(OWL.Nothing.asNode(), OntClass.Named.class);
    }

    @Override
    public OntObjectProperty.Named getOWLTopObjectProperty() {
        return findNodeAs(OWL.topObjectProperty.asNode(), OntObjectProperty.Named.class);
    }

    @Override
    public OntObjectProperty.Named getOWLBottomObjectProperty() {
        return findNodeAs(OWL.bottomObjectProperty.asNode(), OntObjectProperty.Named.class);
    }

    @Override
    public OntDataProperty getOWLTopDataProperty() {
        return findNodeAs(OWL.topDataProperty.asNode(), OntDataProperty.class);
    }

    @Override
    public OntDataProperty getOWLBottomDataProperty() {
        return findNodeAs(OWL.bottomDataProperty.asNode(), OntDataProperty.class);
    }

    /**
     * Returns the {@link Reasoner} which is being used to answer queries to this graph
     * or {@code null} if reasoner is not supported by the model.
     */
    @Override
    public Reasoner getReasoner() {
        InfGraph g = getInfGraph();
        return g != null ? g.getReasoner() : null;
    }

    /**
     * Switches on/off derivation logging.
     * If this option is enabled, then each time a derivation is made, that fact is recorded,
     * and the resulting record can be accessed through a later call to getDerivation.
     * This can take up a lot of space!
     */
    @Override
    public void setDerivationLogging(boolean logOn) {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.setDerivationLogging(logOn);
        }
    }

    /**
     * Returns the derivation of the given statement (which should be the result of some previous list operation).
     * Not all reasoners support derivations.
     *
     * @param statement {@link Statement} to get derivation information
     * @return an iterator over {@code Derivation} records or {@code null} if there is no derivation information
     * available for this triple
     * @see Derivation
     */
    @Override
    public Iterator<Derivation> getDerivation(Statement statement) {
        return (getGraph() instanceof InfGraph) ? ((InfGraph) getGraph()).getDerivation(statement.asTriple()) : null;
    }

    /**
     * Returns {@link InfGraph} or {@code null} if no-inf model
     */
    public InfGraph getInfGraph() {
        return graph instanceof InfGraph ? (InfGraph) graph : null;
    }

    /**
     * Causes the inference model to reconsult the underlying data to take into account changes.
     * Normally, changes are made through the InfModel's, add and remove calls are will be handled appropriately.
     * However, in some cases, changes are made "behind the InfModel's back and
     * this forces a full reconsult of the changed data.
     */
    @Override
    public void rebind() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.rebind();
        }
    }

    /**
     * Performs any initial processing and caching.
     * This call is optional.
     * Most engines either have negligible set-up work or will perform an implicit "prepare" if necessary.
     * The call is provided for those occasions where substantial preparation work is possible
     * (e.g. running a forward chaining rule system)
     * and where an application might wish greater control over when
     * this preparation is done rather than just leaving to be done at first query time.
     */
    @Override
    public void prepare() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.prepare();
        }
    }

    /**
     * Resets any internal caches.
     * Some systems, such as the tabled backchainer, retain information after each query.
     * A reset will wipe this information preventing unbounded memory use at the expense of more expensive future queries.
     * A reset does not cause the raw data to be reconsulted and so is less expensive than a rebind.
     */
    @Override
    public void reset() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.reset();
        }
    }

    /**
     * Tests the consistency of the underlying data.
     * This normally tests the validity of the bound instance data against the bound schema data.
     *
     * @return a {@link ValidityReport} structure
     */
    @Override
    public ValidityReport validate() {
        InfGraph graph = getInfGraph();
        return graph != null ? graph.validate() : null;
    }

    /**
     * Finds all the statements matching a pattern.
     * Returns an iterator over all the statements in a model that match a pattern.
     * <p>
     * The SPO terms may refer to resources which are temporarily defined in the "posit" model.
     * This allows one, for example, to query what resources are of type CE where CE is a
     * class expression rather than a named class - put CE in the posit arg.</p>
     *
     * @param subject   The subject sought
     * @param predicate The predicate sought
     * @param object    The value sought
     * @param posit     Model containing additional assertions to be considered when matching statements
     * @return an iterator over the subjects
     */
    @Override
    public StmtIterator listStatements(Resource subject, Property predicate, RDFNode object, Model posit) {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            Graph gp = posit == null ? GraphMemFactory.createGraphMem() : posit.getGraph();
            Iterator<Triple> iter = graph.find(asNode(subject), asNode(predicate), asNode(object), gp);
            return IteratorFactory.asStmtIterator(iter, this);
        } else {
            return null;
        }
    }

    @Override
    public InfModel asInferenceModel() {
        return this;
    }

    /**
     * Returns a derivations model.
     * The rule reasoners typically create a graph containing those triples added to the base graph due to rule firings.
     * In some applications, it can be useful to be able to access those deductions directly,
     * without seeing the raw data which triggered them.
     * In particular, this allows the forward rules to be used as if they were rewrite transformation rules.
     *
     * @return The derivation model, if one is defined, or else {@code null}
     */
    @Override
    public Model getDeductionsModel() {
        if (deductionsModel == null) {
            InfGraph infGraph = getInfGraph();
            if (infGraph != null) {
                Graph deductionsGraph = infGraph.getDeductionsGraph();
                if (deductionsGraph != null) {
                    deductionsModel = new ModelCom(deductionsGraph);
                }
            }
        } else {
            // ensure that the cached model sees the updated changes from the underlying reasoner graph
            Objects.requireNonNull(getInfGraph()).prepare();
        }
        return deductionsModel;
    }

    /**
     * Returns the raw RDF model being processed
     * (i.e. the argument to the {@link Reasoner#bind(Graph)} call that created this {@link InfModel}).
     */
    @Override
    public Model getRawModel() {
        return getBaseModel();
    }

    private Model getReasonerCapabilities() {
        Reasoner reasoner = getReasoner();
        return reasoner != null ? reasoner.getReasonerCapabilities() : null;
    }

    /**
     * Returns a {@link RDFNode} for the given type and, if the result is present, caches it node at the model level.
     * The method works silently: normally no exception is expected.
     *
     * @param node {@link Node}
     * @param type {@link Class}-type
     * @param <N>  any subtype of {@link RDFNode}
     * @return {@link RDFNode} or {@code null}
     * @throws RuntimeException unexpected misconfiguration (RDF recursion, wrong input, personality mismatch, etc.)
     * @see #getNodeAs(Node, Class)
     */
    @Override
    public <N extends RDFNode> N findNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAs(node, type);
        } catch (OntJenaException.Conversion ignore) {
            // ignore
            return null;
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node, cannot be {@code null}
     * @throws OntJenaException unable to construct new RDF view for whatever reason
     * @throws RuntimeException unexpected misconfiguration (wrong inputs, personality mismatch)
     */
    @Override
    public <N extends RDFNode> N getNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAsInternal(node, type);
        } catch (OntJenaException e) {
            throw e;
        } catch (Exception e) {
            throw new OntJenaException.Conversion(String.format(
                    "Failed to convert node <%s> to <%s>. Profile <%s>",
                    node, OntEnhNodeFactories.viewAsString(type), getOntPersonality().getName()
            ), e);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type,
     * taking into account possible graph recursions.
     * For internal usage only.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node or {@code null} if no match found
     * @throws OntJenaException.Recursion if a graph recursion is detected
     * @throws RuntimeException           unexpected misconfiguration
     * @see #getNodeAs(Node, Class)
     */
    @Override
    public <N extends RDFNode> N safeFindNodeAs(Node node, Class<N> type) {
        Set<Node> nodes = visited.get();
        try {
            if (nodes.add(node)) {
                return getNodeAsInternal(node, type);
            }
            throw new OntJenaException.Recursion("Can't cast to " + OntEnhNodeFactories.viewAsString(type) + ": " +
                    "graph contains a recursion for node <" + node + ">");
        } catch (OntJenaException.Conversion | PersonalityConfigException r) {
            return null;
        } finally {
            nodes.remove(node);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node
     * @throws org.apache.jena.enhanced.PersonalityConfigException if personality is misconfigured
     *                                                             or the given {@code type} is absent in it;
     *                                                             normally this should not happen
     * @throws NullPointerException                                if any input is {@code null}
     * @throws JenaException                                       unable to construct a new RDF view
     */
    protected <N extends RDFNode> N getNodeAsInternal(Node node, Class<N> type) {
        return super.getNodeAs(Objects.requireNonNull(node, "Null node"),
                Objects.requireNonNull(type, "Null class view."));
    }

    @Override
    public String toString() {
        return String.format("OntGraphModel{%s}", Graphs.getName(getBaseGraph()));
    }

}
