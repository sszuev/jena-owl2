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
 *
 * @see Named - an OWL Class
 * @see <a href="https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions">2.1 Class Expressions</a>
 * @see <a href="https://www.w3.org/TR/owl2-syntax/#Class_Expressions">8 Class Expressions</a>
 */
public interface OntClass extends OntObject, AsNamed<OntClass.Named>, HasDisjoint<OntClass> {

    /**
     * Answers a {@code Stream} over all the class expressions
     * that are declared to be subclasses of this class expression.
     * The return {@code Stream} is distinct and this instance is not included in it.
     * The flag {@code direct} allows some selectivity over the classes that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *   :B rdfs:subClassOf :A.
     *   :C rdfs:subClassOf :A.
     *   :D rdfs:subClassOf :C.
     * }</pre>
     * (so {@code A} has two subclasses, {@code B} and {@code C}, and {@code C} has subclass {@code D})
     * In a raw model, with no inference support,
     * listing the subclasses of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subClassOf} is known to be transitive, so
     * the subclasses iterator will include {@code D}.
     * The {@code direct} subclasses are those members of the closure of the subClassOf relation,
     * restricted to classes that cannot be reached by a longer route,
     * i.e., the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct subclasses of {@code A} are {@code B} and {@code C} only,
     * and not {@code D} - even in an inferencing graph.
     * Note that this is not the same as the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :D rdfs:subClassOf :A.
     * }</pre>
     * Now, in the raw graph, {@code A} has subclass {@code C}.
     * But the direct subclasses of {@code A} remain {@code B} and {@code C},
     * since there is a longer path {@code A-C-D}
     * that means that {@code D} is not a direct subclass of {@code A}.
     * The assertion in the raw graph that {@code A} has subclass {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean} - if {@code true} answers the directly adjacent classes in the subclass relation:
     *               i.e., eliminate any class
     *               for which there is a longer route
     *               to reach that parent under the subclass relation;
     *               if {@code false} answers all subclasses found by inferencer,
     *               which usually means entire hierarchy down the tree;
     *               this class is not included
     * @return <b>distinct</b> {@code Stream} of sub {@link OntClass class expression}s
     * @see #subClasses()
     * @see #superClasses(boolean)
     * @see org.apache.jena.ontology.OntClass#listSubClasses(boolean)
     */
    Stream<OntClass> subClasses(boolean direct);

    /**
     * Answers a {@code Stream} over the class-expressions
     * for which this class expression is declared a subclass.
     * The return {@code Stream} is distinct and this instance is not included in it.
     * <p>
     * The flag {@code direct} allows some selectivity over the classes that appear in the {@code Stream}.
     * Consider the following scenario:
     * <pre>{@code
     *   :A rdfs:subClassOf :B .
     *   :A rdfs:subClassOf :C .
     *   :C rdfs:subClassOf :D .
     * }</pre>
     * (so {@code A} has superclasses {@code B} and {@code C}, and {@code C} has superclass {@code D})
     * In a raw model, with no inference support, listing the superclasses of {@code A} will answer {@code B} and {@code C}.
     * In an inferencing model, {@code rdfs:subClassOf} is known to be transitive,
     * so the superclasses iterator will include {@code D}.
     * The {@code direct} superclasses are those members of the closure of the inverse-subClassOf relation,
     * restricted to classes that cannot be reached by a longer route,
     * i.e., the ones that are <em>directly</em> adjacent to the given root.
     * Thus, the direct superclasses of {@code A} are {@code B} and {@code C} only, and not {@code D} - even in an inferencing graph.
     * Note that this is not the same as the entailments from the raw graph.
     * Suppose we add to this example:
     * <pre>{@code
     *   :A rdfs:subClassOf :D .
     * }</pre>
     * Now, in the raw graph, {@code A} has superclasses {@code B}, {@code C}, {@code D}.
     * But the direct superclasses of {@code A} remain only {@code B} and C,
     * since there is a longer path {@code A-C-D} that means that {@code D} is not a direct superclass of {@code A}.
     * The assertion in the raw graph that {@code A} has superclass {@code D} is essentially redundant,
     * since this can be inferred from the closure of the graph.
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent classes in the superclass relation,
     *               i.e., eliminate any class
     *               for which there is a longer route
     *               to reach that parent under the superclass relation;
     *               if {@code false} answers all superclasses found by inferencer,
     *               which usually means entire hierarchy up the tree;
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
     * @param direct {@code boolean} if true, only direct instances are counted
     *               (i.e., not instances of subclasses of this class)
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
     * Note that many cases of determining whether a property is associated with a class depend on RDFS or OWL reasoning.
     * This method may therefore return complete results only in models that have an attached reasoner.
     * For built-in properties the method returns always {@code false}.
     * If there are no domains for the property, then it is considered as global and is attached to root classes.
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntClass#hasDeclaredProperty(Property, boolean)}.
     *
     * @param property {@link OntProperty}, not {@code null}
     * @param direct   {@code boolean}: if {@code true} analyses only the directly adjacent domains in the subclass relation,
     *                 otherwise takes into account the class hierarchy
     * @return {@code boolean}, {@code true} if the property is associated with this class by its domain, otherwise {@code false}
     * @see org.apache.jena.ontology.OntClass#hasDeclaredProperty(Property, boolean)
     */
    boolean hasDeclaredProperty(OntProperty property, boolean direct);

    /**
     * Returns a {@code Stream} over the {@link OntProperty properties} associated with a frame-like view of this class.
     * This captures an intuitive notion of the <em>properties of a class</em>.
     * This can be useful in presenting an ontology class in a user interface,
     * for example, by automatically constructing a form to instantiate instances of the class.
     * The properties in the frame-like view of the class are determined by comparing
     * the domain of properties in this class's {@link OntModel} with the class itself.
     * See: <a href="https://jena.apache.org/documentation/notes/rdf-frames.html">Apache Jena: Presenting RDF as frames</a> for more details.
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntClass#listDeclaredProperties(boolean)}}.
     *
     * @param direct {@code boolean}: if {@code true} analyses only the directly adjacent domains in the subclass relation,
     *               otherwise takes into account the class hierarchy
     * @return a <b>distinct</b> {@code Stream} of {@link OntProperty object, datatype and annotation properties}, attached to this class
     * @see #properties()
     */
    Stream<OntProperty> declaredProperties(boolean direct);

    /**
     * Answers true if this class is one of the roots of the local class hierarchy.
     * This will be true if either (i) this class has either {@code owl:Thing} or {@code rdfs:Resource} as a direct superclass,
     * or (ii) it has no declared superclasses.
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
    Stream<OntList<OntRelationalProperty>> hasKeys();

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
     * of {@link OntRelationalProperty Object or Data Property Expression}s
     * that is attached to this Class Expression using the predicate {@link OWL#hasKey owl:hasKey}.
     * The resulting rdf-list will consist of all the elements of the specified collection
     * in the same order but with exclusion of duplicates.
     * Note: {@code null}s in the collection will cause {@link OntJenaException.IllegalArgument exception}.
     * For additional information about {@code HasKey} logical construction see
     * <a href="https://www.w3.org/TR/owl2-syntax/#Keys">9.5 Keys</a> specification.
     *
     * @param objectProperties {@link Collection} (preferably {@link Set})
     *                         of {@link OntObjectProperty object property expression}s
     * @param dataProperties   {@link Collection} (preferably {@link Set})
     *                         of {@link OntDataProperty data property expression}s
     * @return {@link OntList} of {@link OntRelationalProperty}s
     * @see #addHasKey(Collection, Collection)
     */
    OntList<OntRelationalProperty> createHasKey(Collection<OntObjectProperty> objectProperties,
                                                Collection<OntDataProperty> dataProperties);

    /**
     * Creates a {@code HasKey} logical construction as {@link OntList ontology list}
     * and returns the statement {@code C owl:hasKey ( P1 ... Pm R1 ... Rn )}
     * to allow the subsequent addition of annotations.
     * About RDF Graph annotation specification sees, for example,
     * <a href="https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations">2.3.1 Axioms that Generate a Main Triple</a>.
     *
     * @param properties Array of {@link OntRelationalProperty}s without {@code null}s
     * @return {@link OntStatement} with a possibility to annotate
     * @see #addHasKeyStatement(Collection, Collection)
     * @see #addHasKey(OntRelationalProperty...)
     * @see #removeHasKey(Resource)
     * @see #clearHasKeys()
     */
    OntStatement addHasKeyStatement(OntRelationalProperty... properties);

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
     * Returns disjoint class-objects.
     * This includes {@code thisClass owl:disjointWith otherClass},
     * {@code otherClass owl:disjointWith thisClass} statements and {@code owl:AllDisjointClasses} construct.
     *
     * @return a {@code Stream} of {@link OntClass}s
     * @see OntDisjoint.Classes
     */
    Stream<OntClass> disjointClasses();

    /**
     * Lists all equivalent classes.
     * The statement patter to search for is {@code C1 owl:equivalentClass C2}.
     *
     * @return {@code Stream} of {@link OntClass}s
     * @see OntDataRange.Named#equivalentClasses()
     */
    Stream<OntClass> equivalentClasses();

    /**
     * {@inheritDoc}
     */
    @Override
    default Named asNamed() {
        return as(OntClass.Named.class);
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
     * Equivalent to {@code this.declaredProperties(false)}
     *
     * @return a <b>distinct</b> {@code Stream} of (object, datatype &amp; annotation properties), attached to this class
     * @see #declaredProperties(boolean)
     */
    default Stream<OntProperty> declaredProperties() {
        return declaredProperties(false);
    }

    /**
     * Lists all direct or indirect subclasses for this class expression, i.e., all subclasses found by inferencer,
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
     * Answers a class that is the subclass of this class.
     * If there is more than one such class, an arbitrary selection is made.
     * Unlike the method {@link #subClasses()}, this method can return this class itself.
     *
     * @return {@link Optional} wrapping {@link OntClass}
     */
    default Optional<OntClass> subClass() {
        try (Stream<OntClass> classes = getModel()
                .statements(null, RDFS.subClassOf, this)
                .map(OntStatement::getSubject)
                .filter(it -> it.canAs(OntClass.class))
                .map(it -> it.as(OntClass.class).asSubClass())
                .filter(Objects::nonNull)) {
            return classes.findFirst();
        }
    }

    /**
     * Lists all direct and indirect superclasses for this class expression, i.e., all superclasses found by inferencer,
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
     * Answers a class that is the superclass of this class.
     * If there is more than one such class, an arbitrary selection is made.
     * Unlike the method {@link #superClasses()}, this method can return this class itself.
     *
     * @return {@link Optional} wrapping {@link OntClass}
     */
    default Optional<OntClass> superClass() {
        try (Stream<OntClass> classes = this.statements(RDFS.subClassOf)
                .map(OntStatement::getSubject)
                .filter(it -> it.canAs(OntClass.class))
                .map(it -> it.as(OntClass.class).asSuperClass())
                .filter(Objects::nonNull)) {
            return classes.findFirst();
        }
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
     * Adds the given class as a superclass
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
     * Creates an {@code owl:hasKey} statement returning the root statement to allow the subsequent annotations adding.
     *
     * @param objectProperties the collection of {@link OntObjectProperty}s, not {@code null} and cannot contain {@code null}s
     * @param dataProperties   the collection of {@link OntDataProperty}s, not {@code null} and cannot contain {@code null}s
     * @return {@link OntStatement} to allow the subsequent annotations addition
     * @see #addHasKeyStatement(OntRelationalProperty...)
     * @see #addHasKey(OntRelationalProperty...)
     * @see <a href='https://www.w3.org/TR/owl2-mapping-to-rdf/#Translation_of_Annotations'>2.3.1 Axioms that Generate a Main Triple</a>
     */
    default OntStatement addHasKeyStatement(Collection<OntObjectProperty> objectProperties,
                                            Collection<OntDataProperty> dataProperties) {
        return createHasKey(objectProperties, dataProperties).getMainStatement();
    }

    /**
     * Adds the given class as a superclass
     * and returns this class expression instance to allow cascading calls.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSubClassOfStatement(OntClass)
     * @see #removeSuperClass(Resource)
     */
    default OntClass addSuperClass(OntClass other) {
        OntJenaException.checkSupported(this.asSubClass() != null && other.asSuperClass() != null);
        addSubClassOfStatement(other);
        return this;
    }

    /**
     * Adds the given class as a subclass
     * and returns this class expression instance to allow cascading calls.
     *
     * @param other {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addSuperClass(OntClass)
     */
    default OntClass addSubClass(OntClass other) {
        OntJenaException.checkSupported(other.asSubClass() != null && this.asSuperClass() != null);
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
        OntJenaException.checkSupported(other.asDisjointClass() != null && this.asDisjointClass() != null);
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
        OntJenaException.checkSupported(this.asEquivalentClass() != null && other.asEquivalentClass() != null);
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
     * @see #addHasKey(OntRelationalProperty...)
     */
    default OntClass addHasKey(Collection<OntObjectProperty> objectProperties, Collection<OntDataProperty> dataProperties) {
        addHasKeyStatement(objectProperties, dataProperties);
        return this;
    }

    /**
     * Creates an {@code owl:hasKey} statement returning this class to allow cascading calls.
     *
     * @param properties Array of {@link OntRelationalProperty}s without {@code null}s
     * @return <b>this</b> instance to allow cascading calls
     * @see #addHasKeyStatement(OntRelationalProperty...)
     * @see #addHasKey(Collection, Collection)
     * @see #removeHasKey(Resource)
     * @see #clearHasKeys()
     */
    default OntClass addHasKey(OntRelationalProperty... properties) {
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
     * Removes a superclass relationship for the given resource including all possible annotations.
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
     * i.e., all those statements with the predicate
     * {@link OWL#hasKey owl:hasKey} for which this resource is a subject.
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
     * @return {@code Optional} around {@link OntList} of {@link OntRelationalProperty data and object property expression}s
     */
    default Optional<OntList<OntRelationalProperty>> findHasKey(RDFNode list) {
        try (Stream<OntList<OntRelationalProperty>> res = hasKeys().filter(r -> Objects.equals(r, list))) {
            return res.findFirst();
        }
    }

    /**
     * Lists all key properties.
     * I.e., returns all object- and datatype-properties that belong to
     * the {@code C owl:hasKey ( P1 ... Pm R1 ... Rn )} statements,
     * where {@code C} is this class expression,
     * {@code Pi} is a property expression, and {@code Ri} is a data(-type) property.
     * If there are several []-lists in the model that satisfy these conditions,
     * all their content will be merged into the one distinct stream.
     *
     * @return <b>distinct</b> {@code Stream} of {@link OntObjectProperty object} and {@link OntDataProperty data} properties
     * @see #hasKeys()
     */
    default Stream<OntRelationalProperty> fromHasKey() {
        return hasKeys().flatMap(OntList::members).distinct();
    }

    /**
     * Returns this class if the specification allows it to be in subclass position, otherwise returns {@code null}.
     * Some profiles (e.g., OWL2 QL, OWL2 RL)
     * distinguish constructions with respect to their position in the axiom statements.
     *
     * @return {@link OntClass} or {@code null}
     */
    default OntClass asSubClass() {
        return this;
    }

    /**
     * Returns this class if the specification allows it to be in superclass position, otherwise returns {@code null}.
     * Some profiles (e.g., OWL2 QL, OWL2 RL)
     * distinguish constructions with respect to their position in the axiom statements.
     *
     * @return {@link OntClass} or {@code null}
     */
    default OntClass asSuperClass() {
        return this;
    }

    /**
     * Returns this class if the specification allows it to make class assertions, otherwise returns {@code null}.
     * Some profiles (e.g., OWL2 QL, OWL2 RL)
     * distinguish constructions with respect to their position in the axiom statements.
     *
     * @return {@link OntClass} or {@code null}
     */
    default OntClass asAssertionClass() {
        return this;
    }

    /**
     * Returns this class if the specification allows it to be in equivalent
     * ({@code owl:equivalentClass}) position, otherwise returns {@code null}.
     * Some profiles (e.g., OWL2 QL, OWL2 RL)
     * distinguish constructions with respect to their position in the axiom statements.
     *
     * @return {@link OntClass} or {@code null}
     */
    default OntClass asEquivalentClass() {
        return this;
    }

    /**
     * Returns this class if the specification allows it to be in disjoint
     * ({@code owl:disjointWith}, {@code owl:AllDisjointClasses}) position, otherwise returns {@code null}.
     * Some profiles (e.g., OWL2 QL, OWL2 RL)
     * distinguish constructions with respect to their position in the axiom statements.
     *
     * @return {@link OntClass} or {@code null}
     */
    default OntClass asDisjointClass() {
        return this;
    }

    /*
     * ============================
     * All known Class Expressions:
     * ============================
     */

    /**
     * @see OntModel#createObjectSomeValuesFrom(OntObjectProperty, OntClass)
     */
    interface ObjectSomeValuesFrom extends ValueRestriction<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectSomeValuesFrom>, SetProperty<OntObjectProperty, ObjectSomeValuesFrom> {
    }

    /**
     * @see OntModel#createDataSomeValuesFrom(OntDataProperty, OntDataRange)
     */
    interface DataSomeValuesFrom extends ValueRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataSomeValuesFrom>, SetProperty<OntDataProperty, DataSomeValuesFrom> {
    }

    /**
     * @see OntModel#createObjectAllValuesFrom(OntObjectProperty, OntClass)
     */
    interface ObjectAllValuesFrom extends ValueRestriction<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectAllValuesFrom>, SetProperty<OntObjectProperty, ObjectAllValuesFrom> {
    }

    /**
     * @see OntModel#createDataAllValuesFrom(OntDataProperty, OntDataRange)
     */
    interface DataAllValuesFrom extends ValueRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataAllValuesFrom>, SetProperty<OntDataProperty, DataAllValuesFrom> {
    }

    /**
     * @see OntModel#createObjectHasValue(OntObjectProperty, OntIndividual)
     */
    interface ObjectHasValue extends ValueRestriction<OntIndividual, OntObjectProperty>,
            SetValue<OntIndividual, ObjectHasValue>, SetProperty<OntObjectProperty, ObjectHasValue> {
    }

    /**
     * @see OntModel#createDataHasValue(OntDataProperty, Literal)
     */
    interface DataHasValue extends ValueRestriction<Literal, OntDataProperty>,
            SetValue<Literal, DataHasValue>, SetProperty<OntDataProperty, DataHasValue> {
    }

    /**
     * @see OntModel#createObjectMinCardinality(OntObjectProperty, int, OntClass)
     */
    interface ObjectMinCardinality extends CardinalityRestriction<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectMinCardinality>,
            SetProperty<OntObjectProperty, ObjectMinCardinality>,
            SetCardinality<ObjectMinCardinality> {
    }

    /**
     * @see OntModel#createDataMinCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataMinCardinality extends CardinalityRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataMinCardinality>,
            SetProperty<OntDataProperty, DataMinCardinality>,
            SetCardinality<DataMinCardinality> {
    }

    /**
     * @see OntModel#createDataMaxCardinality(OntDataProperty, int, OntDataRange)
     */
    interface ObjectMaxCardinality extends CardinalityRestriction<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectMaxCardinality>,
            SetProperty<OntObjectProperty, ObjectMaxCardinality>,
            SetCardinality<ObjectMaxCardinality> {
    }

    /**
     * @see OntModel#createDataMaxCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataMaxCardinality extends CardinalityRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataMaxCardinality>,
            SetProperty<OntDataProperty, DataMaxCardinality>,
            SetCardinality<DataMaxCardinality> {
    }

    /**
     * @see OntModel#createObjectCardinality(OntObjectProperty, int, OntClass)
     */
    interface ObjectCardinality extends CardinalityRestriction<OntClass, OntObjectProperty>,
            SetValue<OntClass, ObjectCardinality>,
            SetProperty<OntObjectProperty, ObjectCardinality>,
            SetCardinality<ObjectCardinality> {
    }

    /**
     * @see OntModel#createDataCardinality(OntDataProperty, int, OntDataRange)
     */
    interface DataCardinality extends CardinalityRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, DataCardinality>,
            SetProperty<OntDataProperty, DataCardinality>,
            SetCardinality<DataCardinality> {
    }

    /**
     * @see OntModel#createHasSelf(OntObjectProperty)
     */
    interface HasSelf extends UnaryRestriction<OntObjectProperty>, SetProperty<OntObjectProperty, HasSelf> {
    }

    /**
     * @see OntModel#createObjectUnionOf(Collection)
     */
    interface UnionOf extends LogicalExpression, CollectionOf<OntClass>, SetComponents<OntClass, UnionOf> {
    }

    /**
     * @see OntModel#createObjectOneOf(Collection)
     */
    interface OneOf extends LogicalExpression, CollectionOf<OntIndividual>, SetComponents<OntIndividual, OneOf> {
    }

    /**
     * @see OntModel#createObjectIntersectionOf(Collection)
     */
    interface IntersectionOf extends LogicalExpression, CollectionOf<OntClass>, SetComponents<OntClass, IntersectionOf> {
    }

    /**
     * @see OntModel#createObjectComplementOf(OntClass)
     */
    interface ComplementOf extends LogicalExpression, HasValue<OntClass>, SetValue<OntClass, ComplementOf> {
    }

    /**
     * @see OntModel#createDataAllValuesFrom(Collection, OntDataRange)
     */
    interface NaryDataAllValuesFrom extends NaryRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, NaryDataAllValuesFrom>, SetProperties<OntDataProperty, NaryDataAllValuesFrom> {
    }

    /**
     * @see OntModel#createDataSomeValuesFrom(Collection, OntDataRange)
     */
    interface NaryDataSomeValuesFrom extends NaryRestriction<OntDataRange, OntDataProperty>,
            SetValue<OntDataRange, NaryDataSomeValuesFrom>, SetProperties<OntDataProperty, NaryDataSomeValuesFrom> {
    }

    /**
     * An OWL Class {@link OntEntity Entity}, a named class expression.
     * This is an analogue of {@link org.apache.jena.ontology.OntClass}, but for OWL2.
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
         * Note: {@code null}s in the collection will cause {@link OntJenaException.IllegalArgument exception}.
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
            OntClass.super.addSuperClass(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addSubClass(OntClass other) {
            OntClass.super.addSubClass(other);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Named addEquivalentClass(OntClass other) {
            OntClass.super.addEquivalentClass(other);
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
        default Named addHasKey(OntRelationalProperty... properties) {
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
        @Override
        default Named addDisjointClass(OntClass other) {
            OntClass.super.addDisjointClass(other);
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
         * i.e., all those statements with the predicate {@link OWL#disjointUnionOf owl:disjointUnionOf}
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
     * An abstract for *Value*Restrictions (e.g. {@link DataHasValue},  {@link ObjectAllValuesFrom})
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRelationalProperty}
     */
    interface ValueRestriction<O extends RDFNode, P extends OntRelationalProperty>
            extends ComponentRestriction<O, P> {
    }

    /**
     * An abstraction for Cardinality Restrictions.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRelationalProperty}
     */
    interface CardinalityRestriction<O extends OntObject, P extends OntRelationalProperty>
            extends HasCardinality, ComponentRestriction<O, P> {
    }

    /**
     * An abstract class expression (Restriction) that has component (i.e., 'filler' in OWL-API terms):
     * all Cardinality Restrictions, Existential/Universal Restrictions, Individual/Literal Value Restrictions.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRelationalProperty}
     */
    interface ComponentRestriction<O extends RDFNode, P extends OntRelationalProperty>
            extends UnaryRestriction<P>, HasValue<O> {
    }

    /**
     * An abstraction that unites all {@link Restriction Restriction}s
     * with the predicate {@link OWL#onProperties owl:onProperties}.
     *
     * @param <O> a value type
     * @param <P> any subtype of {@link OntRelationalProperty}
     */
    interface NaryRestriction<O extends OntObject, P extends OntRelationalProperty>
            extends Restriction, HasProperties<P>, HasValue<O> {
    }

    /**
     * An abstract class expression that unites all {@link Restriction Restriction}s
     * with the predicate {@link OWL#onProperty owl:onProperty}.
     *
     * @param <P> any subtype of {@link OntRelationalProperty}
     */
    interface UnaryRestriction<P extends OntRelationalProperty> extends Restriction, HasProperty<P> {
    }

    /**
     * A supertype for all class expressions with the type {@link OWL#Restriction}.
     */
    interface Restriction extends OntClass {
    }

    /**
     * An abstract class expression that unites class expressions consisting of multiple components.
     * There are three kinds of such expressions: {@link UnionOf}, {@link IntersectionOf} and {@link OneOf}.
     *
     * @param <O> a {@link OntObject} component type
     */
    interface CollectionOf<O extends OntObject> extends OntClass, HasRDFNodeList<O> {
        /**
         * Lists all allowed components of the collection.
         * Note that the returned values are not necessarily the same as {@link OntList#members()} output:
         * some profiles (e.g., OWL2 QL) impose some restrictions.
         *
         * @return a {@code Stream} of {@link O}s
         */
        default Stream<O> components() {
            return getList().members();
        }
    }

    /**
     * Describes Boolean Connectives and Enumeration of Individuals
     * ({@link ComplementOf}, {@link UnionOf}, {@link IntersectionOf} and {@link OneOf}).
     */
    interface LogicalExpression extends OntClass {
    }

}