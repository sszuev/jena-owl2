package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A base abstraction for any Class Expressions (both named and anonymous).
 * <p>
 * Created @ssz on 01.11.2016.
 *
 * @see Named - an OWL Class
 * @see <a href="https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions">2.1 Class Expressions</a>
 * @see <a href="https://www.w3.org/TR/owl2-syntax/#Class_Expressions">8 Class Expressions</a>
 */
public interface OntClass extends OntObject, AsNamed<OntClass.Named>, HasDisjoint<OntClass> {

    /**
     * Answers a {@code Stream} over all the class expressions
     * that are declared to be sub-classes of this class expression.
     * The return {@code Stream} is distinct and this instance is not included into it.
     * The flag {@code direct} allows some selectivity over the classes that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *   :B rdfs:subClassOf :A.
     *   :C rdfs:subClassOf :A.
     *   :D rdfs:subClassOf :C.
     * }</pre>
     * (so {@code A} has two sub-classes, {@code B} and {@code C}, and {@code C} has sub-class {@code D})
     * In a raw model, with no inference support, listing the sub-classes of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subClassOf} is known to be transitive, so
     * the sub-classes iterator will include {@code D}.
     * The {@code direct} sub-classes are those members of the closure of the subClassOf relation,
     * restricted to classes that cannot be reached by a longer route,
     * i.e. the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct sub-classes of {@code A} are {@code B} and {@code C} only, and not {@code D} - even in an inferencing graph.
     * Note that this is not the same as the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :D rdfs:subClassOf :A.
     * }</pre>
     * Now, in the raw graph, {@code A} has sub-class {@code C}. But the direct sub-classes of {@code A} remain {@code B} and {@code C},
     * since there is a longer path {@code A-C-D} that means that {@code D} is not a direct sub-class of {@code A}.
     * The assertion in the raw graph that {@code A} has sub-class {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean} - if {@code true} answers the directly adjacent classes in the sub-class relation:
     *               i.e. eliminate any class for which there is a longer route to reach that parent under the sub-class relation;
     *               if {@code false} answers all sub-classes found by inferencer, which usually means entire hierarchy down the tree;
     *               this class is not included
     * @return <b>distinct</b> {@code Stream} of sub {@link OntClass class expression}s
     * @see #subClasses()
     * @see #superClasses(boolean)
     * @see org.apache.jena.ontology.OntClass#listSubClasses(boolean)
     */
    Stream<OntClass> subClasses(boolean direct);

    /**
     * Answers a {@code Stream} over the class-expressions
     * for which this class expression is declared a sub-class.
     * The return {@code Stream} is distinct and this instance is not included into it.
     * <p>
     * The flag {@code direct} allows some selectivity over the classes that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *   :A rdfs:subClassOf :B .
     *   :A rdfs:subClassOf :C .
     *   :C rdfs:subClassOf :D .
     * }</pre>
     * (so {@code A} has super-classes {@code B} and {@code C}, and {@code C} has super-class {@code D})
     * In a raw model, with no inference support, listing the super-classes of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subClassOf} is known to be transitive,
     * so the super-classes iterator will include {@code D}.
     * The {@code direct} super-classes are those members of the closure of the inverse-subClassOf relation,
     * restricted to classes that cannot be reached by a longer route,
     * i.e. the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct super-classes of {@code A} are {@code B} and {@code C} only, and not {@code D} - even in an inferencing graph.
     * Note that this is not the same as the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :A rdfs:subClassOf :D .
     * }</pre>
     * Now, in the raw graph, {@code A} has super-classes {@code B}, {@code C}, {@code D}.
     * But the direct super-classes of {@code A} remain only {@code B} and C,
     * since there is a longer path {@code A-C-D} that means that {@code D} is not a direct super-class of {@code A}.
     * The assertion in the raw graph that {@code A} has super-class {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent classes in the super-class relation,
     *               i.e. eliminate any class for which there is a longer route to reach that parent under the super-class relation;
     *               if {@code false} answers all super-classes found by inferencer, which usually means entire hierarchy up the tree;
     *               this class is not included
     * @return <b>distinct</b> {@code Stream} of super {@link OntClass class expression}s
     * @see #superClasses()
     * @see #subClasses(boolean)
     * @see org.apache.jena.ontology.OntClass#listSuperClasses(boolean)
     */
    Stream<OntClass> superClasses(boolean direct);

    /**
     * Lists all individuals, directly or indirectly connected to this class.
     * The search pattern is {@code a rdf:type C}, where {@code C} is class expression and {@code a} individual.
     *
     * @param direct {@code boolean} if true, only direct instances are counted (i.e. not instances of sub-classes of this class)
     * @return a {@code Stream} of {@link OntIndividual}s
     */
    Stream<OntIndividual> individuals(boolean direct);

    /**
     * Returns {@code true} if the given property is associated with a frame-like view of this class.
     * This captures an informal notion of the <em>properties of a class</em>,
     * by looking at the domains of the property in this class's model, and matching them to this class.
     * A full description of the frame-like view of a class may be found in:
     * <a href="https://jena.apache.org/documentation/notes/rdf-frames.html">Apache Jena: RDF frames how-to</a> for full details.
     * <p>
     * Note that many cases of determining whether a property is associated with a class depends on RDFS or OWL reasoning.
     * This method may therefore return complete results only in models that have an attached reasoner.
     * For built-in properties the method returns always {@code false}.
     * If there is no domains for the property, then it is considered as global and is attached to root classes.
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntClass#hasDeclaredProperty(Property, boolean)}.
     *
     * @param property {@link OntRealProperty}, not {@code null}
     * @param direct   {@code boolean}: if {@code true} analyses only the directly adjacent domains in the sub-class relation,
     *                 otherwise takes into account the class hierarchy
     * @return {@code boolean}, {@code true} if the property is associated with this class by its domain, otherwise {@code false}
     * @see org.apache.jena.ontology.OntClass#hasDeclaredProperty(Property, boolean)
     */
    boolean hasDeclaredProperty(OntRealProperty property, boolean direct);

    /**
     * Returns a {@code Stream} over the {@link OntRealProperty properties} associated with a frame-like view of this class.
     * This captures an intuitive notion of the <em>properties of a class</em>.
     * This can be useful in presenting an ontology class in a user interface,
     * for example by automatically constructing a form to instantiate instances of the class.
     * The properties in the frame-like view of the class are determined by comparing
     * the domain of properties in this class's {@link OntModel} with the class itself.
     * See: <a href="https://jena.apache.org/documentation/notes/rdf-frames.html">Apache Jena: Presenting RDF as frames</a> for more details.
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntClass#listDeclaredProperties(boolean)}}.
     *
     * @param direct {@code boolean}: if {@code true} analyses only the directly adjacent domains in the sub-class relation,
     *               otherwise takes into account the class hierarchy
     * @return a <b>distinct</b> {@code Stream} of {@link OntRealProperty object and date properties}, attached to this class
     * @see #properties()
     */
    Stream<OntRealProperty> declaredProperties(boolean direct);

    /**
     * Answers true if this class is one of the roots of the local class hierarchy.
     * This will be true if either (i) this class has either {@code owl:Thing} or {@code rdfs:Resource} as a direct super-class,
     * or (ii) it has no declared super-classes.
     * <p>
     * {@code owl:Nothing} cannot be root.
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntClass#isHierarchyRoot()}.
     *
     * @return {@code true} if this class is the root of the class hierarchy in the model it is attached to
     */
    boolean isHierarchyRoot();

    /**
     * Lists all {@code HasKey} {@link OntList ontology []-list}s
     * that are attached to this class expression on predicate {@link OWL#hasKey owl:hasKey}.
     *
     * @return {@code Stream} of {@link OntList}s with parameter-type {@code OntDOP}
     */
    Stream<OntList<OntRealProperty>> hasKeys();

    /**
     * Creates an anonymous individual which is of this class-expression type.
     *
     * @return {@link OntIndividual.Anonymous}
     * @see OntIndividual#attachClass(OntClass)
     * @see #individuals()
     */
    OntIndividual.Anonymous createIndividual();

    /**
     * Creates a named individual which is of this class type.
     *
     * @param uri, String, not {@code null}
     * @return {@link OntIndividual.Named}
     * @see OntIndividual#attachClass(OntClass)
     * @see #individuals()
     */
    OntIndividual.Named createIndividual(String uri);

    /**
     * Creates a {@code HasKey} logical construction as {@link OntList ontology []-list}
     * of {@link OntRealProperty Object or Data Property Expression}s
     * that is attached to this Class Expression using the predicate {@link OWL#hasKey owl:hasKey}.
     * The resulting rdf-list will consist of all the elements of the specified collection
     * in the same order but with exclusion of duplicates.
     * Note: {@code null}s in collection will cause {@link OntJenaException.IllegalArgument exception}.
     * For additional information about {@code HasKey} logical construction see
     * <a href="https://www.w3.org/TR/owl2-syntax/#Keys">9.5 Keys</a> specification.
     *
     * @param objectProperties {@link Collection} (preferably {@link Set})
     *                         of {@link OntObjectProperty object property expression}s
     * @param dataProperties   {@link Collection} (preferably {@link Set})
     *                         of {@link OntDataProperty data property expression}s
     * @return {@link OntList} of {@link OntRealProperty}s
     * @see #addHasKey(Collection, Collection)
     */
    OntList<OntRealProperty> createHasKey(Collection<OntObjectProperty> objectProperties,
                                          Collection<OntDataProperty> dataProperties);

    /**
     * Creates a {@code HasKey} logical construction as {@link OntList ontology list}
     * and returns the statement {@code C owl:hasKey ( P1 ... Pm R1 ... Rn )}
     * to allow the subsequent addition of annotations.
     * About RDF Graph annotation specification see, for example,
     * <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.3.1 Axioms that Generate a Main Triple</a>.
     *
     * @param properties Array of {@link OntRealProperty}s without {@code null}s
     * @return {@link OntStatement} with a possibility to annotate
     * @see #addHasKeyStatement(Collection, Collection)
     * @see #addHasKey(OntRealProperty...)
     * @see #removeHasKey(Resource)
     * @see #clearHasKeys()
     */
    OntStatement addHasKeyStatement(OntRealProperty... properties);

    /**
     * Deletes the given {@code HasKey} list including its annotations.
     *
     * @param list {@link Resource} can be {@link OntList} or {@link RDFList}
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException if the list is not found
     */
    OntClass removeHasKey(Resource list);

    /**
     * Answers {@code true} if this class is disjoint with the given class.
     *
     * @param candidate {@link Resource} a class to test
     * @return {@code true} if this class is disjoint with the given class
     */
    boolean isDisjoint(Resource candidate);

    /**
     * {@inheritDoc}
     */
    @Override
    default Named asNamed() {
        return as(Named.class);
    }

    /**
     * Lists all individuals taking into account class hierarchy.
     * Equivalent to {@code this.individuals(false)}
     *
     * @return a {@code Stream} of {@link OntIndividual}s
     * @see OntClass#individuals(boolean)
     */
    default Stream<OntIndividual> individuals() {
        return individuals(false);
    }

    /**
     * Lists all properties attached to this class in a {@code rdfs:domain} statement.
     * The property is considered as attached if
     * the property and the class expression are both included in the property domain axiom statement:
     * <ul>
     * <li>{@code R rdfs:domain C} - {@code R} is a data property, {@code C} - this class expression</li>
     * <li>{@code P rdfs:domain C} - {@code P} is an object property expression, {@code C} - this class expression</li>
     * <li>{@code A rdfs:domain U} - {@code A} is annotation property, {@code U} is IRI (this class expression)</li>
     * </ul>
     *
     * @return {@code Stream} of {@link OntProperty}s
     * @see OntProperty#domains()
     */
    default Stream<OntProperty> properties() {
        return getModel().statements(null, RDFS.domain, this)
                .map(s -> s.getSubject().getAs(OntProperty.class))
                .filter(Objects::nonNull);
    }

    /**
     * Lists all direct or indirect sub-classes for this class expression, i.e. all sub-classes found by inferencer,
     * which usually means entire hierarchy down the tree; this class is not included.
     * The search pattern is {@code Ci rdfs:subClassOf C}.
     * <p>
     * Equivalent to {@code this.subClasses(false)}.
     *
     * @return {@code Stream} of {@link OntClass}s
     * @see #subClasses(boolean)
     */
    default Stream<OntClass> subClasses() {
        return subClasses(false);
    }

    /**
     * Lists all direct and indirect super-classes for this class expression, i.e. all super-classes found by inferencer,
     * which usually means entire hierarchy up the tree; this class is not included.
     * The search pattern is {@code C rdfs:subClassOf Ci}.
     * <p>
     * Equivalent to {@code this.superClasses(false)}.
     *
     * @return {@code Stream} of {@link OntClass}s
     * @see #superClasses(boolean)
     */
    default Stream<OntClass> superClasses() {
        return superClasses(false);
    }

    /**
     * Lists all {@code OntDisjoint} sections where this class is a member.
     *
     * @return a {@code Stream} of {@link OntDisjoint.Classes}
     */
    @Override
    default Stream<OntDisjoint.Classes> disjoints() {
        return getModel().ontObjects(OntDisjoint.Classes.class).filter(d -> d.members().anyMatch(this::equals));
    }

    /**
     * Returns disjoint class-objects.
     * The statement pattern to search for is {@code thisClass owl:disjointWith otherClass}.
     *
     * @return a {@code Stream} of {@link OntClass}s
     * @see OntDisjoint.Classes
     */
    default Stream<OntClass> disjointClasses() {
        return objects(OWL.disjointWith, OntClass.class);
    }

    /**
     * Lists all equivalent classes.
     * The statement patter to search for is {@code C1 owl:equivalentClass C2}.
     *
     * @return {@code Stream} of {@link OntClass}s
     * @see OntDataRange.Named#equivalentClasses()
     */
    default Stream<OntClass> equivalentClasses() {
        return objects(OWL.equivalentClass, OntClass.class);
    }

    /**
     * Adds the given class as a super class
     * and returns the corresponding statement to provide the ability to add annotations.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return {@link OntStatement} to allow the subsequent annotations addition
     * @see #addSuperClass(OntClass)
     * @see #removeSuperClass(Resource)
     */
    default OntStatement addSubClassOfStatement(OntClass other) {
        return addStatement(RDFS.subClassOf, other);
    }

    /**
     * Adds the given class as a disjoint class
     * and returns the corresponding statement to provide the ability to add annotations.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return {@link OntStatement} to allow the subsequent annotations addition
     * @see #addDisjointClass(OntClass)
     * @see #removeDisjointClass(Resource)
     * @see OntDisjoint.Classes
     */
    default OntStatement addDisjointWithStatement(OntClass other) {
        return addStatement(OWL.disjointWith, other);
    }

    /**
     * Adds the given class as an equivalent class
     * and returns the corresponding statement to provide the ability to add annotations.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return {@link OntStatement} to allow the subsequent annotations addition
     * @see #addEquivalentClass(OntClass)
     * @see #removeEquivalentClass(Resource)
     * @see OntDataRange.Named#addEquivalentClassStatement(OntDataRange)
     */
    default OntStatement addEquivalentClassStatement(OntClass other) {
        return addStatement(OWL.equivalentClass, other);
    }

    /**
     * Creates an {@code owl:hasKey} statement returning root statement to allow the subsequent annotations adding.
     *
     * @param objectProperties the collection of {@link OntObjectProperty}s, not {@code null} and cannot contain {@code null}s
     * @param dataProperties   the collection of {@link OntDataProperty}s, not {@code null} and cannot contain {@code null}s
     * @return {@link OntStatement} to allow the subsequent annotations addition
     * @see #addHasKeyStatement(OntRealProperty...)
     * @see #addHasKey(OntRealProperty...)
     * @see <a href='https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations'>2.3.1 Axioms that Generate a Main Triple</a>
     */
    default OntStatement addHasKeyStatement(Collection<OntObjectProperty> objectProperties, Collection<OntDataProperty> dataProperties) {
        return createHasKey(objectProperties, dataProperties).getMainStatement();
    }

    /**
     * Adds the given class as a super class
     * and returns this class expression instance to allow cascading calls.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSubClassOfStatement(OntClass)
     * @see #removeSuperClass(Resource)
     */
    default OntClass addSuperClass(OntClass other) {
        addSubClassOfStatement(other);
        return this;
    }

    /**
     * Adds the given class as a sub-class
     * and returns this class expression instance to allow cascading calls.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSuperClass(OntClass)
     */
    default OntClass addSubClass(OntClass other) {
        other.addSuperClass(this);
        return this;
    }

    /**
     * Adds the given class as a disjoint class
     * and returns this class expression instance to allow cascading calls.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDisjointWithStatement(OntClass)
     * @see #removeDisjointClass(Resource)
     */
    default OntClass addDisjointClass(OntClass other) {
        addDisjointWithStatement(other);
        return this;
    }

    /**
     * Adds a new equivalent class.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addEquivalentClassStatement(OntClass)
     * @see #removeDisjointClass(Resource)
     */
    default OntClass addEquivalentClass(OntClass other) {
        addEquivalentClassStatement(other);
        return this;
    }

    /**
     * Creates an {@code owl:hasKey} statement returning this class to allow cascading calls.
     *
     * @param objectProperties the collection of {@link OntObjectProperty}s
     * @param dataProperties   the collection of {@link OntDataProperty}s
     * @return <b>this</b> instance to allow cascading calls
     * @see #addHasKeyStatement(Collection, Collection)
     * @see #addHasKey(OntRealProperty...)
     */
    default OntClass addHasKey(Collection<OntObjectProperty> objectProperties, Collection<OntDataProperty> dataProperties) {
        addHasKeyStatement(objectProperties, dataProperties);
        return this;
    }

    /**
     * Creates an {@code owl:hasKey} statement returning this class to allow cascading calls.
     *
     * @param properties Array of {@link OntRealProperty}s without {@code null}s
     * @return <b>this</b> instance to allow cascading calls
     * @see #addHasKeyStatement(OntRealProperty...)
     * @see #addHasKey(Collection, Collection)
     * @see #removeHasKey(Resource)
     * @see #clearHasKeys()
     */
    default OntClass addHasKey(OntRealProperty... properties) {
        addHasKeyStatement(properties);
        return this;
    }

    /**
     * Removes the given individual from the set of instances that are members of this class.
     * This is effectively equivalent to the {@link OntIndividual#detachClass(Resource)} method
     * if the specified resource is {@link OntIndividual}.
     *
     * @param individual {@link Resource} a resource denoting an individual that is no longer to be a member of this class
     * @return <b>this</b> instance to allow cascading calls
     */
    default OntClass removeIndividual(Resource individual) {
        getModel().remove(individual, RDF.type, this);
        return this;
    }

    /**
     * Removes a super-class relationship for the given resource including all possible annotations.
     * No-op in case no match found.
     * Removes all {@link RDFS#subClassOf rdfs:subClassOf} statements with all their annotations
     * in case {@code null} is specified.
     *
     * @param other {@link Resource} or {@code null} to remove all {@code rdfs:subClassOf} statements
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSubClassOfStatement(OntClass)
     * @see #addSuperClass(OntClass)
     */
    default OntClass removeSuperClass(Resource other) {
        remove(RDFS.subClassOf, other);
        return this;
    }

    /**
     * Removes the specified disjoint class resource.
     * No-op in case no match found.
     * Removes all {@link OWL#disjointWith owl:disjointWith} statements with all their annotations
     * in case {@code null} is specified.
     *
     * @param other {@link Resource}, or {@code null} to remove all disjoint classes
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDisjointWithStatement(OntClass)
     * @see #addDisjointClass(OntClass)
     * @see OntDisjoint.Classes
     */
    default OntClass removeDisjointClass(Resource other) {
        remove(OWL.disjointWith, other);
        return this;
    }

    /**
     * Removes the given equivalent class resource including the statement's annotations.
     * No-op in case no match found.
     * Removes all {@link OWL#equivalentClass owl:equivalentClass} statements with all their annotations
     * in case {@code null} is specified.
     *
     * @param other {@link Resource}, or {@code null} to remove all equivalent classes
     * @return <b>this</b> instance to allow cascading calls
     * @see #addEquivalentClassStatement(OntClass)
     * @see #addEquivalentClass(OntClass)
     * @see OntDataRange.Named#removeEquivalentClass(Resource)
     */
    default OntClass removeEquivalentClass(Resource other) {
        remove(OWL.equivalentClass, other);
        return this;
    }

    /**
     * Deletes all {@code HasKey} []-list including its annotations,
     * i.e. all those statements with the predicate {@link OWL#hasKey owl:hasKey} for which this resource is a subject.
     *
     * @return <b>this</b> instance to allow cascading calls
     * @throws OntJenaException if the list is not found
     */
    default OntClass clearHasKeys() {
        hasKeys().collect(Collectors.toList()).forEach(this::removeHasKey);
        return this;
    }

    /**
     * Finds a {@code HasKey} logical construction
     * attached to this class expression by the specified rdf-node in the form of {@link OntList}.
     *
     * @param list {@link RDFNode}
     * @return {@code Optional} around {@link OntList} of {@link OntRealProperty data and object property expression}s
     */
    default Optional<OntList<OntRealProperty>> findHasKey(RDFNode list) {
        try (Stream<OntList<OntRealProperty>> res = hasKeys().filter(r -> Objects.equals(r, list))) {
            return res.findFirst();
        }
    }

    /**
     * Lists all key properties.
     * I.e. returns all object- and datatype- properties which belong to
     * the {@code C owl:hasKey ( P1 ... Pm R1 ... Rn )} statements,
     * where {@code C} is this class expression,
     * {@code Pi} is a property expression, and {@code Ri} is a data(-type) property.
     * If there are several []-lists in the model that satisfy these conditions,
     * all their content will be merged into the one distinct stream.
     *
     * @return <b>distinct</b> {@code Stream} of {@link OntObjectProperty object} and {@link OntDataProperty data} properties
     * @see #hasKeys()
     */
    default Stream<OntRealProperty> fromHasKey() {
        return hasKeys().flatMap(OntList::members).distinct();
    }

    /*
     * ============================
     * All known Class Expressions:
     * ============================
     */

    /**
     * @see OntModel#createObjectSomeValuesFrom(OntObjectProperty, OntClass)
     */
    interface ObjectSomeValuesFrom extends ComponentRestrictionCE<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectSomeValuesFrom>, SetProperty<OntObjectProperty, ObjectSomeValuesFrom> {
    }

    /**
     * @see OntModel#createDataSomeValuesFrom(OntDataProperty, OntDataRange)
     */
    interface DataSomeValuesFrom extends ComponentRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataSomeValuesFrom>, SetProperty<OntDataProperty, DataSomeValuesFrom> {
    }

    /**
     * @see OntModel#createObjectAllValuesFrom(OntObjectProperty, OntClass)
     */
    interface ObjectAllValuesFrom extends ComponentRestrictionCE<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectAllValuesFrom>, SetProperty<OntObjectProperty, ObjectAllValuesFrom> {
    }

    /**
     * @see OntModel#createDataAllValuesFrom(OntDataProperty, OntDataRange)
     */
    interface DataAllValuesFrom extends ComponentRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataAllValuesFrom>, SetProperty<OntDataProperty, DataAllValuesFrom> {
    }

    /**
     * @see OntModel#createObjectHasValue(OntObjectProperty, OntIndividual)
     */
    interface ObjectHasValue extends ComponentRestrictionCE<OntIndividual, OntObjectProperty>,
            SetValue<OntIndividual, ObjectHasValue>, SetProperty<OntObjectProperty, ObjectHasValue> {
    }

    /**
     * @see OntModel#createDataHasValue(OntDataProperty, Literal)
     */
    interface DataHasValue extends ComponentRestrictionCE<Literal, OntDataProperty>,
            SetValue<Literal, DataHasValue>, SetProperty<OntDataProperty, DataHasValue> {
    }

    /**
     * @see OntModel#createObjectMinCardinality(OntObjectProperty, int, OntClass)
     */
    interface ObjectMinCardinality extends CardinalityRestrictionCE<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectMinCardinality>,
            SetProperty<OntObjectProperty, ObjectMinCardinality>,
            SetCardinality<ObjectMinCardinality> {
    }

    /**
     * @see OntModel#createDataMinCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataMinCardinality extends CardinalityRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataMinCardinality>,
            SetProperty<OntDataProperty, DataMinCardinality>,
            SetCardinality<DataMinCardinality> {
    }

    /**
     * @see OntModel#createDataMaxCardinality(OntDataProperty, int, OntDataRange)
     */
    interface ObjectMaxCardinality extends CardinalityRestrictionCE<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectMaxCardinality>,
            SetProperty<OntObjectProperty, ObjectMaxCardinality>,
            SetCardinality<ObjectMaxCardinality> {
    }

    /**
     * @see OntModel#createDataMaxCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataMaxCardinality extends CardinalityRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataMaxCardinality>,
            SetProperty<OntDataProperty, DataMaxCardinality>,
            SetCardinality<DataMaxCardinality> {
    }

    /**
     * @see OntModel#createObjectCardinality(OntObjectProperty, int, OntClass)
     */
    interface ObjectCardinality extends CardinalityRestrictionCE<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectCardinality>,
            SetProperty<OntObjectProperty, ObjectCardinality>,
            SetCardinality<ObjectCardinality> {
    }

    /**
     * @see OntModel#createDataCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataCardinality extends CardinalityRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataCardinality>,
            SetProperty<OntDataProperty, DataCardinality>,
            SetCardinality<DataCardinality> {
    }

    /**
     * @see OntModel#createHasSelf(OntObjectProperty)
     */
    interface HasSelf extends UnaryRestrictionCE<OntObjectProperty>, SetProperty<OntObjectProperty, HasSelf> {
    }

    /**
     * @see OntModel#createObjectUnionOf(Collection)
     */
    interface UnionOf extends ComponentsCE<OntClass>, SetComponents<OntClass, UnionOf> {
    }

    /**
     * @see OntModel#createObjectOneOf(Collection)
     */
    interface OneOf extends ComponentsCE<OntIndividual>, SetComponents<OntIndividual, OneOf> {
    }

    /**
     * @see OntModel#createObjectIntersectionOf(Collection)
     */
    interface IntersectionOf extends ComponentsCE<OntClass>, SetComponents<OntClass, IntersectionOf> {
    }

    /**
     * @see OntModel#createObjectComplementOf(OntClass)
     */
    interface ComplementOf extends OntClass, HasValue<OntClass>, SetValue<OntClass, ComplementOf> {
    }

    /**
     * @see OntModel#createDataAllValuesFrom(Collection, OntDataRange)
     */
    interface NaryDataAllValuesFrom extends NaryRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, NaryDataAllValuesFrom>, SetProperties<OntDataProperty, NaryDataAllValuesFrom> {
    }

    /**
     * @see OntModel#createDataSomeValuesFrom(Collection, OntDataRange)
     */
    interface NaryDataSomeValuesFrom extends NaryRestrictionCE<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, NaryDataSomeValuesFrom>, SetProperties<OntDataProperty, NaryDataSomeValuesFrom> {
    }

    /**
     * An OWL Class {@link OntEntity Entity}, a named class expression.
     * This is an analogue of {@link org.apache.jena.ontology.OntClass}, but for OWL2.
     * <p>
     * Created @ssz on 01.11.2016.
     *
     * @see <a href='https://www.w3.org/TR/owl2-syntax/#Classes'>5.1 Classes</a>
     */
    interface Named extends OntEntity, OntClass {

        /**
         * Lists all {@code DisjointUnion} {@link OntList ontology list}s that are attached to this OWL Class
         * on predicate {@link OWL#disjointUnionOf owl:disjointUnionOf}.
         *
         * @return {@code Stream} of {@link OntList}s with parameter-type {@code OntCE}
         */
        Stream<OntList<OntClass>> disjointUnions();

        /**
         * Creates a {@code DisjointUnion} as {@link OntList ontology []-list} of {@link OntClass Class Expression}s
         * that is attached to this OWL Class using the predicate {@link OWL#disjointUnionOf owl:disjointUnionOf}.
         * The resulting rdf-list will consist of all the elements of the specified collection
         * in the same order but with exclusion of duplicates.
         * Note: {@code null}s in collection will cause {@link OntJenaException.IllegalArgument exception}.
         * For additional information about {@code DisjointUnion} logical construction see
         * <a href="https://www.w3.org/TR/owl2-syntax/#Disjoint_Union_of_Class_Expressions">9.1.4 Disjoint Union of Class Expressions</a>.
         *
         * @param classes {@link Collection} (preferably {@link Set}) of {@link OntClass class expression}s
         * @return {@link OntList} of {@link OntClass}s
         * @see #addDisjointUnionOfStatement(OntClass...)
         * @see #removeDisjointUnion(Resource)
         */
        OntList<OntClass> createDisjointUnion(Collection<OntClass> classes);

        /**
         * Deletes the given {@code DisjointUnion} list including its annotations.
         *
         * @param list {@link Resource} can be {@link OntList} or {@link RDFList}
         * @return <b>this</b> instance to allow cascading calls
         * @throws OntJenaException if the list is not found
         * @see #addDisjointUnion(Collection)
         * @see #createDisjointUnion(Collection)
         * @see #addDisjointUnionOfStatement(OntClass...)
         * @see #createDisjointUnion(Collection)
         */
        Named removeDisjointUnion(Resource list);

        /**
         * {@inheritDoc}
         */
        @Override
        default Named asNamed() {
            return this;
        }

        /**
         * Finds a {@code DisjointUnion} logical construction
         * attached to this class by the specified rdf-node in the form of {@link OntList}.
         *
         * @param list {@link RDFNode}
         * @return {@code Optional} around {@link OntList} of {@link OntClass class expression}s
         */
        default Optional<OntList<OntClass>> findDisjointUnion(RDFNode list) {
            try (Stream<OntList<OntClass>> res = disjointUnions().filter(r -> Objects.equals(r, list))) {
                return res.findFirst();
            }
        }

        /**
         * Creates a {@code DisjointUnion} {@link OntList ontology list}
         * and returns the statement {@code CN owl:disjointUnionOf ( C1 ... Cn )} to allow the addition of annotations.
         * About RDF Graph annotation specification see, for example,
         * <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.3.1 Axioms that Generate a Main Triple</a>.
         *
         * @param classes Array of {@link OntClass class expressions} without {@code null}s,
         *                duplicates will be discarded and order will be saved
         * @return {@link OntStatement} to allow the subsequent annotations addition
         * @see #createDisjointUnion(Collection)
         * @see #createDisjointUnion(Collection)
         * @see #addDisjointUnion(OntClass...)
         * @see #addDisjointUnionOfStatement(OntClass...)
         * @see #removeDisjointUnion(Resource)
         */
        default OntStatement addDisjointUnionOfStatement(OntClass... classes) {
            return addDisjointUnionOfStatement(Arrays.stream(classes).collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        /**
         * Creates a disjoint-union section returning its root statement to allow adding annotations.
         * The triple pattern: {@code CN owl:disjointUnionOf ( C1 ... Cn )}.
         *
         * @param classes a collection of {@link OntClass class expression}s without {@code null}s
         * @return {@link OntStatement} to allow the subsequent annotations addition
         * @see #createDisjointUnion(Collection)
         * @see <a href='https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations'>2.3.1 Axioms that Generate a Main Triple</a>
         * @see #createDisjointUnion(Collection)
         * @see #addDisjointUnion(Collection)
         * @see #addDisjointUnionOfStatement(Collection)
         * @see #removeDisjointUnion(Resource)
         */
        default OntStatement addDisjointUnionOfStatement(Collection<OntClass> classes) {
            return createDisjointUnion(classes).getMainStatement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addSuperClass(OntClass other) {
            addSubClassOfStatement(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addDisjointClass(OntClass other) {
            addDisjointWithStatement(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addEquivalentClass(OntClass other) {
            addEquivalentClassStatement(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addHasKey(Collection<OntObjectProperty> objectProperties, Collection<OntDataProperty> dataProperties) {
            addHasKeyStatement(objectProperties, dataProperties);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addHasKey(OntRealProperty... properties) {
            addHasKeyStatement(properties);
            return this;
        }

        /**
         * @param classes a collection of {@link OntClass class expression}s without {@code null}s
         * @return <b>this</b> instance to allow cascading calls
         */
        default Named addDisjointUnion(Collection<OntClass> classes) {
            addDisjointUnionOfStatement(classes);
            return this;
        }

        /**
         * @param classes Array of {@link OntClass class expressions} without {@code null}s,
         *                duplicates will be discarded and order will be saved
         * @return <b>this</b> instance to allow cascading calls
         */
        default Named addDisjointUnion(OntClass... classes) {
            addDisjointUnionOfStatement(classes);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        default Named removeSuperClass(Resource other) {
            OntClass.super.removeSuperClass(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        default Named removeDisjointClass(Resource other) {
            OntClass.super.removeDisjointClass(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        default Named removeEquivalentClass(Resource other) {
            OntClass.super.removeEquivalentClass(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        default Named clearHasKeys() {
            OntClass.super.clearHasKeys();
            return this;
        }

        /**
         * Deletes all {@code DisjointUnion} []-lists including their annotations,
         * i.e. all those statements with the predicate {@link OWL#disjointUnionOf owl:disjointUnionOf}
         * for which this resource is a subject.
         *
         * @return <b>this</b> instance to allow cascading calls
         * @see #removeDisjointUnion(Resource)
         */
        default Named clearDisjointUnions() {
            disjointUnions().collect(Collectors.toSet()).forEach(this::removeDisjointUnion);
            return this;
        }

        /**
         * Returns all class expressions from the right part of the statement with this class as a subject
         * and {@link OWL#disjointUnionOf owl:disjointUnionOf} as a predicate
         * (the triple pattern: {@code CN owl:disjointUnionOf ( C1 ... Cn )}).
         * If there are several []-lists in the model that satisfy these conditions,
         * all their content will be merged into the one distinct stream.
         *
         * @return <b>distinct</b> stream of {@link OntClass class expressions}s
         * @see #disjointUnions()
         */
        default Stream<OntClass> fromDisjointUnionOf() {
            return disjointUnions().flatMap(OntList::members).distinct();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addComment(String txt) {
            return addComment(txt, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addComment(String txt, String lang) {
            return annotate(getModel().getRDFSComment(), txt, lang);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addLabel(String txt) {
            return addLabel(txt, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addLabel(String txt, String lang) {
            return annotate(getModel().getRDFSLabel(), txt, lang);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named annotate(OntAnnotationProperty predicate, String txt, String lang) {
            return annotate(predicate, getModel().createLiteral(txt, lang));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named annotate(OntAnnotationProperty predicate, RDFNode value) {
            addAnnotation(predicate, value);
            return this;
        }
    }

    /*
     * ===========================
     * Abstract class expressions:
     * ===========================
     */

    /**
     * An abstraction for Boolean Connectives (with exclude of {@link ComplementOf}) and Enumeration of Individuals.
     *
     * @param <O> a component type
     */
    interface ComponentsCE<O extends OntObject> extends OntClass, HasRDFNodeList<O> {
    }

    /**
     * An abstraction for Cardinality Restrictions.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRealProperty}
     */
    interface CardinalityRestrictionCE<O extends OntObject, P extends OntRealProperty>
            extends HasCardinality, ComponentRestrictionCE<O, P> {
    }

    /**
     * An abstract class expression (Restriction) that has component (i.e. 'filler' in OWL-API terms):
     * all Cardinality Restrictions, Existential/Universal Restrictions, Individual/Literal Value Restrictions.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRealProperty}
     */
    interface ComponentRestrictionCE<O extends RDFNode, P extends OntRealProperty>
            extends UnaryRestrictionCE<P>, HasValue<O> {
    }

    /**
     * An abstraction that unites all {@link RestrictionCE Restriction}s
     * with the predicate {@link OWL#onProperties owl:onProperties}.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRealProperty}
     */
    interface NaryRestrictionCE<O extends OntObject, P extends OntRealProperty>
            extends RestrictionCE<P>, HasProperties<P>, HasValue<O> {
    }

    /**
     * An abstract class expression that unites all {@link RestrictionCE Restriction}s
     * with the predicate {@link OWL#onProperty owl:onProperty}.
     *
     * @param <P> any subtype of {@link OntRealProperty}
     */
    interface UnaryRestrictionCE<P extends OntRealProperty> extends RestrictionCE<P> {
    }

    /**
     * An abstract class expression that unites all class expressions with the type {@link OWL#Restriction}.
     *
     * @param <P> any subtype of {@link OntRealProperty}
     */
    interface RestrictionCE<P extends OntRealProperty> extends OntClass, HasProperty<P> {
    }
}