package com.github.sszuev.jena.ontapi.impl;

/**
 * Configuration to control {@link com.github.sszuev.jena.ontapi.model.OntModel} behavior.
 * TODO: this could be a temporary solution.
 */
public class OntModelConfig {
    public static final OntModelConfig DEFAULT = new OntModelConfig(false, false, false);

    protected final boolean useBuiltinHierarchySupport;
    protected final boolean useOWLv1Vocabulary;
    protected final boolean useNamedIndividualDeclaration;

    protected OntModelConfig(
            boolean useBuiltinHierarchySupport,
            boolean useOWLv1Vocabulary,
            boolean useNamedIndividualDeclaration
    ) {
        this.useBuiltinHierarchySupport = useBuiltinHierarchySupport;
        this.useOWLv1Vocabulary = useOWLv1Vocabulary;
        this.useNamedIndividualDeclaration = useNamedIndividualDeclaration;
    }

    /**
     * Answers {@code true} if the class/property hierarchies
     * (e.g., see {@link com.github.sszuev.jena.ontapi.model.OntClass#subClasses()})
     * are to be inferred by the naked model itself using builtin algorithms.
     */
    public boolean useBuiltinHierarchySupport() {
        return useBuiltinHierarchySupport;
    }

    /**
     * Answers {@code true} if OWL1.1 vocabulary should be used in some cases,
     * i.e. {@code owl:distinctMembers} instead of {@code owl:members},
     * {@code rdfs:Datatype} instead of {@code owl:DataRange}.
     */
    public boolean useOWLv1Vocabulary() {
        return useOWLv1Vocabulary;
    }

    /**
     * User {@code owl:NamedIndividual}.
     */
    public boolean useNamedIndividualDeclaration() {
        return useNamedIndividualDeclaration;
    }

    public OntModelConfig useBuiltinHierarchySupport(boolean with) {
        return new OntModelConfig(
                with,
                this.useOWLv1Vocabulary,
                this.useNamedIndividualDeclaration
        );
    }

    public OntModelConfig useOWLv1Vocabulary(boolean with) {
        return new OntModelConfig(
                this.useBuiltinHierarchySupport,
                with,
                this.useNamedIndividualDeclaration
        );
    }

    public OntModelConfig useNamedIndividualDeclaration(boolean with) {
        return new OntModelConfig(
                this.useBuiltinHierarchySupport,
                this.useOWLv1Vocabulary,
                with
        );
    }

}
