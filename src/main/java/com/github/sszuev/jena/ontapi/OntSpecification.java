package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import org.apache.jena.reasoner.ReasonerFactory;

/**
 * Encapsulates a description of the components of an ontology model.
 *
 * @see org.apache.jena.ontology.OntModelSpec
 */
public class OntSpecification {
    /**
     * A specification for OWL 2 DL models that are stored in memory and do no additional entailment reasoning.
     */
    public static final OntSpecification OWL2_DL_BUILTIN_INF = new OntSpecification(OntPersonalities.OWL2_PERSONALITY_STRICT, null);
    private final OntPersonality personality;
    private final ReasonerFactory reasonerFactory;

    public OntSpecification(OntPersonality personality, ReasonerFactory reasonerFactory) {
        this.personality = personality;
        this.reasonerFactory = reasonerFactory;
    }

    public OntPersonality getPersonality() {
        return personality;
    }

    public ReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }
}
