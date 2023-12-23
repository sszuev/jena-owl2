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
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABC;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCD;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFBCF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesBCA;

public class OntClassSubClassesTest {

    private static Set<String> subClasses(OntModel m, String name, boolean direct) {
        return m.getResource(NS + name).as(OntClass.class).subClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> subClasses(OntModel m, String name) {
        return m.getResource(NS + name).as(OntClass.class).subClasses().map(Resource::getLocalName).collect(Collectors.toSet());
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
            "RDFS_MEM",
    })
    public void testSubClass0a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.subClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testSubClass0b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertEquals(a, a.subClass().orElseThrow());
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
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
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
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses2(TestSpec spec) {
        //    A
        //  / |
        // B  C
        //     \
        //      D

        OntModel m = createClassesABCD(OntModelFactory.createModel(spec.inst));
        OntClass A = m.getResource(NS + "A").as(OntClass.class);
        OntClass B = m.getResource(NS + "B").as(OntClass.class);
        OntClass C = m.getResource(NS + "C").as(OntClass.class);
        OntClass D = m.getResource(NS + "D").as(OntClass.class);

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
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_RDFS_INF",
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

        Assertions.assertEquals(Set.of("C", "B"), directA, "Wrong direct nodes for A");
        Assertions.assertEquals(Set.of("D", "E"), directB, "Wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "Wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "Wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "Wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "Wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "B", "D", "E", "F"), indirectA, "Wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), indirectB, "Wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), indirectC, "Wrong indirect nodes for C");
        Assertions.assertEquals(Set.of(), indirectD, "Wrong indirect nodes for D");
        Assertions.assertEquals(Set.of(), indirectE, "Wrong indirect nodes for E");
        Assertions.assertEquals(Set.of(), indirectF, "Wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
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

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("D", "E"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");

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
            "OWL1_MEM",
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

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("H", "G"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");
        Assertions.assertEquals(Set.of(), directG, "wrong direct nodes for G");
        Assertions.assertEquals(Set.of(), directH, "wrong direct nodes for H");
        Assertions.assertEquals(Set.of("M", "L"), directK, "wrong direct nodes for K");
        Assertions.assertEquals(Set.of(), directL, "wrong direct nodes for L");
        Assertions.assertEquals(Set.of(), directM, "wrong direct nodes for M");

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
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
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

        Assertions.assertEquals(Set.of("C", "B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("E", "D"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("H", "G", "K"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");
        Assertions.assertEquals(Set.of(), directG, "wrong direct nodes for G");
        Assertions.assertEquals(Set.of("M", "L"), directH, "wrong direct nodes for H");
        Assertions.assertEquals(Set.of("M", "L"), directK, "wrong direct nodes for K");
        Assertions.assertEquals(Set.of(), directL, "wrong direct nodes for L");
        Assertions.assertEquals(Set.of(), directM, "wrong direct nodes for M");

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "G", "H", "K", "L", "M"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("D", "E", "G", "H", "K", "L", "M"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("F", "E"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("G", "H", "K", "L", "M"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of(), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of(), indirectF, "wrong indirect nodes for F");
        Assertions.assertEquals(Set.of(), indirectG, "wrong indirect nodes for G");
        Assertions.assertEquals(Set.of("L", "M"), indirectH, "wrong indirect nodes for H");
        Assertions.assertEquals(Set.of("H", "L", "M"), indirectK, "wrong indirect nodes for K");
        Assertions.assertEquals(Set.of(), indirectL, "wrong indirect nodes for L");
        Assertions.assertEquals(Set.of(), indirectM, "wrong indirect nodes for M");
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
            "RDFS_MEM",
    })
    public void testListSubClasses5b(TestSpec spec) {
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

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of(), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
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

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("A"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("A"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of(), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("A", "C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("A", "B"), indirectC, "wrong indirect nodes for C");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
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

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("A"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
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

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListSubClasses7c(TestSpec spec) {
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

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of("A", "C"), directB);
        Assertions.assertEquals(Set.of("A", "B"), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("A", "C"), indirectB);
        Assertions.assertEquals(Set.of("A", "B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
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

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListSubClasses8b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Assertions.assertEquals(Set.of("B"), directA);
        Assertions.assertEquals(Set.of("A"), directB);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses9a(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of(), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directE, "wrong direct nodes for E");

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "D"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses9b(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of("B"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("A"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("B"), directE, "wrong direct nodes for E");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("A"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("B"), indirectE, "wrong indirect nodes for E");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListSubClasses9c(TestSpec spec) {
        //  A   B
        //  .\ /.
        //  . C .
        //  . | .
        //  . D .
        //  ./  .
        //  A   .   E
        //   \  .  |
        //    \ . /
        //      B
        OntModel m = TestModelFactory.createClassesABCDAEB(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of("B", "C", "D"), directA);
        Assertions.assertEquals(Set.of("A", "C", "D"), directB);
        Assertions.assertEquals(Set.of("A", "B", "D"), directC);
        Assertions.assertEquals(Set.of("A", "B", "C"), directD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), directE);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of("A", "C", "D"), indirectB);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C", "D"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses10a(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of("C", "E"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("E"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("F"), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of("C"), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "E"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses10b(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of(), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of(), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of(), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListSubClasses10c(TestSpec spec) {
        //      A       B
        //    /   \   / |
        //  /       C   |
        // |      / .   |
        // |    D   .   |
        // |  / |   .   |
        // E    |   .   |
        //   \  |   .   |
        //     F ...... F
        //       \  .
        //        \ .
        //          C
        OntModel m = createClassesABCDEFBCF(OntModelFactory.createModel(spec.inst));

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

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), directC, "wrong direct nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), directD, "wrong direct nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), directE, "wrong direct nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), directF, "wrong direct nodes for F");

        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C", "D", "E", "F"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("D", "E", "F"), indirectC, "wrong indirect nodes for C");
        Assertions.assertEquals(Set.of("C", "E", "F"), indirectD, "wrong indirect nodes for D");
        Assertions.assertEquals(Set.of("C", "D", "F"), indirectE, "wrong indirect nodes for E");
        Assertions.assertEquals(Set.of("C", "D", "E"), indirectF, "wrong indirect nodes for F");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSubClasses11a(TestSpec spec) {
        //    A
        //  /  \
        // B  = C
        OntModel m = createClassesABC(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of(), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of(), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("B"), indirectC, "wrong indirect nodes for C");
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testListSubClasses11b(TestSpec spec) {
        //    A
        //  /  \
        // B  = C
        OntModel m = createClassesABC(OntModelFactory.createModel(spec.inst));

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        Set<String> directC = subClasses(m, "C", true);
        Set<String> indirectC = subClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA, "wrong direct nodes for A");
        Assertions.assertEquals(Set.of("C"), directB, "wrong direct nodes for B");
        Assertions.assertEquals(Set.of("B"), directC, "wrong direct nodes for C");

        Assertions.assertEquals(Set.of("B", "C"), indirectA, "wrong indirect nodes for A");
        Assertions.assertEquals(Set.of("C"), indirectB, "wrong indirect nodes for B");
        Assertions.assertEquals(Set.of("B"), indirectC, "wrong indirect nodes for C");
    }

}
