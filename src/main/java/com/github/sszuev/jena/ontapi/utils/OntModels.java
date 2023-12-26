package com.github.sszuev.jena.ontapi.utils;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.UnionGraph;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntListImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntStatementImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntID;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.model.RDFNodeList;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A collection of utilitarian methods to work with {@link OntModel OWL Model} and all its related stuff:
 * {@link OntObject Ontology Object},
 * {@link OntEntity Ontology Entity},
 * {@link RDFNodeList Node List},
 * {@link OntStatement Ontology Statement}.
 * <p>
 * Created by @szz on 11.06.2019.
 */
public class OntModels {

    /**
     * Determines the actual ontology object type.
     *
     * @param object instance of {@link O}
     * @param <O>    any subtype of {@link OntObject}
     * @return {@link Class}-type of {@link O}
     */
    @SuppressWarnings("unchecked")
    public static <O extends OntObject> Class<O> getOntType(O object) {
        Class<O> res;
        if (object instanceof OntObjectImpl) {
            res = (Class<O>) object.objectType();
        } else {
            res = (Class<O>) OntObjectImpl.findActualClass(object);
        }
        return OntJenaException.notNull(res, "Can't determine the type of object " + object);
    }

    /**
     * Creates an anonymous individual for the given {@link RDFNode RDF Node}, that must be associated with a model.
     * The result anonymous individual could be true (i.e. instance of some owl class)
     * or fake (any blank node can be represented as it).
     *
     * @param inModel {@link RDFNode}, not {@code null}
     * @return {@link OntIndividual.Anonymous}
     * @throws OntJenaException if the node cannot be present as anonymous individual
     */
    public static OntIndividual.Anonymous asAnonymousIndividual(RDFNode inModel) {
        return OntIndividualImpl.createAnonymousIndividual(inModel);
    }

    /**
     * Inserts the given ontology in the dependencies of each ontology from the specified collection,
     * provided as {@code Supplier} (the {@code manager} parameter).
     * Can be used to fix missed graph links or
     * to replace existing dependency with the new one in case {@code replace} is {@code true}.
     *
     * @param manager the collection of other ontologies in form of {@link Supplier} that answers a {@code Stream}
     * @param ont     {@link OntModel} the ontology to insert, must be named
     * @param replace if {@code true} then any existing graph,
     *                that is linked through the {@code owl:import} declaration,
     *                will be replaced with the given graph,
     *                otherwise the graph will be inserted only if
     *                there is a declaration {@code owl:import} without any graph associated
     * @see OntID#getImportsIRI()
     */
    public static void insert(Supplier<Stream<OntModel>> manager, OntModel ont, boolean replace) {
        String uri = Objects.requireNonNull(ont.getID().getImportsIRI(), "Must be named ontology");
        manager.get()
                .filter(m -> {
                    // select only those, that have the uri in owl:imports:
                    try (Stream<String> uris = m.getID().imports()) {
                        return uris.anyMatch(uri::equals);
                    }
                })
                .peek(m -> {
                    if (!replace) return;
                    // remove a first found previously associated graph:
                    m.imports()
                            .filter(i -> uri.equals(i.getID().getImportsIRI()))
                            .findFirst()
                            .ifPresent(i -> ((UnionGraph) m.getGraph()).removeParent(i.getGraph()));
                })
                .filter(m -> m.imports().map(OntModel::getID).map(OntID::getImportsIRI).noneMatch(uri::equals))
                .forEach(m -> m.addImport(ont));
    }

    /**
     * Synchronizes the import declarations with the graph hierarchy.
     * Underling graph tree may content named graphs which are not included to the {@code owl:imports} declaration.
     * This method tries to fix such a situation by modifying base graph.
     *
     * @param m {@link OntModel}, not {@code null}
     * @throws StackOverflowError in case the given model has a recursion in the hierarchy
     * @see Graphs#importsTreeAsString(Graph)
     */
    public static void syncImports(OntModel m) {
        OntID id = m.getID();
        id.removeAll(OWL.imports);
        m.imports()
                .peek(OntModels::syncImports)
                .map(OntModel::getID)
                .filter(Resource::isURIResource)
                .map(OntID::getImportsIRI)
                .forEach(id::addImport);
    }

    /**
     * Recursively lists all models that are associated with the given model in the form of a flat stream.
     * In normal situation, each of the models must have {@code owl:imports} statement in the overlying graph.
     * In this case the returned stream must correspond the result of the {@link Graphs#dataGraphs(Graph)} method.
     *
     * @param m {@link OntModel}, not {@code null}
     * @return {@code Stream} of models, cannot be empty: must contain at least the input (root) model
     * @throws StackOverflowError in case the given model has a recursion in the hierarchy
     * @see Graphs#dataGraphs(Graph)
     * @see OntID#getImportsIRI()
     */
    public static Stream<OntModel> importsClosure(OntModel m) {
        return Stream.concat(Stream.of(m), m.imports().flatMap(OntModels::importsClosure));
    }

    /**
     * Lists all imported models from the given one.
     *
     * @param model {@link OntModel}
     * @return a {@code ExtendedIterator} of {@link OntModel}s
     * @see OntModel#imports()
     */
    @SuppressWarnings("unchecked")
    public static ExtendedIterator<OntModel> listImports(OntModel model) {
        if (model instanceof OntGraphModelImpl) {
            OntGraphModelImpl m = (OntGraphModelImpl) model;
            ExtendedIterator<?> res = m.listImportModels(m.getOntPersonality());
            return (ExtendedIterator<OntModel>) res;
        }
        return Iterators.create(model.imports().iterator());
    }

    /**
     * Lists all ontology objects with the given {@code type} that are defined in the base graph.
     * See also {@link OntModels#listLocalStatements(OntModel, Resource, Property, RDFNode)} description.
     *
     * @param model {@link OntModel}
     * @param type  {@link Class}-type
     * @param <O>   subclass of {@link OntObject}
     * @return {@link ExtendedIterator} of ontology objects of the type {@link O} that are local to the base graph
     * @see OntModel#ontObjects(Class)
     */
    public static <O extends OntObject> ExtendedIterator<O> listLocalObjects(OntModel model, Class<? extends O> type) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalOntObjects(type);
        }
        Stream<O> res = model.ontObjects(type);
        return Iterators.create(res.iterator()).filterKeep(OntObject::isLocal);
    }

    /**
     * Lists all OWL entities that are defined in the base graph.
     * See also {@link OntModels#listLocalStatements(OntModel, Resource, Property, RDFNode)} description.
     *
     * @param model {@link OntModel}
     * @return {@link ExtendedIterator} of {@link OntEntity}s that are local to the base graph
     * @see OntModel#ontEntities()
     */
    public static ExtendedIterator<OntEntity> listLocalEntities(OntModel model) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalOntEntities();
        }
        return Iterators.create(model.ontEntities().iterator()).filterKeep(OntObject::isLocal);
    }

    /**
     * Lists all members from {@link OntList Ontology List}.
     *
     * @param list {@link RDFNodeList}
     * @param <R>  {@link RDFNode}, a type of list members
     * @return {@link ExtendedIterator} of {@link R}
     */
    public static <R extends RDFNode> ExtendedIterator<R> listMembers(RDFNodeList<R> list) {
        if (list instanceof OntListImpl) {
            return ((OntListImpl<R>) list).listMembers();
        }
        return Iterators.create(list.members().iterator());
    }

    /**
     * Lists all class-types for the given individual.
     *
     * @param i an {@link OntIndividual}, not {@code null}
     * @return an {@link ExtendedIterator} over all direct {@link OntClass class}-types
     */
    public static ExtendedIterator<OntClass> listClasses(OntIndividual i) {
        return i instanceof OntIndividualImpl ? ((OntIndividualImpl) i).listClasses() : Iterators.create(i.classes().iterator());
    }

    /**
     * Lists all model statements, which belong to the base graph, using the given SPO.
     * <p>
     * It is placed here because there is no certainty that methods for working with {@code ExtendedIterator}
     * (like {@link OntGraphModelImpl#listLocalStatements(Resource, Property, RDFNode)})
     * should be placed in the public interfaces:
     * {@code Stream}-based analogues are almost the same but more functional.
     * But the ability to work with {@code ExtendedIterator} is sometimes needed,
     * since it is more lightweight and works a bit faster than Stream-API.
     *
     * @param model {@link OntModel}, not {@code null}
     * @param s     {@link Resource}, can be {@code null} for any
     * @param p     {@link Property}, can be {@code null} for any
     * @param o     {@link RDFNode}, can be {@code null} for any
     * @return an {@link ExtendedIterator} of {@link OntStatement}s local to the base model graph
     * @see OntModel#localStatements(Resource, Property, RDFNode)
     */
    public static ExtendedIterator<OntStatement> listLocalStatements(OntModel model,
                                                                     Resource s,
                                                                     Property p,
                                                                     RDFNode o) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalStatements(s, p, o);
        }
        return model.getBaseGraph().find(ModelCom.asNode(s), ModelCom.asNode(p), ModelCom.asNode(p))
                .mapWith(model::asStatement);
    }

    /**
     * Returns an iterator over all direct annotations of the given ontology statement.
     *
     * @param s {@link OntStatement}
     * @return {@link ExtendedIterator} over {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listAnnotations(OntStatement s) {
        if (s instanceof OntStatementImpl) {
            return ((OntStatementImpl) s).listAnnotations();
        }
        return Iterators.create(s.annotations().iterator());
    }

    /**
     * Lists all direct object's annotations.
     *
     * @param o {@link OntObject}, not {@code null}
     * @return {@link ExtendedIterator} over {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listAnnotations(OntObject o) {
        if (o instanceof OntObjectImpl) {
            return ((OntObjectImpl) o).listAnnotations();
        }
        return Iterators.create(o.annotations().iterator());
    }

    /**
     * Returns an {@code ExtendedIterator} over all {@link OntStatement Ontology Statement}s,
     * which are obtained from splitting the given statement into several equivalent ones but with disjoint annotations.
     * Each of the returned statements is equal to the given, the difference is only in the related annotations.
     * <p>
     * This method can be used in case there are several typed b-nodes for each annotation assertions instead of a single one.
     * Such situation is not a canonical way and should not be widely used, since it is redundant.
     * So usually the result stream contains only a single element: the same {@code OntStatement} instance as the input.
     * <p>
     * The following code demonstrates that non-canonical way of writing annotations with two or more b-nodes:
     * <pre>{@code
     * s A t .
     * _:b0  a                     owl:Axiom .
     * _:b0  A1                    t1 .
     * _:b0  owl:annotatedSource   s .
     * _:b0  owl:annotatedProperty A .
     * _:b0  owl:annotatedTarget   t .
     * _:b1  a                     owl:Axiom .
     * _:b1  A2                    t2 .
     * _:b1  owl:annotatedSource   s .
     * _:b1  owl:annotatedProperty A .
     * _:b1  owl:annotatedTarget   t .
     * }</pre>
     * Here the statement {@code s A t} has two annotations,
     * but they are spread over different resources (statements {@code _:b0 A1 t1} and {@code _:b1 A2 t2}).
     * For this example, the method returns stream of two {@code OntStatement}s, and each of them has only one annotation.
     * For generality, below is an example of the correct and equivalent way to write these annotations,
     * which is the preferred since it is more compact:
     * <pre>{@code
     * s A t .
     * [ a                      owl:Axiom ;
     * A1                     t1 ;
     * A2                     t2 ;
     * owl:annotatedProperty  A ;
     * owl:annotatedSource    s ;
     * owl:annotatedTarget    t
     * ]  .
     * }</pre>
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listSplitStatements(OntStatement statement) {
        return ((OntStatementImpl) statement).listSplitStatements();
    }

    /**
     * Recursively lists all annotations for the given {@link OntStatement Ontology Statement}
     * in the form of a flat stream.
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return a {@code Stream} of {@link OntStatement}s, each of them is annotation property assertion
     * @see #listAllAnnotations(OntStatement)
     */
    public static Stream<OntStatement> annotations(OntStatement statement) {
        if (statement instanceof OntStatementImpl) {
            return Iterators.asStream(listAllAnnotations(statement));
        }
        return statement.annotations().flatMap(s -> Stream.concat(Stream.of(s), annotations(s)));
    }

    /**
     * For the specified {@link OntStatement Statement}
     * lists all its annotation assertions recursively including their sub-annotations.
     * <p>
     * For example, for the following snippet
     * <pre>{@code
     * [ a                      owl:Annotation ;
     *   rdfs:label             "label2" ;
     *   owl:annotatedProperty  rdfs:label ;
     *   owl:annotatedSource    [ a                      owl:Axiom ;
     *                            rdfs:label             "label1" ;
     *                            owl:annotatedProperty  rdfs:comment ;
     *                            owl:annotatedSource    [ a             owl:Ontology ;
     *                                                     rdfs:comment  "comment"
     *                                                   ] ;
     *                            owl:annotatedTarget    "comment"
     *                          ] ;
     *   owl:annotatedTarget    "label1"
     * ] .
     * }</pre>
     * there would be three annotations:
     * {@code _:b0 rdfs:comment "comment"},
     * {@code _:b1 rdfs:label "label1"},
     * {@code _:b2 rdfs:label "label2"}.
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return an {@link ExtendedIterator} of {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listAllAnnotations(OntStatement statement) {
        return Iterators.flatMap(listAnnotations(statement), s -> Iterators.concat(Iterators.of(s), listAllAnnotations(s)));
    }

    /**
     * Answers an {@link OntStatement Ontology Statement} in the specified {@code model}
     * that wraps the given {@code triple}.
     * This method differs from the method {@link OntModel#asStatement(Triple)}
     * in that it provides {@link OntObject#getMainStatement() main statement} if it is possible.
     *
     * @param triple {@link Triple SPO}, not {@code null}
     * @param model  {@link OntModel}, not {@code null}
     * @return {@link OntStatement}
     * @see OntModel#asStatement(Triple)
     * @see OntObject#getMainStatement()
     */
    public static OntStatement toOntStatement(Triple triple, OntModel model) {
        OntStatement res = model.asStatement(triple);
        Resource subj = res.getSubject();
        return Stream.of(OntEntity.class
                        , OntClass.class
                        , OntDataRange.class
                        , OntDisjoint.class
                        , OntObjectProperty.class
                        , OntNegativeAssertion.class
                        , OntSWRL.class)
                .filter(subj::canAs).map(subj::as)
                .map(OntObject::getMainStatement).filter(res::equals)
                .findFirst().orElse(res);
    }

    /**
     * Gets model's config.
     *
     * @param m {@link OntModel}
     * @return {@link OntConfig} or {@code null} if model is not {@link OntEnhGraph}
     */
    public static OntConfig config(OntModel m) {
        return (m instanceof OntEnhGraph) ? ((OntEnhGraph) m).getOntPersonality().getConfig() : null;
    }
}
