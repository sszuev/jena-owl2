package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntClassMiscTest {

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

}
