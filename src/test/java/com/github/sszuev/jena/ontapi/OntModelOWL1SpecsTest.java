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
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sszuev.jena.ontapi.OntModelOWLSpecsTest.assertOntObjectsCount;

public class OntModelOWL1SpecsTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testFamilyListObjectsOWL1(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().toList();
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertInstanceOf(OntClass.IntersectionOf.class, equivalentToWife.get(0));
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());
        List<OntClass> equivalentToSex = m.getOntClass(ns + "Sex").equivalentClasses().toList();
        Assertions.assertEquals(1, equivalentToSex.size());
        Assertions.assertInstanceOf(OntClass.UnionOf.class, equivalentToSex.get(0));
        Assertions.assertEquals(OntClass.UnionOf.class, equivalentToSex.get(0).objectType());

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
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 114);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.Restriction.class, 111);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 4);
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
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testDataRangesForOWL1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        OntDataRange.OneOf d1 = m.createDataOneOf(m.createTypedLiteral(42));
        OntDataRange.OneOf d2 = m.createDataOneOf(m.createTypedLiteral("A"), m.createLiteral("B"));
        OntDataRange.OneOf d3 = m.createResource("X", OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C"))).as(OntDataRange.OneOf.class);
        m.createResource("X", RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C")));
        Assertions.assertEquals(
                List.of(42),
                d1.getList().members().map(Literal::getInt).collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("A", "B"),
                d2.getList().members().map(Literal::getString).sorted().collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("C"),
                d3.getList().members().map(Literal::getString).collect(Collectors.toList())
        );

        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataUnionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataIntersectionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataComplementOf(d1));

        Assertions.assertEquals(3, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testDisjointIndividualsForOWL1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntIndividual i1 = m.getOWLThing().createIndividual("A");
        OntIndividual i2 = m.getOWLThing().createIndividual("B");
        OntDisjoint.Individuals d = m.createDifferentIndividuals(i1, i2);
        Assertions.assertEquals(
                List.of("A", "B"),
                d.members().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        Assertions.assertEquals(8, m.statements().count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointClasses);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointDataProperties);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointObjectProperties);

        Assertions.assertEquals(8, m.statements().count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
            "OWL1_LITE_MEM_RDFS_INF",
            "OWL1_LITE_MEM_TRANS_INF",
            "OWL1_LITE_MEM_RULES_INF",
    })
    public void testOntClassCastOWL1(TestSpec spec) {
        Model g = ModelFactory.createDefaultModel();
        Resource namedRdfsClass = g.createResource("rdfsClass", RDFS.Class);
        Resource namedRdfsDatatype = g.createResource("rdfsDatatype", RDFS.Datatype);
        Resource namedOwlClass = g.createResource("owlClass", OWL.Class);
        Resource anonRdfsClass = g.createResource(RDFS.Class);
        Resource anonRdfsDatatype = g.createResource(RDFS.Datatype);
        Resource anonOwlClass = g.createResource(OWL.Class);
        Resource anonRdfsDomain = g.createResource();
        Resource anonRdfsRange = g.createResource();
        Resource namedRdfsDomain = g.createResource("rdfsDomain");
        Resource namedRdfsRange = g.createResource("rdfsRange");
        g.createResource("p", RDF.Property).addProperty(RDFS.domain, anonRdfsDomain).addProperty(RDFS.range, namedRdfsRange);
        g.createResource(null, RDF.Property).addProperty(RDFS.domain, namedRdfsDomain).addProperty(RDFS.range, anonRdfsRange);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst).setNsPrefixes(PrefixMapping.Standard);
        Assertions.assertTrue(anonOwlClass.inModel(m).canAs(OntClass.class) && !anonOwlClass.inModel(m).canAs(OntClass.Named.class));
        Assertions.assertTrue(anonRdfsClass.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(anonRdfsDatatype.inModel(m).canAs(OntClass.class));
        Stream.of(namedOwlClass, namedRdfsClass, namedRdfsDatatype, namedRdfsDomain, namedRdfsRange)
                .forEach(x -> Assertions.assertTrue(x.inModel(m).canAs(OntClass.Named.class)));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_LITE_MEM",
    })
    public void testDisabledFeaturesOWL1(TestSpec spec) {
        OntModel d = OntModelFactory.createModel();
        d.createOntClass("X").addHasKey(d.createObjectProperty("p"));
        d.createOntClass("X").addDisjointUnion(d.createOntClass("Q"));
        d.createObjectProperty("op1")
                .setReflexive(true).setAsymmetric(true)
                .addDisjointProperty(
                        d.createObjectProperty("op2")
                                .setIrreflexive(true)
                                .addPropertyChain(List.of(d.createObjectProperty("op1")))
                );
        d.createDataProperty("dp1").addEquivalentProperty(d.createDataProperty("dp2"));

        OntModel m = OntModelFactory.createModel(d.getGraph(), OntSpecification.OWL1_DL_MEM);
        OntClass.Named x = m.getOntClass("X");
        OntClass.Named q = m.createOntClass("Q");
        OntDataProperty dp1 = m.createDataProperty("dp1");
        OntObjectProperty op1 = m.getObjectProperty("op1");
        OntDataProperty dp2 = m.createDataProperty("dp2");
        OntObjectProperty op2 = m.getObjectProperty("op2");

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addHasKey(op1));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.createHasKey(List.of(op1), List.of(dp1)));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeHasKey(m.createList()));
        Assertions.assertEquals(0, x.hasKeys().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointUnion(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointUnion(m.createList()));
        Assertions.assertEquals(0, x.disjointUnions().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> dp1.addDisjointProperty(dp2));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> op1.removeDisjointProperty(op2));
        Assertions.assertEquals(0, dp1.disjointProperties().count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, op1::addReflexiveDeclaration);
        Assertions.assertFalse(op1.isReflexive());

        Assertions.assertThrows(OntJenaException.Unsupported.class, op1::addIrreflexiveDeclaration);
        Assertions.assertFalse(op2.isIrreflexive());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> op1.setAsymmetric(false));
        Assertions.assertFalse(op1.isAsymmetric());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> op1.createPropertyChain(List.of(op2)));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> op2.removePropertyChain(op1));
        Assertions.assertEquals(0, op2.propertyChains().count());
    }
}
