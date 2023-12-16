package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntModelIndividualsTest {
    private static final String NS = "http://ex.com/testing/ontology#";

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals1(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test.rdf", Lang.RDFXML);
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
            "OWL1_MEM",
    })
    public void testListIndividuals4(TestSpec spec) {
        // For inference model
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource a0 = m.createResource(NS + "A0");
        m.add(a0, RDF.type, OWL.Class);
        Assertions.assertTrue(m.individuals().findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals5(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        // a0 should be an individual, since we are punning
        Resource a0 = m.createResource(NS + "A0");
        Resource a1 = m.createResource(NS + "A1");
        m.add(a0, RDF.type, OWL.Class);
        m.add(a1, RDF.type, OWL.Class);
        m.add(a0, RDF.type, a1);
        Assertions.assertEquals(List.of(NS + "A0"), m.individuals().map(Resource::getURI).collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListIndividuals6a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(
                List.of(
                        "urn:x-hp:eg/DTPGraphics",
                        "urn:x-hp:eg/budgetGraphics",
                        "urn:x-hp:eg/gamingGraphics"),
                m.individuals().distinct().map(Resource::getURI).sorted().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListIndividuals6c(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(OntModelFactory.createModel(spec.inst),
                "/list-syntax-categories-test-comps.rdf", Lang.RDFXML);
        Assertions.assertEquals(0, m.individuals().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testListIndividuals8(TestSpec spec) {
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

    @ParameterizedTest
    @EnumSource(TestSpec.class)
    public void testListIndividuals9(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        m.createResource("x", m.createResource("X"));
        m.createResource().addProperty(RDF.type, m.createResource("Y"));

        OntClass clazz = m.createOntClass("Q");
        clazz.createIndividual("q");
        clazz.createIndividual();

        List<OntIndividual> individuals = m.individuals().collect(Collectors.toList());

        int expectedNumOfIndividuals = 2;
        Assertions.assertEquals(expectedNumOfIndividuals, individuals.size());
    }
}