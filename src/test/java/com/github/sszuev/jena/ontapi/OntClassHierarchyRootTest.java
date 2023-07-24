package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.github.sszuev.jena.ontapi.TestModelFactory.NS;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDGCFKBEHAG;

public class OntClassHierarchyRootTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testIsHierarchyRoot1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        Assertions.assertTrue(m.getOWLThing().isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testIsHierarchyRoot2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        Assertions.assertFalse(m.getOWLNothing().isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testIsHierarchyRoot4(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass a = m.createOntClass(NS + "A");
        OntClass b = m.createOntClass(NS + "B");
        a.addSubClass(b);
        Assertions.assertTrue(a.isHierarchyRoot());
        Assertions.assertFalse(b.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
    })
    public void testIsHierarchyRoot5(TestSpec spec) {
        // D  THING    G
        // |    |    / .
        // C    F   K  .
        // |    |   |  .
        // B    E   H  .
        // |         \ .
        // A           G
        OntModel m = createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.spec));
        OntClass Thing = OWL.Thing.inModel(m).as(OntClass.class);
        OntClass Nothing = OWL.Nothing.inModel(m).as(OntClass.class);
        m.getOntClass(NS + "F").addSuperClass(Thing);

        Assertions.assertFalse(m.getOntClass(NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "F").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "G").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "H").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "K").isHierarchyRoot());
        Assertions.assertTrue(Thing.isHierarchyRoot());
        Assertions.assertFalse(Nothing.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testIsHierarchyRoot6(TestSpec spec) {
        // D  THING    G
        // |    |    / .
        // C    F   K  .
        // |    |   |  .
        // B    E   H  .
        // |         \ .
        // A           G
        OntModel m = createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.spec));
        OntClass Thing = OWL.Thing.inModel(m).as(OntClass.class);
        OntClass Nothing = OWL.Nothing.inModel(m).as(OntClass.class);
        m.getOntClass(NS + "F").addSuperClass(Thing);

        Assertions.assertFalse(m.getOntClass(NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "F").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "G").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "H").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "K").isHierarchyRoot());
        Assertions.assertTrue(Thing.isHierarchyRoot());
        Assertions.assertFalse(Nothing.isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testIsHierarchyRoot7(TestSpec spec) {
        // D        G
        // |      / .
        // C  F  K  .
        // |  |  |  .
        // B  E  H  .
        // |      \ .
        // A        G
        OntModel m = createClassesDGCFKBEHAG(TestModelFactory.createClassesDGCFKBEHAG(OntModelFactory.createModel(spec.spec)));

        Assertions.assertFalse(m.getOntClass(NS + "A").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "B").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "C").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "D").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "E").isHierarchyRoot());
        Assertions.assertTrue(m.getOntClass(NS + "F").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "G").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "H").isHierarchyRoot());
        Assertions.assertFalse(m.getOntClass(NS + "K").isHierarchyRoot());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testIsHierarchyRoot8(TestSpec spec) {
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

        boolean isHierarchyRootA = m.getOntClass(NS + "A").isHierarchyRoot();
        boolean isHierarchyRootB = m.getOntClass(NS + "B").isHierarchyRoot();
        boolean isHierarchyRootC = m.getOntClass(NS + "C").isHierarchyRoot();
        boolean isHierarchyRootD = m.getOntClass(NS + "D").isHierarchyRoot();
        boolean isHierarchyRootE = m.getOntClass(NS + "E").isHierarchyRoot();
        boolean isHierarchyRootF = m.getOntClass(NS + "F").isHierarchyRoot();
        boolean isHierarchyRootG = m.getOntClass(NS + "G").isHierarchyRoot();
        boolean isHierarchyRootH = m.getOntClass(NS + "H").isHierarchyRoot();
        boolean isHierarchyRootK = m.getOntClass(NS + "K").isHierarchyRoot();
        boolean isHierarchyRootL = m.getOntClass(NS + "L").isHierarchyRoot();
        boolean isHierarchyRootM = m.getOntClass(NS + "M").isHierarchyRoot();

        Assertions.assertTrue(isHierarchyRootA);
        Assertions.assertFalse(isHierarchyRootB);
        Assertions.assertFalse(isHierarchyRootC);
        Assertions.assertFalse(isHierarchyRootD);
        Assertions.assertFalse(isHierarchyRootE);
        Assertions.assertFalse(isHierarchyRootF);
        Assertions.assertFalse(isHierarchyRootG);
        Assertions.assertFalse(isHierarchyRootH);
        Assertions.assertTrue(isHierarchyRootK);
        Assertions.assertFalse(isHierarchyRootL);
        Assertions.assertFalse(isHierarchyRootM);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testIsHierarchyRoot9(TestSpec spec) {
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

        boolean isHierarchyRootA = m.getOntClass(NS + "A").isHierarchyRoot();
        boolean isHierarchyRootB = m.getOntClass(NS + "B").isHierarchyRoot();
        boolean isHierarchyRootC = m.getOntClass(NS + "C").isHierarchyRoot();
        boolean isHierarchyRootD = m.getOntClass(NS + "D").isHierarchyRoot();
        boolean isHierarchyRootE = m.getOntClass(NS + "E").isHierarchyRoot();
        boolean isHierarchyRootF = m.getOntClass(NS + "F").isHierarchyRoot();
        boolean isHierarchyRootG = m.getOntClass(NS + "G").isHierarchyRoot();
        boolean isHierarchyRootH = m.getOntClass(NS + "H").isHierarchyRoot();
        boolean isHierarchyRootK = m.getOntClass(NS + "K").isHierarchyRoot();
        boolean isHierarchyRootL = m.getOntClass(NS + "L").isHierarchyRoot();
        boolean isHierarchyRootM = m.getOntClass(NS + "M").isHierarchyRoot();

        Assertions.assertTrue(isHierarchyRootA);
        Assertions.assertFalse(isHierarchyRootB);
        Assertions.assertFalse(isHierarchyRootC);
        Assertions.assertFalse(isHierarchyRootD);
        Assertions.assertFalse(isHierarchyRootE);
        Assertions.assertFalse(isHierarchyRootF);
        Assertions.assertFalse(isHierarchyRootG);
        Assertions.assertFalse(isHierarchyRootH);
        Assertions.assertFalse(isHierarchyRootK);
        Assertions.assertFalse(isHierarchyRootL);
        Assertions.assertFalse(isHierarchyRootM);
    }

}
