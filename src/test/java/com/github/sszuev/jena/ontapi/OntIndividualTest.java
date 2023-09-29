package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * To test {@link OntIndividual}.
 * <p>
 * Created by @ssz on 11.05.2019.
 */
public class OntIndividualTest {
    private static final String NS = "http://jena.hpl.hp.com/testing/ontology#";

    @Test
    public void testPositiveAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");
        OntAnnotationProperty a = m.getRDFSComment();

        Assertions.assertSame(i1, i1.addAssertion(d, m.createLiteral("1"))
                .addAssertion(d, m.createLiteral("2"))
                .addAssertion(p.asNamed(), i2)
                .addAssertion(a, m.createLiteral("3")));
        Assertions.assertEquals(4, i1.positiveAssertions().count());
        Assertions.assertEquals(2, i1.positiveAssertions(d).count());
        Assertions.assertEquals(8, m.size());

        Assertions.assertSame(i1, i1.removeAssertion(d, null).removeAssertion(p.asNamed(), i2));
        Assertions.assertEquals(1, i1.positiveAssertions().count());
        Assertions.assertSame(i1, i1.removeAssertion(null, null));
        Assertions.assertEquals(0, i1.positiveAssertions().count());
        Assertions.assertEquals(4, m.size());
    }

    @Test
    public void testDifferentIndividuals() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual a = m.createIndividual("A");
        OntIndividual b = m.createOntClass("C1").createIndividual("B");
        OntIndividual c = m.createOntClass("C2").createIndividual();
        OntIndividual d = m.createIndividual("D");

        Assertions.assertNotNull(a.addDifferentFromStatement(b));
        Assertions.assertSame(a, a.addDifferentIndividual(c).addDifferentIndividual(d).removeDifferentIndividual(b));
        Assertions.assertEquals(2, a.differentIndividuals().count());
        Assertions.assertSame(a, a.removeDifferentIndividual(null));
        Assertions.assertEquals(7, m.size());
    }

    @Test
    public void testSameIndividuals() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual a = m.createIndividual("A");
        OntIndividual b = m.createOntClass("C1").createIndividual("B");
        OntIndividual c = m.createOntClass("C2").createIndividual();
        OntIndividual d = m.createIndividual("D");

        Assertions.assertNotNull(a.addSameAsStatement(b));
        Assertions.assertSame(a, a.addSameIndividual(c).addSameIndividual(d).removeSameIndividual(b));
        Assertions.assertEquals(2, a.sameIndividuals().count());
        Assertions.assertSame(a, a.removeSameIndividual(null));
        Assertions.assertEquals(7, m.size());
    }

    @Test
    public void testNegativeAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");

        Assertions.assertSame(i1, i1.addNegativeAssertion(d, m.createLiteral("1")));
        Assertions.assertEquals(1, i1.negativeAssertions().count());
        Assertions.assertEquals(0, i1.positiveAssertions().count());
        Assertions.assertSame(i1, i1.addNegativeAssertion(d, m.createLiteral("2"))
                .addNegativeAssertion(p, i2).addNegativeAssertion(p, i3));
        Assertions.assertEquals(4, i1.negativeAssertions().count());
        Assertions.assertEquals(0, i1.positiveAssertions().count());

        Assertions.assertEquals(2, m.statements(null, OWL.targetIndividual, null).count());
        Assertions.assertEquals(2, m.statements(null, OWL.targetValue, null).count());
        Assertions.assertEquals(21, m.size());

        Assertions.assertSame(i1, i1.removeNegativeAssertion(d, null).removeNegativeAssertion(p.asNamed(), i3));
        Assertions.assertEquals(1, i1.negativeAssertions().count());
        Assertions.assertEquals(1, m.statements(null, OWL.targetIndividual, null).count());
        Assertions.assertEquals(0, m.statements(null, OWL.targetValue, null).count());

        Assertions.assertSame(i1, i1.removeNegativeAssertion(null, null));
        Assertions.assertEquals(5, m.size());
    }

    @Test
    public void testRemoveIndividual() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");

        i1.addNegativeAssertion(p, i2)
                .addAssertion(p.asNamed(), i3)
                .addNegativeAssertion(d, m.createLiteral("1"))
                .addAssertion(d, m.createLiteral("2")).addComment("The individual to test");
        Assertions.assertEquals(16, m.size());

        Assertions.assertEquals(4, m.removeOntObject(i1).size());
    }

    @Test
    public void testClassAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntClass c1 = m.createOntClass("C1");
        OntClass c2 = m.createOntClass("C2");
        OntIndividual i2 = c2.createIndividual();
        long size = 4;

        Assertions.assertSame(i1, i1.detachClass(c1).detachClass(m.getOWLThing()));
        Assertions.assertEquals(size, m.size());

        Assertions.assertNotNull(i1.addClassAssertion(c2));
        Assertions.assertSame(i1, i1.attachClass(c2).attachClass(c1).attachClass(m.getOWLThing()));
        Assertions.assertEquals(3, i1.classes().count());
        Assertions.assertEquals(size + 3, m.size());

        Assertions.assertSame(i1, i1.detachClass(c2));
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertEquals(2, i1.classes().count());
        Assertions.assertSame(i1, i1.detachClass(null));
        Assertions.assertEquals(size, m.size());
        Assertions.assertEquals(0, i1.classes().count());

        Assertions.assertSame(i2, i2.attachClass(m.getOWLThing()).attachClass(c1).attachClass(c2));
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertEquals(3, i2.classes().count());

        // not possible to delete all class assertions:
        try {
            i2.detachClass(null);
            Assertions.fail("Possible to delete all class expressions");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertSame(i2, i2.detachClass(c2).detachClass(m.getOWLThing()));
        Assertions.assertEquals(1, i2.classes().count());
        Assertions.assertEquals(size, m.size());

        // not possible to delete the last class assertions:
        try {
            i2.detachClass(c1);
            Assertions.fail("Possible to delete the last class expressions");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(size, m.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals1(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-individuals-test.rdf", Lang.RDFXML);
        Assertions.assertEquals(Set.of("A0", "A1", "C0", "a0", "a1", "a2", "z0", "z1"),
                m.individuals().map(Resource::getLocalName).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL.Class);
        m.add(OWL.Class, RDF.type, RDFS.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals3(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL.Class);
        m.add(OWL.Class, RDF.type, OWL.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testListIndividuals11(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntClass.Named c0 = m.getOWLThing();
        OntClass.Named c1 = m.createOntClass(NS + "C1");
        OntClass.Named c2 = m.createOntClass(NS + "C2");
        OntClass.Named c3 = m.createOntClass(NS + "C3");

        OntIndividual i1 = c0.createIndividual(NS + "I1");
        OntIndividual i2 = m.createIndividual(NS + "I2");
        OntIndividual i3 = m.createIndividual(NS + "I3");
        OntIndividual i4 = m.createIndividual(NS + "I4");
        OntIndividual i6 = c3.createIndividual();
        OntIndividual i5 = c1.createIndividual(NS + "I5");

        c2.createIndividual(NS + "I5");
        c2.createIndividual(NS + "I3");
        c3.createIndividual(NS + "I3");

        i1.addSameAsStatement(i2);
        i3.addDifferentIndividual(i4);
        i3.addDifferentIndividual(i6);

        // class-assertions:
        Assertions.assertEquals(6, m.statements(null, RDF.type, null)
                .filter(x -> x.getObject().canAs(OntClass.class)).count());
        // all individuals:
        Assertions.assertEquals(6, m.ontObjects(OntIndividual.class).count());
        // named individuals:
        Assertions.assertEquals(5, m.namedIndividuals().peek(x -> Assertions.assertTrue(x.isURIResource())).count());

        // distinct class asserted individuals:
        Assertions.assertEquals(4, m.individuals().count());
        Assertions.assertEquals(1, m.individuals().filter(RDFNode::isAnon).count());
        Assertions.assertEquals(Set.of(i1, i3, i5), m.individuals().filter(it -> !it.isAnon()).collect(Collectors.toSet()));
    }

    @Test
    public void testListDisjoints() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntIndividual i4 = m.createIndividual("I4");
        m.createDifferentIndividuals(i1, i2);
        m.createDifferentIndividuals(i1, i3);

        Assertions.assertEquals(0, i4.disjoints().count());
        Assertions.assertEquals(2, i1.disjoints().count());
        Assertions.assertEquals(1, i2.disjoints().count());
        Assertions.assertEquals(1, i3.disjoints().count());
    }

    @Test
    public void testListOntClasses1() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF);
        OntClass a = m.createOntClass(NS + "A");
        OntClass b = m.createOntClass(NS + "B");
        OntClass c = m.createOntClass(NS + "C");
        OntClass d = m.createOntClass(NS + "D");
        OntClass e = m.createOntClass(NS + "E");
        OntClass f = m.createOntClass(NS + "F");
        OntClass g = m.createOntClass(NS + "G");

        //      A   G
        //     / \   \
        //    B   C = F
        //   / \ /
        //  D   E

        a.addSubClass(b);
        a.addSubClass(c);
        b.addSubClass(d);
        b.addSubClass(e);
        c.addSubClass(e);
        c.addSubClass(f);
        f.addSubClass(c);
        f.addSuperClass(g);

        m.classes().collect(Collectors.toList()).forEach(x -> x.createIndividual(NS + "i" + x.getLocalName()));

        Set<String> directA = m.getIndividual(NS + "iA").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectA = m.getIndividual(NS + "iA").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directB = m.getIndividual(NS + "iB").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectB = m.getIndividual(NS + "iB").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directC = m.getIndividual(NS + "iC").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectC = m.getIndividual(NS + "iC").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directD = m.getIndividual(NS + "iD").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectD = m.getIndividual(NS + "iD").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directE = m.getIndividual(NS + "iE").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectE = m.getIndividual(NS + "iE").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directF = m.getIndividual(NS + "iF").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectF = m.getIndividual(NS + "iF").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

        Set<String> directG = m.getIndividual(NS + "iG").classes(true).map(Resource::getLocalName).collect(Collectors.toSet());
        Set<String> indirectG = m.getIndividual(NS + "iG").classes(false).map(Resource::getLocalName).collect(Collectors.toSet());

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
}
