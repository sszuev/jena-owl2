package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDataRangeImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIDImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.function.Function;

public class OWL1ObjectFactories {

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

    public static final EnhNodeFactory NAMED_CLASS = OntEntities.CLASS.createFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEntities.ANNOTATION_PROPERTY.createFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OntEntities.DATA_PROPERTY.createFactory();
    public static final EnhNodeFactory NAMED_OBJECT_PROPERTY = OntEntities.OBJECT_PROPERTY.createFactory();
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.NamedImpl.class,
            OntIndividualImpl.NamedImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isURI),
            OWL1ObjectFactories::isNamedIndividual
    );
    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            OntIndividualImpl.AnonymousImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isBlank),
            OWL1ObjectFactories::isAnonymousIndividual
    );

    public static final EnhNodeFactory ANY_ENTITY = OntEnhNodeFactories.createFrom(
            OntEnhNodeFactories.createFinder(e -> e.resourceType.asNode(), OntEntities.values()),
            NAMED_CLASS, NAMED_INDIVIDUAL, ANNOTATION_PROPERTY, DATATYPE_PROPERTY, NAMED_OBJECT_PROPERTY
    );

    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    public static final EnhNodeFactory ANY_NAMED_PROPERTY = OntEnhNodeFactories.createFrom(
            OntProperties.NAMED_PROPERTY_FINDER,
            NAMED_OBJECT_PROPERTY,
            DATATYPE_PROPERTY,
            ANNOTATION_PROPERTY
    );
    public static final EnhNodeFactory OBJECT_PROPERTY = NAMED_OBJECT_PROPERTY;
    public static final EnhNodeFactory ANY_DATA_OR_OBJECT_PROPERTY = OntEnhNodeFactories.createFrom(
            OntProperties.NAMED_PROPERTY_FINDER,
            NAMED_OBJECT_PROPERTY,
            DATATYPE_PROPERTY
    );
    public static final EnhNodeFactory ANY_PROPERTY = ANY_NAMED_PROPERTY;

    // Class Expressions (Boolean Connectives and Enumeration of Individuals):
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_CLASS = config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
            OntClassImpl.UnionOfImpl.class,
            OWL.unionOf,
            RDFList.class,
            config);
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_CLASS = config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
            OntClassImpl.IntersectionOfImpl.class,
            OWL.intersectionOf, RDFList.class,
            config);
    public static final Function<OntConfig, EnhNodeFactory> ONE_OF_CLASS = config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
            OntClassImpl.OneOfImpl.class,
            OWL.oneOf,
            RDFList.class,
            config);
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_CLASS = config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
            OntClassImpl.ComplementOfImpl.class,
            OWL.complementOf,
            OntClass.class,
            config);
    // Class Expressions (Restrictions):
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_SOME_VALUES_FROM_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.ObjectSomeValuesFromImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OWL.someValuesFrom,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_SOME_VALUES_FROM_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.DataSomeValuesFromImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OWL.someValuesFrom,
            config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_ALL_VALUES_FROM_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.ObjectAllValuesFromImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OWL.allValuesFrom,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_ALL_VALUES_FROM_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.DataAllValuesFromImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OWL.allValuesFrom,
            config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_HAS_VALUE_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.ObjectHasValueImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.INDIVIDUAL,
            OWL.hasValue,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_HAS_VALUE_CLASS = config -> OntClasses.createComponentRestrictionFactory(
            OntClassImpl.DataHasValueImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.LITERAL,
            OWL.hasValue,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MIN_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.DataMinCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntClassImpl.CardinalityType.MIN,
            config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MIN_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.ObjectMinCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntClassImpl.CardinalityType.MIN,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MAX_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.DataMaxCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntClassImpl.CardinalityType.MAX,
            config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MAX_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.ObjectMaxCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntClassImpl.CardinalityType.MAX,
            config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.DataCardinalityImpl.class,
            OntClasses.RestrictionType.DATA,
            OntClasses.ObjectRestrictionType.DATA_RANGE,
            OntClassImpl.CardinalityType.EXACTLY,
            config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_CARDINALITY_CLASS = config -> OntClasses.createCardinalityRestrictionFactory(
            OntClassImpl.ObjectCardinalityImpl.class,
            OntClasses.RestrictionType.OBJECT,
            OntClasses.ObjectRestrictionType.CLASS,
            OntClassImpl.CardinalityType.EXACTLY,
            config);
    // Boolean Connectives and Enumeration of Individuals (with except of ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENTS_CLASS =
            config -> OntClasses.createClassExpressionFactoryOWL1(
                    config,
                    OntClasses.Factory.Filter.COLLECTION_EXPRESSIONS
            );
    // Cardinality Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CARDINALITY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactoryOWL1(
                    config,
                    OntClasses.Factory.Filter.CARDINALITY_RESTRICTIONS
            );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactoryOWL1(
                    config,
                    OntClasses.Factory.Filter.VALUE_RESTRICTIONS,
                    OntClasses.Factory.Filter.CARDINALITY_RESTRICTIONS
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_PROPERTY_RESTRICTION_CLASS = ANY_RESTRICTION_CLASS;
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENT_RESTRICTION_CLASS = ANY_RESTRICTION_CLASS;
    // All Class Expressions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CLASS =
            config -> OntClasses.createClassExpressionFactoryOWL1(
                    config,
                    OntClasses.Factory.Filter.CLASS_ENTITY,
                    OntClasses.Factory.Filter.COMPLEMENT_OF_EXPRESSION,
                    OntClasses.Factory.Filter.COLLECTION_EXPRESSIONS,
                    OntClasses.Factory.Filter.VALUE_RESTRICTIONS,
                    OntClasses.Factory.Filter.CARDINALITY_RESTRICTIONS
            );

    // Data Range Expressions
    public static final EnhNodeFactory ONE_OF_DATARANGE = OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.OneOfImpl.class,
            OntDataRanges.DR_FINDER_OWL1,
            OntDataRanges.DR_FILTER_OWL1.and(new EnhNodeFilter.HasPredicate(OWL.oneOf))
    );
    public static final EnhNodeFactory ANY_COMPONENTS_DATARANGE = ONE_OF_DATARANGE;
    public static final EnhNodeFactory ANY_DATARANGE = ONE_OF_DATARANGE;

    public static final EnhNodeFactory DIFFERENT_INDIVIDUALS_DISJOINT = OntDisjoints.createFactory(
            OntDisjointImpl.IndividualsImpl.class,
            (n, g) -> new OntDisjointImpl.IndividualsImpl(n, g, true),
            OWL.AllDifferent,
            OntIndividual.class,
            true,
            OWL.distinctMembers
    );
    public static final EnhNodeFactory ANY_DISJOINT = DIFFERENT_INDIVIDUALS_DISJOINT;

    private static boolean isNamedIndividual(Node n, EnhGraph eg) {
        return n.isURI() && isIndividual(n, eg);
    }

    private static boolean isAnonymousIndividual(Node n, EnhGraph eg) {
        return !n.isURI() && isIndividual(n, eg);
    }

    private static boolean isIndividual(Node n, EnhGraph eg) {
        EnhNodeFactory factory = ANY_CLASS.apply(OntEnhGraph.config(eg));
        return Iterators.anyMatch(
                eg.asGraph().find(n, RDF.type.asNode(), Node.ANY)
                        .mapWith(Triple::getObject),
                it -> factory.canWrap(it, eg)
        );
    }

    private static ExtendedIterator<Node> findIndividuals(EnhGraph eg) {
        EnhNodeFactory factory = ANY_CLASS.apply(OntEnhGraph.config(eg));
        return eg.asGraph().find(Node.ANY, RDF.type.asNode(), Node.ANY)
                .filterKeep(t -> factory.canWrap(t.getObject(), eg))
                .mapWith(Triple::getSubject);
    }
}
