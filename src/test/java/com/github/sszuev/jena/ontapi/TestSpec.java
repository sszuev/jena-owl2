package com.github.sszuev.jena.ontapi;

/*
@ParameterizedTest
@EnumSource(names = {
        "OWL2_DL_MEM_RDFS_BUILTIN_INF",
        "OWL2_MEM",
        "OWL1_MEM",
        "RDFS_MEM",
})
*/
@SuppressWarnings("unused")
public enum TestSpec {
    OWL2_DL_MEM_RDFS_BUILTIN_INF(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF),
    OWL2_MEM(OntSpecification.OWL2_MEM),
    OWL1_MEM(OntSpecification.OWL1_MEM),
    RDFS_MEM(OntSpecification.RDFS_MEM),
    ;
    public final OntSpecification inst;

    TestSpec(OntSpecification inst) {
        this.inst = inst;
    }
}
