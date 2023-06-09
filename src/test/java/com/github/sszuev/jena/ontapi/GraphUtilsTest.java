package com.github.sszuev.jena.ontapi;

import com.github.andrewoma.dexx.collection.Sets;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
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
        return Stream.concat(Stream.of(Graphs.getBase(graph)), Graphs.subGraphs(graph).flatMap(GraphUtilsTest::flat));
    }

    @Test
    public void testListBaseGraphs() {
        UnionGraph u = new UnionGraph(UnionGraphTest.createTestMemGraph("a"));
        u.addGraph(UnionGraphTest.createTestMemGraph("b"));
        u.addGraph(UnionGraphTest.createTestMemGraph("c"));
        UnionGraph g2 = new UnionGraph(UnionGraphTest.createTestMemGraph("d"));
        g2.addGraph(UnionGraphTest.createTestMemGraph("e"));
        u.addGraph(g2);
        u.addGraph(new WrappedGraph(UnionGraphTest.createTestMemGraph("x")));
        u.addGraph(new GraphWrapper(UnionGraphTest.createTestMemGraph("y")));

        Set<Graph> actual = Graphs.baseGraphs(u).collect(Collectors.toSet());
        Assertions.assertEquals(7, actual.size());
        Assertions.assertEquals(flat(u).collect(Collectors.toSet()), actual);
    }

    @Test
    public void testIsSized() {
        Assertions.assertTrue(Graphs.isSized(new GraphMem()));
        Assertions.assertTrue(Graphs.isSized(new UnionGraph(new GraphMem())));
        Assertions.assertTrue(Graphs.isSized(new UnionGraph(new GraphWrapper(new GraphMem()))));

        UnionGraph u1 = new UnionGraph(new GraphMem());
        u1.addGraph(u1);
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
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraph(new GraphMem())));
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraph(new GraphWrapper(new GraphMem()))));

        UnionGraph u1 = new UnionGraph(new GraphMem(), false);
        Assertions.assertTrue(Graphs.isDistinct(u1));

        u1.addGraph(new GraphMem());
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

        Graph a = new UnionGraph(new GraphWrapper(new GraphWrapper(g)));
        Assertions.assertTrue(Graphs.isSameBase(a, g));

        MultiUnion b = new MultiUnion();
        b.addGraph(g);
        b.addGraph(new GraphMem());
        Assertions.assertTrue(Graphs.isSameBase(a, b));

        UnionGraph c = new UnionGraph(new GraphWrapper(g));
        Assertions.assertTrue(Graphs.isSameBase(a, c));

        Assertions.assertFalse(Graphs.isSameBase(g, new GraphMem()));

        Graph d = new UnionGraph(new WrappedGraph(new WrappedGraph(g)));
        Assertions.assertFalse(Graphs.isSameBase(a, d));

        Assertions.assertFalse(Graphs.isSameBase(new UnionGraph(g), new UnionGraph(new GraphMem())));

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
}
