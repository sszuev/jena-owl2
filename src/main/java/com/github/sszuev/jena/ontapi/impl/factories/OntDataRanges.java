package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.WrappedFactoryImpl;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntDataRanges {
    public static final EnhNodeFinder DR_FINDER_OWL1 = new EnhNodeFinder.ByType(OWL.DataRange);
    public static final EnhNodeFilter DR_FILTER_OWL1 = new EnhNodeFilter.HasType(OWL.DataRange);

    public static final EnhNodeFinder DR_FINDER_OWL2 = new EnhNodeFinder.ByType(RDFS.Datatype);
    public static final EnhNodeFilter DR_FILTER_OWL2 = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(RDFS.Datatype));

    public static EnhNodeFinder makeFacetRestrictionFinder(Property predicate) {
        return new EnhNodeFinder.ByPredicate(predicate);
    }

    public static EnhNodeFilter makeFacetRestrictionFilter(Property predicate) {
        return EnhNodeFilter.ANON.and(
                (n, g) -> Iterators.anyMatch(g.asGraph().find(n, predicate.asNode(), Node.ANY)
                        .mapWith(Triple::getObject), Node::isLiteral)
        );
    }

    /**
     * A factory to produce {@link OntDataRange}s.
     * <p>
     * Although it would be easy to produce this factory using {@link OntEnhNodeFactories#createFrom(EnhNodeFinder, Class, Class[])},
     * this variant with explicit methods must be a little faster,
     * since there is a reduction of number of some possible repetition calls.
     * Also, everything here is under control.
     * <p>
     * Created by @ssz on 02.02.2019.
     */
    public static class DataRangeFactory extends BaseEnhNodeFactoryImpl {
        private static final Node TYPE = RDF.Nodes.type;
        private static final Node ANY = Node.ANY;
        private static final Node DATATYPE = RDFS.Datatype.asNode();

        private final EnhNodeFactory named = WrappedFactoryImpl.of(OntDataRange.Named.class);
        private final EnhNodeFactory oneOf = WrappedFactoryImpl.of(OntDataRange.OneOf.class);
        private final EnhNodeFactory complementOf = WrappedFactoryImpl.of(OntDataRange.ComplementOf.class);
        private final EnhNodeFactory unionOf = WrappedFactoryImpl.of(OntDataRange.UnionOf.class);
        private final EnhNodeFactory intersectionOf = WrappedFactoryImpl.of(OntDataRange.IntersectionOf.class);
        private final EnhNodeFactory restriction = WrappedFactoryImpl.of(OntDataRange.Restriction.class);
        private final List<EnhNodeFactory> anonymous = Stream.of(complementOf
                , restriction
                , oneOf
                , unionOf
                , intersectionOf).collect(Collectors.toList());

        public static EnhNodeFactory createFactory() {
            return new DataRangeFactory();
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return eg.asGraph().find(ANY, TYPE, DATATYPE)
                    .mapWith(t -> t.getSubject().isURI() ?
                            safeWrap(t.getSubject(), eg, named) :
                            safeWrap(t.getSubject(), eg, anonymous))
                    .filterDrop(Objects::isNull);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (node.isURI())
                return safeWrap(node, eg, named);
            if (!node.isBlank())
                return null;
            if (!eg.asGraph().contains(node, TYPE, DATATYPE))
                return null;
            return safeWrap(node, eg, anonymous);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return named.canWrap(node, eg);
            }
            if (!node.isBlank()) return false;
            if (!eg.asGraph().contains(node, TYPE, DATATYPE))
                return false;
            return canWrap(node, eg, anonymous);
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isURI())
                return named.wrap(node, eg);
            OntJenaException.Conversion ex = new OntJenaException.Conversion("Can't convert node " + node +
                    " to Data Range Expression.");
            if (!node.isBlank())
                throw ex;
            if (!eg.asGraph().contains(node, TYPE, DATATYPE))
                throw ex;
            return wrap(node, eg, ex, anonymous);
        }
    }
}
