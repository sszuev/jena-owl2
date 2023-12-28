package com.github.sszuev.jena.ontapi;

/*
@ParameterizedTest
@EnumSource(names = {
        "OWL2_DL_MEM_RDFS_BUILTIN_INF",
        "OWL2_MEM",
        "OWL2_MEM_RDFS_INF",
        "OWL2_MEM_TRANS_INF",
        "OWL1_MEM",
        "OWL1_MEM_RDFS_INF",
        "OWL1_MEM_TRANS_INF",
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
    OWL2_DL_MEM_RDFS_BUILTIN_INF(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF),

    OWL2_MEM(OntSpecification.OWL2_MEM),
    OWL2_MEM_RDFS_INF(OntSpecification.OWL2_MEM_RDFS_INF),
    OWL2_MEM_TRANS_INF(OntSpecification.OWL2_MEM_TRANS_INF),

    OWL1_MEM(OntSpecification.OWL1_MEM),
    OWL1_MEM_RDFS_INF(OntSpecification.OWL1_MEM_RDFS_INF),
    OWL1_MEM_TRANS_INF(OntSpecification.OWL1_MEM_TRANS_INF),
    OWL1_MEM_RULES_INF(OntSpecification.OWL1_MEM_RULES_INF),

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
}
