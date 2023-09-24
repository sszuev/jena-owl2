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
     * A specification for Ontology models that are stored in memory
     * and use fast but incomplete builtin OWL inference engine for additional entailments.
     * Full support for the OWL v2 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_BUILTIN_INF =
            new OntSpecification(OntPersonalities.OWL2_PERSONALITY_STRICT_PUNNS, null, OntModelConfig.DEFAULT.useBuiltinHierarchySupport(true));

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Full support for the OWL v2 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL2_MEM =
            new OntSpecification(OntPersonalities.OWL2_PERSONALITY_LAX_PUNNS, null, OntModelConfig.DEFAULT);

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Supports original Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL1_MEM =
            new OntSpecification(OntPersonalities.OWL1_PERSONALITY_LAX_PUNNS, null, OntModelConfig.DEFAULT);

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     * TODO: Jena allows to represent virtually any RDF declaration as any Ontology type
     *  (e.g. `_x: rdf:type rdf:Property` as `org.apache.jena.ontology.ObjectProperty`);
     *  It is strange, but we have to offer RDFS_MEM_COMPATIBLE spec ...
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
