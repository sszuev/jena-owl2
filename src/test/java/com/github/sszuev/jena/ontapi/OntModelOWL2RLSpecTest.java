package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;

public class OntModelOWL2RLSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
    })
    public void testSubObjectSomeValuesFrom(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntObjectProperty p = m.createObjectProperty("p");
        OntClass c0 = m.createOntClass("c0");
        OntClass c1 = m.createOntClass("c1");
        OntClass c2 = m.createOntClass("c2");
        OntClass c3 = m.createObjectSomeValuesFrom(p, c0);

        c1.addProperty(RDFS.subClassOf, c3);

        Assertions.assertEquals(0, c1.superClasses().count());
        Assertions.assertEquals(List.of(c1), c3.subClasses().collect(Collectors.toList()));

        c3.addProperty(RDFS.subClassOf, c2);
        Assertions.assertEquals(List.of(c2), c3.superClasses().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(c3), c2.subClasses().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_RL_MEM",
    })
    public void testObjectIntersectionOf(TestSpec spec) {
        OntModel data = OntModelFactory.createModel();
        OntDataProperty p = data.createDataProperty("p");
        OntClass c0 = data.createOntClass("c0");
        OntClass c1 = data.createOntClass("c1");
        OntClass c2 = data.createOntClass("c2");
        OntClass c3 = data.createDataMinCardinality(p, 42, data.getDatatype(XSD.xlong));
        OntClass c4 = data.createObjectIntersectionOf(c0);
        OntClass c5 = data.createObjectIntersectionOf(c1, c3);
        OntClass c6 = data.createObjectIntersectionOf(c1, c2);

        OntModel m = OntModelFactory.createModel(data.getGraph(), spec.inst);
        Assertions.assertFalse(c4.inModel(m).canAs(OntClass.class));
        Assertions.assertFalse(c5.inModel(m).canAs(OntClass.class));
        Assertions.assertTrue(c6.inModel(m).canAs(OntClass.class));

        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntClass.IntersectionOf.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createObjectIntersectionOf(c1, c3));

        m.createObjectIntersectionOf(c1, c5, c2);
        Assertions.assertEquals(5, m.ontObjects(OntClass.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.LogicalExpression.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.CollectionOf.class).count());
        Assertions.assertEquals(2, m.ontObjects(OntClass.IntersectionOf.class).count());
    }
}
