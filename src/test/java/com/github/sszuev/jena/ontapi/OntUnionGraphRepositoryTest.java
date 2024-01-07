package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelGraphInterface;
import org.apache.jena.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntUnionGraphRepositoryTest {

    @Test
    public void testCreateOntModelTree() {
        OntModel a = OntModelFactory.createModel().setID("A").getModel();
        OntModel b = OntModelFactory.createModel().setID("B").getModel();
        OntModel c = OntModelFactory.createModel().setID("C").getModel();
        OntModel d = OntModelFactory.createModel().setID("D").getModel();
        OntModel e = OntModelFactory.createModel().setID("E").getModel();
        OntModel f = OntModelFactory.createModel().setID("F").getModel();

        a.addImport(b);
        a.addImport(c);
        b.addImport(d);
        d.addImport(a); // cycle
        d.addImport(e);

        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();

        OntModel inRepo = OntModelFactory.createModel(a.getGraph(), OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);

        Assertions.assertEquals(5, repository.count());
        Assertions.assertEquals(
                Stream.of(a, b, c, d, e).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );

        d.addImport(f);
        Assertions.assertEquals(6, repository.count());
        Assertions.assertEquals(f.getGraph(), repository.get("F"));
    }

    @Test
    public void testAddImportModel() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository).setID("A").getModel();

        Assertions.assertEquals(List.of("A"), repository.ids().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a.getGraph()), repository.graphs().collect(Collectors.toList()));

        a.createOntClass("FromA");

        OntModel b = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);
        b.createOntClass("FromB");

        b.addImport(a);
        Assertions.assertEquals(2, repository.count());

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

        Assertions.assertSame(ag, repository.get(a.getID().getURI()));
        Assertions.assertSame(bg, repository.get(b.getID().asNode().toString()));
        Assertions.assertSame(cg, repository.get(c.getID().getURI()));

        Assertions.assertEquals(3, repository.ids().count());
    }

    @Test
    public void testAddImportStatement() {
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
