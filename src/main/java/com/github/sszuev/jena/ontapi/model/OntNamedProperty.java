package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Named Ontology property: {@link OntAnnotationProperty}, {@link OntDataProperty} and {@link OntObjectProperty.Named}.
 *
 * @param <P> subtype of {@link OntNamedProperty}
 */
public interface OntNamedProperty<P extends OntNamedProperty<P>> extends OntEntity, Property {

    /**
     * @see Property#isProperty()
     */
    @Override
    default boolean isProperty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addComment(String txt, String lang) {
        return annotate(getModel().getRDFSComment(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addLabel(String txt) {
        return addLabel(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addLabel(String txt, String lang) {
        return annotate(getModel().getRDFSLabel(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P annotate(OntAnnotationProperty predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    default P annotate(OntAnnotationProperty predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return (P) this;
    }
}
