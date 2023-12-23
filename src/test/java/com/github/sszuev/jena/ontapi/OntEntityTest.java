package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OntEntityTest {

    @Test
    public void testDefaultBuiltinClasses() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL.Thing.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(SKOS.Collection.inModel(m).canAs(OntClass.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(SKOS.broader.inModel(m).canAs(OntClass.class));
    }

    @Test
    public void testDefaultBuiltinDatatypes() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(RDF.PlainLiteral.inModel(m).canAs(OntDataRange.class));
        Assertions.assertTrue(RDFS.Literal.inModel(m).canAs(OntDataRange.class));
        Assertions.assertTrue(XSD.xdouble.inModel(m).canAs(OntDataRange.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(SKOS.broader.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(OWL.Nothing.inModel(m).canAs(OntDataRange.class));
        Assertions.assertFalse(OWL.bottomDataProperty.inModel(m).canAs(OntDataRange.class));
    }

    @Test
    public void testDefaultBuiltinDatatypeProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL.topDataProperty.inModel(m).canAs(OntDataProperty.class));

        Assertions.assertFalse(SKOS.altLabel.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDFS.comment.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(OWL.Thing.inModel(m).canAs(OntDataProperty.class));
        Assertions.assertFalse(OWL.topObjectProperty.inModel(m).canAs(OntDataProperty.class));
    }

    @Test
    public void testDefaultBuiltinObjectProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL.topObjectProperty.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertTrue(SKOS.exactMatch.inModel(m).canAs(OntObjectProperty.class));

        Assertions.assertFalse(SKOS.altLabel.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDFS.comment.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntObjectProperty.class));
        Assertions.assertFalse(OWL.Thing.inModel(m).canAs(OntObjectProperty.class));
    }

    @Test
    public void testDefaultBuiltinAnnotationProperties() {
        OntModel m = OntModelFactory.createModel();
        Assertions.assertTrue(OWL.incompatibleWith.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertTrue(SKOS.altLabel.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertTrue(RDFS.comment.inModel(m).canAs(OntAnnotationProperty.class));

        Assertions.assertFalse(RDF.type.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(RDFS.Resource.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(RDF.Bag.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(SKOS.ConceptScheme.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(OWL.Thing.inModel(m).canAs(OntAnnotationProperty.class));
        Assertions.assertFalse(OWL.topObjectProperty.inModel(m).canAs(OntAnnotationProperty.class));
    }
}
