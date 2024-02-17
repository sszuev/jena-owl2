package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.rdf.model.Resource;

/**
 * Default settings for {@link OntConfig}.
 */
public enum OntModelControls {
    /**
     * If {@code true}, {@link com.github.sszuev.jena.ontapi.model.OntID}
     * will be generated automatically if it is absent (as a b-node).
     * A valid OWL ontology must have a single ontology ID.
     */
    USE_GENERATE_ONTOLOGY_HEADER_IF_ABSENT_STRATEGY,
    /**
     * If {@code true}, a multiple ontology header is allowed.
     * Since a valid OWL ontology can have only a single ontology ID, the most suitable will be chosen.
     * Note that if there are several anonymous headers with similar contents,
     * then there is no guarantee that the same node will always be selected after restarting JVM.
     */
    USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY,
    /**
     * If this key is set to {@code true}, then the class/property hierarchies
     * (e.g., see {@link com.github.sszuev.jena.ontapi.model.OntClass#subClasses()})
     * are to be inferred by the naked model itself using builtin algorithms.
     * Should not be used in conjunction with Reasoner.
     *
     * @see OntSpecification#OWL2_DL_MEM_BUILTIN_INF
     */
    USE_BUILTIN_HIERARCHY_SUPPORT,
    /**
     * If this key is set to {@code true}, then OWL1.1 vocabulary should be used in certain cases,
     * i.e. {@code owl:distinctMembers} instead of {@code owl:members},
     * {@code rdfs:Datatype} instead of {@code owl:DataRange}.
     */
    USE_OWL_V1_VOCABULARY,
    /**
     * If this key is set to {@code true}, all class expressions are allowed to be named (can have URI).
     * This option is for compatibility with {@link org.apache.jena.ontology.OntModel}.
     * In OWL2, complex class expression should be anonymous.
     */
    ALLOW_NAMED_CLASS_EXPRESSIONS,
    /**
     * If this key is set to {@code true}, there is a special type of class expressions,
     * which includes any structure declared as {@code owl:Class} or {@code owl:Restriction}
     * that cannot be classified as a specific type.
     * Casting such a construction to a particular class type
     * (e.g. {@code generic.as(OntClass.OneOf.class)}) will result in an exception,
     * but as a class expression, it can a type of the individual, can be a domain for property, etc.
     * This option is for compatibility with {@link org.apache.jena.ontology.OntModel}.
     */
    ALLOW_GENERIC_CLASS_EXPRESSIONS,
    /**
     * Used while {@link com.github.sszuev.jena.ontapi.model.OntModel#individuals()}.
     * If {@code true}, the class type is checked only by declaration
     * ({@code owl:Class} &amp; {@code owl:Restriction} for OWL profile, {@code rdfs:Class} for RDFS profile).
     * Otherwise, a full checking is performed.
     */
    USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS,
    /**
     * If {@code true},
     * named class testing is compatible with the legacy {@link org.apache.jena.ontology.OntModel Jena OntModel},
     * otherwise, a strict check against the specification for the class declaration is performed
     * ({@code owl:Class} for OWL &amp; {@code rdfs:Class} for RDFS types are required).
     * Note that this only applies to
     * {@link org.apache.jena.enhanced.EnhNode#canAs EnhNode#canAs} and {@link org.apache.jena.enhanced.EnhNode#as EnhNode#as} methods;
     * iteration (e.g., methods {@code OntModel.ontObjects(OntClass.class)})
     * still does not take into account classes with incorrect or missing declarations.
     * For legacy Jena's casting rules see {@link org.apache.jena.ontology.Profile} impls.
     */
    USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY,
    /**
     * If this key is set to {@code true},
     * then {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#NamedIndividual owl:NamedIndividual}
     * declaration is used for creating individuals.
     */
    USE_OWL2_NAMED_INDIVIDUAL_DECLARATION_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#hasKey owl:hasKey} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#hasKeys() OntClass#hasKeys()} will return empty {@code Stream},
     * modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#addHasKey(OntRelationalProperty...) OntClass#addHasKey(OntRelationalProperty...)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL2_CLASS_HAS_KEY_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#disjointUnionOf owl:disjointUnionOf} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntClass.Named#disjointUnions() OntClass.Named#disjointUnions()}
     * will return empty {@code Stream}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntClass.Named#addDisjointUnion(OntClass...) OntClass.Named#addDisjointUnion(OntClass...)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#disjointWith owl:disjointWith} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#disjoints() OntClass#disjoints()}
     * will return empty {@code Stream}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#addDisjointClass(OntClass) OntClass#addDisjointClass(OntClass)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_CLASS_DISJOINT_WITH_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#equivalentClass owl:equivalentClass} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#equivalentClasses() OntClass#equivalentClasses()}
     * will return empty {@code Stream}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntClass#addEquivalentClass(OntClass) OntClass#addEquivalentClass(OntClass)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_CLASS_EQUIVALENT_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#propertyDisjointWith owl:propertyDisjointWith} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntRelationalProperty#disjointProperties() OntRelationalProperty#disjointProperties()}
     * will return empty {@code Stream}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#addDisjointProperty(OntObjectProperty) OntObjectProperty#addDisjointProperty(OntObjectProperty)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#equivalentProperty owl:equivalentProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntRelationalProperty#equivalentProperties() OntRelationalProperty#equivalentProperties()}
     * will return empty {@code Stream}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#addEquivalentProperty(OntObjectProperty) OntObjectProperty#addEquivalentProperty(OntObjectProperty)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_EQUIVALENT_FEATURE,
    /**
     * Controls data {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#FunctionalProperty owl:FunctionalProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntDataProperty#isFunctional() OntDataProperty#isFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntDataProperty#setFunctional(boolean) OntDataProperty#setFunctional(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE,
    /**
     * Controls object {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#FunctionalProperty owl:FunctionalProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isFunctional() OntObjectProperty#isFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setFunctional(boolean) OntObjectProperty#setFunctional(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_OBJECT_PROPERTY_FUNCTIONAL_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#InverseFunctionalProperty owl:InverseFunctionalProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isInverseFunctional() OntObjectProperty#isInverseFunctional()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setInverseFunctional(boolean) OntObjectProperty#setInverseFunctional(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#SymmetricProperty owl:SymmetricProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isSymmetric() OntObjectProperty#isSymmetric()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setSymmetric(boolean) OntObjectProperty#setSymmetric(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_SYMMETRIC_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#AsymmetricProperty owl:AsymmetricProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isAsymmetric() OntObjectProperty#isAsymmetric()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setAsymmetric(boolean) OntObjectProperty#setAsymmetric(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_ASYMMETRIC_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#TransitiveProperty owl:TransitiveProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isTransitive() OntObjectProperty#isTransitive()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setTransitive(boolean) OntObjectProperty#setTransitive(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_TRANSITIVE_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#ReflexiveProperty owl:ReflexiveProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isReflexive() OntObjectProperty#isReflexive()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setReflexive(boolean) OntObjectProperty#setReflexive(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_REFLEXIVE_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#IrreflexiveProperty owl:IrreflexiveProperty} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#isIrreflexive() OntObjectProperty#isIrreflexive()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#setIrreflexive(boolean) OntObjectProperty#setIrreflexive(boolean)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#inverseOf owl:inverseOf} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#inverseProperties() OntObjectProperty#inverseProperties()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#addInverseProperty(OntObjectProperty) OntObjectProperty#addInverseProperty(OntObjectProperty)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_INVERSE_OF_FEATURE,
    /**
     * If this key is set to {@code true}, an anonymous inverse object property type is enabled (OWL2 feature).
     * That is {@code _:x owl:inverseOf PN}, where {@code _:x} is a b-node and {@code PN} is a named object property.
     */
    USE_OWL_INVERSE_OBJECT_PROPERTY_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#propertyChainAxiom owl:propertyChainAxiom} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#propertyChains() OntObjectProperty#propertyChains()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntObjectProperty#addPropertyChain(OntObjectProperty...) OntObjectProperty#addPropertyChain(OntObjectProperty...)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_PROPERTY_CHAIN_AXIOM_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#sameAs owl:sameAs} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntIndividual#sameIndividuals() OntIndividual#sameIndividuals()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntIndividual#addSameIndividual(OntIndividual) OntIndividual#addSameIndividual(OntIndividual)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_INDIVIDUAL_SAME_AS_FEATURE,
    /**
     * Controls {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#differentFrom owl:differentFrom} functionality.
     * If disabled,
     * {@link com.github.sszuev.jena.ontapi.model.OntIndividual#differentIndividuals() OntIndividual#differentIndividuals()}
     * will return {@code false}, modification operations, such as
     * {@link com.github.sszuev.jena.ontapi.model.OntIndividual#removeDifferentIndividual(Resource) OntIndividual#removeDifferentIndividual(Resource)},
     * will throw {@link com.github.sszuev.jena.ontapi.OntJenaException.Unsupported OntJenaException.Unsupported} exception.
     */
    USE_OWL_INDIVIDUAL_DIFFERENT_FROM_FEATURE,
    /**
     * If this key is set to {@code true},
     * then {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#qualifiedCardinality owl:qualifiedCardinality},
     * {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#maxQualifiedCardinality owl:maxQualifiedCardinality},
     * {@link com.github.sszuev.jena.ontapi.vocabulary.OWL#minQualifiedCardinality owl:minQualifiedCardinality}
     * predicates are allowed for Cardinality restrictions.
     */
    USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE,
}
