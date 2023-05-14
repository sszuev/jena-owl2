package com.github.sszuev.jena.ontapi;

public enum TestSpec {
    OWL2_DL_MEM_RDFS_BUILTIN_INF(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF),
    OWL2_MEM(OntSpecification.OWL2_MEM),
    ;
    public final OntSpecification spec;

    TestSpec(OntSpecification spec) {
        this.spec = spec;
    }
}
