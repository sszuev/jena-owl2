package com.github.sszuev.jena.ontapi.testutils;

import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ModelTestUtils {
    /**
     * Recursively deletes all resource children.
     *
     * @param inModel Resource from a model
     */
    public static void deleteAll(Resource inModel) {
        deleteAll(inModel, new HashSet<>());
    }

    private static void deleteAll(Resource r, Set<Node> viewed) {
        if (!viewed.add(r.asNode())) {
            return;
        }
        r.listProperties().toSet().forEach(s -> {
            RDFNode o = s.getObject();
            if (o.isAnon()) {
                deleteAll(o.asResource(), viewed);
            }
            r.getModel().remove(s);
        });
    }

    /**
     * Recursively gets all statements related to the specified subject.
     * Note: {@code rdf:List} may content a large number of members (1000+),
     * which may imply heavy calculation.
     *
     * @param inModel Resource with associated model inside.
     * @return a {@code Set} of {@link Statement}s
     */
    public static Set<Statement> getAssociatedStatements(Resource inModel) {
        Set<Statement> res = new HashSet<>();
        calcAssociatedStatements(inModel, res);
        return res;
    }

    private static void calcAssociatedStatements(Resource root, Set<Statement> res) {
        if (root.canAs(RDFList.class)) {
            RDFList list = root.as(RDFList.class);
            if (list.isEmpty()) return;
            ModelUtils.getListStatements(list).forEach(statement -> {
                res.add(statement);
                if (!RDF.first.equals(statement.getPredicate())) return;
                RDFNode obj = statement.getObject();
                if (obj.isAnon())
                    calcAssociatedStatements(obj.asResource(), res);
            });
            return;
        }
        root.listProperties().forEachRemaining(statement -> {
            try {
                if (!statement.getObject().isAnon() ||
                        res.stream().anyMatch(s -> statement.getObject().equals(s.getSubject()))) // to avoid cycles
                    return;
                calcAssociatedStatements(statement.getObject().asResource(), res);
            } finally {
                res.add(statement);
            }
        });
    }

    /**
     * Recursively lists all ascending statements for the given {@link RDFNode RDF Node}.
     * <p>
     * More specifically, this function returns all statements,
     * which have either the specified node in an object position,
     * or its indirect ascendant in a graph tree, found by the same method.
     * Consider, the specified node {@code r} belongs to the following RDF:
     * <pre>{@code
     * <a>  p0 _:b0 .
     * _:b0 p1 _:b1 .
     * _:b1 p2  <x> .
     * _:b1 p3  r .
     * }</pre>
     * In this case the method will return three statements:
     * {@code _:b1 p3 r}, {@code _:b0 p1 _:b1} and {@code <a> p0 _:b0}.
     * The statement {@code _:b1 p2  <x>} is skipped since uri resource {@code <x>} is not an ascendant of {@code r}.
     * <p>
     * This is the opposite of the method {@link #listDescendingStatements(RDFNode)}.
     * <p>
     * Note: there is a danger of {@code StackOverflowError} in case graph contains a recursion.
     *
     * @param object, not {@code null} must be attached to a model
     * @return {@link ExtendedIterator} of {@link Statement}s
     */
    public static ExtendedIterator<Statement> listAscendingStatements(RDFNode object) {
        return Iterators.flatMap(object.getModel().listStatements(null, null, object),
                s -> s.getSubject().isAnon() ?
                        Iterators.concat(Iterators.of(s), listAscendingStatements(s.getSubject())) : Iterators.of(s));
    }

    /**
     * Recursively lists all descending statements for the given {@link RDFNode RDF Node}.
     * <p>
     * More specifically, this function returns all statements,
     * which have either the specified node in a subject position,
     * or its indirect descendant in a graph tree (if the node is anonymous resource), found by the same method.
     * Consider, the specified node {@code <a>} belongs to the following RDF:
     * <pre>{@code
     * <a>  p0 _:b0 .
     * _:b0 p1 _:b1 .
     * _:b1 p2  <x> .
     * <x> p3  <b> .
     * }</pre>
     * In this case the method will return three statements:
     * {@code <a>  p0 _:b0}, {@code :b0 p1 _:b1} and {@code _:b1 p2  <x>}.
     * The last statement is skipped, since {@code <x>} is uri resource.
     * <p>
     * This is the opposite of the method {@link #listAscendingStatements(RDFNode)}.
     * <p>
     * Note: there is a danger of {@code StackOverflowError} in case graph contains a recursion.
     *
     * @param subject, not {@code null} must be attached to a model
     * @return {@link ExtendedIterator} of {@link Statement}s
     */
    public static ExtendedIterator<Statement> listDescendingStatements(RDFNode subject) {
        if (!subject.isResource()) return NullIterator.instance();
        return Iterators.flatMap(subject.asResource().listProperties(),
                s -> s.getObject().isAnon() ?
                        Iterators.concat(Iterators.of(s), listDescendingStatements(s.getResource())) : Iterators.of(s));
    }

    /**
     * Prints a graph hierarchy tree.
     * For a valid ontology it should match an imports ({@code owl:imports}) tree also.
     * For debugging.
     * <p>
     * An examples of possible output:
     * <pre> {@code
     * <http://imports.test.Main.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *      <http://imports.test.D.ttl>
     * }, {@code
     * <http://imports.test.D.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *              <http://imports.test.Main.ttl>
     * } </pre>
     *
     * @param graph {@link Graph}
     * @return hierarchy tree as String
     */
    public static String importsTreeAsString(Graph graph) {
        Function<Graph, String> printDefaultGraphName = g -> g.getClass().getSimpleName() + "@" + Integer.toHexString(g.hashCode());
        return makeImportsTree(graph, g -> {
            if (g.isClosed()) return "Closed(" + printDefaultGraphName.apply(g) + ")";
            String res = Graphs.getName(g);
            if (Graphs.ANONYMOUS_ONTOLOGY_IDENTIFIER.equals(res)) {
                res += "(" + printDefaultGraphName.apply(g) + ")";
            }
            return res;
        }, "\t", "\t", new HashSet<>()).toString();
    }

    private static StringBuilder makeImportsTree(Graph graph,
                                                 Function<Graph, String> getName,
                                                 String indent,
                                                 String step,
                                                 Set<Graph> seen) {
        StringBuilder res = new StringBuilder();
        Graph base = Graphs.getBase(graph);
        String name = getName.apply(base);
        try {
            if (!seen.add(graph)) {
                return res.append(Graphs.RECURSIVE_GRAPH_IDENTIFIER).append(": ").append(name);
            }
            res.append(name).append("\n");
            Graphs.directSubGraphs(graph)
                    .sorted(Comparator.comparingLong(o -> Graphs.directSubGraphs(o).count()))
                    .forEach(sub -> res.append(indent)
                            .append(makeImportsTree(sub, getName, indent + step, step, seen)));
            return res;
        } finally {
            seen.remove(graph);
        }
    }
}
