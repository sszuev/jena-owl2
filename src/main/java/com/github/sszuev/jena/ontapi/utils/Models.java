package com.github.sszuev.jena.ontapi.utils;

import com.github.sszuev.jena.ontapi.impl.objects.OntListImpl;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class-helper to work with {@link Model Jena Model}s and its related objects and components:
 * {@link RDFNode Jena RDF Node}, {@link Literal Jena Literal}, {@link Resource Jena Resource} and
 * {@link Statement Jena Statement}.
 * <p>
 * Created @ssz on 20.10.2016.
 */
@SuppressWarnings("WeakerAccess")
public class Models {
    public static final Comparator<RDFNode> RDF_NODE_COMPARATOR = (r1, r2) -> NodeCmp.compareRDFTerms(r1.asNode(), r2.asNode());
    public static final Comparator<Statement> STATEMENT_COMPARATOR = Comparator
            .comparing(Statement::getSubject, RDF_NODE_COMPARATOR)
            .thenComparing(Statement::getPredicate, RDF_NODE_COMPARATOR)
            .thenComparing(Statement::getObject, RDF_NODE_COMPARATOR);
    public static final RDFNode BLANK = new ResourceImpl();
    public static final Comparator<Statement> STATEMENT_COMPARATOR_IGNORE_BLANK = Comparator
            .comparing((Function<Statement, RDFNode>) s -> s.getSubject().isAnon() ? BLANK : s.getSubject(),
                    RDF_NODE_COMPARATOR)
            .thenComparing(s -> s.getPredicate().isAnon() ? BLANK : s.getPredicate(), RDF_NODE_COMPARATOR)
            .thenComparing(s -> s.getObject().isAnon() ? BLANK : s.getObject(), RDF_NODE_COMPARATOR);

    public static final Literal TRUE = ResourceFactory.createTypedLiteral(Boolean.TRUE);
    public static final Literal FALSE = ResourceFactory.createTypedLiteral(Boolean.FALSE);

    /**
     * Creates a typed []-list with the given type containing the resources from the given collection.
     *
     * @param model   {@link Model model} in which the []-list is created
     * @param type    {@link Resource} the type for new []-list
     * @param members Collection of {@link RDFNode}s
     * @return anonymous resource - the header of the typed []-list
     * @see OntList
     */
    public static RDFList createTypedList(Model model, Resource type, Collection<? extends RDFNode> members) {
        return createTypedList(model, type, members.iterator());
    }

    /**
     * Creates a typed list with the given type containing the resources from the given iterator.
     * A typed list is an anonymous resource that is created using the same rules as the standard {@link RDFList []-list}
     * (that is, using {@link RDF#first rdf:first}, {@link RDF#rest rdf:rest} and {@link RDF#nil rdf:nil} predicates),
     * but each item of this []-list has the specified type on predicate {@link RDF#type rdf:type}.
     *
     * @param model   {@link Model model} in which the []-list is created
     * @param type    {@link Resource} the type for new []-list
     * @param members {@link Iterator} of {@link RDFNode}s
     * @return anonymous resource - the header of the typed []-list
     * @see OntList
     */
    public static RDFList createTypedList(Model model, Resource type, Iterator<? extends RDFNode> members) {
        return OntListImpl.createTypedList((EnhGraph) model, type, members);
    }

    /**
     * Determines is s specified resource belongs to a list.
     *
     * @param model     Model
     * @param candidate Resource to test
     * @return true if specified resource is a member of some rdf:List
     */
    public static boolean isInList(Model model, Resource candidate) {
        return model.contains(null, RDF.first, candidate);
    }

    /**
     * Answers {@code true} iff the given statement belongs to some []-list.
     *
     * @param s {@link Statement}, not {@code null}
     * @return boolean
     */
    public static boolean isInList(Statement s) {
        return RDF.first.equals(s.getPredicate()) || RDF.rest.equals(s.getPredicate()) || RDF.nil.equals(s.getObject());
    }

    /**
     * Answers a set of all the RDF statements whose subject is one of the cells of the given list.
     *
     * @param list []-list, not {@code null}
     * @return a {@code Set} of {@link Statement}s
     */
    public static Set<Statement> getListStatements(RDFList list) {
        return ((RDFListImpl) list).collectStatements();
    }

    /**
     * Replaces namespaces map with new one.
     *
     * @param mapping  {@link PrefixMapping Prefix Mapping} to modify
     * @param prefixes java Map of new prefixes to set
     * @return a {@code Map} of previously associated prefixes
     */
    public static Map<String, String> setNsPrefixes(PrefixMapping mapping, Map<String, String> prefixes) {
        Map<String, String> init = mapping.getNsPrefixMap();
        init.keySet().forEach(mapping::removeNsPrefix);
        prefixes.forEach((p, u) -> mapping.setNsPrefix(p.replaceAll(":$", ""), u));
        return init;
    }

    /**
     * Lists all literal string values (lexical forms) with the given language tag
     * for the specified subject and predicate.
     *
     * @param subject   {@link Resource}, not {@code null}
     * @param predicate {@link Property}, can be {@code null}
     * @param lang      String lang, maybe {@code null} or empty
     * @return {@code Stream} of {@code String}s
     */
    public static Stream<String> langValues(Resource subject, Property predicate, String lang) {
        return Iterators.asStream(subject.listProperties(predicate)
                .mapWith(s -> {
                    if (!s.getObject().isLiteral())
                        return null;
                    if (!filterByLangTag(s.getLiteral(), lang))
                        return null;
                    return s.getString();
                })
                .filterDrop(Objects::isNull));
    }

    /**
     * Answers {@code true} if the literal has the given language tag.
     * The comparison is case-insensitive and ignores trailing spaces,
     * so two tags {@code  en } and {@code En} are considered as equaled.
     *
     * @param literal {@link Literal}, not {@code null}
     * @param tag     String, possible {@code null}
     * @return {@code true} if the given literal has the given tag
     */
    public static boolean filterByLangTag(Literal literal, String tag) {
        String other = literal.getLanguage();
        if (StringUtils.isEmpty(tag))
            return StringUtils.isEmpty(other);
        return tag.trim().equalsIgnoreCase(other);
    }

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
     * @see Models#listDescendingStatements(RDFNode)
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
            getListStatements(list).forEach(statement -> {
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
     * @see Models#getAssociatedStatements(Resource)
     */
    public static ExtendedIterator<Statement> listDescendingStatements(RDFNode subject) {
        if (!subject.isResource()) return NullIterator.instance();
        return Iterators.flatMap(subject.asResource().listProperties(),
                s -> s.getObject().isAnon() ?
                        Iterators.concat(Iterators.of(s), listDescendingStatements(s.getResource())) : Iterators.of(s));
    }

    /**
     * Lists all direct subjects for the given object.
     *
     * @param object {@link RDFNode}, not {@code null}
     * @return <b>distinct</b> {@code Stream} of {@link Resource}s
     * @see Model#listResourcesWithProperty(Property, RDFNode)
     * @see org.apache.jena.graph.GraphUtil#listSubjects(Graph, Node, Node)
     */
    public static Stream<Resource> subjects(RDFNode object) {
        Model m = Objects.requireNonNull(object.getModel(), "No model for a resource " + object);
        return Iterators.fromSet(() -> m.getGraph().find(Node.ANY, Node.ANY, object.asNode())
                .mapWith(t -> m.wrapAsResource(t.getSubject())).toSet());
    }

    /**
     * Returns a string representation of the given Jena statement taking into account PrefixMapping.
     *
     * @param st {@link Statement}, not {@code null}
     * @param pm {@link PrefixMapping}, not {@code null}
     * @return {@code String}
     */
    public static String toString(Statement st, PrefixMapping pm) {
        return String.format("[%s, %s, %s]",
                st.getSubject().asNode().toString(pm, false),
                st.getPredicate().asNode().toString(pm, false),
                st.getObject().asNode().toString(pm, true));
    }

    /**
     * Returns a string representation of the given Jena statement.
     *
     * @param inModel {@link Statement}, not {@code null}
     * @return {@code String}
     */
    public static String toString(Statement inModel) {
        return toString(inModel, inModel.getModel());
    }

    /**
     * Answers {@code true} if the given {@code node} contains the specified {@code uri}.
     *
     * @param node {@link RDFNode}, not {@code null}
     * @param uri  {@code String}, not {@code null}
     * @return boolean
     */
    public static boolean containsURI(RDFNode node, String uri) {
        if (node.isURIResource()) {
            return uri.equals(node.asResource().getURI());
        }
        return node.isLiteral() && uri.equals(node.asLiteral().getDatatypeURI());
    }

    /**
     * Answers {@code true} if the given {@code uri} is a part of the given {@code statement}.
     *
     * @param statement {@link Statement}, not {@code null}
     * @param uri       {@code String}, not {@code null}
     * @return boolean
     */
    public static boolean containsURI(Statement statement, String uri) {
        if (uri.equals(statement.getSubject().getURI())) return true;
        if (uri.equals(statement.getPredicate().getURI())) return true;
        return containsURI(statement.getObject(), uri);
    }
}
