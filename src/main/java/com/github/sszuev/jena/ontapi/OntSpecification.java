package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import java.util.Objects;

/**
 * Encapsulates a description of the components of an ontology model.
 *
 * @see org.apache.jena.ontology.OntModelSpec
 */
public class OntSpecification {

    private static final OntPersonality OWL2_STANDARD_PERSONALITY =
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL_BUILTINS)
                    .setReserved(OntPersonalities.OWL_RESERVED)
                    .setPunnings(OntPersonalities.PunningsMode.LAX.getVocabulary())
                    .setConfig(OntPersonalities.OWL2_CONFIG)
                    .build();

    private static final OntPersonality OWL1_STANDARD_PERSONALITY =
            OntPersonalities.OWL1_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL_BUILTINS)
                    .setReserved(OntPersonalities.OWL_RESERVED)
                    .setPunnings(OntPersonalities.PunningsMode.LAX.getVocabulary())
                    .setConfig(OntPersonalities.OWL1_CONFIG)
                    .build();

    private static final OntPersonality RDFS_PERSONALITY =
            OntPersonalities.RDFS_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.RDFS_BUILTINS)
                    .setReserved(OntPersonalities.RDFS_RESERVED)
                    .setPunnings(OntPersonalities.PunningsMode.LAX.getVocabulary())
                    .setConfig(OntPersonalities.RDFS_CONFIG)
                    .build();

    /**
     * A specification for Ontology models that are stored in memory
     * and use fast but incomplete builtin OWL inference engine for additional entailments
     * that handles hierarchy.
     * It supports {@code rdfs:subClassOf}, {@code rdfs:subPropertyOf} and class-individuals hierarchy,
     * otherwise it behaves like a regular {@link #OWL2_MEM},
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_BUILTIN_INF = new OntSpecification(
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL_BUILTINS)
                    .setReserved(OntPersonalities.OWL_RESERVED)
                    .setPunnings(OntPersonalities.PunningsMode.STRICT.getVocabulary())
                    .setConfig(OntPersonalities.OWL2_CONFIG.setTrue(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT))
                    .build(),
            null
    );

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Full support for the OWL v2 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL2_MEM = new OntSpecification(
            OWL2_STANDARD_PERSONALITY, null
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the RDFS inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_MEM_RDFS_INF = new OntSpecification(
            OWL2_STANDARD_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the transitive inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL2_MEM_TRANS_INF = new OntSpecification(
            OWL2_STANDARD_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Supports original Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL1_MEM = new OntSpecification(
            OWL1_STANDARD_PERSONALITY, null
    );

    /**
     * A specification for OWL1 models that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supports original Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL1_MEM_RDFS_INF = new OntSpecification(
            OWL1_STANDARD_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 models that are stored in memory and use the transitive inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL1_MEM_TRANS_INF = new OntSpecification(
            OWL1_STANDARD_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for RDFS ontology models that are stored in memory
     * and use the RDFS inferencer for additional entailments
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM_RDFS_INF
     */
    public static final OntSpecification RDFS_MEM_RDFS_INF = new OntSpecification(
            RDFS_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM
     */
    public static final OntSpecification RDFS_MEM = new OntSpecification(
            RDFS_PERSONALITY, null
    );

    private final OntPersonality personality;
    private final ReasonerFactory reasonerFactory;

    public OntSpecification(OntPersonality personality, ReasonerFactory reasonerFactory) {
        this.personality = Objects.requireNonNull(personality);
        this.reasonerFactory = reasonerFactory;
    }

    public OntPersonality getPersonality() {
        return personality;
    }

    public ReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OntConfig getConfig() {
        return personality.getConfig();
    }
}
