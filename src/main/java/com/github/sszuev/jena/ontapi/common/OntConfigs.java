package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntModelControls;

/**
 * Collection of ontology configurations.
 */
public class OntConfigs {

    public static final OntConfig OWL2_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setFalse(OntModelControls.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setFalse(OntModelControls.USE_OWL_V1_VOCABULARY)
            .setFalse(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setFalse(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setFalse(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setTrue(OntModelControls.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setTrue(OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setTrue(OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)
            .setTrue(OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE)
            .setTrue(OntModelControls.USE_OWL_CLASS_EQUIVALENT_CLASS_FEATURE);

    public static final OntConfig OWL1_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelControls.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setTrue(OntModelControls.USE_OWL_V1_VOCABULARY)
            .setTrue(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setTrue(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setFalse(OntModelControls.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)
            .setTrue(OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE)
            .setTrue(OntModelControls.USE_OWL_CLASS_EQUIVALENT_CLASS_FEATURE);

    public static final OntConfig OWL1_LITE_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelControls.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setTrue(OntModelControls.USE_OWL_V1_VOCABULARY)
            .setTrue(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setTrue(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setFalse(OntModelControls.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)
            .setFalse(OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE)
            .setTrue(OntModelControls.USE_OWL_CLASS_EQUIVALENT_CLASS_FEATURE);

    public static final OntConfig RDFS_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelControls.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS)
            .setFalse(OntModelControls.USE_OWL_V1_VOCABULARY)
            .setFalse(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setFalse(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)
            .setFalse(OntModelControls.USE_OWL2_NAMED_INDIVIDUAL_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_CLASS_HAS_KEY_FEATURE)
            .setFalse(OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)
            .setFalse(OntModelControls.USE_OWL_CLASS_DISJOINT_WITH_FEATURE)
            .setFalse(OntModelControls.USE_OWL_CLASS_EQUIVALENT_CLASS_FEATURE);
}
