package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.sszuev.jena.ontapi.TestModelFactory.NS;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCD;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesBCA;

public class OntClassSubClassesTest {
    private static Set<String> subClasses(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).subClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> subClasses(OntModel m, String name) {
        return m.getOntClass(NS + name).subClasses().map(Resource::getLocalName).collect(Collectors.toSet());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testSubClassNE(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.subClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "RDFS_MEM",
    })
    public void testListSubClasses1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        A.addSubClass(A);

        Assertions.assertTrue(A.subClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.subClasses(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses2(TestSpec spec) {
        //    A
        //  / |
        // B  C
        //     \
        //      D

        OntModel m = createClassesABCD(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getOntClass(NS + "A");
        OntClass B = m.getOntClass(NS + "B");
        OntClass C = m.getOntClass(NS + "C");
        OntClass D = m.getOntClass(NS + "D");

        Assertions.assertEquals(Set.of(B, C), A.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(false).collect(Collectors.toSet()));

        A.addSubClass(D);
        Assertions.assertEquals(Set.of(B, C, D), A.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C), A.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(B, C, D), A.subClasses(false).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSubClasses3a(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C");

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E");

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "B"), directA);
        Assertions.assertEquals(Set.of("D", "E"), directB);
        Assertions.assertEquals(Set.of("F", "E"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of(), directF);

        Assertions.assertEquals(Set.of("C", "B", "D", "E", "F"), indirectA);
        Assertions.assertEquals(Set.of("E", "D"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses3b(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C");

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "B"), directA);
        Assertions.assertEquals(Set.of("D", "E"), directB);
        Assertions.assertEquals(Set.of("F", "E"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of(), directF);

        Assertions.assertEquals(Set.of("C", "B"), indirectA);
        Assertions.assertEquals(Set.of("E", "D"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses4a(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Set<String> directG = subClasses(m, "G", true);
        Set<String> indirectG = subClasses(m, "G", false);

        Set<String> directH = subClasses(m, "H", true);
        Set<String> indirectH = subClasses(m, "H", true);

        Set<String> directK = subClasses(m, "K", true);
        Set<String> indirectK = subClasses(m, "K", false);

        Set<String> directL = subClasses(m, "L", true);
        Set<String> indirectL = subClasses(m, "L", false);

        Set<String> directM = subClasses(m, "M", true);
        Set<String> indirectM = subClasses(m, "M", false);

        Assertions.assertEquals(Set.of("C", "B"), directA);
        Assertions.assertEquals(Set.of("E", "D"), directB);
        Assertions.assertEquals(Set.of("F", "E"), directC);
        Assertions.assertEquals(Set.of("H", "G"), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of(), directF);
        Assertions.assertEquals(Set.of(), directG);
        Assertions.assertEquals(Set.of(), directH);
        Assertions.assertEquals(Set.of("M", "L"), directK);
        Assertions.assertEquals(Set.of(), directL);
        Assertions.assertEquals(Set.of(), directM);

        Assertions.assertEquals(Set.of("C", "B", "D"), indirectA);
        Assertions.assertEquals(Set.of("E", "D"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of("H", "G"), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
        Assertions.assertEquals(Set.of(), indirectG);
        Assertions.assertEquals(Set.of(), indirectH);
        Assertions.assertEquals(Set.of("M", "L", "H"), indirectK);
        Assertions.assertEquals(Set.of(), indirectL);
        Assertions.assertEquals(Set.of(), indirectM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSubClasses4b(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Set<String> directD = subClasses(m, "D", true);
        Set<String> indirectD = subClasses(m, "D", false);

        Set<String> directE = subClasses(m, "E", true);
        Set<String> indirectE = subClasses(m, "E", false);

        Set<String> directF = subClasses(m, "F", true);
        Set<String> indirectF = subClasses(m, "F", false);

        Set<String> directG = subClasses(m, "G", true);
        Set<String> indirectG = subClasses(m, "G", false);

        Set<String> directH = subClasses(m, "H", true);
        Set<String> indirectH = subClasses(m, "H", true);

        Set<String> directK = subClasses(m, "K", true);
        Set<String> indirectK = subClasses(m, "K", false);

        Set<String> directL = subClasses(m, "L", true);
        Set<String> indirectL = subClasses(m, "L", false);

        Set<String> directM = subClasses(m, "M", true);
        Set<String> indirectM = subClasses(m, "M", false);

        Assertions.assertEquals(Set.of("C", "B"), directA);
        Assertions.assertEquals(Set.of("E", "D"), directB);
        Assertions.assertEquals(Set.of("F", "E"), directC);
        Assertions.assertEquals(Set.of("H", "G", "K"), directD);
        Assertions.assertEquals(Set.of(), directE);
        Assertions.assertEquals(Set.of(), directF);
        Assertions.assertEquals(Set.of(), directG);
        Assertions.assertEquals(Set.of("M", "L"), directH);
        Assertions.assertEquals(Set.of("M", "L"), directK);
        Assertions.assertEquals(Set.of(), directL);
        Assertions.assertEquals(Set.of(), directM);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "G", "H", "K", "L", "M"), indirectA);
        Assertions.assertEquals(Set.of("D", "E", "G", "H", "K", "L", "M"), indirectB);
        Assertions.assertEquals(Set.of("F", "E"), indirectC);
        Assertions.assertEquals(Set.of("G", "H", "K", "L", "M"), indirectD);
        Assertions.assertEquals(Set.of(), indirectE);
        Assertions.assertEquals(Set.of(), indirectF);
        Assertions.assertEquals(Set.of(), indirectG);
        Assertions.assertEquals(Set.of("L", "M"), indirectH);
        Assertions.assertEquals(Set.of("H", "L", "M"), indirectK);
        Assertions.assertEquals(Set.of(), indirectL);
        Assertions.assertEquals(Set.of(), indirectM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses5a(TestSpec spec) {
        //     A
        //     |
        // D = B = C

        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        A.addSubClass(B);
        B.addEquivalentClass(C);
        D.addEquivalentClass(B);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);
        Set<String> directD = subClasses(m, "D", true);

        Set<String> indirectA = subClasses(m, "A");
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);
        Set<String> indirectD = subClasses(m, "D", false);

        Assertions.assertEquals(Set.of("B"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of(), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses6a(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A");
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);

        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses7a(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B"), directA);
        Assertions.assertEquals(Set.of("C"), directB);
        Assertions.assertEquals(Set.of("A"), directC);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSubClasses7b(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> directB = subClasses(m, "B", true);
        Set<String> directC = subClasses(m, "C", true);

        Set<String> indirectA = subClasses(m, "A", false);
        Set<String> indirectB = subClasses(m, "B", false);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses8a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of(), directB);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
    }

}
