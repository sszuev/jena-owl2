package com.github.sszuev.jena.ontapi;

/*
@ParameterizedTest
@EnumSource(names = {
        "OWL2_DL_MEM_RDFS_BUILTIN_INF",
        "OWL2_DL_MEM",
        "OWL2_DL_MEM_RDFS_INF",
        "OWL2_DL_MEM_TRANS_INF",
        "OWL2_DL_MEM_RULES_INF",
        "OWL2_MEM",
        "OWL2_MEM_RDFS_INF",
        "OWL2_MEM_TRANS_INF",
        "OWL2_MEM_RULES_INF",
        "OWL2_MEM_MINI_RULES_INF",
        "OWL2_MEM_MICRO_RULES_INF",
        "OWL2_EL_MEM",
        "OWL2_EL_MEM_RDFS_INF",
        "OWL2_EL_MEM_TRANS_INF",
        "OWL2_EL_MEM_RULES_INF",
        "OWL2_QL_MEM",
        "OWL2_QL_MEM_RDFS_INF",
        "OWL2_QL_MEM_TRANS_INF",
        "OWL2_QL_MEM_RULES_INF",
        "OWL1_DL_MEM",
        "OWL1_DL_MEM_RDFS_INF",
        "OWL1_DL_MEM_TRANS_INF",
        "OWL1_DL_MEM_RULES_INF",
        "OWL1_MEM",
        "OWL1_MEM_RDFS_INF",
        "OWL1_MEM_TRANS_INF",
        "OWL1_MEM_RULES_INF",
        "OWL1_MEM_MINI_RULES_INF",
        "OWL1_MEM_MICRO_RULES_INF",
        "OWL1_LITE_MEM",
        "OWL1_LITE_MEM_RDFS_INF",
        "OWL1_LITE_MEM_TRANS_INF",
        "OWL1_LITE_MEM_RULES_INF",
        "RDFS_MEM",
        "RDFS_MEM_RDFS_INF",
        "RDFS_MEM_TRANS_INF",
})
*/
@SuppressWarnings("ALL")
public enum TestSpec {
    OWL2_MEM(OntSpecification.OWL2_FULL_MEM),
    OWL2_MEM_RDFS_INF(OntSpecification.OWL2_FULL_MEM_RDFS_INF),
    OWL2_MEM_TRANS_INF(OntSpecification.OWL2_FULL_MEM_TRANS_INF),
    OWL2_MEM_RULES_INF(OntSpecification.OWL2_FULL_MEM_RULES_INF),
    OWL2_MEM_MINI_RULES_INF(OntSpecification.OWL2_FULL_MEM_MINI_RULES_INF),
    OWL2_MEM_MICRO_RULES_INF(OntSpecification.OWL2_FULL_MEM_MICRO_RULES_INF),

    OWL2_DL_MEM_RDFS_BUILTIN_INF(OntSpecification.OWL2_DL_MEM_BUILTIN_INF),
    OWL2_DL_MEM(OntSpecification.OWL2_DL_MEM),
    OWL2_DL_MEM_RDFS_INF(OntSpecification.OWL2_DL_MEM_RDFS_INF),
    OWL2_DL_MEM_TRANS_INF(OntSpecification.OWL2_DL_MEM_TRANS_INF),
    OWL2_DL_MEM_RULES_INF(OntSpecification.OWL2_DL_MEM_RULES_INF),

    OWL2_EL_MEM(OntSpecification.OWL2_EL_MEM),
    OWL2_EL_MEM_RDFS_INF(OntSpecification.OWL2_EL_MEM_RDFS_INF),
    OWL2_EL_MEM_TRANS_INF(OntSpecification.OWL2_EL_MEM_TRANS_INF),
    OWL2_EL_MEM_RULES_INF(OntSpecification.OWL2_EL_MEM_RULES_INF),

    OWL2_QL_MEM(OntSpecification.OWL2_QL_MEM),
    OWL2_QL_MEM_RDFS_INF(OntSpecification.OWL2_QL_MEM_RDFS_INF),
    OWL2_QL_MEM_TRANS_INF(OntSpecification.OWL2_QL_MEM_TRANS_INF),
    OWL2_QL_MEM_RULES_INF(OntSpecification.OWL2_QL_MEM_RULES_INF),

    OWL1_MEM(OntSpecification.OWL1_FULL_MEM),
    OWL1_MEM_RDFS_INF(OntSpecification.OWL1_FULL_MEM_RDFS_INF),
    OWL1_MEM_TRANS_INF(OntSpecification.OWL1_FULL_MEM_TRANS_INF),
    OWL1_MEM_RULES_INF(OntSpecification.OWL1_FULL_MEM_RULES_INF),
    OWL1_MEM_MINI_RULES_INF(OntSpecification.OWL1_FULL_MEM_MINI_RULES_INF),
    OWL1_MEM_MICRO_RULES_INF(OntSpecification.OWL1_FULL_MEM_MICRO_RULES_INF),

    OWL1_DL_MEM(OntSpecification.OWL1_DL_MEM),
    OWL1_DL_MEM_RDFS_INF(OntSpecification.OWL1_DL_MEM_RDFS_INF),
    OWL1_DL_MEM_TRANS_INF(OntSpecification.OWL1_DL_MEM_TRANS_INF),
    OWL1_DL_MEM_RULES_INF(OntSpecification.OWL1_DL_MEM_RULES_INF),

    OWL1_LITE_MEM(OntSpecification.OWL1_LITE_MEM),
    OWL1_LITE_MEM_RDFS_INF(OntSpecification.OWL1_LITE_MEM_RDFS_INF),
    OWL1_LITE_MEM_TRANS_INF(OntSpecification.OWL1_LITE_MEM_TRANS_INF),
    OWL1_LITE_MEM_RULES_INF(OntSpecification.OWL1_LITE_MEM_RULES_INF),

    RDFS_MEM(OntSpecification.RDFS_MEM),
    RDFS_MEM_RDFS_INF(OntSpecification.RDFS_MEM_RDFS_INF),
    RDFS_MEM_TRANS_INF(OntSpecification.RDFS_MEM_TRANS_INF),
    ;
    public final OntSpecification inst;

    TestSpec(OntSpecification inst) {
        this.inst = inst;
    }

    boolean isOWL1() {
        return name().startsWith("OWL1");
    }

    boolean isOWL1Lite() {
        return name().startsWith("OWL1_LITE");
    }

    boolean isOWL2() {
        return name().startsWith("OWL2");
    }

    boolean isOWL2EL() {
        return name().startsWith("OWL2_EL");
    }

    boolean isOWL2QL() {
        return name().startsWith("OWL2_QL");
    }

    boolean isRules() {
        return name().endsWith("_RULES_INF");
    }
}
