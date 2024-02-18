package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntDisjointImpl;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.function.BiFunction;

final class OntDisjoints {
    public static final EnhNodeFinder PROPERTIES_FINDER = new EnhNodeFinder.ByType(OWL.AllDisjointProperties);
    public static final EnhNodeFinder DISJOINT_FINDER = OntEnhNodeFactories.createFinder(OWL.AllDisjointClasses,
            OWL.AllDifferent, OWL.AllDisjointProperties);

    public static EnhNodeFactory createDifferentIndividualsFactory(OntConfig config) {
        boolean useDistinctMembers = config.getBoolean(OntModelControls.USE_OWL1_DISTINCT_MEMBERS_PREDICATE_FEATURE);
        boolean compatible = config.getBoolean(OntModelControls.USE_OWL2_DEPRECATED_VOCABULARY_FEATURE);
        Property[] predicates;
        if (useDistinctMembers) {
            predicates = new Property[]{OWL.distinctMembers};
        } else if (compatible) {
            predicates = new Property[]{OWL.members, OWL.distinctMembers};
        } else {
            predicates = new Property[]{OWL.members};
        }
        return createFactory(
                OntDisjointImpl.IndividualsImpl.class,
                (n, g) -> new OntDisjointImpl.IndividualsImpl(n, g, !compatible, useDistinctMembers),
                OWL.AllDifferent,
                OntIndividual.class,
                true,
                predicates
        );
    }

    public static EnhNodeFactory createFactory(
            Class<? extends OntDisjointImpl<?>> impl,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            Resource type,
            Class<? extends RDFNode> view,
            boolean allowEmptyList,
            Property... predicates) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, type, producer);
        EnhNodeFinder finder = new EnhNodeFinder.ByType(type);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(type));
        return OntEnhNodeFactories.createCommon(maker, finder, filter
                .and(getHasPredicatesFilter(predicates))
                .and(getHasMembersOfFilter(view, allowEmptyList, predicates)));
    }

    private static EnhNodeFilter getHasPredicatesFilter(Property... predicates) {
        if (predicates.length == 0) {
            throw new IllegalArgumentException();
        }
        EnhNodeFilter res = new EnhNodeFilter.HasPredicate(predicates[0]);
        for (int i = 1; i < predicates.length; i++) {
            res = res.or(new EnhNodeFilter.HasPredicate(predicates[i]));
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
        if (!STDObjectFactories.RDF_LIST.canWrap(node, graph)) {
            return false;
        }
        if (view == null) return true;
        RDFList list = (RDFList) STDObjectFactories.RDF_LIST.wrap(node, graph);
        return (list.isEmpty() && allowEmptyList) ||
                Iterators.anyMatch(list.iterator().mapWith(RDFNode::asNode), n -> OntEnhGraph.canAs(view, n, graph));
    }
}
