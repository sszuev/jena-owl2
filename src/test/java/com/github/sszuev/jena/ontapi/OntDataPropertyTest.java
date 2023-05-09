package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OntDataPropertyTest {

    @Test
    public void testDataPropertyDomainsAndRanges() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named c = m.createOntClass("C");
        OntDataRange.Named d = m.getDatatype(XSD.xstring);
        OntDataProperty p = m.createDataProperty("D");
        Assertions.assertNotNull(p.addRangeStatement(m.getRDFSLiteral()));
        Assertions.assertNotNull(p.addDomainStatement(m.getOWLNothing()));
        Assertions.assertSame(p, p.addDomain(m.getOWLThing()).addRange(d).addDomain(c));
        Assertions.assertEquals(2, p.ranges().count());
        Assertions.assertEquals(3, p.domains().count());

        Assertions.assertSame(p, p.removeDomain(m.getOWLThing()).removeRange(d));
        Assertions.assertEquals(1, p.ranges().count());
        Assertions.assertEquals(2, p.domains().count());

        p.removeRange(null).removeDomain(null);
        Assertions.assertEquals(2, m.size());
    }

    @Test
    public void testDataSuperProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty p1 = m.createDataProperty("D");
        OntDataProperty p2 = m.createDataProperty("P");
        Assertions.assertNotNull(p1.addSubPropertyOfStatement(m.getOWLBottomDataProperty()));
        Assertions.assertSame(p1, p1.addSuperProperty(m.getOWLTopDataProperty())
                .addSuperProperty(p2));
        Assertions.assertEquals(3, p1.superProperties().count());

        Assertions.assertSame(p1, p1.removeSuperProperty(m.getOWLThing()).removeSuperProperty(m.getOWLTopDataProperty()));
        Assertions.assertEquals(2, p1.superProperties().count());
        p1.removeSuperProperty(null);
        Assertions.assertEquals(0, p1.superProperties().count());
    }

    @Test
    public void testDataPropertyAdditionalDeclarations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty p = m.createDataProperty("P");
        Assertions.assertNotNull(p.addFunctionalDeclaration());
        Assertions.assertTrue(p.isFunctional());
        Assertions.assertSame(p, p.setFunctional(false));
        Assertions.assertFalse(p.isFunctional());
    }

    @Test
    public void testDataPropertyEquivalentProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty a = m.createDataProperty("A");
        OntDataProperty b = m.createDataProperty("B");
        OntDataProperty c = m.createDataProperty("C");
        Assertions.assertNotNull(a.addEquivalentPropertyStatement(b));
        Assertions.assertSame(a, a.addEquivalentProperty(c).addEquivalentProperty(m.getOWLBottomDataProperty()));
        Assertions.assertEquals(3, a.equivalentProperties().count());
        Assertions.assertSame(a, a.removeEquivalentProperty(b).removeEquivalentProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, a.equivalentProperties().count());
        Assertions.assertSame(a, a.removeEquivalentProperty(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testDataPropertyDisjointProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty a = m.createDataProperty("A");
        OntDataProperty b = m.createDataProperty("B");
        OntDataProperty c = m.createDataProperty("C");
        Assertions.assertNotNull(a.addPropertyDisjointWithStatement(b));
        Assertions.assertSame(a, a.addDisjointProperty(c).addDisjointProperty(m.getOWLBottomDataProperty()));
        Assertions.assertEquals(3, a.disjointProperties().count());
        Assertions.assertSame(a, a.removeDisjointProperty(b).removeDisjointProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, a.disjointProperties().count());
        Assertions.assertSame(a, a.removeDisjointProperty(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testListDisjoints() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntDataProperty d3 = m.createDataProperty("d3");
        OntDataProperty d4 = m.createDataProperty("d4");
        m.createDisjointDataProperties(d1, d2);
        m.createDisjointDataProperties(d1, d3);

        Assertions.assertEquals(0, d4.disjoints().count());
        Assertions.assertEquals(2, d1.disjoints().count());
        Assertions.assertEquals(1, d2.disjoints().count());
        Assertions.assertEquals(1, d3.disjoints().count());
    }
}
