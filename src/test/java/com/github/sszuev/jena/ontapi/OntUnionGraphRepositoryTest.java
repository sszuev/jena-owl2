package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class OntUnionGraphRepositoryTest {

    @Test
    public void testAddImportModelSameRepository() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository).setID("A").getModel();
        a.createOntClass("X");

        OntModel b = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);
        b.createOntClass("Q");

        b.addImport(a);

        OntModel c = OntModelFactory.createModel().setID("C").getModel();
        a.addImport(c);

        Assertions.assertEquals(4, a.statements().count());
        Assertions.assertEquals(7, b.statements().count());

        UnionGraph ag = (UnionGraph) a.getGraph();
        UnionGraph bg = (UnionGraph) b.getGraph();
        UnionGraph cg = (UnionGraph) c.getGraph();

        Assertions.assertEquals(1, ag.subGraphs().count());
        Assertions.assertSame(cg, ag.subGraphs().findFirst().orElseThrow());

        Assertions.assertEquals(1, bg.subGraphs().count());
        Assertions.assertSame(ag, bg.subGraphs().findFirst().orElseThrow());

        Assertions.assertEquals(0, cg.subGraphs().count());

        Assertions.assertSame(ag.getBaseGraph(), repository.get(a.getID().getURI()));
        Assertions.assertSame(bg.getBaseGraph(), repository.get(b.getID().asNode().toString()));
        Assertions.assertSame(cg.getBaseGraph(), repository.get(c.getID().getURI()));
    }

    @Test
    public void testAddImportStatementSameRepository() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);
        UnionGraph ag = (UnionGraph) a.getGraph();

        Model b = OntModelFactory.createDefaultModel();
        repository.put("B", b.getGraph());

        Assertions.assertThrows(OntJenaException.IllegalArgument.class, () -> a.getID().addImport("B"));

        Assertions.assertFalse(ag.hasSubGraph());
        Assertions.assertFalse(a.getID().imports().findFirst().isPresent());

        b.createResource("B", OWL.Ontology);

        a.getID().addImport("B");
        Assertions.assertEquals(List.of("B"), a.getID().imports().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(b.getGraph()), ag.subGraphs().map(it -> ((UnionGraph) it).getBaseGraph()).collect(Collectors.toList()));
        Assertions.assertFalse(ag.subGraphs().map(it -> (UnionGraph) it).findFirst().orElseThrow().hasSubGraph());

        a.getID().addImport("C");
        Assertions.assertTrue(ag.subGraphs().map(it -> ((UnionGraph) it).getBaseGraph()).findFirst().isPresent());
        Assertions.assertTrue(ag.subGraphs().findFirst().isPresent());

        Assertions.assertEquals(3, repository.ids().count());
    }
}
