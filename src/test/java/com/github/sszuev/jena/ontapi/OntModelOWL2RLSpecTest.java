package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.vocabulary.RDFS;
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
}
