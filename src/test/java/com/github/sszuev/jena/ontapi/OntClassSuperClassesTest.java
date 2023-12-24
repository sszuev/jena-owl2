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
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesBCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDBCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDBFCEA;

public class OntClassSuperClassesTest {
    private static Set<String> superClasses(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).superClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> superClasses(OntModel m, String name) {
        return m.getOntClass(NS + name).superClasses().map(Resource::getLocalName).collect(Collectors.toSet());
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testSuperClassNE(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.superClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses1a(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("B"), indirectD);
        Assertions.assertEquals(Set.of("B", "C"), indirectE);
        Assertions.assertEquals(Set.of("C"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSuperClasses1b(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B", "C"), directE);

        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectE);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "RDFS_MEM",
    })
    public void testListSuperClasses3a(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses3c(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of("C"), directB);
        Assertions.assertEquals(Set.of("B"), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSuperClasses3d(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);

        String RT = spec == TestSpec.RDFS_MEM_RDFS_INF ? "Resource" : "Thing";

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(RT), directB);
        Assertions.assertEquals(Set.of(RT), directC);

        Assertions.assertEquals(Set.of("B", "C", RT), indirectA);
        Assertions.assertEquals(Set.of("C", RT), indirectB);
        Assertions.assertEquals(Set.of("B", RT), indirectC);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL1_MEM_RDFS_INF",
    })
    public void testListSuperClasses5a(TestSpec spec) {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B");

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E");

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Set<String> directG = superClasses(m, "G", true);
        Set<String> indirectG = superClasses(m, "G", false);

        Set<String> directH = superClasses(m, "H", true);
        Set<String> indirectH = superClasses(m, "H", true);

        Set<String> directK = superClasses(m, "K", true);
        Set<String> indirectK = superClasses(m, "K");

        Set<String> directL = superClasses(m, "L", true);
        Set<String> indirectL = superClasses(m, "L", false);

        Set<String> directM = superClasses(m, "M", true);
        Set<String> indirectM = superClasses(m, "M");

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B"), directD);
        Assertions.assertEquals(Set.of("B", "C"), directE);
        Assertions.assertEquals(Set.of("C"), directF);
        Assertions.assertEquals(Set.of("D"), directG);
        Assertions.assertEquals(Set.of("D"), directH);
        Assertions.assertEquals(Set.of("D"), directK);
        Assertions.assertEquals(Set.of("H", "K"), directL);
        Assertions.assertEquals(Set.of("H", "K"), directM);

        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A", "B"), indirectD);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectE);
        Assertions.assertEquals(Set.of("A", "C"), indirectF);
        Assertions.assertEquals(Set.of("A", "B", "D"), indirectG);
        Assertions.assertEquals(Set.of("D"), indirectH);
        Assertions.assertEquals(Set.of("A", "B", "D", "H"), indirectK);
        Assertions.assertEquals(Set.of("A", "B", "D", "H", "K"), indirectL);
        Assertions.assertEquals(Set.of("A", "B", "D", "H", "K"), indirectM);
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses6a(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = createClassesDBCA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> directB = superClasses(m, "B", true);
        Set<String> directC = superClasses(m, "C", true);
        Set<String> directD = superClasses(m, "D", true);

        Set<String> indirectA = superClasses(m, "A");
        Set<String> indirectB = superClasses(m, "B", false);
        Set<String> indirectC = superClasses(m, "C", false);
        Set<String> indirectD = superClasses(m, "D", true);

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of("D"), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of("D"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
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
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses7a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass A = m.createOntClass(NS + "A");
        A.addSuperClass(A);

        Assertions.assertTrue(A.superClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.superClasses(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "OWL1_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses8a(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("C", "E"), indirectA);
        Assertions.assertEquals(Set.of("D"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("F"), indirectE);
        Assertions.assertEquals(Set.of("D"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testListSuperClasses8b(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of(), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F"), indirectA);
        Assertions.assertEquals(Set.of("D"), indirectB);
        Assertions.assertEquals(Set.of("B", "D"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
        Assertions.assertEquals(Set.of("D", "F"), indirectE);
        Assertions.assertEquals(Set.of("D"), indirectF);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM_RDFS_INF",
    })
    public void testListSuperClasses8g(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = createClassesDBFCEA(OntModelFactory.createModel(spec.inst));

        Set<String> directA = superClasses(m, "A", true);
        Set<String> indirectA = superClasses(m, "A", false);

        Set<String> directB = superClasses(m, "B", true);
        Set<String> indirectB = superClasses(m, "B", false);

        Set<String> directC = superClasses(m, "C", true);
        Set<String> indirectC = superClasses(m, "C", false);

        Set<String> directD = superClasses(m, "D", true);
        Set<String> indirectD = superClasses(m, "D", false);

        Set<String> directE = superClasses(m, "E", true);
        Set<String> indirectE = superClasses(m, "E", false);

        Set<String> directF = superClasses(m, "F", true);
        Set<String> indirectF = superClasses(m, "F", false);

        Assertions.assertEquals(Set.of("C", "E"), directA);
        Assertions.assertEquals(Set.of("D"), directB);
        Assertions.assertEquals(Set.of("B"), directC);
        Assertions.assertEquals(Set.of("Resource"), directD);
        Assertions.assertEquals(Set.of("F"), directE);
        Assertions.assertEquals(Set.of("D"), directF);

        Assertions.assertEquals(Set.of("B", "C", "D", "E", "F", "Resource"), indirectA);
        Assertions.assertEquals(Set.of("D", "Resource"), indirectB);
        Assertions.assertEquals(Set.of("B", "D", "Resource"), indirectC);
        Assertions.assertEquals(Set.of("Resource"), indirectD);
        Assertions.assertEquals(Set.of("D", "F", "Resource"), indirectE);
        Assertions.assertEquals(Set.of("D", "Resource"), indirectF);
    }
}
