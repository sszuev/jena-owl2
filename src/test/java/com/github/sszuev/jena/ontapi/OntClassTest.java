package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.sszuev.jena.ontapi.TestModelFactory.NS;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEF;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesABCDEFGHKLM;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesBCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDBCA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDBFCEA;
import static com.github.sszuev.jena.ontapi.TestModelFactory.createClassesDGCFKBEHAG;

public class OntClassTest {

    private static Set<String> subClasses(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).subClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> superClasses(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).superClasses(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> subClasses(OntModel m, String name) {
        return m.getOntClass(NS + name).subClasses().map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> superClasses(OntModel m, String name) {
        return m.getOntClass(NS + name).superClasses().map(Resource::getLocalName).collect(Collectors.toSet());
    }

    private static Set<String> individuals(OntModel m, String name, boolean direct) {
        return m.getOntClass(NS + name).individuals(direct).map(Resource::getLocalName).collect(Collectors.toSet());
    }

    @Test
    public void testCreateCardinalityRestrictions() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c = m.createOntClass("C");
        OntObjectProperty op = m.createObjectProperty("OP");
        OntDataProperty dp = m.createDataProperty("DP");

        OntClass.ObjectCardinality r1 = m.createObjectCardinality(op, 12, c);
        OntClass.DataMinCardinality r2 = m.createDataMinCardinality(dp, 1, null);
        OntClass.DataMaxCardinality r3 = m.createDataMaxCardinality(dp, 2, m.getRDFSLiteral());
        OntClass.ObjectMinCardinality r4 = m.createObjectMinCardinality(op, 12, m.getOWLThing());
        OntClass.CardinalityRestrictionCE<?, ?> r5 = m.createDataCardinality(dp, 0, m.getDatatype(XSD.xstring));

        Assertions.assertTrue(r1.isQualified());
        Assertions.assertFalse(r2.isQualified());
        Assertions.assertFalse(r3.isQualified());
        Assertions.assertFalse(r4.isQualified());
        Assertions.assertTrue(r5.isQualified());
        long size = m.size();

        Assertions.assertThrows(OntJenaException.IllegalArgument.class, () -> m.createObjectMaxCardinality(op, -12, c));
        Assertions.assertEquals(size, m.size());
    }

    @Test
    public void testListClassHierarchy() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        OntClass c = m.createOntClass("C");
        OntClass d = m.createOntClass("D");
        OntClass e = m.createOntClass("E");
        e.addSuperClass(d);
        a.addSuperClass(b).addSuperClass(c);
        b.addSuperClass(m.createObjectComplementOf(b)).addSuperClass(d);


        Assertions.assertEquals(2, a.superClasses(true).count());
        Assertions.assertEquals(4, a.superClasses(false).count());
        Assertions.assertEquals(2, d.subClasses(true).count());
        Assertions.assertEquals(3, d.subClasses(false).count());
    }

    @Test
    public void testClassExpressionSubClassOf() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        OntClass c = m.createOntClass("C");
        Assertions.assertNotNull(a.addSubClassOfStatement(b));
        Assertions.assertSame(a, a.addSuperClass(c).addSuperClass(m.getOWLThing()).removeSuperClass(b));
        Assertions.assertEquals(2, a.superClasses().count());
        Assertions.assertSame(a, a.removeSuperClass(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testClassExpressionDisjointWith() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        OntClass c = m.createOntClass("C");
        Assertions.assertNotNull(a.addDisjointWithStatement(b));
        Assertions.assertSame(a, a.addDisjointClass(c).addDisjointClass(m.getOWLThing()).removeDisjointClass(b));
        Assertions.assertEquals(2, a.disjointClasses().count());
        Assertions.assertSame(a, a.removeDisjointClass(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testClassExpressionEquivalentClass() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named a = m.createOntClass("A");
        OntClass.Named b = m.createOntClass("B");
        OntClass.Named c = m.createOntClass("C");
        Assertions.assertNotNull(a.addEquivalentClassStatement(b));
        Assertions.assertSame(a, a.addEquivalentClass(c).addEquivalentClass(m.getOWLThing()).removeEquivalentClass(b));
        Assertions.assertEquals(2, a.equivalentClasses().count());
        Assertions.assertSame(a, a.removeEquivalentClass(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testHasKeys() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntObjectProperty o1 = m.createObjectProperty("O1");
        OntObjectProperty o2 = m.createObjectProperty("O2");
        OntDataProperty d1 = m.createDataProperty("D1");
        OntDataProperty d2 = m.createDataProperty("D2");
        OntClass.Named c = m.getOWLThing();
        Assertions.assertNotNull(c.addHasKeyStatement());
        Assertions.assertSame(c, c.addHasKey());
        Assertions.assertEquals(1, c.hasKeys().count());

        Assertions.assertEquals(0, c.fromHasKey().count());
        Assertions.assertSame(c, c.addHasKey(o1, d1).addHasKey(Arrays.asList(o1, o2), Collections.singletonList(d2)));
        Assertions.assertEquals(3, c.hasKeys().count());
        Assertions.assertEquals(4, c.fromHasKey().count());
        Assertions.assertSame(c, c.clearHasKeys());
        Assertions.assertEquals(4, m.size());
    }

    @Test
    public void testClassExpressionComponents() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass("C1");
        OntClass c2 = m.createOntClass("C2");
        OntClass c3 = m.createOntClass("C3");
        OntClass c4 = m.createOntClass("C4");
        OntIndividual i1 = c1.createIndividual();
        OntIndividual i2 = c2.createIndividual("I2");
        OntIndividual i3 = c1.createIndividual();
        OntIndividual i4 = c4.createIndividual("I4");

        List<OntIndividual> list1 = Arrays.asList(i1, i2, i3);
        OntClass.OneOf e1 = m.createObjectOneOf(list1);
        Assertions.assertEquals(list1, e1.getList().members().collect(Collectors.toList()));
        Assertions.assertSame(e1, e1.setComponents(i1, i4));
        Assertions.assertEquals(Arrays.asList(i1, i4), e1.getList().members().collect(Collectors.toList()));

        List<OntClass> list2 = Arrays.asList(c3, c4);
        OntClass.UnionOf e2 = m.createObjectUnionOf(list2);
        Assertions.assertEquals(2, e2.getList().members().count());
        Assertions.assertTrue(e2.setComponents().getList().isEmpty());

        OntClass.IntersectionOf e3 = m.createObjectIntersectionOf(list2);
        Assertions.assertEquals(3, e3.setComponents(Arrays.asList(c1, c2, m.getOWLThing())).getList().members().count());

        Set<RDFNode> expected = new HashSet<>(Arrays.asList(i1, i4, c1, c2, m.getOWLThing()));
        Set<RDFNode> actual = m.ontObjects(OntClass.ComponentsCE.class)
                .map(x -> x.getList())
                .map(x -> x.as(RDFList.class))
                .map(RDFList::asJavaList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testListDisjoints() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass("C1");
        OntClass c2 = m.createOntClass("C2");
        OntClass c3 = m.createOntClass("C3");
        OntClass c4 = m.createOntClass("C4");
        m.createDisjointClasses(c1, c2);
        m.createDisjointClasses(c1, c3);

        Assertions.assertEquals(0, c4.disjoints().count());
        Assertions.assertEquals(2, c1.disjoints().count());
        Assertions.assertEquals(1, c2.disjoints().count());
        Assertions.assertEquals(1, c3.disjoints().count());
    }

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

    @Test
    public void testHasDeclaredProperties() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createObjectUnionOf(m.getOWLThing());
        OntClass c2 = m.createObjectOneOf(m.createIndividual("i1"), m.createIndividual("i2"));
        OntClass c3 = m.createOntClass("C3"); // root
        OntClass c4 = m.createOntClass("C4"); // root
        OntClass c5 = m.createOntClass("C5"); // root

        c1.addSuperClass(c2.addSuperClass(c3));
        c5.addSuperClass(m.getOWLThing());
        OntDataProperty d1 = m.createDataProperty("d1").addDomain(c1);
        OntDataProperty d2 = m.createDataProperty("d2").addDomain(c2);
        OntObjectProperty o1 = m.createObjectProperty("o1").addDomain(c3);
        OntObjectProperty o2 = m.createObjectProperty("o2"); // global

        Assertions.assertFalse(c1.hasDeclaredProperty(m.getOWLBottomDataProperty(), true));
        Assertions.assertFalse(c4.hasDeclaredProperty(m.getOWLTopObjectProperty(), false));

        Assertions.assertTrue(c1.hasDeclaredProperty(d1, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(o2, false));
        Assertions.assertTrue(c1.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(o1, true));
        Assertions.assertFalse(c1.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c2.hasDeclaredProperty(d1, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c2.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c2.hasDeclaredProperty(d1, true));
        Assertions.assertTrue(c2.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c2.hasDeclaredProperty(o1, true));
        Assertions.assertFalse(c2.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c3.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c3.hasDeclaredProperty(d2, false));
        Assertions.assertTrue(c3.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c3.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c3.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c3.hasDeclaredProperty(d2, true));
        Assertions.assertTrue(c3.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c3.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c4.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(d2, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c4.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c4.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c4.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c4.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c4.hasDeclaredProperty(o2, true));

        Assertions.assertFalse(c5.hasDeclaredProperty(d1, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(d2, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(o1, false));
        Assertions.assertTrue(c5.hasDeclaredProperty(o2, false));
        Assertions.assertFalse(c5.hasDeclaredProperty(d1, true));
        Assertions.assertFalse(c5.hasDeclaredProperty(d2, true));
        Assertions.assertFalse(c5.hasDeclaredProperty(o1, true));
        Assertions.assertTrue(c5.hasDeclaredProperty(o2, true));
    }

    @Test
    public void testListDeclaredProperties() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass("A");
        OntClass B = m.createOntClass("B");
        OntClass C = m.createOntClass("C");
        OntClass D = m.createOntClass("D");
        OntClass E = m.createOntClass("E");
        A.addSuperClass(B);
        B.addSuperClass(C);
        C.addSuperClass(D);
        E.addSuperClass(m.getOWLThing());

        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntObjectProperty o1 = m.createObjectProperty("o1");
        OntObjectProperty o2 = m.createObjectProperty("o2");
        o1.addSuperProperty(o2);
        o2.addSuperProperty(m.getOWLTopObjectProperty());
        d1.addDomain(A);
        d2.addDomain(B);
        o1.addDomain(C);
        o2.addDomain(m.getOWLThing());
        m.getOWLBottomDataProperty().addDomain(m.getOWLNothing());

        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), m.getOWLNothing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLNothing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d1), A.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d1, d2, o1, o2), A.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d2), B.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d2, o1, o2), B.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o1), C.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, o2), C.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), D.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), D.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), E.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), E.declaredProperties(false).collect(Collectors.toSet()));
    }

    @Test
    public void testIsDisjoint() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntObjectProperty p = m.createObjectProperty(":P");
        c1.addDisjointClass(c2);
        m.createDisjointClasses(c1, c3, c4);

        Assertions.assertTrue(c1.isDisjoint(c2));
        Assertions.assertTrue(c2.isDisjoint(c1));
        Assertions.assertTrue(c1.isDisjoint(c3));
        Assertions.assertTrue(c3.isDisjoint(c1));
        Assertions.assertTrue(c1.isDisjoint(c4));
        Assertions.assertTrue(c4.isDisjoint(c1));

        Assertions.assertFalse(c1.isDisjoint(c5));
        Assertions.assertFalse(c5.isDisjoint(c1));
        Assertions.assertFalse(c1.isDisjoint(p));
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
            "OWL2_MEM",
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "RDFS_MEM",
    })
    public void testListSubClasses1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
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
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass a = m.createOntClass(NS + "A");
        OntClass b = m.createOntClass(NS + "B");
        OntClass c = m.createOntClass(NS + "C");
        OntClass d = m.createOntClass(NS + "D");
        a.addSubClass(b);
        a.addSubClass(c);
        c.addSubClass(d);

        Assertions.assertEquals(Set.of(b, c), a.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(b, c), a.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(b, c), a.subClasses(false).collect(Collectors.toSet()));

        a.addSubClass(d);
        Assertions.assertEquals(Set.of(b, c, d), a.subClasses().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(b, c), a.subClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(b, c, d), a.subClasses(false).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSubClasses3Inf(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F
        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.spec));

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
    public void testListSubClasses3NoInf(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.spec));

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
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSubClasses4Inf(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.spec));

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
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses4NoInf(TestSpec spec) {
        //      A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createClassesABCDEFGHKLM(OntModelFactory.createModel(spec.spec));

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
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSubClasses5(TestSpec spec) {
        //     A
        //     |
        // D = B = C

        OntModel m = OntModelFactory.createModel(spec.spec);
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
    public void testListSubClasses6(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.spec));

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
    public void testListSubClasses11(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.spec));

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
    public void testListSubClasses12(TestSpec spec) {
        //    A
        //  / .
        // B  .
        // |  .
        // C  .
        //  \ .
        //    A

        OntModel m = createClassesABCA(OntModelFactory.createModel(spec.spec));

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
    public void testListSubClasses15(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        A.addSubClass(B);
        B.addSubClass(A);

        Set<String> directA = subClasses(m, "A", true);
        Set<String> indirectA = subClasses(m, "A", false);

        Set<String> directB = subClasses(m, "B", true);
        Set<String> indirectB = subClasses(m, "B", false);

        System.out.println("DIRECT-A::" + directA);
        System.out.println("DIRECT-B::" + directB);

        System.out.println("INDIRECT-A::" + indirectA);
        System.out.println("INDIRECT-B::" + indirectB);

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of(), directB);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testSuperClassNE(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.superClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testSubClassNE(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass a = m.createOntClass(NS + "A");
        Assertions.assertTrue(a.subClass().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.spec);
        OntClass A = m.createOntClass(NS + "A");
        A.addSuperClass(A);

        Assertions.assertTrue(A.superClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.superClasses(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSuperClasses2(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.spec));

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
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses3NoInf(TestSpec spec) {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        OntModel m = createClassesABCDEF(OntModelFactory.createModel(spec.spec));

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
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses4(TestSpec spec) {
        // B = C
        //  \ |
        //    A

        OntModel m = createClassesBCA(OntModelFactory.createModel(spec.spec));

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
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
    })
    public void testListSuperClasses5Inf(TestSpec spec) {
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
            "RDFS_MEM",
    })
    public void testListSuperClasses6(TestSpec spec) {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A

        OntModel m = createClassesDBCA(OntModelFactory.createModel(spec.spec));

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
            "OWL2_MEM",
            "RDFS_MEM",
    })
    public void testListSuperClasses9(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = createClassesDBFCEA(OntModelFactory.createModel(spec.spec));

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
    })
    public void testListSuperClasses10(TestSpec spec) {
        //    D
        //  /  \
        // B    F
        // |    |
        // C    E
        //  \  /
        //    A

        OntModel m = createClassesDBFCEA(OntModelFactory.createModel(spec.spec));

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
