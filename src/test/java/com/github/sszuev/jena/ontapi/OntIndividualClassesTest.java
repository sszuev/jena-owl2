package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.sszuev.jena.ontapi.TestModelFactory.NS;

public class OntIndividualClassesTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListOntClasses1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        Assertions.assertEquals(
                List.of(A, B),
                x.classes(false).sorted(Comparator.comparing(Resource::getURI)).collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListOntClasses2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);

        OntIndividual x = A.createIndividual();
        x.attachClass(B);

        Assertions.assertEquals(B, x.classes(true).findFirst().orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListOntClasses3a(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = TestModelFactory.createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));

        OntIndividual A = m.getOntClass(NS + "A").createIndividual("iA");
        OntIndividual B = m.getOntClass(NS + "B").createIndividual("iB");
        OntIndividual C = m.getOntClass(NS + "C").createIndividual("iC");
        OntIndividual D = m.getOntClass(NS + "D").createIndividual("iD");
        OntIndividual E = m.getOntClass(NS + "E").createIndividual("iE");
        OntIndividual F = m.getOntClass(NS + "F").createIndividual("iF");
        OntIndividual G = m.getOntClass(NS + "G").createIndividual("iG");

        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = A.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = A.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = B.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = C.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = D.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = D.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = E.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = E.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = F.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = F.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = G.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = G.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A"), directA);
        Assertions.assertEquals(Set.of("B"), directB);
        Assertions.assertEquals(Set.of("C", "F"), directC);
        Assertions.assertEquals(Set.of("D"), directD);
        Assertions.assertEquals(Set.of("E"), directE);
        Assertions.assertEquals(Set.of("C", "F"), directF);
        Assertions.assertEquals(Set.of("G"), directG);

        Assertions.assertEquals(Set.of("A"), indirectA);
        Assertions.assertEquals(Set.of("A", "B"), indirectB);
        Assertions.assertEquals(Set.of("A", "C", "F", "G"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "E", "F", "G"), indirectE);
        Assertions.assertEquals(Set.of("A", "C", "F", "G"), indirectF);
        Assertions.assertEquals(Set.of("G"), indirectG);
    }

    /* TODO
    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListOntClasses3b(TestSpec spec) {
        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E
        OntModel m = TestModelFactory.createClassesAGBCFDE(OntModelFactory.createModel(spec.inst));

        OntIndividual A = m.getOntClass(NS + "A").createIndividual("iA");
        OntIndividual B = m.getOntClass(NS + "B").createIndividual("iB");
        OntIndividual C = m.getOntClass(NS + "C").createIndividual("iC");
        OntIndividual D = m.getOntClass(NS + "D").createIndividual("iD");
        OntIndividual E = m.getOntClass(NS + "E").createIndividual("iE");
        OntIndividual F = m.getOntClass(NS + "F").createIndividual("iF");
        OntIndividual G = m.getOntClass(NS + "G").createIndividual("iG");

        Set<String> directA = A.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = A.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = B.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = C.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = D.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = D.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = E.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = E.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = F.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = F.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = G.classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = G.classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("A"), directA);
        Assertions.assertEquals(Set.of("B"), directB);
        Assertions.assertEquals(Set.of("C"), directC);
        Assertions.assertEquals(Set.of("D"), directD);
        Assertions.assertEquals(Set.of("E"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of("G"), directG);

        Assertions.assertEquals(Set.of("A"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A"), indirectD);
        Assertions.assertEquals(Set.of("A"), indirectE);
        Assertions.assertEquals(Set.of("A"), indirectF);
        Assertions.assertEquals(Set.of("G"), indirectG);
    }
    */
}
