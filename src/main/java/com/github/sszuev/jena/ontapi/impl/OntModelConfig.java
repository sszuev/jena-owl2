package com.github.sszuev.jena.ontapi.impl;

/**
 * Configuration to control {@link OntModelConfig} behavior.
 */
public class OntModelConfig {
    public static final OntModelConfig DEFAULT = new OntModelConfig(false);

    final boolean useBuiltinHierarchySupport;

    private OntModelConfig(boolean useBuiltinHierarchySupport) {
        this.useBuiltinHierarchySupport = useBuiltinHierarchySupport;
    }

    public OntModelConfig useBuiltinHierarchySupport(boolean with) {
        return new OntModelConfig(with);
    }

    public boolean useBuiltinHierarchySupport() {
        return useBuiltinHierarchySupport;
    }
}
