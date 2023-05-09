package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNamedProperty;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * To test properties:
 * {@link OntNamedProperty}, {@link OntProperty} ({@link OntObjectProperty}, {@link OntObjectProperty.Named}, {@link OntDataProperty}, {@link OntRealProperty}, {@link OntAnnotationProperty}).
 * <p>
 * Created by @ssz on 08.05.2019.
 */
public class OntPropertyTest {

    @Test
    public void testCreateProperties() {
        String ns = "http://test.com/graph/7#";

        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("test", ns);
        OntAnnotationProperty a1 = m.createAnnotationProperty(ns + "a-p-1");
        OntAnnotationProperty a2 = m.createAnnotationProperty(ns + "a-p-2");
        m.createObjectProperty(ns + "o-p-1");
        m.createObjectProperty(ns + "o-p-2").createInverse();
        m.createObjectProperty(ns + "o-p-3").createInverse().addComment("Anonymous property expression");
        m.createObjectProperty(ns + "o-p-4")
                .addInverseOfStatement(m.createObjectProperty(ns + "o-p-5"))
                .annotate(a1, m.createLiteral("inverse statement, not inverse-property"));
        m.createDataProperty(ns + "d-p-1");
        m.createDataProperty(ns + "d-p-2").addAnnotation(a2, m.createLiteral("data-property"));


        OntModelTest.simplePropertiesValidation(m);
        Assertions.assertEquals(9, m.ontObjects(OntNamedProperty.class).count());
        Assertions.assertEquals(11, m.ontObjects(OntProperty.class).count());
        Assertions.assertEquals(9, m.ontObjects(OntRealProperty.class).count());
    }

    @Test
    public void testListPropertyHierarchy() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty da = m.createDataProperty("dA");
        OntDataProperty db = m.createDataProperty("dB");

        OntObjectProperty.Named oa = m.createObjectProperty("oA");
        OntObjectProperty.Inverse iob = m.createObjectProperty("oB").createInverse();
        OntObjectProperty.Named oc = m.createObjectProperty("oC");

        OntAnnotationProperty aa = m.createAnnotationProperty("aA");
        OntAnnotationProperty ab = m.createAnnotationProperty("aB");
        OntAnnotationProperty ac = m.createAnnotationProperty("aC");

        da.addSuperProperty(db);
        db.addSuperProperty(m.getOWLBottomDataProperty());

        oc.addSuperProperty(iob);
        iob.addSuperProperty(oa);

        aa.addSuperProperty(ab);
        ab.addSuperProperty(ac).addSuperProperty(m.getRDFSComment());
        ac.addSuperProperty(aa);


        Assertions.assertEquals(1, da.superProperties(true).count());
        Assertions.assertEquals(2, da.superProperties(false).count());

        Assertions.assertEquals(1, iob.subProperties(true).count());
        Assertions.assertEquals(1, iob.subProperties(false).count());
        Assertions.assertEquals(2, oa.subProperties(false).count());

        Assertions.assertEquals(1, ac.superProperties(true).count());
        Assertions.assertEquals(1, ac.subProperties(true).count());
        Assertions.assertEquals(3, ac.superProperties(false).count());
        Assertions.assertEquals(3, m.getRDFSComment().subProperties(false).count());
    }

    @Test
    public void testAnnotationPropertyDomainsAndRanges() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntAnnotationProperty p = m.createAnnotationProperty("A");
        Assertions.assertNotNull(p.addRangeStatement(m.getRDFSComment()));
        Assertions.assertNotNull(p.addDomainStatement(m.getRDFSComment()));
        Assertions.assertSame(p, p.addDomain(m.getOWLThing()).addRange(m.getOWLNothing()).addDomain(m.getRDFSLabel()));
        Assertions.assertEquals(2, p.ranges().count());
        Assertions.assertEquals(3, p.domains().count());

        Assertions.assertSame(p, p.removeDomain(m.getOWLThing()).removeRange(m.getRDFSComment()));
        Assertions.assertEquals(1, p.ranges().count());
        Assertions.assertEquals(2, p.domains().count());
    }

    @Test
    public void testAnnotationSuperProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntAnnotationProperty p = m.createAnnotationProperty("A");
        Assertions.assertNotNull(p.addSubPropertyOfStatement(m.getRDFSComment()));
        Assertions.assertSame(p, p.addSuperProperty(m.getRDFSLabel())
                .addSuperProperty(m.getAnnotationProperty(RDFS.seeAlso)));
        Assertions.assertEquals(3, p.superProperties().count());

        Assertions.assertSame(p, p.removeSuperProperty(m.getOWLThing()).removeSuperProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, p.superProperties().count());
        p.removeSuperProperty(null);
        Assertions.assertEquals(0, p.superProperties().count());
    }

    @Test
    public void testIndirectDomains() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("", "http://ex.com#");
        OntObjectProperty hasDog = m.createObjectProperty(m.expandPrefix(":hasDog"));
        OntDataProperty hasName = m.createDataProperty(m.expandPrefix(":hasName"));
        OntClass animal = m.createOntClass(m.expandPrefix(":Animal"));
        OntClass primate = m.createOntClass(m.expandPrefix(":Primate"));
        OntClass person = m.createOntClass(m.expandPrefix(":Person"));
        primate.addSuperClass(animal);
        person.addSuperClass(primate);
        hasName.addDomain(person);
        hasDog.addDomain(person);

        Assertions.assertEquals(Set.of(person, primate, animal), hasDog.domains(false).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(person, primate, animal), hasName.domains(false).collect(Collectors.toSet()));
    }

    @Test
    public void testDeclaringClasses() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("", "http://ex.com#");

        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.getOWLThing();
        OntClass c6 = m.getOWLNothing();

        OntObjectProperty p1 = m.createObjectProperty(":p1");
        OntObjectProperty p2 = m.createObjectProperty(":p2");
        OntObjectProperty p3 = m.createObjectProperty(":p3");
        OntObjectProperty p4 = m.createObjectProperty(":p4");
        OntDataProperty p5 = m.createDataProperty(":p5");
        OntDataProperty p6 = m.createDataProperty(":p6");
        OntDataProperty p7 = m.createDataProperty(":p7");
        OntObjectProperty p8 = m.getOWLTopObjectProperty();
        OntDataProperty p9 = m.getOWLBottomDataProperty();
        OntObjectProperty p10 = m.getOWLBottomObjectProperty();

        p1.addSuperProperty(p2);
        p2.addSuperProperty(p3);
        p5.addSuperProperty(p6);

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c1.addSuperClass(c4);

        p1.addDomain(c1);
        p2.addDomain(c2);
        p4.addDomain(c4);
        p6.addDomain(c3);
        p7.addDomain(c1);
        p8.addDomain(c5);
        p9.addDomain(c6);

        Assertions.assertEquals(Set.of(c1), p1.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1), p1.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c2), p2.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2), p2.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3, c4), p3.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3, c4), p3.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c4), p4.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c4), p4.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3, c4), p5.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3, c4), p5.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3), p6.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3), p6.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c1), p7.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1), p7.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3, c4), p8.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3, c4), p8.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3, c4), p9.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3, c4), p9.declaringClasses(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(c3, c4), p10.declaringClasses(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(c1, c2, c3, c4), p10.declaringClasses(false).collect(Collectors.toSet()));
    }

    @Test
    public void testReferringRestrictions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);

        OntObjectProperty p1 = m.createObjectProperty(":p1");
        OntObjectProperty p2 = m.createObjectProperty(":p2");
        m.createObjectAllValuesFrom(p1, m.createOntClass(":c1"));
        m.createObjectSomeValuesFrom(p2, m.createOntClass(":c1"));
        m.createObjectHasValue(p2, m.createIndividual(":i1"));
        m.createHasSelf(p2);
        m.createObjectCardinality(p1, 42, null);
        m.createObjectCardinality(p1, 42, m.createOntClass(":c2"));
        m.createObjectMaxCardinality(p2, 42, null);
        m.createObjectMaxCardinality(p1, 42, m.createOntClass(":c2"));
        m.createObjectMinCardinality(p1, 42, m.createOntClass(":c1"));
        m.createObjectMinCardinality(p1, 42, null);

        OntDataProperty p3 = m.createDataProperty(":p3");
        OntDataProperty p4 = m.createDataProperty(":p4");
        m.createDataAllValuesFrom(p3, m.createDatatype(":dt1"));
        m.createDataSomeValuesFrom(p3, m.createDatatype(":dt1"));
        m.createDataHasValue(p4, m.createTypedLiteral(42));
        m.createDataCardinality(p3, 42, m.createDatatype(":dt1"));
        m.createDataCardinality(p3, 42, null);
        m.createDataMaxCardinality(p4, 42, m.createDataOneOf(m.createLiteral("a"), m.createLiteral("b")));
        m.createDataMaxCardinality(p3, 43, null);
        m.createDataMinCardinality(p4, 42, m.createDataRestriction(m.createDatatype(":dt1"),
                m.createFacetRestriction(OntFacetRestriction.TotalDigits.class, m.createTypedLiteral(2))));
        m.createDataMinCardinality(p4, 42, null);

        Assertions.assertEquals(6, p1.referringRestrictions().count());
        Assertions.assertEquals(4, p2.referringRestrictions().count());
        Assertions.assertEquals(5, p3.referringRestrictions().count());
        Assertions.assertEquals(4, p4.referringRestrictions().count());
    }
}
