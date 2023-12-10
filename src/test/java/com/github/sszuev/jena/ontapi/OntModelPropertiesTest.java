package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntModelPropertiesTest {

    private static final String BASE = "http://www.test.com/test";
    private static final String NS = BASE + "#";

    @Test
    public void testListPropertiesWithPunnings() {
        OntModel m = OntModelFactory.createModel(TestSpec.OWL2_DL_MEM_RDFS_BUILTIN_INF.inst);
        m.createResource("X", OWL.ObjectProperty);
        m.createResource("X", OWL.DatatypeProperty);
        Assertions.assertEquals(0, m.objectProperties().count());
        Assertions.assertEquals(0, m.dataProperties().count());
        Assertions.assertEquals(0, m.ontObjects(OntRealProperty.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
    })
    public void testListAllOntProperties1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = Stream.of("ap", "dp", "op", "rp1", "rp2").sorted().collect(Collectors.toList());

        List<String> actual = m.properties()
                .map(Resource::getLocalName)
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testListAllOntProperties1d(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = Stream.of("ap", "dp", "op", "rp1", "rp2", "null").sorted().collect(Collectors.toList());

        List<String> actual = m.properties()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListAllOntProperties1e(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // named object property
        Resource op = m.createResource(NS + "op", OWL.ObjectProperty);
        // inverse object property:
        m.createResource().addProperty(OWL.inverseOf, op);
        // datatype property
        m.createResource(NS + "dp", OWL.DatatypeProperty);
        // annotation property
        m.createResource(NS + "ap", OWL.AnnotationProperty);

        m.createResource(NS + "rp1", RDF.Property);
        m.createResource(NS + "rp2", RDF.Property);
        m.createResource(NS + "rest", RDF.rest);

        List<String> expected = Stream.of("rp1", "rp2").sorted().collect(Collectors.toList());

        List<String> actual = m.properties()
                .map(Resource::getLocalName)
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListAllOntProperties2a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(5, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListAllOntProperties3a(TestSpec spec) {
        OntModel m = TestModelFactory.withBuiltIns(
                RDFIOTestUtils.readResourceToModel(
                        OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                ));
        Assertions.assertEquals(44, m.properties().count());
    }

}
