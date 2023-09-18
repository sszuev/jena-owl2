package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntIndividualImpl;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.impl.objects.RDFSEntity;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntProperty;

/**
 * A helper-factory to produce (OWL1) {@link EnhNodeFactory} factories;
 * for {@link com.github.sszuev.jena.ontapi.common.OntPersonality ont-personalities}
 */
public final class RDFSObjectFactories {

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT, EnhNodeFilter.URI.or(EnhNodeFilter.BLANK));

    public static EnhNodeFactory NAMED_CLASS = RDFSEntity.CLASS.createFactory();
    public static EnhNodeFactory PROPERTY = RDFSEntity.PROPERTY.createFactory();

    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );
    public static EnhNodeFactory ANY_ENTITY = OntEnhNodeFactories.createFrom(
            RDFSEntity.ENTITY_FINDER,
            OntProperty.class,
            OntClass.class
    );
    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividualImpl::testAnonymousIndividual
    );

    public static EnhNodeFactory ANY_CLASS = RDFSEntity.CLASS.createFactory();
}
