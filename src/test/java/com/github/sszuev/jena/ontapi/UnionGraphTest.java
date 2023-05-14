package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.UnmodifiableGraph;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.ClosedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * To test {@link UnionGraph}.
 * <p>
 * Created by @ssz on 21.10.2018.
 */
@SuppressWarnings("WeakerAccess")
public class UnionGraphTest {

    static Graph createNamedGraph(String uri) {
        OntModel m = OntModelFactory.createModel();
        m.setID(uri);
        return m.getBaseGraph();
    }

    @SuppressWarnings("deprecation")
    static Graph createTestMemGraph(String name) {
        return new GraphMem() {
            @Override
            public String toString() {
                return String.format("[%s]", name);
            }
        };
    }

    private static void assertClosed(UnionGraph g, boolean expectedClosed) {
        if (expectedClosed) {
            Assertions.assertTrue(g.isClosed());
            Assertions.assertTrue(g.getBaseGraph().isClosed());
            return;
        }
        Assertions.assertFalse(g.isClosed());
        Assertions.assertFalse(g.getBaseGraph().isClosed());
    }

    @Test
    public void testAddRemoveSubGraphs() {
        UnionGraph a = new UnionGraph(createNamedGraph("a"));
        Graph b = createNamedGraph("b");
        a.addGraph(b);
        UnionGraph c = new UnionGraph(createNamedGraph("c"));
        a.addGraph(c);
        UnionGraph d = new UnionGraph(createNamedGraph("d"));
        c.addGraph(d);
        String tree = Graphs.importsTreeAsString(a);

        Assertions.assertEquals(4, tree.split("\n").length);
        d.addGraph(b);
        tree = Graphs.importsTreeAsString(a);

        Assertions.assertEquals(5, tree.split("\n").length);
        // recursion:
        d.addGraph(c);
        tree = Graphs.importsTreeAsString(a);

        Assertions.assertEquals(6, tree.split("\n").length);

        Graph h = createNamedGraph("H");
        c.addGraph(h);
        a.removeParent(b);
        a.addGraph(b = new UnionGraph(b));
        ((UnionGraph) b).addGraph(h);
        tree = Graphs.importsTreeAsString(a);

        Assertions.assertEquals(8, tree.split("\n").length);

        // remove recursion:
        d.removeParent(c);
        tree = Graphs.importsTreeAsString(a);

        Assertions.assertEquals(7, tree.split("\n").length);
    }

    @Test
    public void testWrapAsUnmodified() {
        Triple a = Triple.create(NodeFactory.createURI("a"), RDF.Nodes.type, OWL.Class.asNode());
        Triple b = Triple.create(NodeFactory.createURI("b"), RDF.Nodes.type, OWL.Class.asNode());

        Graph base = Factory.createDefaultGraph();
        base.getPrefixMapping().setNsPrefixes(OntModelFactory.STANDARD);
        base.add(a);
        Graph unmodified = new UnmodifiableGraph(base);
        Assertions.assertEquals(1, unmodified.find().toSet().size());
        Assertions.assertEquals(4, unmodified.getPrefixMapping().numPrefixes());

        UnionGraph u = new UnionGraph(unmodified);
        Assertions.assertEquals(4, u.getPrefixMapping().numPrefixes());

        try {
            u.getPrefixMapping().setNsPrefix("x", "http://x#");
            Assertions.fail("Possible to add prefix");
        } catch (PrefixMapping.JenaLockedException lj) {
            // expected
        }

        Assertions.assertEquals(4, u.getPrefixMapping().numPrefixes());
        try {
            u.add(b);
            Assertions.fail("Possible to add triple");
        } catch (AddDeniedException aj) {
            // expected
        }
        try {
            u.delete(a);
            Assertions.fail("Possible to delete triple");
        } catch (DeleteDeniedException dj) {
            // expected
        }
        Assertions.assertEquals(1, unmodified.find().toSet().size());

        base.add(b);
        base.getPrefixMapping().setNsPrefix("x", "http://x#").setNsPrefix("y", "http://y#");
        Assertions.assertEquals(2, u.find().toSet().size());
        Assertions.assertEquals(6, u.getPrefixMapping().numPrefixes());
    }

    @Test
    public void testCloseRecursiveGraph() {
        UnionGraph a = new UnionGraph(Factory.createGraphMem());
        UnionGraph b = new UnionGraph(Factory.createGraphMem());
        UnionGraph c = new UnionGraph(Factory.createGraphMem());
        UnionGraph d = new UnionGraph(Factory.createGraphMem());
        UnionGraph e = new UnionGraph(Factory.createGraphMem());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);
        assertClosed(d, false);
        assertClosed(e, false);

        c.addGraph(a);
        b.addGraph(c);
        c.addGraph(b).addGraph(d).addGraph(e);
        a.addGraph(c);

        c.close();
        assertClosed(a, true);
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(d, true);
        assertClosed(e, true);
    }

    @Test
    public void testCloseHierarchyGraph() {
        UnionGraph a = new UnionGraph(Factory.createGraphMem());
        UnionGraph b = new UnionGraph(Factory.createGraphMem());
        UnionGraph c = new UnionGraph(Factory.createGraphMem());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);

        a.addGraph(b.addGraph(c));

        b.close();
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(a, false);

        UnionGraph d = new UnionGraph(Factory.createGraphMem());
        try {
            b.addGraph(d);
            Assertions.fail("Possible to add a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        try {
            b.removeParent(c);
            Assertions.fail("Possible to remove a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        Assertions.assertNotNull(a.addGraph(d));
        Assertions.assertEquals(4, a.listBaseGraphs().toList().size());

        Assertions.assertNotNull(a.removeParent(b));
        Assertions.assertEquals(2, a.listBaseGraphs().toList().size());
    }

    @Test
    public void testDependsOn() {
        Graph g1 = Factory.createGraphMem();
        Graph g2 = Factory.createGraphMem();
        UnionGraph a = new UnionGraph(g1);
        Assertions.assertTrue(a.dependsOn(a));
        Assertions.assertTrue(a.dependsOn(g1));
        Assertions.assertFalse(g1.dependsOn(a));
        Assertions.assertFalse(a.dependsOn(Factory.createGraphMem()));

        UnionGraph b = new UnionGraph(g1);
        UnionGraph c = new UnionGraph(Factory.createGraphMem());
        a.addGraph(b.addGraph(c));
        Assertions.assertEquals(2, a.listBaseGraphs().toList().size());
        String tree = Graphs.importsTreeAsString(a);
        Assertions.assertEquals(3, tree.split("\n").length);
        Assertions.assertEquals(0, StringUtils.countMatches(tree, Graphs.RECURSIVE_GRAPH_IDENTIFIER));

        Assertions.assertTrue(a.dependsOn(b));
        Assertions.assertTrue(a.dependsOn(c));
        Assertions.assertTrue(a.dependsOn(c.getBaseGraph()));
        Assertions.assertFalse(a.dependsOn(g2));

        UnionGraph d = new UnionGraph(createNamedGraph("d"));
        c.addGraph(d);
        // recursion:
        d.addGraph(a);
        Assertions.assertEquals(3, a.listBaseGraphs().toList().size());
        tree = Graphs.importsTreeAsString(a);
        Assertions.assertEquals(5, tree.split("\n").length);
        Assertions.assertEquals(4, StringUtils.countMatches(tree, Graphs.NULL_ONTOLOGY_IDENTIFIER));
        Assertions.assertEquals(1, StringUtils.countMatches(tree, Graphs.RECURSIVE_GRAPH_IDENTIFIER));

        Assertions.assertTrue(a.dependsOn(b));
        Assertions.assertTrue(a.dependsOn(c));
        Assertions.assertTrue(a.dependsOn(d));
        Assertions.assertTrue(c.dependsOn(d));
        Assertions.assertTrue(d.dependsOn(c));
        Assertions.assertTrue(d.dependsOn(a));
        Assertions.assertFalse(a.dependsOn(g2));
    }

    @Test
    public void testListBaseGraphs1() {
        Graph a = createTestMemGraph("a");
        Graph b = createTestMemGraph("b");
        Graph c = createTestMemGraph("c");
        UnionGraph u1 = new UnionGraph(a);
        UnionGraph u2 = new UnionGraph(b);
        UnionGraph u3 = new UnionGraph(c);
        u1.addGraph(u1);
        u1.addGraph(u2);
        u1.addGraph(u3);
        u1.addGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c)), u1.listBaseGraphs().toSet());
    }

    @Test
    public void testListBaseGraphs2() {
        Graph a = createTestMemGraph("a");
        Graph b = createTestMemGraph("b");
        Graph c = createTestMemGraph("c");
        Graph d = createTestMemGraph("d");
        UnionGraph u1 = new UnionGraph(new UnionGraph(a).addGraph(d));
        UnionGraph u2 = new UnionGraph(b);
        UnionGraph u3 = new UnionGraph(new UnionGraph(c));
        u1.addGraph(u1);
        u1.addGraph(u2);
        u1.addGraph(u3);
        u1.addGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c, d)), u1.listBaseGraphs().toSet());
    }
}
