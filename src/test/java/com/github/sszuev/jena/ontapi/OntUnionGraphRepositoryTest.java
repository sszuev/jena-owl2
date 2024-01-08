package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelGraphInterface;
import org.apache.jena.reasoner.InfGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntUnionGraphRepositoryTest {

    @Test
    public void testChangeId1() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel("A", repository);
        OntModel b = OntModelFactory.createModel("B", repository);
        OntModel c = OntModelFactory.createModel("C", repository);

        a.addImport(b);
        Assertions.assertEquals(List.of("A", "B", "C"), repository.ids().sorted().collect(Collectors.toList()));
        Assertions.assertEquals(
                Stream.of(a, b, c).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );

        a.setID("A-X");
        Assertions.assertEquals(3, repository.count());
        Assertions.assertEquals(
                Stream.of(a, b, c).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );

        Assertions.assertEquals("A-X", a.getID().getURI());
        Assertions.assertEquals("B", b.getID().getURI());
        Assertions.assertEquals("C", c.getID().getURI());
        Assertions.assertEquals("A-X", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());
        Assertions.assertEquals(List.of("A-X", "B", "C"), repository.ids().sorted().collect(Collectors.toList()));

        a.getID().setVersionIRI("A-Y");
        Assertions.assertEquals("A-X", a.getID().getURI());
        Assertions.assertEquals("B", b.getID().getURI());
        Assertions.assertEquals("C", c.getID().getURI());
        Assertions.assertEquals("A-Y", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());
        Assertions.assertEquals(List.of("A-Y", "B", "C"), repository.ids().sorted().collect(Collectors.toList()));

        a.statements(null, RDF.type, OWL.Ontology).findFirst().orElseThrow().getSubject()
                .removeAll(OWL.versionIRI)
                .addProperty(OWL.versionIRI, a.createResource("A-Q"));
        Assertions.assertEquals("A-X", a.getID().getURI());
        Assertions.assertEquals("B", b.getID().getURI());
        Assertions.assertEquals("C", c.getID().getURI());
        Assertions.assertEquals("A-Q", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());
        Assertions.assertEquals(List.of("A-Q", "B", "C"), repository.ids().sorted().collect(Collectors.toList()));
        Assertions.assertEquals(
                Stream.of(a, b, c).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );
    }

    @Test
    public void testChangeId2() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel("A", repository);
        OntModel b = OntModelFactory.createModel("B", repository);
        OntModel c = OntModelFactory.createModel("C", repository);

        a.addImport(b);
        Assertions.assertEquals(3, repository.count());

        Assertions.assertThrows(OntJenaException.IllegalArgument.class, () -> b.setID("B-X"));

        Assertions.assertEquals("A", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());

        Assertions.assertThrows(OntJenaException.IllegalArgument.class,
                () -> b.createResource().addProperty(RDF.type, OWL.Ontology)
        );

        Assertions.assertEquals("A", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());

        Assertions.assertThrows(OntJenaException.IllegalArgument.class,
                () -> b.getGraph().add(
                        NodeFactory.createURI("B-v1"),
                        RDF.type.asNode(),
                        OWL.Ontology.asNode()
                )
        );

        Assertions.assertEquals("A", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());

        Assertions.assertThrows(OntJenaException.IllegalArgument.class,
                () -> b.getGraph().delete(
                        Graphs.ontologyNode(b.getGraph()).orElseThrow(),
                        RDF.type.asNode(),
                        OWL.Ontology.asNode()
                )
        );

        Assertions.assertEquals("A", a.getID().getImportsIRI());
        Assertions.assertEquals("B", b.getID().getImportsIRI());
        Assertions.assertEquals("C", c.getID().getImportsIRI());
        Assertions.assertEquals(List.of("A", "B", "C"), repository.ids().sorted().collect(Collectors.toList()));

    }

    @Test
    public void testCreateOntModel1() {
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
    public void testCreateOntModel2() {
        OntModel a = OntModelFactory.createModel().setID("A").getModel();
        OntModel b = OntModelFactory.createModel().setID("B").getModel();
        OntModel c = OntModelFactory.createModel().setID("C").getModel();
        OntModel d = OntModelFactory.createModel().setID("D").getModel();
        OntModel e = OntModelFactory.createModel().setID("E").getModel();
        OntModel f = OntModelFactory.createModel().setID("F").getModel();

        a.addImport(b);
        a.addImport(c);
        d.addImport(b);
        d.addImport(c);
        c.addImport(e);

        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();

        OntModel inRepo = OntModelFactory.createModel(c.getGraph(), OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);

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
    public void testCreateOntModel3() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_RDFS_INF, repository).setID("A").getModel();
        OntModel b = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_TRANS_INF, repository).setID("B").getModel();
        OntModel c = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM, repository).setID("C").getModel();

        a.addImport(b);

        Assertions.assertEquals(3, repository.count());

        Set<Graph> expected = Stream.of(a, b, c)
                .map(ModelGraphInterface::getGraph)
                .map(graph -> graph instanceof InfGraph ? ((InfGraph) graph).getRawGraph() : graph)
                .collect(Collectors.toSet());
        Set<Graph> actual = repository.graphs().collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }


    @Test
    public void testAddImportModel1() {
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
    public void testAddImportModel2() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        Model data = ModelFactory.createDefaultModel();
        data.createResource("A", OWL.Ontology);
        OntModel a = OntModelFactory.createModel(data.getGraph(), OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);

        Assertions.assertEquals(List.of("A"), repository.ids().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a.getGraph()), repository.graphs().collect(Collectors.toList()));

        OntModel b = OntModelFactory.createModel().setID("B").getModel();
        OntModel c = OntModelFactory.createModel().setID("C").getModel();
        OntModel d = OntModelFactory.createModel().setID("D").getModel();
        OntModel e = OntModelFactory.createModel().setID("E").getModel();

        b.addImport(c);
        c.addImport(d);
        c.addImport(e);

        Assertions.assertEquals(1, repository.count());
        a.addImport(c);

        List<Graph> expected = Stream.of(a, b, c, d, e).map(ModelGraphInterface::getGraph).collect(Collectors.toList());
        List<Graph> actual = repository.graphs()
                .sorted(Comparator.comparing(it -> ((UnionGraph) it).getBaseGraph().toString()))
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);

        UnionGraph ag = (UnionGraph) a.getGraph();
        UnionGraph bg = (UnionGraph) b.getGraph();
        UnionGraph cg = (UnionGraph) c.getGraph();
        UnionGraph dg = (UnionGraph) d.getGraph();
        UnionGraph eg = (UnionGraph) e.getGraph();

        Assertions.assertEquals(Set.of(cg), ag.subGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(cg), bg.subGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(dg, eg), cg.subGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(), dg.subGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(), eg.subGraphs().collect(Collectors.toSet()));
    }

    @Test
    public void testAddImportModel3() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        Model data = ModelFactory.createDefaultModel();
        data.createResource("A", OWL.Ontology);
        OntModel a = OntModelFactory.createModel(data.getGraph(), OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);

        Assertions.assertEquals(List.of("A"), repository.ids().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a.getGraph()), repository.graphs().collect(Collectors.toList()));

        OntModel b = OntModelFactory.createModel().setID("B").getModel();
        OntModel c = OntModelFactory.createModel().setID("C").getModel();
        OntModel d = OntModelFactory.createModel().setID("D").getModel();

        b.addImport(c);
        c.addImport(d);

        Assertions.assertEquals(1, repository.count());
        b.addImport(a);

        List<Graph> expected = Stream.of(a, b, c, d).map(ModelGraphInterface::getGraph).collect(Collectors.toList());
        List<Graph> actual = repository.graphs()
                .sorted(Comparator.comparing(it -> ((UnionGraph) it).getBaseGraph().toString()))
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
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

    @Test
    public void testAddGraphData1() {
        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel a = OntModelFactory.createModel("A", repository);
        OntModel b = OntModelFactory.createModel("B", repository);

        a.createOntClass("C-A-1");
        b.createOntClass("C-B");

        Model data1 = ModelFactory.createDefaultModel();
        data1.createResource("C-A-2", OWL.Class);
        data1.createResource("A").addProperty(OWL.imports, data1.createResource("B"));

        a.add(data1);

        Assertions.assertEquals(3, a.classes().count());
        Assertions.assertEquals(List.of(b.getGraph()), ((UnionGraph) a.getGraph()).subGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(2, repository.count());
        Assertions.assertEquals(
                Stream.of(a, b).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );

        Model data2 = ModelFactory.createDefaultModel();
        data2.createResource("X").addProperty(RDF.type, OWL.Ontology);

        // Adding data will result in invalid ontology ID
        Assertions.assertThrows(OntJenaException.IllegalArgument.class, () -> a.add(data2));
        Assertions.assertEquals(4, a.getBaseGraph().size());
        Assertions.assertEquals(List.of("A", "B"), repository.ids().sorted().collect(Collectors.toList()));

        // change id
        Model data3 = ModelFactory.createDefaultModel();
        data3.createResource("A").addProperty(OWL.versionIRI, data3.createResource("A-X"));
        a.add(data3);

        Assertions.assertEquals(3, a.classes().count());
        Assertions.assertEquals(List.of("A-X", "B"), repository.ids().sorted().collect(Collectors.toList()));
        Assertions.assertEquals(
                Stream.of(a, b).map(ModelGraphInterface::getGraph).collect(Collectors.toSet()),
                repository.graphs().collect(Collectors.toSet())
        );
    }

    @Test
    public void testAddGraphData2() {
        OntModel a = OntModelFactory.createModel().setID("A").getModel();
        OntModel b = OntModelFactory.createModel().setID("B").getModel();
        a.addImport(b);

        GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        OntModel A = OntModelFactory.createModel(a.getGraph(), OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository);
        Assertions.assertEquals(3, A.size());
        Assertions.assertEquals(List.of("A", "B"), repository.ids().sorted().collect(Collectors.toList()));

        Model data1 = ModelFactory.createDefaultModel();
        data1.createResource("B", OWL.Ontology);

        b.add(data1);
        Assertions.assertEquals(List.of("A", "B"), repository.ids().sorted().collect(Collectors.toList()));
        Assertions.assertEquals(3, A.size());

        Model data2 = ModelFactory.createDefaultModel();
        data2.createResource("B", OWL.Ontology).addProperty(OWL.versionIRI, data2.createResource("B-v1"));

        // Can't change ontology ID <B>: it is used by <A>
        Assertions.assertThrows(OntJenaException.IllegalArgument.class, () -> b.add(data2));
        Assertions.assertEquals(List.of("A", "B"), repository.ids().sorted().collect(Collectors.toList()));
        Assertions.assertEquals(3, A.size());
    }

}
