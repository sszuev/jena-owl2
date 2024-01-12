package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntConfigs;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLMicroReasonerFactory;
import org.apache.jena.reasoner.rulesys.OWLMiniReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

import java.util.Objects;

/**
 * Encapsulates a description of the components of an ontology model.
 * Contains OWL2, OWL1 and RDFS specifications:
 * <ul>
 *     <li>OWL2 (DL &amp; FULL): full support last version of OWL</li>
 *     <li>OWL1 (DL &amp; FULL): does not support some language construct from OWL2, such as {@code OntDataRange.UnionOf};
 *     supposed to be compatible with legacy Jena's OntModel</li>
 *     <li>OWL1 LITE: does not support some language construct from OWL2 and OWL1, such as {@code OntClass.UnionOf};
 *     supposed to be compatible with old Jena's OntModel</li>
 *     <li>RDFS: supports only RDFS language: rdf:Property, rdf:Class, rdfs:subClassOf, etc</li>
 * </ul>
 *
 * @see <a href="https://www.w3.org/TR/owl2-syntax/">OWL v2</a>
 * @see <a href="https://www.w3.org/TR/2008/WD-owl11-syntax-20080108/">OWL v1.1</a>
 * @see <a href="https://www.w3.org/TR/owl-guide/">OWL v1</a>
 * @see <a href="https://www.w3.org/TR/owl-features/">OWL1 Lite</a>
 * @see org.apache.jena.ontology.OntModelSpec OntModelSpec - legacy Jena's OWL1 specifications
 */
public class OntSpecification {

    private static final OntPersonality OWL2_FULL_PERSONALITY =
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG)
                    .build();

    private static final OntPersonality OWL2_DL_PERSONALITY =
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG)
                    .build();

    private static final OntPersonality OWL1_DL_PERSONALITY =
            OntPersonalities.OWL1_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL1_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_CONFIG)
                    .build();

    private static final OntPersonality OWL1_FULL_PERSONALITY =
            OntPersonalities.OWL1_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_CONFIG)
                    .build();

    private static final OntPersonality OWL1_LITE_PERSONALITY =
            OntPersonalities.OWL1_LITE_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL1_LITE_BUILTINS)
                    .setReserved(OntPersonalities.OWL1_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL1_PUNNINGS)
                    .setConfig(OntConfigs.OWL1_LITE_CONFIG)
                    .build();

    private static final OntPersonality RDFS_PERSONALITY =
            OntPersonalities.RDFS_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.RDFS_BUILTINS)
                    .setReserved(OntPersonalities.RDFS_RESERVED)
                    .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                    .setConfig(OntConfigs.RDFS_CONFIG)
                    .build();

    /*
     * *****************************************************************************************************************
     * OWL 2 DL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL2 ontology models that are stored in memory
     * and use fast but incomplete builtin OWL inference engine for additional entailments
     * that handles hierarchy.
     * It supports {@code rdfs:subClassOf}, {@code rdfs:subPropertyOf} and class-individuals hierarchy,
     * otherwise it behaves like a regular {@link #OWL2_DL_MEM}.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_BUILTIN_INF = new OntSpecification(
            OntPersonalities.OWL2_ONT_PERSONALITY()
                    .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                    .setReserved(OntPersonalities.OWL2_RESERVED)
                    .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                    .setConfig(OntConfigs.OWL2_CONFIG.setTrue(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT))
                    .build(),
            null
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and do no additional entailment reasoning.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM
     */
    public static final OntSpecification OWL2_DL_MEM = new OntSpecification(
            OWL2_DL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_TRANS_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 DL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RULE_INF
     */
    public static final OntSpecification OWL2_DL_MEM_RULES_INF = new OntSpecification(
            OWL2_DL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 2 FULL
     * *****************************************************************************************************************
     */

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Full support for the OWL v2 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL2_FULL_MEM = new OntSpecification(
            OWL2_FULL_PERSONALITY, null
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the RDFS inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_FULL_MEM_RDFS_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 models that are stored in memory and use the transitive inferencer for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL2_FULL_MEM_TRANS_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RULE_INF
     */
    public static final OntSpecification OWL2_FULL_MEM_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the micro OWL rules inference engine for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_MICRO_RULE_INF
     */
    public static final OntSpecification OWL2_FULL_MEM_MICRO_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLMicroReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL2 ontology models
     * that are stored in memory and use the mini OWL rules inference engine for additional entailments.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_MINI_RULE_INF
     */
    public static final OntSpecification OWL2_FULL_MEM_MINI_RULES_INF = new OntSpecification(
            OWL2_FULL_PERSONALITY, OWLMiniReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 DL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL DL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM
     */
    public static final OntSpecification OWL1_DL_MEM = new OntSpecification(
            OWL1_DL_PERSONALITY, null
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL1_DL_MEM_RDFS_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL1_DL_MEM_TRANS_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 DL ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RULE_INF
     */
    public static final OntSpecification OWL1_DL_MEM_RULES_INF = new OntSpecification(
            OWL1_DL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 FULL
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL1_FULL_MEM = new OntSpecification(
            OWL1_FULL_PERSONALITY, null
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL1_FULL_MEM_RDFS_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_TRANS_INF
     */
    public static final OntSpecification OWL1_FULL_MEM_TRANS_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_RULE_INF
     */
    public static final OntSpecification OWL1_FULL_MEM_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the micro OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_MICRO_RULE_INF
     */
    public static final OntSpecification OWL1_FULL_MEM_MICRO_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLMicroReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 ontology models
     * that are stored in memory and use the mini OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM_MINI_RULE_INF
     */
    public static final OntSpecification OWL1_FULL_MEM_MINI_RULES_INF = new OntSpecification(
            OWL1_FULL_PERSONALITY, OWLMiniReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * OWL 1 LITE
     * *****************************************************************************************************************
     */

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_LITE_MEM
     */
    public static final OntSpecification OWL1_LITE_MEM = new OntSpecification(
            OWL1_LITE_PERSONALITY, null
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_LITE_MEM_RDFS_INF
     */
    public static final OntSpecification OWL1_LITE_MEM_RDFS_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the transitive inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_LITE_MEM_TRANS_INF
     */
    public static final OntSpecification OWL1_LITE_MEM_TRANS_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, TransitiveReasonerFactory.theInstance()
    );

    /**
     * A specification for OWL1 Lite ontology models
     * that are stored in memory and use the OWL rules inference engine for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of OWL v1 Lite specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_LITE_MEM_RULES_INF
     */
    public static final OntSpecification OWL1_LITE_MEM_RULES_INF = new OntSpecification(
            OWL1_LITE_PERSONALITY, OWLFBRuleReasonerFactory.theInstance()
    );

    /*
     * *****************************************************************************************************************
     * RDFS
     * *****************************************************************************************************************
     */

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM
     */
    public static final OntSpecification RDFS_MEM = new OntSpecification(
            RDFS_PERSONALITY, null
    );

    /**
     * A specification for RDFS ontology models that are stored in memory
     * and use the RDFS inferencer for additional entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM_RDFS_INF
     */
    public static final OntSpecification RDFS_MEM_RDFS_INF = new OntSpecification(
            RDFS_PERSONALITY, RDFSRuleReasonerFactory.theInstance()
    );

    /**
     * A specification for RDFS ontology models that are stored in memory
     * and use the transitive reasoner for entailments.
     * Supposed to be a replacement for the original legacy Jena interpretation of RDFS specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM_TRANS_INF
     */
    public static final OntSpecification RDFS_MEM_TRANS_INF = new OntSpecification(
            RDFS_PERSONALITY, TransitiveReasonerFactory.theInstance()
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
