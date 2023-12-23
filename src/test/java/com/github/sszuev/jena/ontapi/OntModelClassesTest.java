package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntModelClassesTest {

    @SuppressWarnings("ExtractMethodRecommender")
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
    public void testListClasses1a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        // there is DataHasValue Restriction in the RDF,
        //  but connected property (`owl:onProperty`) has no `owl:DatatypeProperty`
        //  declaration (it is declared as bar `rdf:Property`),
        //  so in strict mode such construction cannot be considered as a valid class expression;
        //  for OWL1 this is correct for compatibility with org.apache.jena.ontology.OntModel
        List<String> expected = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "X0", "X1", "Y0", "Y1", "Z"));
        if (!spec.inst.getPersonality().getName().startsWith("OWL2")) {
            expected.add("null");
        }
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListClasses1b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(0, m.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses1c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        int expectedClassesSize = -1;
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            expectedClassesSize = 25;
        }
        boolean expectedContainsAll = TestSpec.RDFS_MEM_RDFS_INF != spec;
        Assertions.assertEquals(expectedClassesSize, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(
                expectedContainsAll,
                m.ontObjects(OntClass.class).map(Resource::getLocalName).collect(Collectors.toSet())
                        .containsAll(Set.of("Y0", "Z", "B", "D", "Y1", "X0", "C", "E", "X1", "A"))
        );
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
    public void testListClasses2a(TestSpec spec) {
        OntModel m = TestModelFactory.withBuiltIns(
                RDFIOTestUtils.readResourceToModel(
                        OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
                ));
        List<String> expected = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "Nothing", "Thing", "X0", "X1", "Y0", "Y1", "Z", "null"));
        if (!spec.inst.getPersonality().getName().equals("OWL2")) {
            expected.add("null");
        }
        List<String> actual = m.ontObjects(OntClass.class).distinct()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListClasses2b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(0, m.classes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses2c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-with-import.rdf", Lang.RDFXML
        );
        List<String> actual = m.ontObjects(OntClass.class).distinct()
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        Set<String> probes = Set.of("A", "B", "C", "D", "E", "Nothing", "Thing", "X0", "X1", "Y0", "Y1", "Z", "null");
        boolean expectedContainsAll = spec != TestSpec.RDFS_MEM_RDFS_INF;
        int expectedClassCount = -1;
        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            expectedClassCount = 25;
        }
        Assertions.assertEquals(expectedContainsAll, actual.containsAll(probes));
        Assertions.assertEquals(expectedClassCount, actual.size());
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
    public void testListClasses3a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        List<String> expected = List.of(
                "eg:Bundle", "eg:Computer", "eg:GameBundle", "eg:GamingComputer", "eg:GraphicsCard", "eg:MotherBoard",
                "null", "null", "null"
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : m.shortForm(it.getURI()))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListClasses3c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        Assertions.assertTrue(m.ontObjects(OntClass.class).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListClasses3d(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test-comps.rdf", Lang.RDFXML
        );
        List<String> expected = List.of(
                "eg:Bundle", "eg:Computer", "eg:GameBundle", "eg:GraphicsCard", "eg:MotherBoard",
                "null",
                "owl:Class", "owl:ObjectProperty", "owl:Restriction", "owl:TransitiveProperty",
                "rdf:Alt", "rdf:Bag", "rdf:List", "rdf:Property", "rdf:Seq", "rdf:Statement", "rdf:XMLLiteral",
                "rdfs:Class", "rdfs:Container", "rdfs:ContainerMembershipProperty", "rdfs:Datatype", "rdfs:Literal", "rdfs:Resource"
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : m.shortForm(it.getURI()))
                .sorted()
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

}
