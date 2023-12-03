package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import org.apache.jena.reasoner.ReasonerFactory;

import java.util.Objects;

import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL1_CONFIG;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL1_ONT_OBJECT_PERSONALITY;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL2_CONFIG;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL2_ONT_OBJECT_PERSONALITY;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL_BUILTINS;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.OWL_RESERVED;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.PunningsMode;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.RDFS_BUILTINS;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.RDFS_CONFIG;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.RDFS_PERSONALITY;
import static com.github.sszuev.jena.ontapi.common.OntPersonalities.RDFS_RESERVED;

/**
 * Encapsulates a description of the components of an ontology model.
 *
 * @see org.apache.jena.ontology.OntModelSpec
 */
public class OntSpecification {

    /**
     * A specification for Ontology models that are stored in memory
     * and use fast but incomplete builtin OWL inference engine for additional entailments.
     * It supports {@code rdfs:subClassOf} & {@code rdfs:subPropertyOf} hierarchy.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_DL_MEM_RDFS_INF
     */
    public static final OntSpecification OWL2_DL_MEM_RDFS_BUILTIN_INF = new OntSpecification(
            OWL2_ONT_OBJECT_PERSONALITY()
                    .setBuiltins(OWL_BUILTINS)
                    .setReserved(OWL_RESERVED)
                    .setPunnings(PunningsMode.STRICT.getVocabulary())
                    .setConfig(OWL2_CONFIG.useBuiltinHierarchySupport(true))
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
            OWL2_ONT_OBJECT_PERSONALITY()
                    .setBuiltins(OWL_BUILTINS)
                    .setReserved(OWL_RESERVED)
                    .setPunnings(PunningsMode.LAX.getVocabulary())
                    .setConfig(OWL2_CONFIG)
                    .build(),
            null
    );

    /**
     * A specification for Ontology models that are stored in memory and do no additional entailment reasoning.
     * Supports original Jena interpretation of OWL v1.1 specification.
     *
     * @see org.apache.jena.ontology.OntModelSpec#OWL_MEM
     */
    public static final OntSpecification OWL1_MEM = new OntSpecification(
            OWL1_ONT_OBJECT_PERSONALITY()
                    .setBuiltins(OWL_BUILTINS)
                    .setReserved(OWL_RESERVED)
                    .setPunnings(PunningsMode.LAX.getVocabulary())
                    .setConfig(OWL1_CONFIG)
                    .build(),
            null
    );

    /**
     * A specification for RDFS models that are stored in memory and do no additional entailment reasoning.
     * TODO: Jena allows to represent virtually any RDF declaration as any Ontology type
     *  (e.g. `_x: rdf:type rdf:Property` as `org.apache.jena.ontology.ObjectProperty`);
     *  It is strange, but we have to offer RDFS_MEM_COMPATIBLE spec ...
     *
     * @see org.apache.jena.ontology.OntModelSpec#RDFS_MEM
     */
    public static final OntSpecification RDFS_MEM = new OntSpecification(
            RDFS_PERSONALITY()
                    .setBuiltins(RDFS_BUILTINS)
                    .setReserved(RDFS_RESERVED)
                    .setPunnings(PunningsMode.LAX.getVocabulary())
                    .setConfig(RDFS_CONFIG)
                    .build(),
            null
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
