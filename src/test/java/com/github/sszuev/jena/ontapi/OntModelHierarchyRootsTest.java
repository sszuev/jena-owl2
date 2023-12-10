package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.StringReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntModelHierarchyRootsTest {

    private static final String BASE = "http://www.test.com/test";
    private static final String NS = BASE + "#";

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListHierarchyRoots1a(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a owl:Class. ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Resource A = m.getResource(NS + "A");
        Assertions.assertEquals(List.of(A), m.hierarchyRoots().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListHierarchyRoots1b(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a owl:Class. ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Assertions.assertEquals(List.of(), m.hierarchyRoots().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListHierarchyRoots2a(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a owl:Class. "
                        + ":B a owl:Class ; rdfs:subClassOf :A . ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Resource A = m.getResource(NS + "A");
        Assertions.assertEquals(List.of(A), m.hierarchyRoots().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListHierarchyRoots2b(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a owl:Class. "
                        + ":B a owl:Class ; rdfs:subClassOf :A . ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Assertions.assertEquals(List.of(), m.hierarchyRoots().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListHierarchyRoots3a(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a rdfs:Class. "
                        + ":C a rdfs:Class. "
                        + ":B a rdfs:Class ; rdfs:subClassOf :A . ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Assertions.assertEquals(List.of(), m.hierarchyRoots().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListHierarchyRoots3b(TestSpec spec) {
        String doc =
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                        + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                        + "@prefix : <" + NS + ">. "
                        + ":A a rdfs:Class. "
                        + ":C a rdfs:Class. "
                        + ":B a rdfs:Class ; rdfs:subClassOf :A . ";

        OntModel m = OntModelFactory.createModel(spec.inst);
        m.read(new StringReader(doc), NS, "N3");
        Resource A = m.getResource(NS + "A");
        Resource C = m.getResource(NS + "C");
        Assertions.assertEquals(
                List.of(A, C),
                m.hierarchyRoots().sorted(Comparator.comparing(Resource::getLocalName)).collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListHierarchyRoots4a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c0 = m.createOntClass(":C0");
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");

        OntClass c8 = m.createDataSomeValuesFrom(m.createDataProperty(":p1"), m.createDataOneOf(m.createTypedLiteral(42)));
        OntClass c9 = m.createObjectOneOf(m.createIndividual(null, c0), m.createIndividual(null, c1));
        OntClass c10 = m.createObjectComplementOf(c6);
        OntClass c11 = m.getOWLThing();
        OntClass c12 = m.getOWLNothing();

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c12);
        c8.addSuperClass(c9);
        c9.addSuperClass(c5);
        c9.addSuperClass(c7);
        c10.addSuperClass(c11);

        List<OntClass> actual = m.hierarchyRoots().collect(Collectors.toList());
        Set<Resource> expected = Set.of(c10, c0, c4, c7);
        Assertions.assertEquals(4, actual.size());
        Assertions.assertEquals(expected, new HashSet<>(actual));
    }

}
