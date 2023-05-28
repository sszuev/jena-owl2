package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.OntModelConfig;
import org.apache.jena.reasoner.ReasonerFactory;

import java.util.Objects;

/**
 * Encapsulates a description of the components of an ontology model.
 *
 * @see org.apache.jena.ontology.OntModelSpec
 */
public class OntSpecification {
    /**
     * A specification for OWL models that are stored in memory and use fast but incomplete builtin OWL inference engine for additional entailments.
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_BUILTIN_INF =
            new OntSpecification(OntPersonalities.OWL2_PERSONALITY_STRICT, null, OntModelConfig.DEFAULT.useBuiltinHierarchySupport(true));

    /**
     * A specification for OWL models that are stored in memory and do no additional entailment reasoning.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL2_MEM =
            new OntSpecification(OntPersonalities.OWL2_PERSONALITY_LAX, null, OntModelConfig.DEFAULT);

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM
     */
    public static final OntSpecification RDFS_MEM =
            new OntSpecification(OntPersonalities.RDFS_PERSONALITY, null, OntModelConfig.DEFAULT);


    private final OntPersonality personality;
    private final ReasonerFactory reasonerFactory;
    private final OntModelConfig config;

    public OntSpecification(OntPersonality personality, ReasonerFactory reasonerFactory, OntModelConfig config) {
        this.personality = Objects.requireNonNull(personality);
        this.reasonerFactory = reasonerFactory;
        this.config = config == null ? OntModelConfig.DEFAULT : config;
    }

    public OntPersonality getPersonality() {
        return personality;
    }

    public ReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OntModelConfig getConfig() {
        return config;
    }
}
