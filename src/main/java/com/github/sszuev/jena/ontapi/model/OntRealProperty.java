package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * A common (abstract) interface for any Ontology Data and Object Property expressions.
 * In OWL2 terms it is any {@link OntProperty Property Expression} minus {@link OntAnnotationProperty Annotation Property}.
 * <p>
 * Created by @ssz on 21.07.2018.
 */
public interface OntRealProperty extends OntProperty {

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean} if {@code true} answers the directly adjacent properties in the sub-property relation:
     *               i.e. eliminate any properties for which there is a longer route to reach that parent under the sub-property relation
     * @return <b>distinct</b> {@code Stream} of data <b>or</b> object properties
     */
    Stream<? extends OntRealProperty> subProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean}: if {@code true} answers the directly adjacent properties in the super-property relation,
     *               i.e. eliminate any property for which there is a longer route to reach that parent under the super-property relation
     * @return <b>distinct</b> {@code Stream} of data <b>or</b> object properties
     */
    Stream<? extends OntRealProperty> superProperties(boolean direct);

    /**
     * Lists all property ranges,
     * i.e. all objects from statements with this property as subject and {@code rdfs:range} as predicate.
     *
     * @return {@code Stream} of {@link OntObject ontology object}s
     */
    Stream<? extends OntObject> ranges();

    /**
     * {@inheritDoc}
     *
     * @return {@code Stream} of {@link OntRealProperty}s (object <b>or</b> data properties)
     */
    Stream<? extends OntRealProperty> subProperties();

    /**
     * {@inheritDoc}
     *
     * @return {@code Stream} of {@link OntRealProperty}s (object <b>or</b> data properties)
     */
    Stream<? extends OntRealProperty> superProperties();

    /**
     * Answers a {@code Stream} of all the classes in this ontology,
     * such that each returned class has this property as one of its properties in
     * {@link OntClass#declaredProperties(boolean)}.
     * This simulates a frame-like view of properties and classes;
     * for more details see the
     * <a href="https://jena.apache.org/documentation/notes/rdf-frames.html">Apache Jena: Presenting RDF as frames</a>
     * <p>
     * The behavior of this method must be identical to the behavior of the Jena method
     * {@link org.apache.jena.ontology.OntProperty#listDeclaringClasses(boolean)}.
     *
     * @param direct {@code boolean} if {@code true}, use only <em>direct</em> associations between classes and properties
     * @return a {@code Stream} of the classes having this property as one of their declared properties
     */
    Stream<OntClass> declaringClasses(boolean direct);

    /**
     * Lists all properties that are disjoint with this property.
     * In other words, returns all objects from statements of the form {@code P owl:propertyDisjointWith R},
     * where {@code P} is this property and {@code R} is a returned property of the same type.
     *
     * @return {@code Stream} of {@link OntRealProperty}s - object <b>or</b> data properties
     * @see OntDisjoint.Properties
     */
    Stream<? extends OntRealProperty> disjointProperties();

    /**
     * Lists all properties that equivalent to this one.
     * In other words, returns all objects from statements of the form {@code P owl:equivalentProperty R},
     * where {@code P} is this property and {@code R} is a returned property of the same type.
     *
     * @return {@code Stream} of {@link OntRealProperty}s - object <b>or</b> data properties
     */
    Stream<? extends OntRealProperty> equivalentProperties();

    /**
     * Lists all negative property assertions.
     * A negative property assertion is anonymous resource
     * with the type {@link OWL#NegativePropertyAssertion owl:NegativePropertyAssertion}
     * that has a data or object property expression as an object
     * on the predicate {@link OWL#assertionProperty owl:assertionProperty}.
     *
     * @return {@code Stream} of {@link OntNegativeAssertion}
     */
    Stream<? extends OntNegativeAssertion<?, ?>> negativeAssertions();

    /**
     * Adds a statement with the {@link RDFS#domain} as predicate,
     * this property as a subject, and the specified {@link OntClass class expression} as an object.
     *
     * @param ce {@link OntClass}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDomainStatement(OntClass)
     */
    OntRealProperty addDomain(OntClass ce);

    /**
     * {@inheritDoc}
     */
    @Override
    OntRealProperty removeDomain(Resource domain);

    /**
     * {@inheritDoc}
     */
    @Override
    OntRealProperty removeRange(Resource range);

    /**
     * {@inheritDoc}
     */
    @Override
    OntRealProperty removeSuperProperty(Resource property);

    /**
     * Removes the equivalent property statement
     * (a statement with the predicate {@link OWL#equivalentProperty owl:equivalentProperty})
     * for the specified resource (considered as object), including the corresponding statement's annotations.
     * No-op in case no such equivalent property relationship is found.
     * Removes all triples with predicate {@code owl:equivalentProperty} (and all theirs annotation triples)
     * if {@code null} is given.
     *
     * @param property {@link Resource} or {@code null} to remove all equivalent properties
     * @return <b>this</b> instance to allow cascading calls
     */
    OntRealProperty removeEquivalentProperty(Resource property);

    /**
     * Removes the {@code owl:propertyDisjointWith} statement
     * (a statement with the predicate {@link OWL#propertyDisjointWith owl:propertyDisjointWith})
     * for the specified resource (considered as object), including the corresponding statement's annotations.
     * No-op in case no such disjoint property relationship is found.
     * Removes all triples with predicate {@code owl:propertyDisjointWith} (and all theirs annotation triples)
     * if {@code null} is given.
     *
     * @param property {@link Resource} or {@code null} to remove all disjoint properties
     * @return <b>this</b> instance to allow cascading calls
     * @see OntDisjoint.Properties
     */
    OntRealProperty removeDisjointProperty(Resource property);

    /**
     * Answers a {@code Stream} over any restrictions that mention this property as
     * the property that the restriction is adding some constraint to.
     * For example:
     * <pre>
     * {@code
     * _:x rdf:type owl:Restriction.
     * _:x owl:onProperty P.
     * _:x owl:qualifiedCardinality n.
     * _:x owl:onClass C.
     * }
     * </pre>
     * Note that any such restrictions do not affect the global semantics of this property itself.
     * Restrictions define new class expressions, and the property constraints are local to that class expression.
     * This method is provided as a convenience to assist callers to navigate the relationships in the model.
     *
     * @return a {@code Stream} whose values are the restrictions from the local model that reference this property.
     */
    Stream<? extends OntClass.RestrictionCE<? extends OntRealProperty>> referringRestrictions();

    /**
     * Lists all the declared domain class expressions of this property expression.
     * In other words, returns the right-hand sides of statement {@code P rdfs:domain C},
     * where {@code P} is this property expression.
     *
     * @return {@code Stream} of {@link OntClass class expression}s
     */
    @Override
    default Stream<OntClass> domains() {
        return objects(RDFS.domain, OntClass.class);
    }

    /**
     * Gets all direct or indirect domains which present in RDF graph.
     * Indirect domains are calculated using {@code OntClass.superClasses(true)} relationship.
     * For example consider the following statements (only people can have names):
     * <pre>
     * {@code
     * :Primate rdf:type owl:Class .
     * :Person rdf:type owl:Class .
     * :hasName rdf:type owl:DatatypeProperty .
     * :hasName rdfs:domain :Person .
     * :Person rdfs:subClassOf :Primate .
     * }
     * </pre>
     * from these statements it can be derived that only primates can have names
     * (which does not mean that all primates have names):
     * <pre>
     * {@code
     * :hasName rdfs:domain :Primate .
     * }
     * </pre>
     * The same true for object properties: if "only people can have dogs" then "only primates can have dogs"
     *
     * @param direct if {@code true} the method behaves the same as {@link #domains()}
     * @return {@code Stream} of {@link OntClass class expression}s, distinct
     */
    default Stream<OntClass> domains(boolean direct) {
        if (direct) return domains();
        return domains().flatMap(d -> Stream.concat(Stream.of(d), d.superClasses(false))).distinct();
    }

    /**
     * Creates the {@code P rdf:type owl:FunctionalProperty} property declaration statement,
     * where {@code P} is this property.
     *
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #setFunctional(boolean)
     */
    default OntStatement addFunctionalDeclaration() {
        return addStatement(RDF.type, OWL.FunctionalProperty);
    }

    /**
     * Adds a statement {@code P rdfs:domain C},
     * where {@code P} is this property expression and {@code C} is the specified class expression.
     *
     * @param ce {@link OntClass class expression}, not null
     * @return {@link OntStatement} to allow the subsequent addition of annotations
     * @see #addDomain(OntClass)
     */
    default OntStatement addDomainStatement(OntClass ce) {
        return addStatement(RDFS.domain, ce);
    }

    /**
     * Answers {@code true} iff it is a functional (data or object) property expression.
     * A functional property is defined by the statement {@code P rdf:type owl:FunctionalProperty},
     * where {@code P} is this property expression.
     *
     * @return boolean
     */
    default boolean isFunctional() {
        return hasType(OWL.FunctionalProperty);
    }

    /**
     * Adds or removes {@link OWL#FunctionalProperty owl:FunctionalProperty} declaration
     * for this property according to the given boolean flag.
     * Note: the statement is removed along with all its annotations.
     *
     * @param functional {@code true} if should be functional
     * @return <b>this</b> instance to allow cascading calls
     * @see #addFunctionalDeclaration()
     */
    OntRealProperty setFunctional(boolean functional);

}
