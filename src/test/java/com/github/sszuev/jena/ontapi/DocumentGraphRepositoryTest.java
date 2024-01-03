package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.impl.UnionGraphImpl;
import com.github.sszuev.jena.ontapi.impl.repositories.DocumentGraphRepository;
import com.github.sszuev.jena.ontapi.testutils.MiscUtils;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class DocumentGraphRepositoryTest {

    @Test
    public void testLoadResources() {
        DocumentGraphRepository repository =
                GraphRepository.createGraphDocumentRepository(
                                () -> new UnionGraphImpl(GraphMemFactory.createDefaultGraph())
                        )
                        .addMapping("http://www.w3.org/2002/07/owl#", "builtins-owl.rdf")
                        .addMapping("X", "builtins-rdfs.rdf");

        Graph g1 = repository.get("http://www.w3.org/2002/07/owl#");
        Assertions.assertEquals(159, g1.size());
        Assertions.assertInstanceOf(UnionGraph.class, g1);
        Assertions.assertFalse(((UnionGraph) g1).hasSubGraph());

        Graph g2 = repository.get("X");
        Assertions.assertEquals(163, g2.size());
        Assertions.assertInstanceOf(UnionGraph.class, g2);
        Assertions.assertFalse(((UnionGraph) g2).hasSubGraph());

        Assertions.assertEquals(2, repository.ids().count());

        Assertions.assertSame(g2, repository.remove("X"));

        Assertions.assertEquals(1, repository.ids().count());

        repository.clear();

        Assertions.assertEquals(0, repository.ids().count());
    }

    @Test
    public void testLoadFiles(@TempDir Path dir) {
        Path file = MiscUtils.save("/builtins-rdfs.rdf", dir);
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepository(null)
                .addMapping("http://www.w3.org/2002/07/owl#", file.toUri().toString());
        Graph g = repository.get("http://www.w3.org/2002/07/owl#");
        Assertions.assertEquals(163, g.size());
        Assertions.assertTrue(Graphs.isGraphMem(g));
    }
}
