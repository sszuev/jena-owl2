package com.github.sszuev.jena.ontapi.impl;

/**
 * Configuration to control {@link OntModelConfig} behavior.
 */
public class OntModelConfig {
    public static final OntModelConfig DEFAULT = new OntModelConfig(false, false);

    protected final boolean useBuiltinHierarchySupport;
    protected final boolean useOWLv1Vocabulary;

    private OntModelConfig(boolean useBuiltinHierarchySupport, boolean useOWLv1Vocabulary) {
        this.useBuiltinHierarchySupport = useBuiltinHierarchySupport;
        this.useOWLv1Vocabulary = useOWLv1Vocabulary;
    }

    public OntModelConfig useBuiltinHierarchySupport(boolean with) {
        return new OntModelConfig(with, this.useOWLv1Vocabulary);
    }

    public boolean useBuiltinHierarchySupport() {
        return useBuiltinHierarchySupport;
    }

    public OntModelConfig useOWLv1Vocabulary(boolean with) {
        return new OntModelConfig(this.useBuiltinHierarchySupport, with);
    }

    public boolean useOWLv1Vocabulary() {
        return useOWLv1Vocabulary;
    }
}
