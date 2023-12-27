package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;

/**
 * Default settings for {@link OntConfig}.
 */
public enum OntModelConfig {
    /**
     * If this key is set to {@code true}, then the class/property hierarchies
     * (e.g., see {@link com.github.sszuev.jena.ontapi.model.OntClass#subClasses()})
     * are to be inferred by the naked model itself using builtin algorithms.
     * Should not be used in conjunction with Reasoner.
     *
     * @see com.github.sszuev.jena.ontapi.impl.HierarchySupport
     */
    USE_BUILTIN_HIERARCHY_SUPPORT,
    /**
     * If this key is set to {@code true}, then OWL1.1 vocabulary should be used in certain cases,
     * i.e. {@code owl:distinctMembers} instead of {@code owl:members},
     * {@code rdfs:Datatype} instead of {@code owl:DataRange}.
     */
    USE_OWL_V1_VOCABULARY,
    /**
     * If this key is set to {@code true},
     * then {@code owl:NamedIndividual} declaration is required for named individuals.
     */
    USE_OWL2_NAMED_INDIVIDUAL_DECLARATION,
    /**
     * If this key is set to {@code true}, class expressions are allowed to be named (can have URI).
     * This option is for compatibility with {@link org.apache.jena.ontology.OntModel}.
     * In OWL2, class expression should be anonymous.
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
     * ({@code owl:Class} & {@code owl:Restriction} for OWL profile, {@code rdfs:Class} for RDFS profile).
     * Otherwise, a full checking is performed.
     */
    USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS,
}
