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
    USE_NAMED_INDIVIDUAL_DECLARATION,
}
