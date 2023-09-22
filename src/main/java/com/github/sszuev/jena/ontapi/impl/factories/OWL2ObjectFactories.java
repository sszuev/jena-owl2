package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntCEImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDRImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntFRImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIDImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntNPAImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.rdf.model.RDFList;

/**
 * A helper-factory to produce (OWL2) {@link EnhNodeFactory} factories;
 * for {@link com.github.sszuev.jena.ontapi.common.OntPersonality ont-personalities}
 */
public final class OWL2ObjectFactories {

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(
            OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT,
            EnhNodeFilter.URI.or(EnhNodeFilter.BLANK)
    );
    public static final EnhNodeFactory ID = OntEnhNodeFactories.createCommon(
            OntIDImpl.class,
            new EnhNodeFinder.ByType(OWL.Ontology),
            new EnhNodeFilter.HasType(OWL.Ontology)
    );
    public static final EnhNodeFactory ANNOTATION = OntEnhNodeFactories.createCommon(
            OntAnnotationImpl.class,
            OntAnnotations::listRootAnnotations,
            OntAnnotations::testAnnotation
    );

    public static final EnhNodeFactory NAMED_CLASS = OntEntities.CLASS.createFactory();
    public static final EnhNodeFactory NAMED_DATARANGE = OntEntities.DATATYPE.createFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEntities.ANNOTATION_PROPERTY.createFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OntEntities.DATA_PROPERTY.createFactory();
    public static final EnhNodeFactory NAMED_OBJECT_PROPERTY = OntEntities.OBJECT_PROPERTY.createFactory();
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEntities.INDIVIDUAL.createFactory();

    public static final EnhNodeFactory ANY_ENTITY = OntEnhNodeFactories.createFrom(
            OntEnhNodeFactories.createFinder(e -> e.resourceType.asNode(), OntEntities.values()),
            NAMED_CLASS, NAMED_DATARANGE, NAMED_INDIVIDUAL, ANNOTATION_PROPERTY, DATATYPE_PROPERTY, NAMED_OBJECT_PROPERTY
    );

    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividuals::testAnonymousIndividual
    );
    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    public static final EnhNodeFactory INVERSE_OBJECT_PROPERTY = new OntProperties.AnonymousObjectPropertyFactory();
    public static final EnhNodeFactory ANY_NAMED_PROPERTY = OntEnhNodeFactories.createFrom(
            OntProperties.NAMED_PROPERTY_FINDER,
            OntObjectProperty.Named.class,
            OntDataProperty.class,
            OntAnnotationProperty.class
    );
    public static final EnhNodeFactory OBJECT_PROPERTY = OntProperties.createObjectPropertyExpressionFactory();
    public static final EnhNodeFactory ANY_DATA_OR_OBJECT_PROPERTY = new OntProperties.PropertiesFactory()
            .add(OWL.ObjectProperty, OntObjectProperty.class)
            .add(OWL.DatatypeProperty, OntDataProperty.class);
    public static final EnhNodeFactory ANY_PROPERTY = new OntProperties.PropertiesFactory()
            .add(OWL.ObjectProperty, OntObjectProperty.class)
            .add(OWL.DatatypeProperty, OntDataProperty.class)
            .add(OWL.AnnotationProperty, OntAnnotationProperty.class);

    public static final EnhNodeFactory UNION_OF_CLASS = OntClasses.createCEFactory(
            OntCEImpl.UnionOfImpl.class,
            OWL.unionOf,
            RDFList.class
    );
    public static final EnhNodeFactory INTERSECTION_OF_CLASS = OntClasses.createCEFactory(
            OntCEImpl.IntersectionOfImpl.class,
            OWL.intersectionOf, RDFList.class
    );
    public static final EnhNodeFactory ONE_OF_CLASS = OntClasses.createCEFactory(
            OntCEImpl.OneOfImpl.class,
            OWL.oneOf,
            RDFList.class
    );
    public static final EnhNodeFactory COMPLEMENT_OF_CLASS = OntClasses.createCEFactory(
            OntCEImpl.ComplementOfImpl.class,
            OWL.complementOf, OntClass.class
    );
    public static final EnhNodeFactory OBJECT_SOME_VALUES_FROM_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectSomeValuesFromImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OWL.someValuesFrom
    );
    public static final EnhNodeFactory DATA_SOME_VALUES_FROM_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataSomeValuesFromImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OWL.someValuesFrom
    );
    public static final EnhNodeFactory OBJECT_ALL_VALUES_FROM_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectAllValuesFromImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory DATA_ALL_VALUES_FROM_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataAllValuesFromImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory OBJECT_HAS_VALUE_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectHasValueImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.INDIVIDUAL,
            OWL.hasValue
    );
    public static final EnhNodeFactory DATA_HAS_VALUE_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataHasValueImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.LITERAL,
            OWL.hasValue
    );
    public static final EnhNodeFactory DATA_MIN_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataMinCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.MIN
    );
    public static final EnhNodeFactory OBJECT_MIN_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectMinCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.MIN
    );
    public static final EnhNodeFactory DATA_MAX_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataMaxCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.MAX
    );
    public static final EnhNodeFactory OBJECT_MAX_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectMaxCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.MAX
    );
    public static final EnhNodeFactory DATA_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.DataCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.EXACTLY
    );
    public static final EnhNodeFactory OBJECT_CARDINALITY_CLASS = OntClasses.createRestrictionFactory(
            OntCEImpl.ObjectCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.EXACTLY
    );
    public static final EnhNodeFactory HAS_SELF_CLASS = OntEnhNodeFactories.createCommon(
            new OntClasses.HasSelfMaker(),
            OntClasses.RESTRICTION_FINDER,
            EnhNodeFilter.BLANK.and(new OntClasses.HasSelfFilter())
    );
    // see <a href='https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions'>Restrictions Using n-ary Data Range</a>
    public static final EnhNodeFactory NARY_DATA_ALL_VALUES_FROM_CLASS = OntClasses.createNaryFactory(
            OntCEImpl.NaryDataAllValuesFromImpl.class,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory NARY_DATA_SOME_VALUES_FROM_CLASS = OntClasses.createNaryFactory(
            OntCEImpl.NaryDataSomeValuesFromImpl.class,
            OWL.someValuesFrom
    );
    // Boolean Connectives and Enumeration of Individuals:
    public static final EnhNodeFactory ANY_COMPONENTS_CLASS = OntEnhNodeFactories.createFrom(
            OntClasses.CLASS_FINDER,
            OntClass.UnionOf.class,
            OntClass.IntersectionOf.class,
            OntClass.OneOf.class
    );
    // Cardinality Restrictions:
    public static final EnhNodeFactory ANY_CARDINALITY_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntClasses.RESTRICTION_FINDER,
            OntClass.ObjectMaxCardinality.class,
            OntClass.DataMaxCardinality.class,
            OntClass.ObjectMinCardinality.class,
            OntClass.DataMinCardinality.class,
            OntClass.ObjectCardinality.class,
            OntClass.DataCardinality.class
    );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final EnhNodeFactory ANY_COMPONENT_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntClasses.RESTRICTION_FINDER,
            OntClass.ObjectMaxCardinality.class,
            OntClass.DataMaxCardinality.class,
            OntClass.ObjectMinCardinality.class,
            OntClass.DataMinCardinality.class,
            OntClass.ObjectCardinality.class,
            OntClass.DataCardinality.class,
            OntClass.ObjectSomeValuesFrom.class,
            OntClass.DataSomeValuesFrom.class,
            OntClass.ObjectAllValuesFrom.class,
            OntClass.DataAllValuesFrom.class,
            OntClass.ObjectHasValue.class,
            OntClass.DataHasValue.class
    );
    // Cardinality + Existential/Universal Restrictions + Local reflexivity (hasSelf) + Value Restrictions
    // (all them have owl:onProperty):
    public static final EnhNodeFactory ANY_PROPERTY_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntClasses.RESTRICTION_FINDER,
            OntClass.ObjectMaxCardinality.class,
            OntClass.DataMaxCardinality.class,
            OntClass.ObjectMinCardinality.class,
            OntClass.DataMinCardinality.class,
            OntClass.ObjectCardinality.class,
            OntClass.DataCardinality.class,
            OntClass.ObjectSomeValuesFrom.class,
            OntClass.DataSomeValuesFrom.class,
            OntClass.ObjectAllValuesFrom.class,
            OntClass.DataAllValuesFrom.class,
            OntClass.ObjectHasValue.class,
            OntClass.DataHasValue.class,
            OntClass.HasSelf.class
    );
    // Cardinality + Existential/Universal Restrictions + N-ary existential/universal +
    // Local reflexivity (hasSelf) + Value Restrictions:
    public static final EnhNodeFactory ANY_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntClasses.RESTRICTION_FINDER,
            OntClass.ObjectMaxCardinality.class,
            OntClass.DataMaxCardinality.class,
            OntClass.ObjectMinCardinality.class,
            OntClass.DataMinCardinality.class,
            OntClass.ObjectCardinality.class,
            OntClass.DataCardinality.class,
            OntClass.ObjectSomeValuesFrom.class,
            OntClass.DataSomeValuesFrom.class,
            OntClass.ObjectAllValuesFrom.class,
            OntClass.DataAllValuesFrom.class,
            OntClass.ObjectHasValue.class,
            OntClass.DataHasValue.class,
            OntClass.NaryDataSomeValuesFrom.class,
            OntClass.NaryDataAllValuesFrom.class,
            OntClass.HasSelf.class
    );
    // All Class Expressions:
    public static final EnhNodeFactory ANY_CLASS = OntClasses.ClassExpressionFactory.createFactory();

    public static final EnhNodeFactory ONE_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.OneOfImpl.class,
            OntDataRanges.DR_FINDER,
            OntDataRanges.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.oneOf))
    );
    public static final EnhNodeFactory RESTRICTION_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.RestrictionImpl.class,
            OntDataRanges.DR_FINDER,
            OntDataRanges.DR_FILTER
                    .and(new EnhNodeFilter.HasPredicate(OWL.onDatatype))
                    .and(new EnhNodeFilter.HasPredicate(OWL.withRestrictions))
    );
    public static final EnhNodeFactory COMPLEMENT_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.ComplementOfImpl.class,
            OntDataRanges.DR_FINDER,
            OntDataRanges.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.datatypeComplementOf))
    );
    public static final EnhNodeFactory UNION_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.UnionOfImpl.class,
            OntDataRanges.DR_FINDER,
            OntDataRanges.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.unionOf))
    );
    public static final EnhNodeFactory INTERSECTION_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.IntersectionOfImpl.class,
            OntDataRanges.DR_FINDER,
            OntDataRanges.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.intersectionOf))
    );
    public static final EnhNodeFactory ANY_COMPONENTS_DATARANGE = OntEnhNodeFactories.createFrom(
            OntDataRanges.DR_FINDER,
            OntDataRange.OneOf.class,
            OntDataRange.Restriction.class,
            OntDataRange.UnionOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final EnhNodeFactory ANY_DATARANGE = OntDataRanges.DataRangeFactory.createFactory();

    public static final EnhNodeFactory LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.LengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.length),
            OntDataRanges.makeFacetRestrictionFilter(XSD.length)
    );
    public static final EnhNodeFactory MIN_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minLength)
    );
    public static final EnhNodeFactory MAX_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxLength)
    );
    public static final EnhNodeFactory MIN_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minInclusive)
    );
    public static final EnhNodeFactory MAX_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxInclusive)
    );
    public static final EnhNodeFactory MIN_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minExclusive)
    );
    public static final EnhNodeFactory MAX_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxExclusive)
    );
    public static final EnhNodeFactory TOTAL_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.TotalDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.totalDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.totalDigits)
    );
    public static final EnhNodeFactory FRACTION_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.FractionDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.fractionDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.fractionDigits)
    );
    public static final EnhNodeFactory PATTERN_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.PatternImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.pattern),
            OntDataRanges.makeFacetRestrictionFilter(XSD.pattern)
    );
    public static final EnhNodeFactory LANG_RANGE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.LangRangeImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(RDF.langRange),
            OntDataRanges.makeFacetRestrictionFilter(RDF.langRange)
    );
    public static final EnhNodeFactory ANY_FACET_RESTRICTION = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_BLANK_SUBJECT,
            OntFacetRestriction.Length.class,
            OntFacetRestriction.MinLength.class,
            OntFacetRestriction.MaxLength.class,
            OntFacetRestriction.MinInclusive.class,
            OntFacetRestriction.MaxInclusive.class,
            OntFacetRestriction.MinExclusive.class,
            OntFacetRestriction.MaxExclusive.class,
            OntFacetRestriction.TotalDigits.class,
            OntFacetRestriction.FractionDigits.class,
            OntFacetRestriction.Pattern.class,
            OntFacetRestriction.LangRange.class
    );

    public static final EnhNodeFactory CLASSES_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.ClassesImpl.class,
            OWL.AllDisjointClasses,
            OntClass.class,
            true,
            OWL.members
    );
    public static final EnhNodeFactory DIFFERENT_INDIVIDUALS_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.IndividualsImpl.class,
            OWL.AllDifferent,
            OntIndividual.class,
            true,
            OWL.members,
            OWL.distinctMembers
    );
    public static final EnhNodeFactory OBJECT_PROPERTIES_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.ObjectPropertiesImpl.class,
            OWL.AllDisjointProperties,
            OntObjectProperty.class,
            false,
            OWL.members
    );
    public static final EnhNodeFactory DATA_PROPERTIES_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.DataPropertiesImpl.class,
            OWL.AllDisjointProperties,
            OntDataProperty.class,
            false,
            OWL.members
    );
    public static final EnhNodeFactory ANY_PROPERTIES_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.PROPERTIES_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class
    );
    public static final EnhNodeFactory ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );

    public static final EnhNodeFactory OBJECT_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNPAImpl.ObjectAssertionImpl.class,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FILTER,
            new EnhNodeFilter.HasPredicate(OWL.targetIndividual)
    );
    public static final EnhNodeFactory DATA_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNPAImpl.DataAssertionImpl.class,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FILTER,
            new EnhNodeFilter.HasPredicate(OWL.targetValue)
    );
    public static final EnhNodeFactory ANY_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createFrom(
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntNegativeAssertion.WithObjectProperty.class,
            OntNegativeAssertion.WithDataProperty.class
    );

}
