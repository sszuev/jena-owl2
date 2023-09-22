package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

final class OntDisjoints {
    public static final EnhNodeFinder PROPERTIES_FINDER = new EnhNodeFinder.ByType(OWL.AllDisjointProperties);
    public static final EnhNodeFinder DISJOINT_FINDER = OntEnhNodeFactories.createFinder(OWL.AllDisjointClasses,
            OWL.AllDifferent, OWL.AllDisjointProperties);

    public static EnhNodeFactory createFactory(Class<? extends OntDisjointImpl<?>> impl,
                                               Resource type,
                                               Class<? extends RDFNode> view,
                                               boolean allowEmptyList,
                                               Property... predicates) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, type);
        EnhNodeFinder finder = new EnhNodeFinder.ByType(type);
        EnhNodeFilter filter = EnhNodeFilter.BLANK.and(new EnhNodeFilter.HasType(type));
        return OntEnhNodeFactories.createCommon(maker, finder, filter
                .and(getHasPredicatesFilter(predicates))
                .and(getHasMembersOfFilter(view, allowEmptyList, predicates)));
    }

    private static EnhNodeFilter getHasPredicatesFilter(Property... predicates) {
        EnhNodeFilter res = EnhNodeFilter.FALSE;
        for (Property p : predicates) {
            res = res.or(new EnhNodeFilter.HasPredicate(p));
        }
        return res;
    }

    private static EnhNodeFilter getHasMembersOfFilter(Class<? extends RDFNode> view,
                                                       boolean allowEmptyList,
                                                       Property... predicates) {
        return (node, eg) -> {
            ExtendedIterator<Node> res = listRoots(node, eg.asGraph(), predicates);
            try {
                while (res.hasNext()) {
                    if (testList(res.next(), eg, view, allowEmptyList)) return true;
                }
            } finally {
                res.close();
            }
            return false;
        };
    }

    private static ExtendedIterator<Node> listRoots(Node node, Graph graph, Property... predicates) {
        return Iterators.flatMap(Iterators.of(predicates),
                p -> graph.find(node, p.asNode(), Node.ANY).mapWith(Triple::getObject));
    }

    private static boolean testList(Node node, EnhGraph graph, Class<? extends RDFNode> view, boolean allowEmptyList) {
        if (!RDFListImpl.factory.canWrap(node, graph)) return false;
        if (view == null) return true;
        RDFList list = RDFListImpl.factory.wrap(node, graph).as(RDFList.class);
        return (list.isEmpty() && allowEmptyList) ||
                Iterators.anyMatch(list.iterator().mapWith(RDFNode::asNode), n -> OntEnhGraph.canAs(view, n, graph));
    }
}
