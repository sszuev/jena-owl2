package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelFactory;
import com.github.sszuev.jena.ontapi.OntVocabulary;
import com.github.sszuev.jena.ontapi.impl.objects.OWL2Entity;
import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntCEImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDRImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntFRImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIDImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntNPAImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntPEImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntSWRLImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotation;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntID;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNamedProperty;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.rdf.model.impl.AltImpl;
import org.apache.jena.rdf.model.impl.BagImpl;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.rdf.model.impl.ReifiedStatementImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.SeqImpl;
import org.apache.jena.vocabulary.RDFS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Settings and personalities that are used for constructing {@link OntModel}.
 * An access point to several predefined {@link OntPersonality Ontology Personality} constants.
 * <p>
 * Created by @ssz on 04.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntPersonalities {

    /**
     * A system-wide vocabulary.
     */
    private static final OntVocabulary VOCABULARY = OntVocabulary.Factory.get();
    /**
     * Default builtins vocabulary.
     */
    private static final OntPersonality.Builtins BUILTINS = createBuiltinsVocabulary(VOCABULARY);
    /**
     * Default reserved vocabulary.
     */
    private static final OntPersonality.Reserved RESERVED = createReservedVocabulary(VOCABULARY);

    /**
     * Standard resources. Private access since this constant is mutable.
     *
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    private static final Personality<RDFNode> STANDARD_PERSONALITY = new Personality<RDFNode>()
            .add(Resource.class, ResourceImpl.factory)
            .add(Property.class, PropertyImpl.factory)
            .add(Literal.class, LiteralImpl.factory)
            .add(Container.class, ResourceImpl.factory)
            .add(Alt.class, AltImpl.factory)
            .add(Bag.class, BagImpl.factory)
            .add(Seq.class, SeqImpl.factory)
            .add(ReifiedStatement.class, ReifiedStatementImpl.reifiedStatementFactory)
            .add(RDFList.class, RDFListImpl.factory)
            .add(RDFNode.class, ResourceImpl.rdfNodeFactory);

    /**
     * Default personality builder. Private access since this constant is mutable.
     */
    private static final PersonalityBuilder ONT_PERSONALITY_BUILDER = new PersonalityBuilder()
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OntObjectImpl.objectFactory)

            // ont-id:
            .add(OntID.class, OntIDImpl.idFactory)

            // annotation object:
            .add(OntAnnotation.class, OntAnnotationImpl.annotationFactory)

            // entities:
            .add(OntClass.Named.class, OWL2Entity.CLASS.createFactory())
            .add(OntDataRange.Named.class, OWL2Entity.DATATYPE.createFactory())
            .add(OntIndividual.Named.class, OWL2Entity.INDIVIDUAL.createFactory())
            .add(OntObjectProperty.Named.class, OWL2Entity.OBJECT_PROPERTY.createFactory())
            .add(OntDataProperty.class, OWL2Entity.DATA_PROPERTY.createFactory())
            .add(OntAnnotationProperty.class, OWL2Entity.ANNOTATION_PROPERTY.createFactory())
            .add(OntEntity.class, OWL2Entity.ALL)

            // individuals:
            .add(OntIndividual.Anonymous.class, OntIndividualImpl.anonymousIndividualFactory)
            .add(OntIndividual.class, OntIndividualImpl.abstractIndividualFactory)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OntPEImpl.inversePropertyFactory)
            .add(OntObjectProperty.class, OntPEImpl.abstractOPEFactory)
            .add(OntRealProperty.class, OntPEImpl.abstractDOPFactory)
            .add(OntNamedProperty.class, OntPEImpl.abstractNamedPropertyFactory)
            .add(OntProperty.class, OntPEImpl.abstractPEFactory)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OntCEImpl.objectSomeValuesOfCEFactory)
            .add(OntClass.DataSomeValuesFrom.class, OntCEImpl.dataSomeValuesOfCEFactory)
            .add(OntClass.ObjectAllValuesFrom.class, OntCEImpl.objectAllValuesOfCEFactory)
            .add(OntClass.DataAllValuesFrom.class, OntCEImpl.dataAllValuesOfCEFactory)
            .add(OntClass.ObjectHasValue.class, OntCEImpl.objectHasValueCEFactory)
            .add(OntClass.DataHasValue.class, OntCEImpl.dataHasValueCEFactory)
            .add(OntClass.ObjectMinCardinality.class, OntCEImpl.objectMinCardinalityCEFactory)
            .add(OntClass.DataMinCardinality.class, OntCEImpl.dataMinCardinalityCEFactory)
            .add(OntClass.ObjectMaxCardinality.class, OntCEImpl.objectMaxCardinalityCEFactory)
            .add(OntClass.DataMaxCardinality.class, OntCEImpl.dataMaxCardinalityCEFactory)
            .add(OntClass.ObjectCardinality.class, OntCEImpl.objectCardinalityCEFactory)
            .add(OntClass.DataCardinality.class, OntCEImpl.dataCardinalityCEFactory)
            .add(OntClass.HasSelf.class, OntCEImpl.hasSelfCEFactory)
            .add(OntClass.UnionOf.class, OntCEImpl.unionOfCEFactory)
            .add(OntClass.OneOf.class, OntCEImpl.oneOfCEFactory)
            .add(OntClass.IntersectionOf.class, OntCEImpl.intersectionOfCEFactory)
            .add(OntClass.ComplementOf.class, OntCEImpl.complementOfCEFactory)
            .add(OntClass.NaryDataAllValuesFrom.class, OntCEImpl.naryDataAllValuesFromCEFactory)
            .add(OntClass.NaryDataSomeValuesFrom.class, OntCEImpl.naryDataSomeValuesFromCEFactory)
            .add(OntClass.ComponentsCE.class, OntCEImpl.abstractComponentsCEFactory)
            .add(OntClass.CardinalityRestrictionCE.class, OntCEImpl.abstractCardinalityRestrictionCEFactory)
            .add(OntClass.ComponentRestrictionCE.class, OntCEImpl.abstractComponentRestrictionCEFactory)
            .add(OntClass.UnaryRestrictionCE.class, OntCEImpl.abstractPropertyRestrictionCEFactory)
            .add(OntClass.RestrictionCE.class, OntCEImpl.abstractRestrictionCEFactory)
            .add(OntClass.class, OntCEImpl.abstractCEFactory)

            // data ranges:
            .add(OntDataRange.OneOf.class, OntDRImpl.oneOfDRFactory)
            .add(OntDataRange.Restriction.class, OntDRImpl.restrictionDRFactory)
            .add(OntDataRange.ComplementOf.class, OntDRImpl.complementOfDRFactory)
            .add(OntDataRange.UnionOf.class, OntDRImpl.unionOfDRFactory)
            .add(OntDataRange.IntersectionOf.class, OntDRImpl.intersectionOfDRFactory)
            .add(OntDataRange.ComponentsDR.class, OntDRImpl.abstractComponentsDRFactory)
            .add(OntDataRange.class, OntDRImpl.abstractDRFactory)

            // facet restrictions:
            .add(OntFacetRestriction.Length.class, OntFRImpl.lengthFRFactory)
            .add(OntFacetRestriction.MinLength.class, OntFRImpl.minLengthFRFactory)
            .add(OntFacetRestriction.MaxLength.class, OntFRImpl.maxLengthFRFactory)
            .add(OntFacetRestriction.MinInclusive.class, OntFRImpl.minInclusiveFRFactory)
            .add(OntFacetRestriction.MaxInclusive.class, OntFRImpl.maxInclusiveFRFactory)
            .add(OntFacetRestriction.MinExclusive.class, OntFRImpl.minExclusiveFRFactory)
            .add(OntFacetRestriction.MaxExclusive.class, OntFRImpl.maxExclusiveFRFactory)
            .add(OntFacetRestriction.Pattern.class, OntFRImpl.patternFRFactory)
            .add(OntFacetRestriction.TotalDigits.class, OntFRImpl.totalDigitsFRFactory)
            .add(OntFacetRestriction.FractionDigits.class, OntFRImpl.fractionDigitsFRFactory)
            .add(OntFacetRestriction.LangRange.class, OntFRImpl.langRangeFRFactory)
            .add(OntFacetRestriction.class, OntFRImpl.abstractFRFactory)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OntNPAImpl.objectNPAFactory)
            .add(OntNegativeAssertion.WithDataProperty.class, OntNPAImpl.dataNPAFactory)
            .add(OntNegativeAssertion.class, OntNPAImpl.abstractNPAFactory)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OntDisjointImpl.disjointClassesFactory)
            .add(OntDisjoint.Individuals.class, OntDisjointImpl.differentIndividualsFactory)
            .add(OntDisjoint.ObjectProperties.class, OntDisjointImpl.objectPropertiesFactory)
            .add(OntDisjoint.DataProperties.class, OntDisjointImpl.dataPropertiesFactory)
            .add(OntDisjoint.Properties.class, OntDisjointImpl.abstractPropertiesFactory)
            .add(OntDisjoint.class, OntDisjointImpl.abstractDisjointFactory)

            // SWRL objects:
            .add(OntSWRL.Variable.class, OntSWRLImpl.variableSWRLFactory)
            .add(OntSWRL.Builtin.class, OntSWRLImpl.builtinWRLFactory)
            .add(OntSWRL.IArg.class, OntSWRLImpl.iArgSWRLFactory)
            .add(OntSWRL.DArg.class, OntSWRLImpl.dArgSWRLFactory)
            .add(OntSWRL.Arg.class, OntSWRLImpl.abstractArgSWRLFactory)
            .add(OntSWRL.Atom.WithBuiltin.class, OntSWRLImpl.builtInAtomSWRLFactory)
            .add(OntSWRL.Atom.WithClass.class, OntSWRLImpl.classAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDataRange.class, OntSWRLImpl.dataRangeAtomSWRLFactory)
            .add(OntSWRL.Atom.WithObjectProperty.class, OntSWRLImpl.individualAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDataProperty.class, OntSWRLImpl.dataValuedAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDifferentIndividuals.class, OntSWRLImpl.differentIndividualsAtomSWRLFactory)
            .add(OntSWRL.Atom.WithSameIndividuals.class, OntSWRLImpl.sameIndividualsAtomSWRLFactory)
            .add(OntSWRL.Atom.Unary.class, OntSWRLImpl.abstractUnarySWRLFactory)
            .add(OntSWRL.Atom.Binary.class, OntSWRLImpl.abstractBinarySWRLFactory)
            .add(OntSWRL.Atom.class, OntSWRLImpl.abstractAtomSWRLFactory)
            .add(OntSWRL.class, OntSWRLImpl.abstractSWRLFactory)
            .add(OntSWRL.Imp.class, OntSWRLImpl.impSWRLFactory);
    /**
     * Personalities which don't care about the owl-entities "punnings" (no restriction on the type declarations).
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#LAX
     */
    public static final OntPersonality OWL2_PERSONALITY_LAX = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(PunningsMode.LAX.getVocabulary())
            .build();
    /**
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
     * @see PunningsMode#STRICT
     */
    public static final OntPersonality OWL2_PERSONALITY_STRICT = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(PunningsMode.STRICT.getVocabulary())
            .build();
    /**
     * The week variant of previous constant: there are two forbidden intersections:
     * <ul>
     * <li>{@link OntDataRange.Named}  &lt;-&gt; {@link OntClass.Named}</li>
     * <li>{@link OntObjectProperty.Named} &lt;-&gt; {@link OntDataProperty}</li>
     * </ul>
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#MEDIUM
     */
    public static final OntPersonality OWL2_PERSONALITY_MEDIUM = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(PunningsMode.MEDIUM.getVocabulary())
            .build();
    /**
     * Use {@link PunningsMode#MEDIUM} by default as a trade-off between the specification and the number of checks,
     * which are usually not necessary and only load the system.
     */
    private static OntPersonality personality = OWL2_PERSONALITY_MEDIUM;

    /**
     * Returns the standard jena {@link Personality} as modifiable copy.
     * It contains {@code 10} standard resource factories which are used by RDFS model
     * ({@link Model}, the default model implementation).
     *
     * @return {@link Personality} of {@link RDFNode}s
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    public static Personality<RDFNode> getStandardPersonality() {
        return STANDARD_PERSONALITY.copy();
    }

    /**
     * Returns a fresh copy of {@link PersonalityBuilder} with {@code 93} resource factories inside
     * ({@code 10} standard + {@code 87} ontological).
     * The returned instance contains everything needed, and can be modified to build a new {@link OntPersonality}.
     *
     * @return {@link PersonalityBuilder}
     */
    public static PersonalityBuilder getPersonalityBuilder() {
        return ONT_PERSONALITY_BUILDER.copy();
    }

    /**
     * Gets a system-wide personalities.
     *
     * @return {@link OntPersonality}
     * @see OntModelFactory
     */
    public static OntPersonality getPersonality() {
        return personality;
    }

    /**
     * Sets a system-wide personalities.
     *
     * @param other {@link OntPersonality}, not {@code null}
     * @return {@link OntPersonality}, a previous associated system-wide personalities
     */
    public static OntPersonality setPersonality(OntPersonality other) {
        OntPersonality res = personality;
        personality = OntJenaException.notNull(other, "Null personality specified.");
        return res;
    }

    /**
     * Creates a {@link OntPersonality.Builtins builtins personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Builtins}
     */
    public static OntPersonality.Builtins createBuiltinsVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends OntObject>, Set<Node>> res = new HashMap<>();
        res.put(OntAnnotationProperty.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinAnnotationProperties()));
        res.put(OntDataProperty.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinDatatypeProperties()));
        res.put(OntObjectProperty.Named.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinObjectProperties()));
        res.put(OntDataRange.Named.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinDatatypes()));
        res.put(OntClass.Named.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinClasses()));
        res.put(OntSWRL.Builtin.class, ModelUtils.asUnmodifiableNodeSet(voc.getBuiltinSWRLs()));
        res.put(OntIndividual.Named.class, Collections.emptySet());
        return new VocabularyImpl.EntitiesImpl(res);
    }

    /**
     * Creates a {@link OntPersonality.Reserved reserved personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Reserved}
     */
    public static OntPersonality.Reserved createReservedVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends Resource>, Set<Node>> res = new HashMap<>();
        res.put(Resource.class, ModelUtils.asUnmodifiableNodeSet(voc.getSystemResources()));
        res.put(Property.class, ModelUtils.asUnmodifiableNodeSet(voc.getSystemProperties()));
        return new VocabularyImpl.ReservedIml(res);
    }

    /**
     * Creates a {@link OntPersonality.Punnings punnings personality vocabulary} according to {@link PunningsMode}.
     *
     * @param mode {@link PunningsMode}, not {@code null}
     * @return {@link OntPersonality.Punnings}
     */
    private static OntPersonality.Punnings createPunningsVocabulary(PunningsMode mode) {
        Map<Class<? extends OntObject>, Set<Node>> res = new HashMap<>();
        if (!PunningsMode.LAX.equals(mode)) {
            toMap(res, OntClass.Named.class, RDFS.Datatype);
            toMap(res, OntDataRange.Named.class, OWL.Class);
        }
        if (PunningsMode.STRICT.equals(mode)) {
            toMap(res, OntAnnotationProperty.class, OWL.ObjectProperty, OWL.DatatypeProperty);
            toMap(res, OntDataProperty.class, OWL.ObjectProperty, OWL.AnnotationProperty);
            toMap(res, OntObjectProperty.Named.class, OWL.DatatypeProperty, OWL.AnnotationProperty);
        }
        if (PunningsMode.MEDIUM.equals(mode)) {
            toMap(res, OntDataProperty.class, OWL.ObjectProperty);
            toMap(res, OntObjectProperty.Named.class, OWL.DatatypeProperty);
        }
        OntEntity.listEntityTypes().forEachRemaining(t -> res.computeIfAbsent(t, k -> Collections.emptySet()));
        //return type -> fromMap(res, type);
        return new VocabularyImpl.EntitiesImpl(res);
    }

    @SafeVarargs
    private static <K, V extends RDFNode> void toMap(Map<K, Set<Node>> map, K key, V... values) {
        map.put(key, Arrays.stream(values).map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet()));
    }

    /**
     * A standard personality mode to manage punnings.
     */
    public enum PunningsMode {
        /**
         * The following punnings are considered as illegal and are excluded:
         * <ul>
         * <li>owl:Class &lt;-&gt; rdfs:Datatype</li>
         * <li>owl:ObjectProperty &lt;-&gt; owl:DatatypeProperty</li>
         * <li>owl:ObjectProperty &lt;-&gt; owl:AnnotationProperty</li>
         * <li>owl:AnnotationProperty &lt;-&gt; owl:DatatypeProperty</li>
         * </ul>
         */
        STRICT,
        /**
         * Forbidden intersections of rdf-declarations:
         * <ul>
         * <li>Class &lt;-&gt; Datatype</li>
         * <li>ObjectProperty &lt;-&gt; DataProperty</li>
         * </ul>
         */
        MEDIUM,
        /**
         * Allow everything.
         */
        LAX,
        ;

        private OntPersonality.Punnings punnings;

        public OntPersonality.Punnings getVocabulary() {
            return punnings == null ? punnings = createPunningsVocabulary(this) : punnings;
        }
    }

}
