package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntModelConfig;
import com.github.sszuev.jena.ontapi.OntVocabulary;
import com.github.sszuev.jena.ontapi.impl.factories.OWL1ObjectFactories;
import com.github.sszuev.jena.ontapi.impl.factories.OWL2ObjectFactories;
import com.github.sszuev.jena.ontapi.impl.factories.RDFSObjectFactories;
import com.github.sszuev.jena.ontapi.impl.factories.SWRLObjectFactories;
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

    public static final OntPersonality.Builtins RDFS_BUILTINS = createBuiltinsVocabulary(OntVocabulary.Factory.RDFS_VOCABULARY);
    public static final OntPersonality.Reserved RDFS_RESERVED = createReservedVocabulary(OntVocabulary.Factory.RDFS_VOCABULARY);
    public static final OntPersonality.Builtins OWL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.Factory.FULL_VOCABULARY);
    public static final OntPersonality.Reserved OWL_RESERVED = createReservedVocabulary(OntVocabulary.Factory.FULL_VOCABULARY);

    public static final OntConfig OWL2_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setFalse(OntModelConfig.USE_OWL_V1_VOCABULARY)
            .setTrue(OntModelConfig.USE_NAMED_INDIVIDUAL_DECLARATION)
            .setFalse(OntModelConfig.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setFalse(OntModelConfig.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setFalse(OntModelConfig.ALLOW_GENERIC_UNION_RESTRICTIONS)
            .setFalse(OntModelConfig.ALLOW_GENERIC_RESTRICTIONS);
    public static final OntConfig OWL1_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelConfig.USE_OWL_V1_VOCABULARY)
            .setFalse(OntModelConfig.USE_NAMED_INDIVIDUAL_DECLARATION)
            .setTrue(OntModelConfig.ALLOW_NAMED_CLASS_EXPRESSIONS)
            .setTrue(OntModelConfig.ALLOW_GENERIC_CLASS_EXPRESSIONS)
            .setTrue(OntModelConfig.ALLOW_GENERIC_UNION_RESTRICTIONS)
            .setTrue(OntModelConfig.ALLOW_GENERIC_RESTRICTIONS);
    public static final OntConfig RDFS_CONFIG = OntConfig.DEFAULT
            .setFalse(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
            .setTrue(OntModelConfig.USE_OWL_V1_VOCABULARY) // <- for RDFS it doesn't matter
            .setFalse(OntModelConfig.USE_NAMED_INDIVIDUAL_DECLARATION);

    /**
     * Standard resources. Private access since this constant is mutable.
     *
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    @SuppressWarnings("deprecation : ReifiedStatement")
    public static final Personality<RDFNode> STANDARD_PERSONALITY = new Personality<RDFNode>()
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
    private static final OntObjectPersonalityBuilder RDFS_OBJECT_FACTORIES = new OntObjectPersonalityBuilder()
            .setName("RDFS")
            .addPersonality(STANDARD_PERSONALITY)
            .add(OntObject.class, RDFSObjectFactories.ANY_OBJECT)
            .add(OntEntity.class, RDFSObjectFactories.ANY_ENTITY)
            .add(OntIndividual.Named.class, RDFSObjectFactories.NAMED_INDIVIDUAL)
            .add(OntIndividual.Anonymous.class, RDFSObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, RDFSObjectFactories.ANY_INDIVIDUAL)
            .add(OntProperty.class, RDFSObjectFactories.PROPERTY)
            .add(OntClass.class, RDFSObjectFactories.ANY_CLASS)
            .add(OntClass.Named.class, RDFSObjectFactories.NAMED_CLASS);

    /**
     * For OWL1.1 Ontologies, limited functionality.
     */
    private static final OntObjectPersonalityBuilder OWL1_OBJECT_FACTORIES = new OntObjectPersonalityBuilder()
            .setName("OWL1")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL1ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL1ObjectFactories.ID)

            // entities:
            .add(OntClass.Named.class, OWL1ObjectFactories.NAMED_CLASS)
            .add(OntIndividual.Named.class, OWL1ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL1ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL1ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL1ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL1ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL1ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL1ObjectFactories.ANY_INDIVIDUAL)

            // properties:
            .add(OntObjectProperty.class, OWL1ObjectFactories.OBJECT_PROPERTY)
            .add(OntRealProperty.class, OWL1ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL1ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL1ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL1ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL1ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL1ObjectFactories.OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL1ObjectFactories.DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL1ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL1ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.ObjectMinCardinality.class, OWL1ObjectFactories.OBJECT_MIN_CARDINALITY_CLASS)
            .add(OntClass.DataMinCardinality.class, OWL1ObjectFactories.DATA_MIN_CARDINALITY_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL1ObjectFactories.OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL1ObjectFactories.DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.ObjectCardinality.class, OWL1ObjectFactories.OBJECT_CARDINALITY_CLASS)
            .add(OntClass.DataCardinality.class, OWL1ObjectFactories.DATA_CARDINALITY_CLASS)
            .add(OntClass.UnionOf.class, OWL1ObjectFactories.UNION_OF_CLASS)
            .add(OntClass.OneOf.class, OWL1ObjectFactories.ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL1ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL1ObjectFactories.COMPLEMENT_OF_CLASS)
            .add(OntClass.LogicalExpression.class, OWL1ObjectFactories.ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL1ObjectFactories.ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL1ObjectFactories.ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.CardinalityRestriction.class, OWL1ObjectFactories.ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL1ObjectFactories.ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL1ObjectFactories.ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL1ObjectFactories.ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL1ObjectFactories.ANY_CLASS)

            // data ranges:
            .add(OntDataRange.OneOf.class, OWL1ObjectFactories.ONE_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL1ObjectFactories.ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL1ObjectFactories.ANY_DATARANGE)

            .add(OntDisjoint.Individuals.class, OWL1ObjectFactories.DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.class, OWL1ObjectFactories.ANY_DISJOINT);

    /**
     * Default personality builder for OWL2. Private access since this constant is mutable.
     */
    private static final OntObjectPersonalityBuilder OWL2_OBJECT_FACTORIES = new OntObjectPersonalityBuilder()
            .setName("OWL2")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL2ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL2ObjectFactories.ID)

            // annotation object:
            .add(OntAnnotation.class, OWL2ObjectFactories.ANNOTATION)

            // entities:
            .add(OntClass.Named.class, OWL2ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL2ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL2ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL2ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL2ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL2ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL2ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL2ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL2ObjectFactories.ANY_INDIVIDUAL)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OWL2ObjectFactories.INVERSE_OBJECT_PROPERTY)
            .add(OntObjectProperty.class, OWL2ObjectFactories.OBJECT_PROPERTY)
            .add(OntRealProperty.class, OWL2ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL2ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL2ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL2ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL2ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL2ObjectFactories.OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL2ObjectFactories.DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL2ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL2ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.ObjectMinCardinality.class, OWL2ObjectFactories.OBJECT_MIN_CARDINALITY_CLASS)
            .add(OntClass.DataMinCardinality.class, OWL2ObjectFactories.DATA_MIN_CARDINALITY_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL2ObjectFactories.OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL2ObjectFactories.DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.ObjectCardinality.class, OWL2ObjectFactories.OBJECT_CARDINALITY_CLASS)
            .add(OntClass.DataCardinality.class, OWL2ObjectFactories.DATA_CARDINALITY_CLASS)
            .add(OntClass.HasSelf.class, OWL2ObjectFactories.HAS_SELF_CLASS)
            .add(OntClass.UnionOf.class, OWL2ObjectFactories.UNION_OF_CLASS)
            .add(OntClass.OneOf.class, OWL2ObjectFactories.ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL2ObjectFactories.COMPLEMENT_OF_CLASS)
            .add(OntClass.NaryDataAllValuesFrom.class, OWL2ObjectFactories.NARY_DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.NaryDataSomeValuesFrom.class, OWL2ObjectFactories.NARY_DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.LogicalExpression.class, OWL2ObjectFactories.ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL2ObjectFactories.ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL2ObjectFactories.ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.CardinalityRestriction.class, OWL2ObjectFactories.ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL2ObjectFactories.ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL2ObjectFactories.ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL2ObjectFactories.ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL2ObjectFactories.ANY_CLASS)

            // data ranges:
            .add(OntDataRange.OneOf.class, OWL2ObjectFactories.ONE_OF_DATARANGE)
            .add(OntDataRange.Restriction.class, OWL2ObjectFactories.RESTRICTION_DATARANGE)
            .add(OntDataRange.ComplementOf.class, OWL2ObjectFactories.COMPLEMENT_OF_DATARANGE)
            .add(OntDataRange.UnionOf.class, OWL2ObjectFactories.UNION_OF_DATARANGE)
            .add(OntDataRange.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL2ObjectFactories.ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL2ObjectFactories.ANY_DATARANGE)

            // facet restrictions:
            .add(OntFacetRestriction.Length.class, OWL2ObjectFactories.LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinLength.class, OWL2ObjectFactories.MIN_LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxLength.class, OWL2ObjectFactories.MAX_LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinInclusive.class, OWL2ObjectFactories.MIN_INCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxInclusive.class, OWL2ObjectFactories.MAX_INCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinExclusive.class, OWL2ObjectFactories.MIN_EXCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxExclusive.class, OWL2ObjectFactories.MAX_EXCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.Pattern.class, OWL2ObjectFactories.PATTERN_FACET_RESTRICTION)
            .add(OntFacetRestriction.TotalDigits.class, OWL2ObjectFactories.TOTAL_DIGITS_FACET_RESTRICTION)
            .add(OntFacetRestriction.FractionDigits.class, OWL2ObjectFactories.FRACTION_DIGITS_FACET_RESTRICTION)
            .add(OntFacetRestriction.LangRange.class, OWL2ObjectFactories.LANG_RANGE_FACET_RESTRICTION)
            .add(OntFacetRestriction.class, OWL2ObjectFactories.ANY_FACET_RESTRICTION)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OWL2ObjectFactories.OBJECT_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.WithDataProperty.class, OWL2ObjectFactories.DATA_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.class, OWL2ObjectFactories.ANY_NEGATIVE_PROPERTY_ASSERTION)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OWL2ObjectFactories.CLASSES_DISJOINT)
            .add(OntDisjoint.Individuals.class, OWL2ObjectFactories.DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.ObjectProperties.class, OWL2ObjectFactories.OBJECT_PROPERTIES_DISJOINT)
            .add(OntDisjoint.DataProperties.class, OWL2ObjectFactories.DATA_PROPERTIES_DISJOINT)
            .add(OntDisjoint.Properties.class, OWL2ObjectFactories.ANY_PROPERTIES_DISJOINT)
            .add(OntDisjoint.class, OWL2ObjectFactories.ANY_DISJOINT)

            // SWRL objects:
            .add(OntSWRL.Variable.class, SWRLObjectFactories.VARIABLE_SWRL)
            .add(OntSWRL.Builtin.class, SWRLObjectFactories.BUILTIN_SWRL)
            .add(OntSWRL.IArg.class, SWRLObjectFactories.IARG_SWRL)
            .add(OntSWRL.DArg.class, SWRLObjectFactories.DARG_SWRL)
            .add(OntSWRL.Arg.class, SWRLObjectFactories.ANY_ARG_SWRL)
            .add(OntSWRL.Atom.WithBuiltin.class, SWRLObjectFactories.BUILT_IN_ATOM_SWRL)
            .add(OntSWRL.Atom.WithClass.class, SWRLObjectFactories.CLASS_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDataRange.class, SWRLObjectFactories.DATA_RANGE_ATOM_SWRL)
            .add(OntSWRL.Atom.WithObjectProperty.class, SWRLObjectFactories.INDIVIDUAL_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDataProperty.class, SWRLObjectFactories.DATA_VALUED_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDifferentIndividuals.class, SWRLObjectFactories.DIFFERENT_INDIVIDUALS_ATOM_SWRL)
            .add(OntSWRL.Atom.WithSameIndividuals.class, SWRLObjectFactories.SAME_INDIVIDUALS_ATOM_SWRL)
            .add(OntSWRL.Atom.Unary.class, SWRLObjectFactories.ANY_UNARY_ATOM_SWRL)
            .add(OntSWRL.Atom.Binary.class, SWRLObjectFactories.ANY_BINARY_ATOM_SWRL)
            .add(OntSWRL.Atom.class, SWRLObjectFactories.ANY_ATOM_SWRL)
            .add(OntSWRL.Imp.class, SWRLObjectFactories.IMPL_SWRL)
            .add(OntSWRL.class, SWRLObjectFactories.ANY_OBJECT_SWRL);

    /**
     * Mutable {@link OntObjectPersonalityBuilder} for RDFS Ontologies.
     */
    public static OntObjectPersonalityBuilder RDFS_PERSONALITY() {
        return RDFS_OBJECT_FACTORIES.copy();
    }

    /**
     * Mutable {@link OntObjectPersonalityBuilder} for OWL1 Ontologies.
     */
    public static OntObjectPersonalityBuilder OWL1_ONT_OBJECT_PERSONALITY() {
        return OWL1_OBJECT_FACTORIES.copy();
    }

    /**
     * Mutable {@link OntObjectPersonalityBuilder} for OWL2 Ontologies.
     */
    public static OntObjectPersonalityBuilder OWL2_ONT_OBJECT_PERSONALITY() {
        return OWL2_OBJECT_FACTORIES.copy();
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
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     */
    public enum PunningsMode {
        /**
         * Personality with four kinds of restriction on a {@code rdf:type} intersection (i.e. "illegal punnings"):
         * <ul>
         * <li>Named owl:Class &lt;-&gt; Named rdfs:Datatype</li>
         * <li>Named owl:ObjectProperty &lt;-&gt; owl:DatatypeProperty</li>
         * <li>Named owl:ObjectProperty &lt;-&gt; owl:AnnotationProperty</li>
         * <li>owl:AnnotationProperty &lt;-&gt; owl:DatatypeProperty</li>
         * </ul>
         * each of the pairs above can't exist in the form of OWL-Entity in the same model at the same time.
         * From specification: "OWL 2 DL imposes certain restrictions:
         * it requires that a name cannot be used for both a class and a datatype and
         * that a name can only be used for one kind of property."
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
