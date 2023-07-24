package com.github.sszuev.jena.ontapi;

/*
@ParameterizedTest
@EnumSource(names = {
        "OWL2_DL_MEM_RDFS_BUILTIN_INF",
        "OWL2_MEM",
        "RDFS_MEM",
})
*/
public enum TestSpec {
    OWL2_DL_MEM_RDFS_BUILTIN_INF(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF),
    OWL2_MEM(OntSpecification.OWL2_MEM),
    RDFS_MEM(OntSpecification.RDFS_MEM),
    ;
    public final OntSpecification spec;

    TestSpec(OntSpecification spec) {
        this.spec = spec;
    }
}
