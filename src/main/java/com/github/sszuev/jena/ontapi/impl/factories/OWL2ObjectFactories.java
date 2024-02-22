package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDataRangeImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntFacetRestrictionImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIDImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntNegativePropertyAssertionImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
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

import java.util.function.Function;

/**
 * A helper-factory to produce (OWL2) {@link EnhNodeFactory} factories;
 * for {@link com.github.sszuev.jena.ontapi.common.OntPersonality ont-personalities}
 */
public final class OWL2ObjectFactories {

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(
            OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT,
            EnhNodeFilter.URI.or(EnhNodeFilter.ANON)
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

    public static final EnhNodeFactory NAMED_CLASS = OntEntities.createOWL2NamedClassFactory();
    public static final EnhNodeFactory NAMED_DATARANGE = OntEntities.createOWL2NamedDataRangeFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEntities.createAnnotationPropertyFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OntEntities.createDataPropertyFactory();
    public static final Function<OntConfig, EnhNodeFactory> NAMED_OBJECT_PROPERTY = OntEntities::createNamedObjectPropertyFactory;
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEntities.createNamedIndividualFactory();

    public static final Function<OntConfig, EnhNodeFactory> ANY_ENTITY = config -> OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            NAMED_CLASS,
            NAMED_DATARANGE,
            NAMED_INDIVIDUAL,
            ANNOTATION_PROPERTY,
            DATATYPE_PROPERTY,
            NAMED_OBJECT_PROPERTY.apply(config)
    );

    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividuals::testAnonymousIndividual
    );
    public static final EnhNodeFactory DL_ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );
    public static final EnhNodeFactory EL_ANY_INDIVIDUAL = NAMED_INDIVIDUAL;
    public static final EnhNodeFactory QL_ANY_INDIVIDUAL = NAMED_INDIVIDUAL;

    public static final EnhNodeFactory INVERSE_OBJECT_PROPERTY = new OntProperties.AnonymousObjectPropertyFactory();

    public static final Function<OntConfig, EnhNodeFactory> ANY_NAMED_PROPERTY = config -> OntEnhNodeFactories.createFrom(
            NAMED_OBJECT_PROPERTY.apply(config),
            DATATYPE_PROPERTY,
            ANNOTATION_PROPERTY
    );
    public static final EnhNodeFactory DL_OBJECT_PROPERTY = new OntProperties.ObjectPropertyExpressionFactory();
    public static final Function<OntConfig, EnhNodeFactory> EL_OBJECT_PROPERTY = NAMED_OBJECT_PROPERTY;
    public static final EnhNodeFactory QL_OBJECT_PROPERTY = DL_OBJECT_PROPERTY;

    public static final EnhNodeFactory DL_ANY_DATA_OR_OBJECT_PROPERTY = OntEnhNodeFactories.createFrom(
            DATATYPE_PROPERTY,
            DL_OBJECT_PROPERTY
    );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_DATA_OR_OBJECT_PROPERTY = config ->
            OntEnhNodeFactories.createFrom(
                    DATATYPE_PROPERTY,
                    EL_OBJECT_PROPERTY.apply(config)
            );
    public static final EnhNodeFactory QL_ANY_DATA_OR_OBJECT_PROPERTY = DL_ANY_DATA_OR_OBJECT_PROPERTY;
    public static final Function<OntConfig, EnhNodeFactory> ANY_PROPERTY = OntProperties::createFactory;

    // Class Expressions
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.UnionOfImpl.class,
                    OWL.unionOf,
                    RDFList.class,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.IntersectionOfImpl.class,
                    OWL.intersectionOf,
                    RDFList.class,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DL_ONE_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.OneOfImpl.class,
                    OWL.oneOf,
                    RDFList.class,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> EL_ONE_OF_CLASS = OntClasses::createOWL2ELOneOfEnumerationFactory;
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.ComplementOfImpl.class,
                    OWL.complementOf,
                    OntClass.class,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL.someValuesFrom,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL.someValuesFrom,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectAllValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL.allValuesFrom,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataAllValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL.allValuesFrom,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_HAS_VALUE_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectHasValueImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.INDIVIDUAL,
                    OWL.hasValue,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_HAS_VALUE_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataHasValueImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.LITERAL,
                    OWL.hasValue,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MIN_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataMinCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.MIN,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MIN_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectMinCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.MIN,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MAX_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataMaxCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.MAX,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MAX_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectMaxCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.MAX,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.EXACTLY,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.EXACTLY,
                    config);
    public static final EnhNodeFactory HAS_SELF_CLASS = OntEnhNodeFactories.createCommon(
            new OntClasses.HasSelfMaker(),
            OntClasses.RESTRICTION_FINDER,
            EnhNodeFilter.ANON.and(new OntClasses.HasSelfFilter())
    );
    // see <a href='https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions'>Restrictions Using n-ary Data Range</a>
    public static final EnhNodeFactory NARY_DATA_ALL_VALUES_FROM_CLASS = OntClasses.createNaryRestrictionFactory(
            OntClassImpl.NaryDataAllValuesFromImpl.class,
            OWL.allValuesFrom
    );
    public static final EnhNodeFactory NARY_DATA_SOME_VALUES_FROM_CLASS = OntClasses.createNaryRestrictionFactory(
            OntClassImpl.NaryDataSomeValuesFromImpl.class,
            OWL.someValuesFrom
    );
    // Boolean Connectives and Enumeration of Individuals (with except of ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_COLLECTION_OF_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COLLECTION_OF_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COLLECTION_OF_CLASS = INTERSECTION_OF_CLASS;
    // Boolean Connectives and Enumeration of Individuals + ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    // Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM
            );
    // Cardinality Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CARDINALITY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY
            );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );

    // Cardinality + Existential/Universal Restrictions + Local reflexivity (hasSelf) + Value Restrictions
    // (all them have owl:onProperty):
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM
            );

    // Cardinality + Existential/Universal Restrictions + N-ary existential/universal +
    // Local reflexivity (hasSelf) + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_NARY_ALL_VALUES_FROM
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM
            );

    // All Class Expressions:
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_NARY_ALL_VALUES_FROM,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_NARY_ALL_VALUES_FROM,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );

    // Data Range Expressions
    public static final Function<OntConfig, EnhNodeFactory> RESTRICTION_DATARANGE = config -> OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.RestrictionImpl.class,
            OntDataRanges.makeOWLFinder(config),
            OntDataRanges.makeOWLFilter(config)
                    .and(new EnhNodeFilter.HasPredicate(OWL.onDatatype))
                    .and(new EnhNodeFilter.HasPredicate(OWL.withRestrictions))
    );
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.ComplementOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL.datatypeComplementOf))
            );
    public static final Function<OntConfig, EnhNodeFactory> DL_ONE_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.OneOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL.oneOf)));
    public static final Function<OntConfig, EnhNodeFactory> EL_ONE_OF_DATARANGE = OntDataRanges::createOWL2ELOneOfEnumerationFactory;
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.UnionOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL.unionOf))
            );
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_DATARANGE = config -> OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.IntersectionOfImpl.class,
            OntDataRanges.makeOWLFinder(config),
            OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL.intersectionOf))
    );
    public static final Function<OntConfig, EnhNodeFactory> DL_ANY_COMPONENTS_DATARANGE = config -> OntEnhNodeFactories.createFrom(
            OntDataRanges.makeOWLFinder(config),
            OntDataRange.OneOf.class,
            OntDataRange.Restriction.class,
            OntDataRange.UnionOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COMPONENTS_DATARANGE = config -> OntEnhNodeFactories.createFrom(
            OntDataRanges.makeOWLFinder(config),
            OntDataRange.OneOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COMPONENTS_DATARANGE = INTERSECTION_OF_DATARANGE;
    public static final EnhNodeFactory DL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.RESTRICTION,
            OntDataRanges.Type.COMPLEMENT_OF,
            OntDataRanges.Type.ONE_OF,
            OntDataRanges.Type.UNION_OF,
            OntDataRanges.Type.INTERSECTION_OF
    );
    public static final EnhNodeFactory EL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.ONE_OF,
            OntDataRanges.Type.INTERSECTION_OF
    );
    public static final EnhNodeFactory QL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.INTERSECTION_OF
    );

    public static final EnhNodeFactory LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.LengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.length),
            OntDataRanges.makeFacetRestrictionFilter(XSD.length)
    );
    public static final EnhNodeFactory MIN_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minLength)
    );
    public static final EnhNodeFactory MAX_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxLength)
    );
    public static final EnhNodeFactory MIN_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minInclusive)
    );
    public static final EnhNodeFactory MAX_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxInclusive)
    );
    public static final EnhNodeFactory MIN_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minExclusive)
    );
    public static final EnhNodeFactory MAX_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxExclusive)
    );
    public static final EnhNodeFactory TOTAL_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.TotalDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.totalDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.totalDigits)
    );
    public static final EnhNodeFactory FRACTION_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.FractionDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.fractionDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.fractionDigits)
    );
    public static final EnhNodeFactory PATTERN_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.PatternImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.pattern),
            OntDataRanges.makeFacetRestrictionFilter(XSD.pattern)
    );
    public static final EnhNodeFactory LANG_RANGE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.LangRangeImpl.class,
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
            OntDisjointImpl.ClassesImpl::new,
            OWL.AllDisjointClasses,
            OntClass.class,
            true,
            OWL.members
    );
    public static final Function<OntConfig, EnhNodeFactory> DIFFERENT_INDIVIDUALS_DISJOINT =
            OntDisjoints::createDifferentIndividualsFactory;
    public static final EnhNodeFactory OBJECT_PROPERTIES_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.ObjectPropertiesImpl.class,
            OntDisjointImpl.ObjectPropertiesImpl::new,
            OWL.AllDisjointProperties,
            OntObjectProperty.class,
            false,
            OWL.members
    );
    public static final EnhNodeFactory DATA_PROPERTIES_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.DataPropertiesImpl.class,
            OntDisjointImpl.DataPropertiesImpl::new,
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
    public static final EnhNodeFactory DL_ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );
    public static final EnhNodeFactory EL_ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );
    public static final EnhNodeFactory QL_ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );

    public static final EnhNodeFactory OBJECT_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNegativePropertyAssertionImpl.ObjectAssertionImpl.class,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FILTER,
            new EnhNodeFilter.HasPredicate(OWL.targetIndividual)
    );
    public static final EnhNodeFactory DATA_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNegativePropertyAssertionImpl.DataAssertionImpl.class,
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
