package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.sszuev.jena.ontapi.OntModelOWLSpecsTest.testListObjects;

public class OntModelOWL2ELSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testPizzaObjects1b(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);

        Map<Class<? extends OntObject>, Integer> expected = new HashMap<>();
        expected.put(OntClass.ObjectSomeValuesFrom.class, 155);
        expected.put(OntClass.DataSomeValuesFrom.class, 0);
        expected.put(OntClass.ObjectAllValuesFrom.class, 0);
        expected.put(OntClass.DataAllValuesFrom.class, 0);
        expected.put(OntClass.ObjectHasValue.class, 6);
        expected.put(OntClass.DataHasValue.class, 0);
        expected.put(OntClass.ObjectMinCardinality.class, 0);
        expected.put(OntClass.DataMinCardinality.class, 0);
        expected.put(OntClass.ObjectMaxCardinality.class, 0);
        expected.put(OntClass.DataMaxCardinality.class, 0);
        expected.put(OntClass.ObjectCardinality.class, 0);
        expected.put(OntClass.DataCardinality.class, 0);
        expected.put(OntClass.HasSelf.class, 0);
        expected.put(OntClass.UnionOf.class, 0);
        expected.put(OntClass.OneOf.class, 1);
        expected.put(OntClass.IntersectionOf.class, 15);
        expected.put(OntClass.ComplementOf.class, 0);
        expected.put(OntClass.NaryDataAllValuesFrom.class, 0);
        expected.put(OntClass.NaryDataSomeValuesFrom.class, 0);
        expected.put(OntClass.LogicalExpression.class, 16);
        expected.put(OntClass.CollectionOf.class, 16);
        expected.put(OntClass.ValueRestriction.class, 161);
        expected.put(OntClass.CardinalityRestriction.class, 0);
        expected.put(OntClass.ComponentRestriction.class, 161);
        expected.put(OntClass.UnaryRestriction.class, 161);
        expected.put(OntClass.Restriction.class, 161);
        expected.put(OntClass.class, 277);

        testListObjects(m, expected);

        List<OntClass.Named> classes = m.ontObjects(OntClass.Named.class).collect(Collectors.toList());
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL.Class)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isURIResource).toSet().size();
        int actualClassesCount = classes.size();
        Assertions.assertEquals(expectedClassesCount, actualClassesCount);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testOntProperties(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);

        OntObjectProperty p1 = m.createObjectProperty("op-1");
        OntObjectProperty p2 = m.createObjectProperty("op-2");
        OntDataProperty d = m.createDataProperty("dp");
        Resource i = m.createResource().addProperty(OWL.inverseOf, p1);

        Stream.of(
                OntProperty.class,
                OntObjectProperty.class,
                OntObjectProperty.Named.class,
                OntObjectProperty.Inverse.class,
                OntAnnotationProperty.class,
                OntAnnotationProperty.class).forEach(t -> {
            Assertions.assertFalse(m.createResource("x", OWL.IrreflexiveProperty).canAs(t));
            Assertions.assertFalse(m.createResource("q", OWL.InverseFunctionalProperty).canAs(t));
            Assertions.assertFalse(m.createResource("s", OWL.IrreflexiveProperty).canAs(t));
            Assertions.assertFalse(m.createResource("d", OWL.SymmetricProperty).canAs(t));
            Assertions.assertFalse(m.createResource("f", OWL.AsymmetricProperty).canAs(t));
            Assertions.assertFalse(i.canAs(t), "Can as " + t.getSimpleName());
        });

        p1.addProperty(RDF.type, OWL.FunctionalProperty);
        d.addProperty(RDF.type, OWL.FunctionalProperty);
        Assertions.assertTrue(d.isFunctional());
        Assertions.assertFalse(p1.isFunctional());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> p1.setAsymmetric(true));
        p1.addInverseProperty(p2);
        if (spec == TestSpec.OWL2_EL_MEM_RDFS_INF) {
            Assertions.assertEquals(17, m.properties().count());
        } else {
            Assertions.assertEquals(3, m.properties().count());
        }
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_EL_MEM",
            "OWL2_EL_MEM_RDFS_INF",
            "OWL2_EL_MEM_TRANS_INF",
    })
    public void testCreateConstructs(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty p1 = m.createObjectProperty("op-1");
        OntObjectProperty p2 = m.createObjectProperty("op-2");
        OntClass.Named c1 = m.createOntClass("c-1");
        OntClass.Named c2 = m.createOntClass("c-2");
        OntDataProperty d = m.createDataProperty("dp");

        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataAllValuesFrom(d, m.getDatatype(XSD.xstring.getURI()))
        );

        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectAllValuesFrom(p1, c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectMinCardinality(p2, 42, c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> c1.createDisjointUnion(List.of(c2))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectComplementOf(c2)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createObjectUnionOf(c1)
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataUnionOf(m.createDatatype("dr"))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataComplementOf(m.createDatatype("dr"))
        );
        Assertions.assertThrows(OntJenaException.Unsupported.class,
                () -> m.createDataRestriction(m.createDatatype("dr"))
        );
    }
}
