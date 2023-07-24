package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.sszuev.jena.ontapi.TestModelFactory.NS;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesBCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDBCA;

public class OntClassIndividualsTest {

    private static Set<String> individuals(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).individuals(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    @Test
    public void testRemoveIndividual() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        Resource i1 = m.createResource(":I1", c1).addProperty(RDF.type, OWL.NamedIndividual);
        m.createResource(":I2", c2).addProperty(RDF.type, OWL.NamedIndividual);
        Assertions.assertEquals(2, m.individuals().count());

        Assertions.assertSame(c1, c1.removeIndividual(i1));
        Assertions.assertEquals(0, c1.individuals().count());
        Assertions.assertEquals(1, m.individuals().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListIndividuals1(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.spec));
        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");
        OntClass E = m.getOntClass(NS + "E");
        OntClass F = m.getOntClass(NS + "F");

        A.createIndividual(NS + "iA");
        B.createIndividual(NS + "iB");
        C.createIndividual(NS + "iC");
        D.createIndividual(NS + "iD");
        E.createIndividual(NS + "iE");

        Set<String> directA = A.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = A.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = B.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = C.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = D.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = D.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = E.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = E.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = F.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = F.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iE"), directE);
        Assertions.assertEquals(Set.of(), directF);
        Assertions.assertEquals(Set.of("iA", "iB", "iC", "iD", "iE"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iD", "iE"), indirectB);
        Assertions.assertEquals(Set.of("iE", "iC"), indirectC);
        Assertions.assertEquals(Set.of("iD"), indirectD);
        Assertions.assertEquals(Set.of("iE"), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {"OWL2_DL_MEM_RDFS_BUILTIN_INF"})
    public void testListIndividuals2(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.spec));
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB", "iC"), directB);
        Assertions.assertEquals(Set.of("iB", "iC"), directC);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {"OWL2_DL_MEM_RDFS_BUILTIN_INF"})
    public void testListIndividuals3(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = createClassesDBCA(OntModelFactory.createModel(spec.spec));
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iC"), indirectC);
        Assertions.assertEquals(Set.of("iA", "iC", "iD"), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListInstances3(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.spec));
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = individuals(m, "A", true);
        Set<String> indirectA = individuals(m, "A", false);

        Set<String> directB = individuals(m, "B", true);
        Set<String> indirectB = individuals(m, "B", false);

        Set<String> directC = individuals(m, "C", true);
        Set<String> indirectC = individuals(m, "C", false);

        Set<String> directD = individuals(m, "D", true);
        Set<String> indirectD = individuals(m, "D", false);

        Set<String> directE = individuals(m, "E", true);
        Set<String> indirectE = individuals(m, "E", false);

        Set<String> directF = individuals(m, "F", true);
        Set<String> indirectF = individuals(m, "F", false);

        Set<String> directG = individuals(m, "G", true);
        Set<String> indirectG = individuals(m, "G", false);

        Set<String> directH = individuals(m, "H", true);
        Set<String> indirectH = individuals(m, "H", true);

        Set<String> directK = individuals(m, "K", true);
        Set<String> indirectK = individuals(m, "K", false);

        Set<String> directL = individuals(m, "L", true);
        Set<String> indirectL = individuals(m, "L", false);

        Set<String> directM = individuals(m, "M", true);
        Set<String> indirectM = individuals(m, "M", false);

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iE"), directE);
        Assertions.assertEquals(Set.of("iF"), directF);
        Assertions.assertEquals(Set.of("iG"), directG);
        Assertions.assertEquals(Set.of("iK", "iH"), directH);
        Assertions.assertEquals(Set.of("iK", "iH"), directK);
        Assertions.assertEquals(Set.of("iL"), directL);
        Assertions.assertEquals(Set.of("iM"), directM);

        Assertions.assertEquals(Set.of("iA", "iB", "iC", "iD", "iE", "iF", "iG", "iH", "iK", "iL", "iM"), indirectA);
        Assertions.assertEquals(Set.of("iB", "iD", "iE", "iG", "iH", "iK", "iL", "iM"), indirectB);
        Assertions.assertEquals(Set.of("iC", "iE", "iF"), indirectC);
        Assertions.assertEquals(Set.of("iD", "iG", "iH", "iK", "iL", "iM"), indirectD);
        Assertions.assertEquals(Set.of("iE"), indirectE);
        Assertions.assertEquals(Set.of("iF"), indirectF);
        Assertions.assertEquals(Set.of("iG"), indirectG);
        Assertions.assertEquals(Set.of("iK", "iH"), indirectH);
        Assertions.assertEquals(Set.of("iH", "iK", "iL", "iM"), indirectK);
        Assertions.assertEquals(Set.of("iL"), indirectL);
        Assertions.assertEquals(Set.of("iM"), indirectM);
    }

}
