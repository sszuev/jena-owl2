package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * Interface encapsulating a {named} Annotation Property.
 * This is an extension to the standard jena {@link Property},
 * the {@link OntEntity OWL Entity} and the {@link OntProperty abstract property expression} interfaces.
 * In OWL2 an Annotation Property cannot be anonymous.
 * <p>
 * Created @ssz on 01.11.2016.
 *
 * @see <a href='https://www.w3.org/TR/owl2-syntax/#Annotation_Properties'>5.5 Annotation Properties</a>
 */
public interface OntAnnotationProperty extends OntProperty, OntNamedProperty<OntAnnotationProperty> {

    /**
     * {@inheritDoc}
     *
     * @return <b>distinct</b> {@code Stream} of annotation properties
     */
    Stream<OntAnnotationProperty> superProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @return <b>distinct</b> {@code Stream} of annotation properties
     */
    Stream<OntAnnotationProperty> subProperties(boolean direct);

    /**
     * Lists all valid annotation property domains in the form of java {@code Stream}.
     *
     * @return {@code Stream} of uri-{@link Resource}s
     */
    @Override
    Stream<Resource> domains();

    /**
     * Lists all valid annotation property ranges.
     *
     * @return {@code Stream} of uri-{@link Resource}s
     */
    @Override
    Stream<Resource> ranges();

    /**
     * Adds domain statement {@code A rdfs:domain U},
     * where {@code A} is this annotation property and {@code U} is any IRI.
     *
     * @param domain uri-{@link Resource}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @throws OntJenaException in case of anonymous resource is specified
     * @see #domains()
     * @see #removeDomain(Resource)
     * @see #addDomain(Resource)
     * @see #addRangeStatement(Resource)
     */
    OntStatement addDomainStatement(Resource domain);

    /**
     * Adds range statement {@code A rdfs:range U}, where {@code A} is an annotation property, {@code U} is any IRI.
     *
     * @param range uri-{@link Resource}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     * @throws OntJenaException in case of input is anonymous resource
     * @see #ranges()
     * @see #removeRange(Resource)
     * @see #addRange(Resource)
     * @see #addDomainStatement(Resource)
     */
    OntStatement addRangeStatement(Resource range);

    /**
     * Lists all direct super properties.
     * The pattern is {@code A1 rdfs:subPropertyOf A2},
     * where {@code A1} is this property and {@code A2} is what needs to be returned.
     *
     * @return {@code Stream} of {@link OntAnnotationProperty}s
     * @see #addSubPropertyOfStatement(OntAnnotationProperty)
     * @see #addSuperProperty(OntAnnotationProperty)
     * @see #removeSuperProperty(Resource)
     * @see #superProperties(boolean)
     */
    @Override
    default Stream<OntAnnotationProperty> superProperties() {
        return objects(RDFS.subPropertyOf, OntAnnotationProperty.class);
    }

    /**
     * Adds a statement with the {@link RDFS#range} as predicate and the specified {@code uri} as an object.
     *
     * @param uri an URI-{@link Resource}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addRangeStatement(Resource)
     */
    default OntAnnotationProperty addRange(Resource uri) {
        addRangeStatement(uri);
        return this;
    }

    /**
     * Adds a statement with the {@link RDFS#domain} as predicate and the specified {@code uri} as an object.
     *
     * @param uri an URI-{@link Resource}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #addDomainStatement(Resource)
     */
    default OntAnnotationProperty addDomain(Resource uri) {
        addDomainStatement(uri);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    default OntAnnotationProperty removeDomain(Resource domain) {
        remove(RDFS.domain, domain);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    default OntAnnotationProperty removeRange(Resource range) {
        remove(RDFS.range, range);
        return this;
    }

    /**
     * Adds the given property as super property returning this property itself.
     *
     * @param property {@link OntAnnotationProperty}, not {@code null}
     * @return <b>this</b> instance to allow cascading calls
     * @see #removeSuperProperty(Resource)
     */
    default OntAnnotationProperty addSuperProperty(OntAnnotationProperty property) {
        addSubPropertyOfStatement(property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default OntAnnotationProperty removeSuperProperty(Resource property) {
        remove(RDFS.subPropertyOf, property);
        return this;
    }

    /**
     * Adds the given property as super property returning a new statement to annotate.
     * The triple pattern is {@code this rdfs:subPropertyOf property}).
     *
     * @param property {@link OntAnnotationProperty}, not {@code null}
     * @return {@link OntStatement} to allow subsequent annotations adding
     */
    default OntStatement addSubPropertyOfStatement(OntAnnotationProperty property) {
        return addStatement(RDFS.subPropertyOf, property);
    }

}
