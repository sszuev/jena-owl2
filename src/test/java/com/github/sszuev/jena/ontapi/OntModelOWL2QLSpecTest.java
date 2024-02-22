package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
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
    }
}
