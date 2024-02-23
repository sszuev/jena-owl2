package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class OntModelOWL2QLSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
    })
    public void testObjectSomeValuesFrom(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty p = data.createObjectProperty("p");
        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectSomeValuesFrom(p, c0);
        OntClass c2 = data.createObjectSomeValuesFrom(p, data.createObjectUnionOf(c0));
        OntClass c3 = data.createObjectSomeValuesFrom(p, data.getOWLThing());

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c1.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));
        Assertions.assertFalse(c2.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.ObjectSomeValuesFrom.class));

        Assertions.assertTrue(c1.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c2.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(3, m.ontObjects(OntClass.class).count());

        OntClass oc1 = c1.inModel(m).as(OntClass.ObjectSomeValuesFrom.class);
        OntClass oc3 = c3.inModel(m).as(OntClass.ObjectSomeValuesFrom.class);

        Assertions.assertFalse(oc1.canBeSubClass());
        Assertions.assertTrue(oc3.canBeSubClass());
        Assertions.assertTrue(oc1.canBeSuperClass());
        Assertions.assertTrue(oc3.canBeSuperClass());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectSomeValuesFrom(p, oc1));

        Assertions.assertFalse(m.createObjectSomeValuesFrom(p, c0).canBeSubClass());
        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntClass.ObjectSomeValuesFrom.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
    })
    public void testObjectIntersectionOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty op = data.createObjectProperty("p");
        OntDataProperty dp = data.createDataProperty("d");
        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectUnionOf(c0);

        OntClass c2 = data.createObjectSomeValuesFrom(op, c0);
        OntClass c3 = data.createDataSomeValuesFrom(dp, data.getDatatype(XSD.xstring.getURI()));
        OntClass c4 = data.createDataSomeValuesFrom(dp, data.getDatatype(OWL.rational.getURI()));
        OntClass c5 = data.createObjectSomeValuesFrom(op, data.getOWLThing());

        OntClass c6 = data.createObjectIntersectionOf(c2, c1);
        OntClass c7 = data.createObjectIntersectionOf(c2, c3);
        OntClass c8 = data.createObjectIntersectionOf(c2, c3, c5, c1);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c6.inModel(m).canAs(OntClass.IntersectionOf.class));
        Assertions.assertTrue(c7.inModel(m).canAs(OntClass.IntersectionOf.class));
        Assertions.assertTrue(c8.inModel(m).canAs(OntClass.IntersectionOf.class));

        Assertions.assertEquals(2, m.ontObjects(OntClass.IntersectionOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(7, m.ontObjects(OntClass.class).count());


        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createObjectIntersectionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c0));
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c0, c1));
        m.createObjectIntersectionOf(c0, c4);
        Assertions.assertEquals(8, m.ontObjects(OntClass.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_QL_MEM",
    })
    public void testObjectComplementOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntObjectProperty op = data.createObjectProperty("p");
        OntDataProperty dp = data.createDataProperty("d");

        OntClass c0 = data.createOntClass("c");
        OntClass c1 = data.createObjectUnionOf(c0);
        OntClass c2 = data.createDataSomeValuesFrom(dp, data.getDatatype(XSD.xstring.getURI()));

        OntClass c3 = data.createObjectComplementOf(c0);
        OntClass c4 = data.createObjectComplementOf(c1);
        OntClass c5 = data.createObjectComplementOf(c2);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertTrue(c3.inModel(m).canAs(OntClass.ComplementOf.class));
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.ComplementOf.class));
        Assertions.assertTrue(c5.inModel(m).canAs(OntClass.ComplementOf.class));

        Assertions.assertEquals(2, m.ontObjects(OntClass.ComplementOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectComplementOf(c1));
        m.createObjectComplementOf(c2);
        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
    }
}
