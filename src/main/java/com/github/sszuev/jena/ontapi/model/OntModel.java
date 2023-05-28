package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.vocabulary.RDFS;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An enhanced view of a {@link Model Jena Model} about which is known to contain <b>OWL</b> ontology data.
 * The view supports OWL2 DL specification,
 * and is an analogue of {@link org.apache.jena.ontology.OntModel Apache Jena OntModel}, which only covers the OWL1 syntax.
 * <p>
 * In addition to the standard {@link Resource Jena Resource}s and {@link Statement Jena Statement}s
 * this model provides access to different ontological components in the form of {@link OntObject Object}s
 * and {@link OntStatement Ontology Statement}s that support OWL Annotations.
 * Some of the {@link OntObject}s can be constructed using another kind of resource -
 * {@link OntList}, which is an extended analogue of the standard {@link RDFList Jena RDFList}.
 * <p>
 * The model also has a component-level support of Semantic Web Rule Language (SWRL).
 * <p>
 * In additional to native Jena {@link org.apache.jena.util.iterator.ExtendedIterator Extended Iterator}s,
 * this model also provides access to RDF in the form of {@link Stream}s, that obey the same rules:
 * both {@code Stream} and {@code ExtendedIterator} must be closed explicitly
 * if they are no longer needed but not yet exhausted, see {@link org.apache.jena.util.iterator.ClosableIterator}.
 * <p>
 * Impl note: this model does not support {@link org.apache.jena.ontology.Profile Jena Profile}s mechanism,
 * and model configuration is delegated directly to the extended {@link OntPersonality Personality}.
 * <p>
 * Created by @ssz on 11.11.2016.
 *
 * @see org.apache.jena.ontology.OntModel
 * @see <a href='https://www.w3.org/TR/owl2-mapping-to-rdf'>OWL2 RDF mapping</a>
 * @see <a href='https://www.w3.org/TR/owl2-quick-reference/'>A Quick Guide</a>
 * @see <a href='https://www.w3.org/TR/owl2-syntax/'>OWL 2 Web Ontology Language Structural Specification and Functional-Style Syntax (Second Edition)</a>
 * @see <a href='https://www.w3.org/Submission/SWRL/'>SWRL: A Semantic Web Rule Language Combining OWL and RuleML</a>
 */
public interface OntModel extends Model,
        MutableModel<OntModel>, PrefixedModel<OntModel>, IOModel<OntModel>,
        CreateClasses, CreateRanges, CreateDisjoint, CreateSWRL {

    /**
     * Returns the base {@code Graph},
     * i.e. the primary ontological {@code Graph} that does not contain any sub-graphs hierarchy.
     * Only the base graph can be edited from this interface view.
     * To get the whole union graph use the method {@link #getGraph()}.
     *
     * @return {@link Graph}
     * @see #getGraph()
     */
    Graph getBaseGraph();

    /**
     * Finds an Ontology ID object.
     * <p>
     * Since OWL2 graph can only contain single {@code @uri rdf:type owl:Ontology} triple inside itself,
     * in case there are more than one such {@code Resource}s with the type {@link OWL#Ontology owl:Ontology},
     * the method chooses the most bulky one (i.e. those that contains the largest number of associated statements)
     * and all the other triples leave intact.
     * No changes in the {@code Graph} is made.
     * The method works only with the {@link #getBaseGraph() base graph}.
     *
     * @return an {@code Optional} that contains the {@link OntID}
     * @see #setID(String)
     * @see Graphs#ontologyNode
     */
    Optional<OntID> id();

    /**
     * Creates a new {@code @uri rdf:type owl:Ontology} statement for the specified {@code uri}
     * and wraps it as Ontology ID Resource.
     * Removes all extra ontology objects if they are present and moves their content to the new one,
     * as it is required by OWL2 specification.
     *
     * @param uri String, can be {@code null} to make this ontology to be anonymous
     * @return the new {@link OntID} instance
     * @throws OntJenaException if ontology can't be added (e.g. due to collision with imports)
     * @see #getID()
     */
    OntID setID(String uri);

    /**
     * Adds a sub model both to the {@code owl:import} section and to the graph hierarchy.
     *
     * @param m {@link OntModel ont jena model} to add, not {@code null}
     * @return this model to allow cascading calls
     * @throws OntJenaException if specified ontology is anonymous
     *                          or already present in the imports (both as graph and in owl-declaration)
     * @see OntID#addImport(String)
     */
    OntModel addImport(OntModel m) throws OntJenaException;

    /**
     * Removes a sub-model from {@code owl:import} and from the graph hierarchy.
     * Does nothing, if the specified model does not belong to this ontology.
     * Matching is performed by graph, not uri (see {@link #hasImport(OntModel)} description).
     *
     * @param m {@link OntModel ont jena model} to remove, not {@code null}
     * @return <b>this</b> model to allow cascading calls
     * @see OntID#removeImport(String)
     * @see #hasImport(OntModel)
     */
    OntModel removeImport(OntModel m);

    /**
     * Removes the import (both {@code owl:import} declaration and the corresponding graph)
     * by the given uri if it is found.
     *
     * @param uri String, an iri of ontology to find, not {@code null}
     * @return <b>this</b> model to allow cascading calls
     * @see OntID#getImportsIRI()
     * @see #hasImport(String)
     */
    OntModel removeImport(String uri);

    /**
     * Lists all sub-models
     * that belong to the top-level hierarchy and have {@code owl:import} reference inside the base graph.
     * Caution: since recursive hierarchies are not prohibited,
     * the rectilinear usage of this method may cause a StackOverflow Error.
     *
     * @return {@code Stream} of {@link OntModel}s
     * @see OntID#imports()
     */
    Stream<OntModel> imports();

    /**
     * Answers {@code true} if the given model is present in the {@link OWL#imports owl:imports} of this model.
     * This means that at the top-level of the import hierarchy there is a base graph of the given {@code other} model.
     * Please note: the model may contain the same uri as that of the specified model, but a different (base) graph,
     * i.e. if the method {@link #hasImport(String)} returns {@code true},
     * it does not mean this method also returns {@code true}.
     *
     * @param other {@link OntModel} to test, not {@code null}
     * @return {@code true} if the model is in imports
     */
    boolean hasImport(OntModel other);

    /**
     * Answers {@code true} if the model has a graph with the given uri both in {@code owl:imports} and graph-hierarchy.
     *
     * @param uri String, not {@code null}
     * @return boolean
     * @see OntID#getImportsIRI()
     */
    boolean hasImport(String uri);

    /**
     * Lists all ont-objects of the specified type.
     *
     * @param type {@link Class} the concrete type of {@link OntObject}, not {@code null}
     * @param <O>  any ont-object subtype
     * @return {@code Stream} of {@link OntObject}s of the type {@link O}
     * @see #ontEntities()
     */
    <O extends OntObject> Stream<O> ontObjects(Class<? extends O> type);

    /**
     * Lists all entities declared in the model.
     * Built-ins are not included.
     * The retrieved entities can belong to the underlying graphs also.
     * Note: this method returns non-distinct stream,
     * while the expression {@code ontObjects(OntEntity.class)} is supposed to be distinct stream.
     * The duplicate elements (by {@code equals} and {@code hasCode}, not by real class-type)
     * means that there is so-called punning.
     *
     * @return {@code Stream} of {@link OntEntity}
     * @see #ontObjects(Class)
     * @see #ontEntities(Class)
     */
    Stream<OntEntity> ontEntities();

    /**
     * Lists all class-asserted individuals.
     * <p>
     * A class assertion axiom is a statement {@code a rdf:type C},
     * where {@code a} is a retrieving individual (named or anonymous) and {@code C} is any class expression.
     * Notice, that the method {@link OntModel#ontObjects(Class)}
     * called with the parameter {@code OntIndividual.class}
     * (i.e. {@code model.ontObject(OntIndividual.class)}) must return all individuals from a model,
     * even those which have no explicit declarations (e.g. any part of {@code owl:sameAs} is an individual),
     * while this method returns only class-asserted individuals.
     * Also notice: the method {@link #namedIndividuals()} must return only explicitly declared named individuals,
     * while this method does not require the declaration {@link OWL#NamedIndividual owl:NamedIndividual}
     * to be present for an individual: according to the specification it is optional, for more details see
     * <a href="https://www.w3.org/TR/owl2-syntax/#Typing_Constraints_of_OWL_2_DL">5.8.1 Typing Constraints of OWL 2 DL</a>.
     * Also note: in case of valid distinct {@link #getGraph() RDF graph}
     * the returned {@code Stream} is also distinct,
     * which means an individual that has more than one class assertions, must appear in the stream only once.
     *
     * @return {@code Stream} of {@link OntIndividual}s
     * @see OntModel#namedIndividuals()
     */
    Stream<OntIndividual> individuals();

    /**
     * Returns an ont-entity for the specified type and uri.
     * This method can also be used to wrap builtin entities, which, in fact, does not belong to the graph,
     * but can be considered as belonged to the model.
     * An IRI for such a built-in entity must be in
     * the {@link OntPersonality.Builtins Builtins Vocabulary},
     * otherwise the method returns {@code null}.
     *
     * @param type {@link Class}, the type of {@link OntEntity}, not {@code null}.
     * @param uri, String, not {@code null}.
     * @param <E>  type of ont-entity
     * @return {@link OntEntity} or {@code null}
     * @see #fetchOntEntity(Class, String)
     */
    <E extends OntEntity> E getOntEntity(Class<E> type, String uri);

    /**
     * Lists all ont-statements.
     *
     * @return {@code Stream} of {@link OntStatement}
     * @see Model#listStatements()
     */
    Stream<OntStatement> statements();

    /**
     * Lists all statements for the specified subject, predicate and object (SPO).
     *
     * @param s {@link Resource}, the subject
     * @param p {@link Property}, the predicate
     * @param o {@link RDFNode}, the object
     * @return {@code Stream} of {@link OntStatement}
     * @see Model#listStatements(Resource, Property, RDFNode)
     */
    Stream<OntStatement> statements(Resource s, Property p, RDFNode o);

    /**
     * Lists all statements from the {@link OntModel#getBaseGraph() base graph}
     * for the specified subject, predicate and object.
     * Effectively equivalent to the {@code model.statements(s, p, o).filter(OntStatement::isLocal)} expression.
     *
     * @param s {@link Resource}, the subject
     * @param p {@link Property}, the predicate
     * @param o {@link RDFNode}, the object
     * @return {@code Stream} of {@link OntStatement}
     * @see OntModel#statements(Resource, Property, RDFNode)
     * @see OntStatement#isLocal()
     */
    Stream<OntStatement> localStatements(Resource s, Property p, RDFNode o);

    /**
     * Answers an {@link OntStatement Ontology Statement} in this model who's SPO is that of the {@code triple}.
     *
     * @param triple {@link Triple}, not {@code null}
     * @return {@link OntStatement}
     */
    @Override
    OntStatement asStatement(Triple triple);

    /**
     * Answers iff the statement belongs to the base graph.
     *
     * @param statement {@link Statement}
     * @return true if statement is local.
     * @see OntStatement#isLocal()
     * @see OntObject#isLocal()
     * @see OntModel#localStatements(Resource, Property, RDFNode)
     */
    boolean isLocal(Statement statement);

    /**
     * Removes the given {@link OntObject Ontology Object} from the graph-model
     * including its {@link OntObject#content() content} and annotations.
     * This operation does not guarantee clearing all object references:
     * it takes into account only statements where the given object in a subject position.
     * For example, in case of deleting an OWL class
     * that is on the right side in a statement with the predicate {@code rdfs:subClassOf},
     * that statement remains unchanged in the graph, but becomes meaningless:
     * its right side will no longer be a class, but just uri.
     * But if a class is on the left side of the statement with the {@code rdfs:subClassOf} predicate,
     * that statement is being removed from the graph along with its annotations,
     * because it belongs to the class content.
     *
     * @param obj {@link OntObject}
     * @return <b>this</b> model
     * @see OntObject#content()
     */
    OntModel removeOntObject(OntObject obj);

    /**
     * Removes the statement from the graph-model including its annotations with sub-annotations hierarchy.
     *
     * @param statement {@link OntStatement}
     * @return <b>this</b> model
     * @see #remove(Statement)
     */
    OntModel removeOntStatement(OntStatement statement);

    /**
     * Creates an owl-entity by the {@code type} and {@code iri}.
     *
     * @param type {@link Class}, the type of {@link OntEntity}, not {@code null}
     * @param iri  String, not {@code null}
     * @param <E>  type of ont-entity
     * @return {@link OntEntity}
     * @throws OntJenaException.Creation in case something is wrong
     * @see #getOntEntity(Class, String)
     */
    <E extends OntEntity> E createOntEntity(Class<E> type, String iri);

    /**
     * Creates a facet restriction by the given type and literal value.
     * Each call to this method creates a fresh b-node within the graph.
     *
     * @param type    {@link Class}, the type of {@link OntFacetRestriction}, not {@code null}
     * @param literal {@link Literal}, not {@code null}
     * @param <F>     type of ont-facet-restriction
     * @return {@link OntFacetRestriction}
     * @see OntDataRange.Restriction
     * @see OntModel#createDataRestriction(OntDataRange.Named, Collection)
     */
    <F extends OntFacetRestriction> F createFacetRestriction(Class<F> type, Literal literal);

    /**
     * Returns the {@link Model standard jena model} that corresponds to the {@link #getBaseGraph() base graph}.
     * Note: there is the {@link org.apache.jena.enhanced.BuiltinPersonalities#model Jena Builtin Personality}
     * within the returned model.
     *
     * @return {@link Model}
     * @see #getBaseGraph()
     */
    Model getBaseModel();

    /**
     * Creates an inference model shadow using this model as data.
     * Note(1): there is the {@link org.apache.jena.enhanced.BuiltinPersonalities#model Jena Builtin Personality}
     * within the returned model.
     * Note(2): any changes in the returned {@link InfModel Inference Model} do not affect on this model.
     *
     * @param reasoner {@link Reasoner}, not {@code null}
     * @return {@link InfModel}
     * @throws org.apache.jena.reasoner.ReasonerException if the data is ill-formed according to the
     *                                                    constraints imposed by this reasoner.
     */
    InfModel getInferenceModel(Reasoner reasoner);

    /*
     * ===================================
     * Default methods for simplification:
     * ===================================
     */

    /**
     * Gets the Ontology ID object.
     * <p>
     * Since OWL2 graph can only contain the one {@code @uri rdf:type owl:Ontology} triple inside,
     * this method creates such statement if it absent;
     * in case there are more than one {@code Resource} with the type equaled to {@link OWL#Ontology owl:Ontology},
     * it chooses the most bulky one (i.e. those that contains the largest number of associated statements)
     * and all the others leave intact.
     *
     * @return {@link OntID} an existing or fresh {@link Resource},
     * that is subject in the {@code _:x rdf:type owl:Ontology} statement
     */
    default OntID getID() {
        return id().orElseGet(() -> createResource(OWL.Ontology).as(OntID.class));
    }

    default OntClass.Named createOntClass(String uri) {
        return createOntEntity(OntClass.Named.class, uri);
    }

    default OntDataRange.Named createDatatype(String uri) {
        return createOntEntity(OntDataRange.Named.class, uri);
    }

    default OntIndividual.Named createIndividual(String uri) {
        return createOntEntity(OntIndividual.Named.class, uri);
    }

    default OntAnnotationProperty createAnnotationProperty(String uri) {
        return createOntEntity(OntAnnotationProperty.class, uri);
    }

    default OntDataProperty createDataProperty(String uri) {
        return createOntEntity(OntDataProperty.class, uri);
    }

    default OntObjectProperty.Named createObjectProperty(String uri) {
        return createOntEntity(OntObjectProperty.Named.class, uri);
    }

    default OntClass.Named getOntClass(String uri) {
        return getOntEntity(OntClass.Named.class, uri);
    }

    default OntDataRange.Named getDatatype(String uri) {
        return getOntEntity(OntDataRange.Named.class, uri);
    }

    default OntIndividual.Named getIndividual(String uri) {
        return getOntEntity(OntIndividual.Named.class, uri);
    }

    default OntAnnotationProperty getAnnotationProperty(String uri) {
        return getOntEntity(OntAnnotationProperty.class, uri);
    }

    default OntDataProperty getDataProperty(String uri) {
        return getOntEntity(OntDataProperty.class, uri);
    }

    default OntObjectProperty.Named getObjectProperty(String uri) {
        return getOntEntity(OntObjectProperty.Named.class, uri);
    }

    default OntClass.Named getOntClass(Resource uri) {
        return getOntClass(uri.getURI());
    }

    default OntDataRange.Named getDatatype(Resource uri) {
        return getDatatype(uri.getURI());
    }

    default OntIndividual.Named getIndividual(Resource uri) {
        return getIndividual(uri.getURI());
    }

    default OntAnnotationProperty getAnnotationProperty(Resource uri) {
        return getAnnotationProperty(uri.getURI());
    }

    default OntDataProperty getDataProperty(Resource uri) {
        return getDataProperty(uri.getURI());
    }

    default OntObjectProperty.Named getObjectProperty(Resource uri) {
        return getObjectProperty(uri.getURI());
    }

    default Stream<OntStatement> localStatements() {
        return localStatements(null, null, null);
    }

    default <E extends OntEntity> Stream<E> ontEntities(Class<E> type) {
        return ontObjects(type);
    }

    /**
     * Retrieves a {@link OntDataRange.Named datatype} from the given literal.
     *
     * @param literal {@link Literal}, not {@code null}
     * @return {@link OntDataRange.Named}
     */
    default OntDataRange.Named getDatatype(Literal literal) {
        String uri = literal.getDatatypeURI();
        if (uri != null) {
            return getDatatype(uri);
        }
        String lang = literal.getLanguage();
        if (lang != null && !lang.isEmpty()) {
            return getDatatype(RDF.langString);
        }
        return getDatatype(XSD.xstring);
    }

    /**
     * Returns an entity of the given type and with the specified URI, creating it if needed.
     *
     * @param type a class-type of entity
     * @param uri  String uri, not {@code null}
     * @param <E>  any subtype of {@link OntEntity}
     * @return {@link E}
     */
    default <E extends OntEntity> E fetchOntEntity(Class<E> type, String uri) {
        E res = getOntEntity(type, uri);
        return res == null ? createOntEntity(type, uri) : res;
    }

    /**
     * Lists all named class expressions (OWL classes).
     *
     * @return {@code Stream} of {@link OntClass.Named Ontology Class}es
     */
    default Stream<OntClass.Named> classes() {
        return ontEntities(OntClass.Named.class);
    }

    /**
     * Answers a {@code Stream} over the classes in this ontology model
     * that represent the uppermost nodes of the class hierarchy.
     *
     * @return a {@code Stream} of the root {@link OntClass classes} in the local class hierarchy
     */
    default Stream<OntClass> hierarchyRoots() {
        return ontObjects(OntClass.class)
                .filter(c -> !c.isURIResource() || !c.asNamed().isBuiltIn())
                .filter(OntClass::isHierarchyRoot);
    }

    /**
     * Lists all annotation properties.
     *
     * @return {@code Stream} of {@link OntAnnotationProperty Annotation Property}s
     */
    default Stream<OntAnnotationProperty> annotationProperties() {
        return ontEntities(OntAnnotationProperty.class);
    }

    /**
     * Lists all OntProperties.
     *
     * @return {@code Stream} of {@link OntProperty OntProperty}s
     */
    default Stream<OntProperty> properties() {
        return ontObjects(OntProperty.class);
    }

    /**
     * Lists all data properties.
     *
     * @return {@code Stream} of {@link OntDataProperty Data Property}s
     */
    default Stream<OntDataProperty> dataProperties() {
        return ontEntities(OntDataProperty.class);
    }

    /**
     * Lists all named object property expressions (object properties in short).
     *
     * @return {@code Stream} of {@link OntObjectProperty.Named Named Object Property}s
     */
    default Stream<OntObjectProperty.Named> objectProperties() {
        return ontEntities(OntObjectProperty.Named.class);
    }

    /**
     * Lists all datatypes (named data range expressions).
     *
     * @return {@code Stream} of {@link OntDataRange.Named Ontology Datatype}s
     */
    default Stream<OntDataRange.Named> datatypes() {
        return ontEntities(OntDataRange.Named.class);
    }

    /**
     * Lists all named individuals,
     * i.e. all those individuals which have explicitly {@link OWL#NamedIndividual owl:NamedIndividual} declaration.
     *
     * @return {@code Stream} of {@link OntIndividual.Named Named Individual}s
     * @see #individuals()
     * @see OntClass#individuals()
     */
    default Stream<OntIndividual.Named> namedIndividuals() {
        return ontEntities(OntIndividual.Named.class);
    }

    /**
     * Returns {@link OntEntity OWL Entity} with the specified class-type and {@code uri}.
     *
     * @param type {@code Class}, not {@code null}
     * @param uri  {@link Resource}, must be URI, not {@code null}
     * @param <E>  any {@link OntEntity} subtype, not {@code null}
     * @return a {@link E} instance
     */
    default <E extends OntEntity> E getOntEntity(Class<E> type, Resource uri) {
        return getOntEntity(type, uri.getURI());
    }

    /*
     * ===================================
     * Some common built-in OWL2 entities:
     * ===================================
     */

    default OntAnnotationProperty getRDFSComment() {
        return getAnnotationProperty(RDFS.comment);
    }

    default OntAnnotationProperty getRDFSLabel() {
        return getAnnotationProperty(RDFS.label);
    }

    default OntClass.Named getOWLThing() {
        return getOntClass(OWL.Thing);
    }

    default OntClass.Named getOWLNothing() {
        return getOntClass(OWL.Nothing);
    }

    default OntDataRange.Named getRDFSLiteral() {
        return getDatatype(RDFS.Literal);
    }

    default OntObjectProperty.Named getOWLTopObjectProperty() {
        return getObjectProperty(OWL.topObjectProperty);
    }

    default OntObjectProperty.Named getOWLBottomObjectProperty() {
        return getObjectProperty(OWL.bottomObjectProperty);
    }

    default OntDataProperty getOWLTopDataProperty() {
        return getDataProperty(OWL.topDataProperty);
    }

    default OntDataProperty getOWLBottomDataProperty() {
        return getDataProperty(OWL.bottomDataProperty);
    }
}
