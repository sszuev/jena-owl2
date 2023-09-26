package com.github.sszuev.jena.ontapi.impl;

/**
 * Configuration to control {@link OntModelConfig} behavior.
 */
public class OntModelConfig {
    public static final OntModelConfig DEFAULT = new OntModelConfig(false, false, false);

    protected final boolean useBuiltinHierarchySupport;
    protected final boolean useOWLv1Vocabulary;
    protected final boolean useNamedIndividualDeclaration;

    private OntModelConfig(boolean useBuiltinHierarchySupport, boolean useOWLv1Vocabulary, boolean useNamedIndividualDeclaration) {
        this.useBuiltinHierarchySupport = useBuiltinHierarchySupport;
        this.useOWLv1Vocabulary = useOWLv1Vocabulary;
        this.useNamedIndividualDeclaration = useNamedIndividualDeclaration;
    }

    public OntModelConfig useBuiltinHierarchySupport(boolean with) {
        return new OntModelConfig(with, this.useOWLv1Vocabulary, this.useNamedIndividualDeclaration);
    }

    public OntModelConfig useOWLv1Vocabulary(boolean with) {
        return new OntModelConfig(this.useBuiltinHierarchySupport, with, this.useNamedIndividualDeclaration);
    }

    public OntModelConfig useNamedIndividualDeclaration(boolean with) {
        return new OntModelConfig(this.useBuiltinHierarchySupport, this.useOWLv1Vocabulary, with);
    }

    public boolean useBuiltinHierarchySupport() {
        return useBuiltinHierarchySupport;
    }

    public boolean useOWLv1Vocabulary() {
        return useOWLv1Vocabulary;
    }

    public boolean useNamedIndividualDeclaration() {
        return useNamedIndividualDeclaration;
    }
}
