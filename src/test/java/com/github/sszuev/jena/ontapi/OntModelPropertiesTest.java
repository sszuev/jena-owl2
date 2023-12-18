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

import java.util.ArrayList;
import java.util.Arrays;
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
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
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

        List<String> expected = new ArrayList<>(Arrays.asList("ap", "dp", "op", "rp1", "rp2"));
        if (spec.inst.getPersonality().getName().startsWith("OWL2")) {
            // support inverseOf property
            expected.add("null");
        }
        expected.sort(String::compareTo);

        List<String> actual = m.properties()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListAllOntProperties1b(TestSpec spec) {
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

        List<String> expected = new ArrayList<>(Arrays.asList(
                "ap", "comment", "domain", "dp", "first", "isDefinedBy", "label", "object", "rp1", "op",
                "predicate", "range", "rp2", "rest", "seeAlso", "subClassOf", "subPropertyOf", "subject", "type"
        ));
        if (spec.inst.getPersonality().getName().startsWith("OWL2")) {
            // support inverseOf property
            expected.add("null");
        }
        expected.sort(String::compareTo);

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
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListAllOntProperties2a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(5, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListAllOntProperties2b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(19, m.properties().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListAllOntProperties3a(TestSpec spec) {
        OntModel m = TestModelFactory.withBuiltIns(
                RDFIOTestUtils.readResourceToModel(
                        OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                ));
        Assertions.assertEquals(44, m.properties().count());
    }

}
