package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNamedProperty;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.sszuev.jena.ontapi.OntModelOWLSpecsTest.assertOntObjectsCount;

public class OntModelOWLLiteSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testFamilyListObjectsOWL1Lite(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertInstanceOf(OntClass.IntersectionOf.class, equivalentToWife.get(0));
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());
        List<OntClass> equivalentToSex = m.getOntClass(ns + "Sex").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToSex.size());
        // generic class:
        Assertions.assertFalse(equivalentToSex.get(0) instanceof OntClass.UnionOf);
        Assertions.assertNotEquals(OntClass.UnionOf.class, equivalentToSex.get(0).objectType());

        assertOntObjectsCount(m, OntObject.class, 1684);
        assertOntObjectsCount(m, OntEntity.class, 151);
        assertOntObjectsCount(m, OntNamedProperty.class, 90);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        // owl:NamedIndividual is not valid class-type in OWL1:
        assertOntObjectsCount(m, OntIndividual.Named.class, 3);
        assertOntObjectsCount(m, OntObjectProperty.Named.class, 80);
        assertOntObjectsCount(m, OntAnnotationProperty.class, 1);
        assertOntObjectsCount(m, OntDataProperty.class, 9);

        assertOntObjectsCount(m, OntObjectProperty.class, 80);
        assertOntObjectsCount(m, OntRelationalProperty.class, 89);
        assertOntObjectsCount(m, OntProperty.class, 90);

        assertOntObjectsCount(m, OntDataRange.class, 0);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntDataRange.OneOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Restriction.class, 0);
        assertOntObjectsCount(m, OntDataRange.UnionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.ComplementOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.IntersectionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Combination.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        assertOntObjectsCount(m, OntClass.class, 289);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 109);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.Restriction.class, 111);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 0);
        assertOntObjectsCount(m, OntClass.OneOf.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectSomeValuesFrom.class, 111);
        assertOntObjectsCount(m, OntClass.ObjectAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.DataSomeValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.HasSelf.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataSomeValuesFrom.class, 0);
    }


    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testDataRangesForOWL1Lite(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        m.createResource("X", OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("A")));
        m.createResource(null, OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("B")));
        m.createResource(null, RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C")));
        m.createResource("Q", RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("D")));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataOneOf(m.createTypedLiteral(42)));
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataUnionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataIntersectionOf);

        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());
    }

    @Test
    public void testDisabledFeaturesOWL1Lite() {
        Model d = OntModelFactory.createDefaultModel();
        d.createResource("X", OWL.Class).addProperty(OWL.disjointWith, d.createResource("Q", OWL.Class));
        d.createResource("iW", d.createResource("W", OWL.Class))
                .addProperty(OWL.sameAs, d.createResource("iQ", d.getResource("Q")));

        OntModel m = OntModelFactory.createModel(d.getGraph(), OntSpecification.OWL1_LITE_MEM);
        OntClass.Named x = m.getOntClass("X");
        OntClass.Named q = m.getOntClass("Q");
        OntIndividual iW = m.getIndividual("iW");
        OntIndividual iQ = m.getIndividual("iQ");

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointClass(q));
        Assertions.assertEquals(0, x.disjoints().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> iW.addSameIndividual(iQ));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> iQ.removeSameIndividual(iW));
        Assertions.assertEquals(0, iW.sameIndividuals().count());
    }
}
