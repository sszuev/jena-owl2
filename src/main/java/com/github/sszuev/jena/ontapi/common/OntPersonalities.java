package com.github.sszuev.jena.ontapi.common;

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
import com.github.sszuev.jena.ontapi.impl.objects.RDFSEntity;
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

    private static final OntPersonality.Builtins RDFS_BUILTINS = createBuiltinsVocabulary(OntVocabulary.Factory.RDFS_VOCABULARY);
    private static final OntPersonality.Reserved RDFS_RESERVED = createReservedVocabulary(OntVocabulary.Factory.RDFS_VOCABULARY);
    private static final OntPersonality.Builtins OWL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.Factory.FULL_VOCABULARY);
    private static final OntPersonality.Reserved OWL_RESERVED = createReservedVocabulary(OntVocabulary.Factory.FULL_VOCABULARY);

    /**
     * Standard resources. Private access since this constant is mutable.
     *
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    private static final Personality<RDFNode> RDF_PERSONALITY = new Personality<RDFNode>()
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
     * For RDFS Ontologies, limited functionality.
     *
     * @see <a href='https://www.w3.org/TR/rdf12-schema/'>RDF 1.2 Schema</a>
     */
    public static final OntPersonality RDFS_PERSONALITY = new PersonalityBuilder()
            .addPersonality(RDF_PERSONALITY)
            .add(OntObject.class, OntObjectImpl.ONT_OBJECT_FACTORY)
            .add(OntEntity.class, RDFSEntity.ALL)
            .add(OntIndividual.Anonymous.class, OntIndividualImpl.OWL2_ANONYMOUS_INDIVIDUAL_FACTORY)
            .add(OntIndividual.class, OntIndividualImpl.OWL2_INDIVIDUAL_FACTORY)
            .add(OntProperty.class, RDFSEntity.PROPERTY.createFactory())
            .add(OntClass.class, RDFSEntity.CLASS.createFactory())
            .add(OntClass.Named.class, RDFSEntity.CLASS.createFactory())
            .setBuiltins(RDFS_BUILTINS)
            .setReserved(RDFS_RESERVED)
            .setPunnings(PunningsMode.LAX.getVocabulary())
            .build();
    /**
     * Default personality builder. Private access since this constant is mutable.
     */
    private static final PersonalityBuilder OWL_ONT_PERSONALITY_BUILDER = new PersonalityBuilder()
            .addPersonality(RDF_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OntObjectImpl.ONT_OBJECT_FACTORY)

            // ont-id:
            .add(OntID.class, OntIDImpl.ONT_ID_FACTORY)

            // annotation object:
            .add(OntAnnotation.class, OntAnnotationImpl.OWL2_ANNOTATION_FACTORY)

            // entities:
            .add(OntClass.Named.class, OWL2Entity.CLASS.createFactory())
            .add(OntDataRange.Named.class, OWL2Entity.DATATYPE.createFactory())
            .add(OntIndividual.Named.class, OWL2Entity.INDIVIDUAL.createFactory())
            .add(OntObjectProperty.Named.class, OWL2Entity.OBJECT_PROPERTY.createFactory())
            .add(OntDataProperty.class, OWL2Entity.DATA_PROPERTY.createFactory())
            .add(OntAnnotationProperty.class, OWL2Entity.ANNOTATION_PROPERTY.createFactory())
            .add(OntEntity.class, OWL2Entity.ALL)

            // individuals:
            .add(OntIndividual.Anonymous.class, OntIndividualImpl.OWL2_ANONYMOUS_INDIVIDUAL_FACTORY)
            .add(OntIndividual.class, OntIndividualImpl.OWL2_INDIVIDUAL_FACTORY)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OntPEImpl.OWL2_INVERSE_PROPERTY_FACTORY)
            .add(OntObjectProperty.class, OntPEImpl.OWL2_OBJECT_PROPERTY_EXPRESSION_FACTORY)
            .add(OntRealProperty.class, OntPEImpl.OWL2_DATA_OR_OBJECT_PROPERTY_FACTORY)
            .add(OntNamedProperty.class, OntPEImpl.OWL2_NAMED_PROPERTY_FACTORY)
            .add(OntProperty.class, OntPEImpl.OWL2_PROPERTY_FACTORY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OntCEImpl.OWL2_OBJECT_SOME_VALUES_OF_CE_FACTORY)
            .add(OntClass.DataSomeValuesFrom.class, OntCEImpl.OWL2_DATA_SOME_VALUES_OF_CE_FACTORY)
            .add(OntClass.ObjectAllValuesFrom.class, OntCEImpl.OWL2_OBJECT_ALL_VALUES_OF_CE_FACTORY)
            .add(OntClass.DataAllValuesFrom.class, OntCEImpl.OWL2_DATA_ALL_VALUES_OF_CE_FACTORY)
            .add(OntClass.ObjectHasValue.class, OntCEImpl.OWL2_OBJECT_HAS_VALUE_CE_FACTORY)
            .add(OntClass.DataHasValue.class, OntCEImpl.OWL2_DATA_HAS_VALUE_CE_FACTORY)
            .add(OntClass.ObjectMinCardinality.class, OntCEImpl.OWL2_OBJECT_MIN_CARDINALITY_CE_FACTORY)
            .add(OntClass.DataMinCardinality.class, OntCEImpl.OWL2_DATA_MIN_CARDINALITY_CE_FACTORY)
            .add(OntClass.ObjectMaxCardinality.class, OntCEImpl.OWL2_OBJECT_MAX_CARDINALITY_CE_FACTORY)
            .add(OntClass.DataMaxCardinality.class, OntCEImpl.OWL2_DATA_MAX_CARDINALITY_CE_FACTORY)
            .add(OntClass.ObjectCardinality.class, OntCEImpl.OWL2_OBJECT_CARDINALITY_CE_FACTORY)
            .add(OntClass.DataCardinality.class, OntCEImpl.OWL2_DATA_CARDINALITY_CE_FACTORY)
            .add(OntClass.HasSelf.class, OntCEImpl.OWL2_HAS_SELF_CE_FACTORY)
            .add(OntClass.UnionOf.class, OntCEImpl.OWL2_UNION_OF_CE_FACTORY)
            .add(OntClass.OneOf.class, OntCEImpl.OWL2_ONE_OF_CE_FACTORY)
            .add(OntClass.IntersectionOf.class, OntCEImpl.OWL_2INTERSECTION_OF_CE_FACTORY)
            .add(OntClass.ComplementOf.class, OntCEImpl.OWL2_COMPLEMENT_OF_CE_FACTORY)
            .add(OntClass.NaryDataAllValuesFrom.class, OntCEImpl.OWL2_NARY_DATA_ALL_VALUES_FROM_CE_FACTORY)
            .add(OntClass.NaryDataSomeValuesFrom.class, OntCEImpl.OWL2_NARY_DATA_SOME_VALUES_FROM_CE_FACTORY)
            .add(OntClass.ComponentsCE.class, OntCEImpl.OWL2_COMPONENTS_CE_FACTORY)
            .add(OntClass.CardinalityRestrictionCE.class, OntCEImpl.OWL2_CARDINALITY_RESTRICTION_CE_FACTORY)
            .add(OntClass.ComponentRestrictionCE.class, OntCEImpl.OWL2_COMPONENT_RESTRICTION_CE_FACTORY)
            .add(OntClass.UnaryRestrictionCE.class, OntCEImpl.OWL2_PROPERTY_RESTRICTION_CE_FACTORY)
            .add(OntClass.RestrictionCE.class, OntCEImpl.OWL2_RESTRICTION_CE_FACTORY)
            .add(OntClass.class, OntCEImpl.OWL2_CE_FACTORY)

            // data ranges:
            .add(OntDataRange.OneOf.class, OntDRImpl.OWL2_ONE_OF_DR_FACTORY)
            .add(OntDataRange.Restriction.class, OntDRImpl.OWL2_RESTRICTION_DR_FACTORY)
            .add(OntDataRange.ComplementOf.class, OntDRImpl.OWL2_COMPLEMENT_OF_DR_FACTORY)
            .add(OntDataRange.UnionOf.class, OntDRImpl.OWL2_UNION_OF_DR_FACTORY)
            .add(OntDataRange.IntersectionOf.class, OntDRImpl.OWL2_INTERSECTION_OF_DR_FACTORY)
            .add(OntDataRange.ComponentsDR.class, OntDRImpl.OWL2_COMPONENTS_DR_FACTORY)
            .add(OntDataRange.class, OntDRImpl.OWL2_DR_FACTORY)

            // facet restrictions:
            .add(OntFacetRestriction.Length.class, OntFRImpl.OWL2_LENGTH_FR_FACTORY)
            .add(OntFacetRestriction.MinLength.class, OntFRImpl.OWL2_MIN_LENGTH_FR_FACTORY)
            .add(OntFacetRestriction.MaxLength.class, OntFRImpl.OWL2_MAX_LENGTH_FR_FACTORY)
            .add(OntFacetRestriction.MinInclusive.class, OntFRImpl.OWL2_MIN_INCLUSIVE_FR_FACTORY)
            .add(OntFacetRestriction.MaxInclusive.class, OntFRImpl.OWL2_MAX_INCLUSIVE_FR_FACTORY)
            .add(OntFacetRestriction.MinExclusive.class, OntFRImpl.OWL2_MIN_EXCLUSIVE_FR_FACTORY)
            .add(OntFacetRestriction.MaxExclusive.class, OntFRImpl.OWL2_MAX_EXCLUSIVE_FR_FACTORY)
            .add(OntFacetRestriction.Pattern.class, OntFRImpl.OWL2_PATTERN_FR_FACTORY)
            .add(OntFacetRestriction.TotalDigits.class, OntFRImpl.OWL2_TOTAL_DIGITS_FR_FACTORY)
            .add(OntFacetRestriction.FractionDigits.class, OntFRImpl.OWL2_FRACTION_DIGITS_FR_FACTORY)
            .add(OntFacetRestriction.LangRange.class, OntFRImpl.OWL2_LANG_RANGE_FR_FACTORY)
            .add(OntFacetRestriction.class, OntFRImpl.OWL2_FR_FACTORY)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OntNPAImpl.OWL2_OBJECT_NPA_FACTORY)
            .add(OntNegativeAssertion.WithDataProperty.class, OntNPAImpl.OWL2_DATA_NPA_FACTORY)
            .add(OntNegativeAssertion.class, OntNPAImpl.OWL2_NPA_FACTORY)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OntDisjointImpl.OWL2_DISJOINT_CLASSES_FACTORY)
            .add(OntDisjoint.Individuals.class, OntDisjointImpl.OWL2_DIFFERENT_INDIVIDUALS_FACTORY)
            .add(OntDisjoint.ObjectProperties.class, OntDisjointImpl.OWL2_OBJECT_PROPERTIES_FACTORY)
            .add(OntDisjoint.DataProperties.class, OntDisjointImpl.OWL2_DATA_PROPERTIES_FACTORY)
            .add(OntDisjoint.Properties.class, OntDisjointImpl.OWL2_PROPERTIES_FACTORY)
            .add(OntDisjoint.class, OntDisjointImpl.OWL2_DISJOINT_FACTORY)

            // SWRL objects:
            .add(OntSWRL.Variable.class, OntSWRLImpl.SWRL_VARIABLE_FACTORY)
            .add(OntSWRL.Builtin.class, OntSWRLImpl.SWRL_BUILTIN_FACTORY)
            .add(OntSWRL.IArg.class, OntSWRLImpl.SWRL_IARG_FACTORY)
            .add(OntSWRL.DArg.class, OntSWRLImpl.SWRL_DARG_FACTORY)
            .add(OntSWRL.Arg.class, OntSWRLImpl.SWRL_ARG_FACTORY)
            .add(OntSWRL.Atom.WithBuiltin.class, OntSWRLImpl.SWRL_BUILT_IN_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithClass.class, OntSWRLImpl.SWRL_CLASS_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithDataRange.class, OntSWRLImpl.SWRL_DATA_RANGE_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithObjectProperty.class, OntSWRLImpl.SWRL_INDIVIDUAL_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithDataProperty.class, OntSWRLImpl.SWRL_DATA_VALUED_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithDifferentIndividuals.class, OntSWRLImpl.SWRL_DIFFERENT_INDIVIDUALS_ATOM_FACTORY)
            .add(OntSWRL.Atom.WithSameIndividuals.class, OntSWRLImpl.SWRL_SAME_INDIVIDUALS_ATOM_FACTORY)
            .add(OntSWRL.Atom.Unary.class, OntSWRLImpl.SWRL_UNARY_FACTORY)
            .add(OntSWRL.Atom.Binary.class, OntSWRLImpl.SWRL_BINARY_FACTORY)
            .add(OntSWRL.Atom.class, OntSWRLImpl.SWRL_ATOM_FACTORY)
            .add(OntSWRL.Imp.class, OntSWRLImpl.SWRL_IMP_FACTORY)
            .add(OntSWRL.class, OntSWRLImpl.SWRL_OBJECT_FACTORY);
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
            .setBuiltins(OWL_BUILTINS)
            .setReserved(OWL_RESERVED)
            .setPunnings(PunningsMode.MEDIUM.getVocabulary())
            .build();
    /**
     * Personalities which don't care about the owl-entities "punnings" (no restriction on the type declarations).
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see PunningsMode#LAX
     */
    public static final OntPersonality OWL2_PERSONALITY_LAX = getPersonalityBuilder()
            .setBuiltins(OWL_BUILTINS)
            .setReserved(OWL_RESERVED)
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
            .setBuiltins(OWL_BUILTINS)
            .setReserved(OWL_RESERVED)
            .setPunnings(PunningsMode.STRICT.getVocabulary())
            .build();

    /**
     * Returns a fresh copy of {@link PersonalityBuilder} with {@code 93} resource factories inside
     * ({@code 10} standard + {@code 87} ontological).
     * The returned instance contains everything needed, and can be modified to build a new {@link OntPersonality}.
     *
     * @return {@link PersonalityBuilder}
     */
    public static PersonalityBuilder getPersonalityBuilder() {
        return OWL_ONT_PERSONALITY_BUILDER.copy();
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
        OntEntity.TYPES.forEach(t -> res.computeIfAbsent(t, k -> Collections.emptySet()));
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
         * Allow any entity type intersections.
         */
        LAX,
        ;

        private OntPersonality.Punnings punnings;

        public OntPersonality.Punnings getVocabulary() {
            return punnings == null ? punnings = createPunningsVocabulary(this) : punnings;
        }
    }

}
