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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntClassTest {
    private static final String NS = "http://example.com/test#";

    private static OntModel createABCDEFGHKLMModel(OntModel m) {
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");
        OntClass G = m.createOntClass(NS + "G");
        OntClass H = m.createOntClass(NS + "H");
        OntClass K = m.createOntClass(NS + "K");
        OntClass L = m.createOntClass(NS + "L");
        OntClass M = m.createOntClass(NS + "M");

        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        A.addSubClass(D);
        A.addSubClass(B);
        A.addSubClass(C);

        B.addSubClass(D);
        B.addSubClass(E);

        C.addSubClass(E);
        C.addSubClass(F);

        D.addSubClass(G);
        D.addSubClass(H);

        H.addSubClass(K);

        K.addSubClass(H);
        K.addSubClass(L);
        K.addSubClass(M);
        return m;
    }

    private static OntModel createABCDEFModel(OntModel m) {
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        OntClass E = m.createOntClass(NS + "E");
        OntClass F = m.createOntClass(NS + "F");

        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        A.addSubClass(B);
        A.addSubClass(C);
        B.addSubClass(D);
        B.addSubClass(E);
        C.addSubClass(E);
        C.addSubClass(F);
        return m;
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

    @Test
    public void testIsHierarchyRoot1() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass a = m.createOntClass(NS + "A");
        OntClass b = m.createOntClass(NS + "B");
        a.addSubClass(b);
        Assertions.assertTrue(a.isHierarchyRoot());
        Assertions.assertFalse(b.isHierarchyRoot());
    }

    @Test
    public void testIsHierarchyRoot2() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");
        OntClass c8 = m.createOntClass(":C8");
        OntClass c9 = m.createOntClass(":C9");
        OntClass c10 = m.getOWLThing();
        OntClass c11 = m.getOWLNothing();

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c10);
        c7.addSuperClass(c8);
        c8.addSuperClass(c9);
        c9.addSuperClass(c7);

        Assertions.assertFalse(c1.isHierarchyRoot());
        Assertions.assertFalse(c2.isHierarchyRoot());
        Assertions.assertFalse(c3.isHierarchyRoot());
        Assertions.assertTrue(c4.isHierarchyRoot());
        Assertions.assertFalse(c5.isHierarchyRoot());
        Assertions.assertTrue(c6.isHierarchyRoot());
        Assertions.assertTrue(c7.isHierarchyRoot());
        Assertions.assertTrue(c8.isHierarchyRoot());
        Assertions.assertTrue(c9.isHierarchyRoot());
        Assertions.assertTrue(c10.isHierarchyRoot());
        Assertions.assertFalse(c11.isHierarchyRoot());
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
        OntClass c1 = m.createOntClass("C1");
        OntClass c2 = m.createOntClass("C2");
        OntClass c3 = m.createOntClass("C3");
        OntClass c4 = m.createOntClass("C4");
        OntClass c5 = m.createOntClass("C5");
        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(m.getOWLThing());
        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntObjectProperty o1 = m.createObjectProperty("o1");
        OntObjectProperty o2 = m.createObjectProperty("o2");
        o1.addSuperProperty(o2);
        o2.addSuperProperty(m.getOWLTopObjectProperty());
        d1.addDomain(c1);
        d2.addDomain(c2);
        o1.addDomain(c3);
        o2.addDomain(m.getOWLThing());
        m.getOWLBottomDataProperty().addDomain(m.getOWLNothing());

        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), m.getOWLNothing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLNothing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d1), c1.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d1, d2, o1, o2), c1.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d2), c2.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d2, o1, o2), c2.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o1), c3.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, o2), c3.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), c4.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), c4.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), c5.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), c5.declaredProperties(false).collect(Collectors.toSet()));
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

    @Test
    public void testListSubClasses1() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass a = m.createOntClass(NS + "A");
        a.addSubClass(a);

        Assertions.assertTrue(a.subClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(a.subClasses(false).findFirst().isEmpty());
    }

    @Test
    public void testListSubClasses2() {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createABCDEFGHKLMModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));

        Set<String> directA = m.getOntClass(NS + "A").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getOntClass(NS + "D").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getOntClass(NS + "D").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getOntClass(NS + "E").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getOntClass(NS + "E").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = m.getOntClass(NS + "F").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = m.getOntClass(NS + "F").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = m.getOntClass(NS + "G").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = m.getOntClass(NS + "G").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directH = m.getOntClass(NS + "H").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectH = m.getOntClass(NS + "H").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directK = m.getOntClass(NS + "K").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectK = m.getOntClass(NS + "K").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directL = m.getOntClass(NS + "L").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectL = m.getOntClass(NS + "L").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directM = m.getOntClass(NS + "M").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectM = m.getOntClass(NS + "M").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

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

    @Test
    public void testListSubClasses3() {
        OntModel m = createABCDEFModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));

        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        Set<String> directA = m.getOntClass(NS + "A").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").subClasses().map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getOntClass(NS + "D").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getOntClass(NS + "D").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getOntClass(NS + "E").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getOntClass(NS + "E").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = m.getOntClass(NS + "F").subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = m.getOntClass(NS + "F").subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

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

    @Test
    public void testListSubClasses4() {
        //     A
        //     |
        // D = B = C
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        A.addSubClass(B);
        B.addEquivalentClass(C);
        D.addEquivalentClass(B);

        Set<String> directA = A.subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directB = B.subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directC = C.subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directD = D.subClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> indirectA = A.subClasses().map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = D.subClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of(), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @Test
    public void testListSuperClasses1() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        A.addSuperClass(A);

        Assertions.assertTrue(A.superClasses(true).findFirst().isEmpty());
        Assertions.assertTrue(A.superClasses(false).findFirst().isEmpty());
    }

    @Test
    public void testListSuperClasses2() {
        OntModel m = createABCDEFModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));

        Set<String> directA = m.getOntClass(NS + "A").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getOntClass(NS + "E").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getOntClass(NS + "E").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of(), directA);
        Assertions.assertEquals(Set.of("A"), directB);
        Assertions.assertEquals(Set.of("A"), directC);
        Assertions.assertEquals(Set.of("B", "C"), directE);

        Assertions.assertEquals(Set.of(), indirectA);
        Assertions.assertEquals(Set.of("A"), indirectB);
        Assertions.assertEquals(Set.of("A"), indirectC);
        Assertions.assertEquals(Set.of("A", "B", "C"), indirectE);
    }

    @Test
    public void testListSuperClasses3() {
        // B = C
        //  \ |
        //    A
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        A.addSuperClass(B);
        A.addSuperClass(C);
        B.addSuperClass(C);
        C.addSuperClass(B);

        Set<String> directA = A.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directB = B.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directC = C.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> indirectA = A.superClasses().map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of(), directC);

        Assertions.assertEquals(Set.of("B", "C"), indirectA);
        Assertions.assertEquals(Set.of("C"), indirectB);
        Assertions.assertEquals(Set.of("B"), indirectC);
    }

    @Test
    public void testListSuperClasses4() {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M

        OntModel m = createABCDEFGHKLMModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));

        Set<String> directA = m.getOntClass(NS + "A").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").superClasses().map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getOntClass(NS + "D").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getOntClass(NS + "D").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getOntClass(NS + "E").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getOntClass(NS + "E").superClasses().map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = m.getOntClass(NS + "F").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = m.getOntClass(NS + "F").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = m.getOntClass(NS + "G").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = m.getOntClass(NS + "G").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directH = m.getOntClass(NS + "H").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectH = m.getOntClass(NS + "H").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directK = m.getOntClass(NS + "K").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectK = m.getOntClass(NS + "K").superClasses().map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directL = m.getOntClass(NS + "L").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectL = m.getOntClass(NS + "L").superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directM = m.getOntClass(NS + "M").superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectM = m.getOntClass(NS + "M").superClasses().map(Resource::getLocalName).collect(Collectors.toSet());

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

    @Test
    public void testListSuperClasses5() {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        C.addSubClass(A);
        B.addSubClass(A);
        D.addSubClass(C);
        D.addSubClass(A);

        Set<String> directA = A.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directB = B.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directC = C.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> directD = D.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> indirectA = A.superClasses().map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = B.superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = C.superClasses(false).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = D.superClasses(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("B", "C"), directA);
        Assertions.assertEquals(Set.of(), directB);
        Assertions.assertEquals(Set.of("D"), directC);
        Assertions.assertEquals(Set.of(), directD);

        Assertions.assertEquals(Set.of("B", "C", "D"), indirectA);
        Assertions.assertEquals(Set.of(), indirectB);
        Assertions.assertEquals(Set.of("D"), indirectC);
        Assertions.assertEquals(Set.of(), indirectD);
    }

    @Test
    public void testListIndividuals1() {
        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F
        OntModel m = createABCDEFModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));
        OntClass a = m.getOntClass(NS + "A");
        OntClass b = m.getOntClass(NS + "B");
        OntClass c = m.getOntClass(NS + "C");
        OntClass d = m.getOntClass(NS + "D");
        OntClass e = m.getOntClass(NS + "E");
        OntClass f = m.getOntClass(NS + "F");

        a.createIndividual(NS + "iA");
        b.createIndividual(NS + "iB");
        c.createIndividual(NS + "iC");
        d.createIndividual(NS + "iD");
        e.createIndividual(NS + "iE");

        Set<String> directA = a.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = a.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = b.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = b.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = c.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = c.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = d.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = d.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = e.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = e.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = f.individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = f.individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

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

    @Test
    public void testListIndividuals2() {
        // B = C
        //  \ |
        //    A
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        A.addSuperClass(B);
        A.addSuperClass(C);
        B.addSuperClass(C);
        C.addSuperClass(B);
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = m.getOntClass(NS + "A").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB", "iC"), directB);
        Assertions.assertEquals(Set.of("iB", "iC"), directC);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iB", "iC"), indirectC);
    }

    @Test
    public void testListIndividuals3() {
        //     D
        //    | \
        // B  |  C
        //  \ | /
        //    A
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass A = m.createOntClass(NS + "A");
        OntClass B = m.createOntClass(NS + "B");
        OntClass C = m.createOntClass(NS + "C");
        OntClass D = m.createOntClass(NS + "D");
        C.addSubClass(A);
        B.addSubClass(A);
        D.addSubClass(C);
        D.addSubClass(A);
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = m.getOntClass(NS + "A").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getOntClass(NS + "D").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getOntClass(NS + "D").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Assertions.assertEquals(Set.of("iA"), directA);
        Assertions.assertEquals(Set.of("iB"), directB);
        Assertions.assertEquals(Set.of("iC"), directC);
        Assertions.assertEquals(Set.of("iD"), directD);
        Assertions.assertEquals(Set.of("iA"), indirectA);
        Assertions.assertEquals(Set.of("iA", "iB"), indirectB);
        Assertions.assertEquals(Set.of("iA", "iC"), indirectC);
        Assertions.assertEquals(Set.of("iA", "iC", "iD"), indirectD);
    }

    @Test
    public void testListInstances3() {
        //     A
        //   /  / \
        //  /  B   C
        //  | / \ / \
        //  D   E   F
        // / \
        // G  H = K
        //       / \
        //      L   M
        OntModel m = createABCDEFGHKLMModel(OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF));
        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = m.getOntClass(NS + "A").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getOntClass(NS + "A").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getOntClass(NS + "B").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getOntClass(NS + "B").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getOntClass(NS + "C").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getOntClass(NS + "C").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getOntClass(NS + "D").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getOntClass(NS + "D").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getOntClass(NS + "E").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getOntClass(NS + "E").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = m.getOntClass(NS + "F").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = m.getOntClass(NS + "F").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = m.getOntClass(NS + "G").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = m.getOntClass(NS + "G").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directH = m.getOntClass(NS + "H").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectH = m.getOntClass(NS + "H").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directK = m.getOntClass(NS + "K").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectK = m.getOntClass(NS + "K").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directL = m.getOntClass(NS + "L").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectL = m.getOntClass(NS + "L").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directM = m.getOntClass(NS + "M").individuals(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectM = m.getOntClass(NS + "M").individuals(false).map(Resource::getLocalName).collect(Collectors.toSet());

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
