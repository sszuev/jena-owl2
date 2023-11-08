package com.github.sszuev.jena.ontapi.impl;

import com.github.sszuev.jena.ontapi.model.OntModel;

public interface HasConfig {

    OntModelConfig getConfig();

    static OntModelConfig config(OntModel m) {
        return (m instanceof HasConfig) ? ((HasConfig) m).getConfig() : null;
    }
}
