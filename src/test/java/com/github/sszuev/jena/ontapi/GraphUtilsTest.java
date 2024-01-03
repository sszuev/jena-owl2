package com.github.sszuev.jena.ontapi;

import com.github.andrewoma.dexx.collection.Sets;
import com.github.sszuev.jena.ontapi.impl.UnionGraphImpl;
import com.github.sszuev.jena.ontapi.testutils.ModelTestUtils;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelGraphInterface;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * To test {@link Graphs} utility class.
 * Created by @szz on 11.06.2019.
 */
@SuppressWarnings("deprecation")
public class GraphUtilsTest {

    private static Stream<Graph> flat(Graph graph) {
        if (graph == null) return Stream.empty();
        return Stream.concat(Stream.of(Graphs.getBase(graph)), Graphs.directSubGraphs(graph).flatMap(GraphUtilsTest::flat));
    }

    @Test
    public void testListBaseGraphs() {
        UnionGraph u = new UnionGraphImpl(UnionGraphTest.createTestMemGraph("a"));
        u.addSubGraph(UnionGraphTest.createTestMemGraph("b"));
        u.addSubGraph(UnionGraphTest.createTestMemGraph("c"));
        UnionGraph g2 = new UnionGraphImpl(UnionGraphTest.createTestMemGraph("d"));
        g2.addSubGraph(UnionGraphTest.createTestMemGraph("e"));
        u.addSubGraph(g2);
        u.addSubGraph(new WrappedGraph(UnionGraphTest.createTestMemGraph("x")));
        u.addSubGraph(new GraphWrapper(UnionGraphTest.createTestMemGraph("y")));

        Set<Graph> actual = Graphs.dataGraphs(u).collect(Collectors.toSet());
        Assertions.assertEquals(7, actual.size());
        Assertions.assertEquals(flat(u).collect(Collectors.toSet()), actual);
    }

    @Test
    public void testIsSized() {
        Assertions.assertTrue(Graphs.isSized(new GraphMem()));
        Assertions.assertTrue(Graphs.isSized(new UnionGraphImpl(new GraphMem())));
        Assertions.assertTrue(Graphs.isSized(new UnionGraphImpl(new GraphWrapper(new GraphMem()))));

        UnionGraph u1 = new UnionGraphImpl(new GraphMem());
        u1.addSubGraph(u1);
        Assertions.assertFalse(Graphs.isSized(u1));

        Graph g = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
                throw new AssertionError();
            }
        };
        Assertions.assertFalse(Graphs.isSized(g));
    }

    @Test
    public void testIsDistinct() {
        Assertions.assertTrue(Graphs.isDistinct(new GraphMem()));
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraphImpl(new GraphMem())));
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraphImpl(new GraphWrapper(new GraphMem()))));

        UnionGraph u1 = new UnionGraphImpl(new GraphMem(), false);
        Assertions.assertTrue(Graphs.isDistinct(u1));

        u1.addSubGraph(new GraphMem());
        Assertions.assertFalse(Graphs.isDistinct(u1));

        Graph g = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
                throw new AssertionError();
            }
        };
        Assertions.assertFalse(Graphs.isDistinct(g));
    }

    @Test
    public void testIsSame() {
        Graph g = Factory.createGraphMem();
        Assertions.assertTrue(Graphs.isSameBase(g, g));

        Graph a = new UnionGraphImpl(new GraphWrapper(new GraphWrapper(g)));
        Assertions.assertTrue(Graphs.isSameBase(a, g));

        MultiUnion b = new MultiUnion();
        b.addGraph(g);
        b.addGraph(new GraphMem());
        Assertions.assertTrue(Graphs.isSameBase(a, b));

        UnionGraph c = new UnionGraphImpl(new GraphWrapper(g));
        Assertions.assertTrue(Graphs.isSameBase(a, c));

        Assertions.assertFalse(Graphs.isSameBase(g, new GraphMem()));

        Graph d = new UnionGraphImpl(new WrappedGraph(new WrappedGraph(g)));
        Assertions.assertFalse(Graphs.isSameBase(a, d));

        Assertions.assertFalse(Graphs.isSameBase(new UnionGraphImpl(g), new UnionGraphImpl(new GraphMem())));

        MultiUnion e = new MultiUnion();
        e.addGraph(new GraphMem());
        e.addGraph(g);
        Assertions.assertFalse(Graphs.isSameBase(b, e));
    }

    @Test
    public void testCollectPrefixes() {
        Graph a = new GraphMem();
        Graph b = new GraphMem();
        Graph c = new GraphMem();
        a.getPrefixMapping().setNsPrefix("a1", "x1").setNsPrefix("a2", "x2");
        b.getPrefixMapping().setNsPrefix("b1", "x3");
        c.getPrefixMapping().setNsPrefix("b2", "x4");

        Assertions.assertEquals(4, Graphs.collectPrefixes(Arrays.asList(a, b, c)).numPrefixes());
        Assertions.assertEquals(3, Graphs.collectPrefixes(Arrays.asList(a, b)).numPrefixes());
        Assertions.assertEquals(2, Graphs.collectPrefixes(Arrays.asList(b, c)).numPrefixes());
        Assertions.assertEquals(1, Graphs.collectPrefixes(Collections.singleton(b)).numPrefixes());

        try {
            Graphs.collectPrefixes(Sets.of(b, c)).setNsPrefix("X", "x");
            Assertions.fail();
        } catch (PrefixMapping.JenaLockedException j) {
            // expected
        }
    }

    @Test
    public void testMakeUnionGraph() {
        String B = "b";
        String C = "c";
        String D = "d";
        String E = "e";
        String F = "f";
        String G = "g";
        String H = "h";
        String K = "k";

        Model mA = ModelFactory.createDefaultModel();
        Model mB = ModelFactory.createDefaultModel();
        Model mC = ModelFactory.createDefaultModel();
        Model mD = ModelFactory.createDefaultModel();
        Model mE = ModelFactory.createDefaultModel();
        Model mF = ModelFactory.createDefaultModel();
        Model mG = ModelFactory.createDefaultModel();
        Model mH = ModelFactory.createDefaultModel();
        Model mK = ModelFactory.createDefaultModel();

        //   a
        //  / \
        // c    b
        // . \
        // .   d
        // .   |
        // .   e
        // . / | \
        // с   g  f
        //       /
        //      h
        mA.createResource().addProperty(RDF.type, OWL.Ontology)
                .addProperty(OWL.imports, mA.createResource(B))
                .addProperty(OWL.imports, mA.createResource(C));
        mB.createResource(B, OWL.Ontology);
        mC.createResource(C, OWL.Ontology).addProperty(OWL.imports, mC.createResource(D));
        mD.createResource(D, OWL.Ontology).addProperty(OWL.imports, mD.createResource(E));
        mE.createResource(E, OWL.Ontology)
                .addProperty(OWL.imports, mE.createResource(F))
                .addProperty(OWL.imports, mE.createResource(G))
                .addProperty(OWL.imports, mE.createResource(C)); // cycle
        mF.createResource(F, OWL.Ontology)
                .addProperty(OWL.imports, mE.createResource(H));
        mG.createResource(G, OWL.Ontology);
        mH.createResource(H, OWL.Ontology);
        mK.createResource(K, OWL.Ontology);

        UnionGraph actual = Graphs.makeOntUnion(
                mA.getGraph(),
                Stream.of(mA, mB, mC, mD, mE, mF, mG, mH, mK)
                        .map(ModelGraphInterface::getGraph)
                        .collect(Collectors.toSet()),
                UnionGraphImpl::new
        );

        Assertions.assertSame(mA.getGraph(), actual.getBaseGraph());
        Assertions.assertEquals(List.of(B, C), ModelTestUtils.getSubGraphsIris(actual));

        UnionGraph gB = (UnionGraph) ModelTestUtils.findSubGraphByIri(actual, B).orElseThrow();
        UnionGraph gC = (UnionGraph) ModelTestUtils.findSubGraphByIri(actual, C).orElseThrow();
        Assertions.assertSame(mB.getGraph(), gB.getBaseGraph());
        Assertions.assertSame(mC.getGraph(), gC.getBaseGraph());
        Assertions.assertFalse(gB.hasSubGraph());
        Assertions.assertEquals(List.of(D), ModelTestUtils.getSubGraphsIris(gC));

        UnionGraph gD = (UnionGraph) ModelTestUtils.findSubGraphByIri(gC, D).orElseThrow();
        Assertions.assertSame(mD.getGraph(), gD.getBaseGraph());
        Assertions.assertEquals(List.of(E), ModelTestUtils.getSubGraphsIris(gD));

        UnionGraph gE = (UnionGraph) ModelTestUtils.findSubGraphByIri(gD, E).orElseThrow();
        Assertions.assertSame(mE.getGraph(), gE.getBaseGraph());
        Assertions.assertEquals(List.of(C, F, G), ModelTestUtils.getSubGraphsIris(gE));

        UnionGraph gCofE = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, C).orElseThrow();
        UnionGraph gF = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, F).orElseThrow();
        UnionGraph gG = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, G).orElseThrow();
        Assertions.assertSame(gC, gCofE);
        Assertions.assertSame(mF.getGraph(), gF.getBaseGraph());
        Assertions.assertSame(mG.getGraph(), gG.getBaseGraph());
        Assertions.assertFalse(gG.hasSubGraph());
        Assertions.assertEquals(List.of(H), ModelTestUtils.getSubGraphsIris(gF));

        UnionGraph gH = (UnionGraph) ModelTestUtils.findSubGraphByIri(gF, H).orElseThrow();
        Assertions.assertSame(mH.getGraph(), gH.getBaseGraph());
        Assertions.assertFalse(gH.hasSubGraph());
    }

    @Test
    public void testIsOntUnionGraph() {
        String A = "a";
        String B = "b";
        String C = "c";
        String D = "d";
        String E = "e";

        Model mA = ModelFactory.createDefaultModel();
        Model mB = ModelFactory.createDefaultModel();
        Model mC = ModelFactory.createDefaultModel();
        Model mD = ModelFactory.createDefaultModel();
        Model mE = ModelFactory.createDefaultModel();
        mA.createResource(A, OWL.Ontology)
                .addProperty(OWL.imports, mA.createResource(B))
                .addProperty(OWL.imports, mA.createResource(C))
                .addProperty(OWL.imports, mD.createResource(E));
        mB.createResource(B, OWL.Ontology);
        mC.createResource(C, OWL.Ontology).addProperty(OWL.imports, mC.createResource(D));
        mD.createResource(D, OWL.Ontology);

        UnionGraph u = new UnionGraphImpl(mA.getGraph())
                .addSubGraph(new UnionGraphImpl(mB.getGraph()))
                .addSubGraph(new UnionGraphImpl(mC.getGraph())
                        .addSubGraph(new UnionGraphImpl(mD.getGraph())));

        Assertions.assertTrue(Graphs.isOntUnionGraph(u));

        u.addSubGraph(new UnionGraphImpl(mE.getGraph()));
        Assertions.assertFalse(Graphs.isOntUnionGraph(u));

        mE.createResource(E, OWL.Ontology);
        Assertions.assertTrue(Graphs.isOntUnionGraph(u));
    }
}
