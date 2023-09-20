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
import com.github.sszuev.jena.ontapi.impl.objects.OntPEImpl;
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
            OntAnnotationImpl::listRootAnnotations,
            OntAnnotationImpl::testAnnotation
    );

    public static final EnhNodeFactory ANY_ENTITY = OWLEntityFactories.createAnyEntityFactory();
    public static final EnhNodeFactory NAMED_CLASS = OWLEntityFactories.CLASS.createThisEntityFactory();
    public static final EnhNodeFactory NAMED_DATARANGE = OWLEntityFactories.DATATYPE.createThisEntityFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OWLEntityFactories.ANNOTATION_PROPERTY.createThisEntityFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OWLEntityFactories.DATA_PROPERTY.createThisEntityFactory();
    public static final EnhNodeFactory NAMED_OBJECT_PROPERTY = OWLEntityFactories.OBJECT_PROPERTY.createThisEntityFactory();
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OWLEntityFactories.INDIVIDUAL.createThisEntityFactory();
    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividualImpl::testAnonymousIndividual
    );
    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    public static final EnhNodeFactory INVERSE_OBJECT_PROPERTY = new OntPEImpl.AnonymousObjectPropertyFactory();
    public static final EnhNodeFactory ANY_NAMED_PROPERTY = OntEnhNodeFactories.createFrom(
            OntPEImpl.NAMED_PROPERTY_FINDER,
            OntObjectProperty.Named.class,
            OntDataProperty.class,
            OntAnnotationProperty.class
    );
    public static final EnhNodeFactory OBJECT_PROPERTY = OntPEImpl.createObjectPropertyExpressionFactory();
    public static final EnhNodeFactory ANY_DATA_OR_OBJECT_PROPERTY = new OntPEImpl.PropertiesFactory()
            .add(OWL.ObjectProperty, OntObjectProperty.class)
            .add(OWL.DatatypeProperty, OntDataProperty.class);
    public static final EnhNodeFactory ANY_PROPERTY = new OntPEImpl.PropertiesFactory()
            .add(OWL.ObjectProperty, OntObjectProperty.class)
            .add(OWL.DatatypeProperty, OntDataProperty.class)
            .add(OWL.AnnotationProperty, OntAnnotationProperty.class);

    public static final EnhNodeFactory UNION_OF_CLASS = OntCEImpl.createCEFactory(
            OntCEImpl.UnionOfImpl.class,
            OWL.unionOf,
            RDFList.class
    );
    public static final EnhNodeFactory INTERSECTION_OF_CLASS = OntCEImpl.createCEFactory(
            OntCEImpl.IntersectionOfImpl.class,
            OWL.intersectionOf, RDFList.class
    );
    public static final EnhNodeFactory ONE_OF_CLASS = OntCEImpl.createCEFactory(
            OntCEImpl.OneOfImpl.class,
            OWL.oneOf,
            RDFList.class
    );
    public static final EnhNodeFactory COMPLEMENT_OF_CLASS = OntCEImpl.createCEFactory(
            OntCEImpl.ComplementOfImpl.class,
            OWL.complementOf, OntClass.class
    );
    public static final EnhNodeFactory OBJECT_SOME_VALUES_FROM_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectSomeValuesFromImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.CLASS,
            OWL.someValuesFrom
    );
    public static final EnhNodeFactory DATA_SOME_VALUES_FROM_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataSomeValuesFromImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.DATA_RANGE,
            OWL.someValuesFrom
    );
    public static final EnhNodeFactory OBJECT_ALL_VALUES_FROM_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectAllValuesFromImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.CLASS,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory DATA_ALL_VALUES_FROM_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataAllValuesFromImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.DATA_RANGE,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory OBJECT_HAS_VALUE_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectHasValueImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.INDIVIDUAL,
            OWL.hasValue
    );
    public static final EnhNodeFactory DATA_HAS_VALUE_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataHasValueImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.LITERAL,
            OWL.hasValue
    );
    public static final EnhNodeFactory DATA_MIN_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataMinCardinalityImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.MIN
    );
    public static final EnhNodeFactory OBJECT_MIN_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectMinCardinalityImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.MIN
    );
    public static final EnhNodeFactory DATA_MAX_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataMaxCardinalityImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.MAX
    );
    public static final EnhNodeFactory OBJECT_MAX_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectMaxCardinalityImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.MAX
    );
    public static final EnhNodeFactory DATA_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.DataCardinalityImpl.class,
            OntCEImpl.RestrictionType.DATA,
            OntCEImpl.ObjectRestrictionType.DATA_RANGE,
            OntCEImpl.CardinalityType.EXACTLY
    );
    public static final EnhNodeFactory OBJECT_CARDINALITY_CLASS = OntCEImpl.createRestrictionFactory(
            OntCEImpl.ObjectCardinalityImpl.class,
            OntCEImpl.RestrictionType.OBJECT,
            OntCEImpl.ObjectRestrictionType.CLASS,
            OntCEImpl.CardinalityType.EXACTLY
    );
    public static final EnhNodeFactory HAS_SELF_CLASS = OntEnhNodeFactories.createCommon(
            new OntCEImpl.HasSelfMaker(),
            OntCEImpl.RESTRICTION_FINDER,
            EnhNodeFilter.BLANK.and(new OntCEImpl.HasSelfFilter())
    );
    // see <a href='https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions'>Restrictions Using n-ary Data Range</a>
    public static final EnhNodeFactory NARY_DATA_ALL_VALUES_FROM_CLASS = OntCEImpl.createNaryFactory(
            OntCEImpl.NaryDataAllValuesFromImpl.class,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory NARY_DATA_SOME_VALUES_FROM_CLASS = OntCEImpl.createNaryFactory(
            OntCEImpl.NaryDataSomeValuesFromImpl.class,
            OWL.someValuesFrom
    );
    // Boolean Connectives and Enumeration of Individuals:
    public static final EnhNodeFactory ANY_COMPONENTS_CLASS = OntEnhNodeFactories.createFrom(
            OntCEImpl.CLASS_FINDER,
            OntClass.UnionOf.class,
            OntClass.IntersectionOf.class,
            OntClass.OneOf.class
    );
    // Cardinality Restrictions:
    public static final EnhNodeFactory ANY_CARDINALITY_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntCEImpl.RESTRICTION_FINDER,
            OntClass.ObjectMaxCardinality.class,
            OntClass.DataMaxCardinality.class,
            OntClass.ObjectMinCardinality.class,
            OntClass.DataMinCardinality.class,
            OntClass.ObjectCardinality.class,
            OntClass.DataCardinality.class
    );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final EnhNodeFactory ANY_COMPONENT_RESTRICTION_CLASS = OntEnhNodeFactories.createFrom(
            OntCEImpl.RESTRICTION_FINDER,
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
            OntCEImpl.RESTRICTION_FINDER,
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
            OntCEImpl.RESTRICTION_FINDER,
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
    public static final EnhNodeFactory ANY_CLASS = OntCEImpl.ClassExpressionFactory.createFactory();

    public static final EnhNodeFactory ONE_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.OneOfImpl.class,
            OntDRImpl.DR_FINDER,
            OntDRImpl.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.oneOf))
    );
    public static final EnhNodeFactory RESTRICTION_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.RestrictionImpl.class,
            OntDRImpl.DR_FINDER,
            OntDRImpl.DR_FILTER
                    .and(new EnhNodeFilter.HasPredicate(OWL.onDatatype))
                    .and(new EnhNodeFilter.HasPredicate(OWL.withRestrictions))
    );
    public static final EnhNodeFactory COMPLEMENT_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.ComplementOfImpl.class,
            OntDRImpl.DR_FINDER,
            OntDRImpl.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.datatypeComplementOf))
    );
    public static final EnhNodeFactory UNION_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.UnionOfImpl.class,
            OntDRImpl.DR_FINDER,
            OntDRImpl.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.unionOf))
    );
    public static final EnhNodeFactory INTERSECTION_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDRImpl.IntersectionOfImpl.class,
            OntDRImpl.DR_FINDER,
            OntDRImpl.DR_FILTER.and(new EnhNodeFilter.HasPredicate(OWL.intersectionOf))
    );
    public static final EnhNodeFactory ANY_COMPONENTS_DATARANGE = OntEnhNodeFactories.createFrom(
            OntDRImpl.DR_FINDER,
            OntDataRange.OneOf.class,
            OntDataRange.Restriction.class,
            OntDataRange.UnionOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final EnhNodeFactory ANY_DATARANGE = OntDRImpl.DataRangeFactory.createFactory();

    public static final EnhNodeFactory LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.LengthImpl.class,
            OntFRImpl.makeFinder(XSD.length),
            OntFRImpl.makeFilter(XSD.length)
    );
    public static final EnhNodeFactory MIN_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinLengthImpl.class,
            OntFRImpl.makeFinder(XSD.minLength),
            OntFRImpl.makeFilter(XSD.minLength)
    );
    public static final EnhNodeFactory MAX_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxLengthImpl.class,
            OntFRImpl.makeFinder(XSD.maxLength),
            OntFRImpl.makeFilter(XSD.maxLength)
    );
    public static final EnhNodeFactory MIN_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinInclusiveImpl.class,
            OntFRImpl.makeFinder(XSD.minInclusive),
            OntFRImpl.makeFilter(XSD.minInclusive)
    );
    public static final EnhNodeFactory MAX_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxInclusiveImpl.class,
            OntFRImpl.makeFinder(XSD.maxInclusive),
            OntFRImpl.makeFilter(XSD.maxInclusive)
    );
    public static final EnhNodeFactory MIN_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MinExclusiveImpl.class,
            OntFRImpl.makeFinder(XSD.minExclusive),
            OntFRImpl.makeFilter(XSD.minExclusive)
    );
    public static final EnhNodeFactory MAX_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.MaxExclusiveImpl.class,
            OntFRImpl.makeFinder(XSD.maxExclusive),
            OntFRImpl.makeFilter(XSD.maxExclusive)
    );
    public static final EnhNodeFactory TOTAL_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.TotalDigitsImpl.class,
            OntFRImpl.makeFinder(XSD.totalDigits),
            OntFRImpl.makeFilter(XSD.totalDigits)
    );
    public static final EnhNodeFactory FRACTION_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.FractionDigitsImpl.class,
            OntFRImpl.makeFinder(XSD.fractionDigits),
            OntFRImpl.makeFilter(XSD.fractionDigits)
    );
    public static final EnhNodeFactory PATTERN_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.PatternImpl.class,
            OntFRImpl.makeFinder(XSD.pattern),
            OntFRImpl.makeFilter(XSD.pattern)
    );
    public static final EnhNodeFactory LANG_RANGE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFRImpl.LangRangeImpl.class,
            OntFRImpl.makeFinder(RDF.langRange),
            OntFRImpl.makeFilter(RDF.langRange)
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

    public static final EnhNodeFactory CLASSES_DISJOINT = OntDisjointImpl.createFactory(
            OntDisjointImpl.ClassesImpl.class,
            OWL.AllDisjointClasses,
            OntClass.class,
            true,
            OWL.members
    );
    public static final EnhNodeFactory DIFFERENT_INDIVIDUALS_DISJOINT = OntDisjointImpl.createFactory(
            OntDisjointImpl.IndividualsImpl.class,
            OWL.AllDifferent,
            OntIndividual.class,
            true,
            OWL.members,
            OWL.distinctMembers
    );
    public static final EnhNodeFactory OBJECT_PROPERTIES_DISJOINT = OntDisjointImpl.createFactory(
            OntDisjointImpl.ObjectPropertiesImpl.class,
            OWL.AllDisjointProperties,
            OntObjectProperty.class,
            false,
            OWL.members
    );
    public static final EnhNodeFactory DATA_PROPERTIES_DISJOINT = OntDisjointImpl.createFactory(
            OntDisjointImpl.DataPropertiesImpl.class,
            OWL.AllDisjointProperties,
            OntDataProperty.class,
            false,
            OWL.members
    );
    public static final EnhNodeFactory ANY_PROPERTIES_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjointImpl.PROPERTIES_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class
    );
    public static final EnhNodeFactory ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjointImpl.DISJOINT_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );

    public static final EnhNodeFactory OBJECT_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNPAImpl.ObjectAssertionImpl.class,
            OntNPAImpl.NPA_FINDER,
            OntNPAImpl.NPA_FILTER,
            new EnhNodeFilter.HasPredicate(OWL.targetIndividual)
    );
    public static final EnhNodeFactory DATA_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNPAImpl.DataAssertionImpl.class,
            OntNPAImpl.NPA_FINDER,
            OntNPAImpl.NPA_FILTER,
            new EnhNodeFilter.HasPredicate(OWL.targetValue)
    );
    public static final EnhNodeFactory ANY_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createFrom(
            OntNPAImpl.NPA_FINDER,
            OntNegativeAssertion.WithObjectProperty.class,
            OntNegativeAssertion.WithDataProperty.class
    );

}
