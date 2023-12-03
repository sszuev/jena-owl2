package com.github.sszuev.jena.ontapi.common;

/**
 * Configuration to control {@link com.github.sszuev.jena.ontapi.model.OntModel} and {@link OntPersonality} behavior.
 */
public class OntConfig {
    public static final OntConfig DEFAULT = new OntConfig(false, false, false);

    protected final boolean useBuiltinHierarchySupport;
    protected final boolean useOWLv1Vocabulary;
    protected final boolean useNamedIndividualDeclaration;

    protected OntConfig(
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

    public OntConfig useBuiltinHierarchySupport(boolean with) {
        return new OntConfig(
                with,
                this.useOWLv1Vocabulary,
                this.useNamedIndividualDeclaration
        );
    }

    public OntConfig useOWLv1Vocabulary(boolean with) {
        return new OntConfig(
                this.useBuiltinHierarchySupport,
                with,
                this.useNamedIndividualDeclaration
        );
    }

    public OntConfig useNamedIndividualDeclaration(boolean with) {
        return new OntConfig(
                this.useBuiltinHierarchySupport,
                this.useOWLv1Vocabulary,
                with
        );
    }

}
