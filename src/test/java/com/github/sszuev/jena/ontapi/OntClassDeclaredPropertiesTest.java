package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class OntClassDeclaredPropertiesTest {

    @Test
    public void testListDeclaredProperties() {
        OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_BUILTIN_INF).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass A = m.createOntClass("A");
        OntClass B = m.createOntClass("B");
        OntClass C = m.createOntClass("C");
        OntClass D = m.createOntClass("D");
        OntClass E = m.createOntClass("E");
        A.addSuperClass(B);
        B.addSuperClass(C);
        C.addSuperClass(D);
        E.addSuperClass(m.getOWLThing());

        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntObjectProperty o1 = m.createObjectProperty("o1");
        OntObjectProperty o2 = m.createObjectProperty("o2");
        o1.addSuperProperty(o2);
        o2.addSuperProperty(m.getOWLTopObjectProperty());
        d1.addDomain(A);
        d2.addDomain(B);
        o1.addDomain(C);
        o2.addDomain(m.getOWLThing());
        m.getOWLBottomDataProperty().addDomain(m.getOWLNothing());

        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLThing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(), m.getOWLNothing().declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), m.getOWLNothing().declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d1), A.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d1, d2, o1, o2), A.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(d2), B.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d2, o1, o2), B.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o1), C.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o1, o2), C.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), D.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), D.declaredProperties(false).collect(Collectors.toSet()));

        Assertions.assertEquals(Set.of(o2), E.declaredProperties(true).collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(o2), E.declaredProperties(false).collect(Collectors.toSet()));
    }

}
