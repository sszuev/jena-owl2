package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.impl.UnionGraphImpl;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.ModelTestUtils;
import com.github.sszuev.jena.ontapi.testutils.UnmodifiableGraph;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        UnionGraph a = new UnionGraphImpl(createNamedGraph("a"));
        Graph b = createNamedGraph("b");
        a.addSubGraph(b);
        UnionGraph c = new UnionGraphImpl(createNamedGraph("c"));
        a.addSubGraph(c);
        UnionGraph d = new UnionGraphImpl(createNamedGraph("d"));
        c.addSubGraph(d);
        String tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(4, tree.split("\n").length);
        d.addSubGraph(b);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(5, tree.split("\n").length);
        // recursion:
        d.addSubGraph(c);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(6, tree.split("\n").length);

        Graph h = createNamedGraph("H");
        c.addSubGraph(h);
        a.removeSubGraph(b);
        a.addSubGraph(b = new UnionGraphImpl(b));
        ((UnionGraph) b).addSubGraph(h);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(8, tree.split("\n").length);

        // remove recursion:
        d.removeSubGraph(c);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(7, tree.split("\n").length);
    }

    @Test
    public void testWrapAsUnmodified() {
        Triple a = Triple.create(NodeFactory.createURI("a"), RDF.Nodes.type, OWL.Class.asNode());
        Triple b = Triple.create(NodeFactory.createURI("b"), RDF.Nodes.type, OWL.Class.asNode());

        Graph base = GraphMemFactory.createDefaultGraph();
        base.getPrefixMapping().setNsPrefixes(OntModelFactory.STANDARD);
        base.add(a);
        Graph unmodified = new UnmodifiableGraph(base);
        Assertions.assertEquals(1, unmodified.find().toSet().size());
        Assertions.assertEquals(4, unmodified.getPrefixMapping().numPrefixes());

        UnionGraph u = new UnionGraphImpl(unmodified);
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
        UnionGraph a = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraph b = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraph c = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraph d = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraph e = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);
        assertClosed(d, false);
        assertClosed(e, false);

        c.addSubGraph(a);
        b.addSubGraph(c);
        c.addSubGraph(b).addSubGraph(d).addSubGraph(e);
        a.addSubGraph(c);

        c.close();
        assertClosed(a, true);
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(d, true);
        assertClosed(e, true);
    }

    @Test
    public void testCloseHierarchyGraph() {
        UnionGraphImpl a = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraphImpl b = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        UnionGraphImpl c = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);

        a.addSubGraph(b.addSubGraph(c));

        b.close();
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(a, false);

        UnionGraphImpl d = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        try {
            b.addSubGraph(d);
            Assertions.fail("Possible to add a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        try {
            b.removeSubGraph(c);
            Assertions.fail("Possible to remove a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        Assertions.assertNotNull(a.addSubGraph(d));
        Assertions.assertEquals(4, a.listSubGraphBases().toList().size());

        Assertions.assertNotNull(a.removeSubGraph(b));
        Assertions.assertEquals(2, a.listSubGraphBases().toList().size());
    }

    @Test
    public void testDependsOn() {
        Graph g1 = GraphMemFactory.createGraphMem();
        Graph g2 = GraphMemFactory.createGraphMem();
        UnionGraphImpl a = new UnionGraphImpl(g1);
        Assertions.assertTrue(a.dependsOn(a));
        Assertions.assertTrue(a.dependsOn(g1));
        Assertions.assertFalse(g1.dependsOn(a));
        Assertions.assertFalse(a.dependsOn(GraphMemFactory.createGraphMem()));

        UnionGraphImpl b = new UnionGraphImpl(g1);
        UnionGraphImpl c = new UnionGraphImpl(GraphMemFactory.createGraphMem());
        a.addSubGraph(b.addSubGraph(c));
        Assertions.assertEquals(2, a.listSubGraphBases().toList().size());
        String tree = ModelTestUtils.importsTreeAsString(a);
        Assertions.assertEquals(3, tree.split("\n").length);
        Assertions.assertEquals(0, StringUtils.countMatches(tree, ModelTestUtils.RECURSIVE_GRAPH_IDENTIFIER));

        Assertions.assertTrue(a.dependsOn(b));
        Assertions.assertTrue(a.dependsOn(c));
        Assertions.assertTrue(a.dependsOn(c.getBaseGraph()));
        Assertions.assertFalse(a.dependsOn(g2));

        UnionGraphImpl d = new UnionGraphImpl(createNamedGraph("d"));
        c.addSubGraph(d);
        // recursion:
        d.addSubGraph(a);
        Assertions.assertEquals(3, a.listSubGraphBases().toList().size());
        tree = ModelTestUtils.importsTreeAsString(a);
        Assertions.assertEquals(5, tree.split("\n").length);
        Assertions.assertEquals(4, StringUtils.countMatches(tree, ModelTestUtils.ANONYMOUS_ONTOLOGY_IDENTIFIER));
        Assertions.assertEquals(1, StringUtils.countMatches(tree, ModelTestUtils.RECURSIVE_GRAPH_IDENTIFIER));

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
        UnionGraphImpl u1 = new UnionGraphImpl(a);
        UnionGraphImpl u2 = new UnionGraphImpl(b);
        UnionGraphImpl u3 = new UnionGraphImpl(c);
        u1.addSubGraph(u1);
        u1.addSubGraph(u2);
        u1.addSubGraph(u3);
        u1.addSubGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c)), u1.listSubGraphBases().toSet());
    }

    @Test
    public void testListBaseGraphs2() {
        Graph a = createTestMemGraph("a");
        Graph b = createTestMemGraph("b");
        Graph c = createTestMemGraph("c");
        Graph d = createTestMemGraph("d");
        UnionGraphImpl u1 = new UnionGraphImpl(new UnionGraphImpl(a).addSubGraph(d));
        UnionGraphImpl u2 = new UnionGraphImpl(b);
        UnionGraphImpl u3 = new UnionGraphImpl(new UnionGraphImpl(c));
        u1.addSubGraph(u1);
        u1.addSubGraph(u2);
        u1.addSubGraph(u3);
        u1.addSubGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c, d)), u1.listSubGraphBases().toSet());
    }

    @Test
    public void testListParents() {
        UnionGraph a = new UnionGraphImpl(createNamedGraph("A"));
        UnionGraph b = new UnionGraphImpl(createNamedGraph("B"));
        UnionGraph c = new UnionGraphImpl(createNamedGraph("C"));
        UnionGraph d = new UnionGraphImpl(createNamedGraph("D"));
        UnionGraph e = new UnionGraphImpl(createNamedGraph("E"));

        a.addSubGraph(b);
        a.addSubGraph(c);
        b.addSubGraph(d);
        a.addSubGraph(d);
        d.addSubGraph(e);
        e.addSubGraph(a);
        e.addSubGraph(e);

        Assertions.assertEquals(List.of(e), a.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a), b.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a), c.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(Set.of(a, b), d.superGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d, e), e.superGraphs().collect(Collectors.toSet()));
    }

}
