package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntModelConfig;

/**
 * Collection of ontology configurations.
 */
public class OntConfigs {
    public static final OntConfig OWL2_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setFalse(OntModelConfig.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setFalse(OntModelConfig.USE_OWL_V1_VOCABULARY)
            .setFalse(OntModelConfig.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setFalse(OntModelConfig.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setFalse(OntModelConfig.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setTrue(OntModelConfig.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setTrue(OntModelConfig.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setTrue(OntModelConfig.USE_OWL2_CLASS_DISJOINT_UNION_FEATURE);
    public static final OntConfig OWL1_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelConfig.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setTrue(OntModelConfig.USE_OWL_V1_VOCABULARY)
            .setTrue(OntModelConfig.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setTrue(OntModelConfig.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelConfig.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setFalse(OntModelConfig.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setFalse(OntModelConfig.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setFalse(OntModelConfig.USE_OWL2_CLASS_DISJOINT_UNION_FEATURE);
    public static final OntConfig RDFS_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelConfig.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setFalse(OntModelConfig.USE_OWL_V1_VOCABULARY)
            .setFalse(OntModelConfig.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setFalse(OntModelConfig.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelConfig.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setFalse(OntModelConfig.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setFalse(OntModelConfig.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setFalse(OntModelConfig.USE_OWL2_CLASS_DISJOINT_UNION_FEATURE);
}
