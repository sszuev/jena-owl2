package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.common.PunningsMode;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;

class TestOntPersonalities {
    /**
     * OWL2 Personality, that has default settings and does not care about the owl-entities "punnings"
     * (no restriction on the type declarations).
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#FULL
     */
    public static final OntPersonality OWL2_PERSONALITY_LAX_PUNNS = OntPersonalities.OWL2_ONT_PERSONALITY()
            .setName("TEST-OWL2_PERSONALITY_LAX_PUNNS")
            .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
            .setReserved(OntPersonalities.OWL2_RESERVED)
            .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
            .setConfig(OntConfig.DEFAULT)
            .build();
    /**
     * OWL2 Personality.
     * The stronger variant of previous constant: there are two forbidden intersections:
     * <ul>
     * <li>{@link OntDataRange.Named}  &lt;-&gt; {@link OntClass.Named}</li>
     * <li>{@link OntObjectProperty.Named} &lt;-&gt; {@link OntDataProperty}</li>
     * </ul>
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#DL_WEAK
     */
    public static final OntPersonality OWL2_PERSONALITY_MEDIUM_PUNNS = OntPersonalities.OWL2_ONT_PERSONALITY()
            .setName("TEST-OWL2_PERSONALITY_MEDIUM_PUNNS")
            .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
            .setReserved(OntPersonalities.OWL2_RESERVED)
            .setPunnings(OntPersonalities.OWL_DL_WEAK_PUNNINGS)
            .setConfig(OntConfig.DEFAULT)
            .build();
    /**
     * OWL2 Personality.
     * Personality with four kinds of restriction on a {@code rdf:type} intersection (i.e. "illegal punnings"):
     * <ul>
     * <li>{@link OntDataRange.Named}  &lt;-&gt; {@link OntClass.Named}</li>
     * <li>{@link OntAnnotationProperty} &lt;-&gt; {@link OntObjectProperty.Named}</li>
     * <li>{@link OntObjectProperty.Named} &lt;-&gt; {@link OntDataProperty}</li>
     * <li>{@link OntDataProperty} &lt;-&gt; {@link OntAnnotationProperty}</li>
     * </ul>
     * each of the pairs above can't exist in the form of OWL-Entity in the same model at the same time.
     * From specification: "OWL 2 DL imposes certain restrictions:
     * it requires that a name cannot be used for both a class and a datatype and
     * that a name can only be used for one kind of property."
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#DL2
     */
    public static final OntPersonality OWL2_PERSONALITY_STRICT_PUNNS = OntPersonalities.OWL2_ONT_PERSONALITY()
            .setName("TEST-OWL2_PERSONALITY_STRICT_PUNNS")
            .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
            .setReserved(OntPersonalities.OWL2_RESERVED)
            .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
            .setConfig(OntConfig.DEFAULT)
            .build();
}
