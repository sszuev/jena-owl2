package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;

public class OntModelClassesTest {

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
    })
    public void testListClasses1a(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        List<String> actual = m.ontObjects(OntClass.class)
                .map(it -> it.isAnon() ? "null" : it.getLocalName())
                .sorted()
                .collect(Collectors.toList());
        // TODO: there is DataHasValue Restriction in the RDF,
        //  but connected property (`owl:onProperty`) has no `owl:DatatypeProperty`
        //  declaration (it is declared as bar `rdf:Property`),
        //  so in strict mode such construction cannot be considered as a valid class expression
        List<String> expected = List.of("A", "B", "C", "D", "E", "X0", "X1", "Y0", "Y1", "Z");
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
    })
    public void testListClasses1b(TestSpec spec) {
        OntModel m = RDFIOTestUtils.readResourceToModel(
                OntModelFactory.createModel(spec.inst), "/list-syntax-categories-test.rdf", Lang.RDFXML
        );
        Assertions.assertEquals(0, m.ontObjects(OntClass.class).count());
    }
}
