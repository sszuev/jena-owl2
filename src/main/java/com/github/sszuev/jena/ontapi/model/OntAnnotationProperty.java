package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.stream.Stream;

/**
 * Interface encapsulating a {named} Annotation Property.
 * This is an extension to the standard jena {@link Property},
 * the {@link OntEntity OWL Entity} and the {@link OntProperty abstract property expression} interfaces.
 * In OWL2, an Annotation Property cannot be anonymous.
 *
 * @see <a href='https://www.w3.org/TR/owl2-syntax/#Annotation_Properties'>5.5 Annotation Properties</a>
 */
public interface OntAnnotationProperty extends OntProperty, OntNamedProperty<OntAnnotationProperty> {

    /**
     * {@inheritDoc}
     *
     * @param direct {@code boolean} if {@code true} answers the directly adjacent properties in the sub-property relation:
     *               i.e. eliminate any properties for which there is a longer route to reach that parent under the super-property relation
     * @return <b>distinct</b> {@code Stream} of annotation properties
     */
    Stream<OntAnnotationProperty> subProperties(boolean direct);

    /**
     * {@inheritDoc}
     *
     * @return <b>distinct</b> {@code Stream} of annotation properties
     */
    Stream<OntAnnotationProperty> superProperties(boolean direct);

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
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Ai rdfs:subPropertyOf Aj}, where {@code Ai, Aj} are annotation properties.
     *
     * @return {@code Stream} of {@link OntAnnotationProperty}s
     * @see #subProperties(boolean)
     */
    @Override
    default Stream<OntAnnotationProperty> subProperties() {
        return subProperties(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pattern is {@code Ai rdfs:subPropertyOf Aj}, where {@code Ai, Aj} are annotation properties.
     *
     * @return {@code Stream} of {@link OntAnnotationProperty}s
     * @see #superProperties(boolean)
     */
    @Override
    default Stream<OntAnnotationProperty> superProperties() {
        return superProperties(false);
    }

    /**
     * Adds a statement with the {@link RDFS#range} as predicate and the specified {@code uri} as an object.
     *
     * @param uri a URI-{@link Resource}, not {@code null}
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
     * @param uri a URI-{@link Resource}, not {@code null}
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

}
