package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.WrappedEnhNodeFactory;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class OntDataRanges {
    public static final EnhNodeFinder DR_FINDER_OWL1 = new EnhNodeFinder.ByType(OWL.DataRange);
    public static final EnhNodeFilter DR_FILTER_OWL1 = new EnhNodeFilter.HasType(OWL.DataRange);

    public static final EnhNodeFinder DR_FINDER_OWL2 = new EnhNodeFinder.ByType(RDFS.Datatype);
    public static final EnhNodeFilter DR_FILTER_OWL2 = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(RDFS.Datatype));
    // owl:DataRange is deprecated in OWL 2, replaced by rdfs:Datatype, but for compatibility needs to handle it
    public static final EnhNodeFinder DR_FULL_FINDER_OWL2 = new EnhNodeFinder.ByTypes(Set.of(RDFS.Datatype, OWL.DataRange));
    public static final EnhNodeFilter DR_FULL_FILTER_OWL2 = EnhNodeFilter.ANON.and(
            new EnhNodeFilter.HasOneOfType(Set.of(RDFS.Datatype, OWL.DataRange))
    );

    public static EnhNodeFinder makeFacetRestrictionFinder(Property predicate) {
        return new EnhNodeFinder.ByPredicate(predicate);
    }

    public static EnhNodeFilter makeFacetRestrictionFilter(Property predicate) {
        return EnhNodeFilter.ANON.and(
                (n, g) -> Iterators.anyMatch(g.asGraph().find(n, predicate.asNode(), Node.ANY)
                        .mapWith(Triple::getObject), Node::isLiteral)
        );
    }

    public static EnhNodeFactory createDataRangeFactory(Type ... types) {
        Set<EnhNodeFactory> factories = new LinkedHashSet<>();
        for (Type t : types) {
            if (t == Type.COMPLEMENT_OF) {
                factories.add(Factory.COMPLEMENT_OF_FACTORY);
            }
            if (t == Type.RESTRICTION) {
                factories.add(Factory.RESTRICTION_FACTORY);
            }
            if (t == Type.ONE_OF) {
                factories.add(Factory.ONE_OF_FACTORY);
            }
            if (t == Type.INTERSECTION_OF) {
                factories.add(Factory.INTERSECTION_OF_FACTORY);
            }
            if (t == Type.UNION_OF) {
                factories.add(Factory.UNION_OF_FACTORY);
            }
        }
        return new Factory(factories.stream().collect(Collectors.toUnmodifiableList()));
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
    public static class Factory extends BaseEnhNodeFactoryImpl {
        private static final Node TYPE = RDF.Nodes.type;
        private static final Node ANY = Node.ANY;
        private static final Node PRIMARY_DATATYPE_TYPE = RDFS.Datatype.asNode();
        // owl:DataRange is deprecated in OWL 2, replaced by rdfs:Datatype, but for compatibility needs to handle it
        private static final Node SECONDARY_DATATYPE_TYPE = OWL.DataRange.asNode();

        private static final EnhNodeFactory NAMED_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.Named.class);
        private static final EnhNodeFactory ONE_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.OneOf.class);
        private static final EnhNodeFactory COMPLEMENT_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.ComplementOf.class);
        private static final EnhNodeFactory UNION_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.UnionOf.class);
        private static final EnhNodeFactory INTERSECTION_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.IntersectionOf.class);
        private static final EnhNodeFactory RESTRICTION_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.Restriction.class);
        private final List<EnhNodeFactory> anonymousDatarangeFactories;

        private Factory(List<EnhNodeFactory> anonymousDatarangeFactories) {
            this.anonymousDatarangeFactories = Objects.requireNonNull(anonymousDatarangeFactories);
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return eg.asGraph().find(ANY, TYPE, PRIMARY_DATATYPE_TYPE)
                    .mapWith(t -> t.getSubject().isURI() ?
                            safeWrap(t.getSubject(), eg, NAMED_FACTORY) :
                            safeWrap(t.getSubject(), eg, anonymousDatarangeFactories))
                    .andThen(
                            eg.asGraph().find(ANY, TYPE, SECONDARY_DATATYPE_TYPE)
                                    .mapWith(t -> t.getSubject().isURI() ? null : safeWrap(t.getSubject(), eg, ONE_OF_FACTORY))
                    )
                    .filterDrop(Objects::isNull);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return safeWrap(node, eg, NAMED_FACTORY);
            }
            if (!node.isBlank()) {
                return null;
            }
            if (!eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)
                    && !eg.asGraph().contains(node, TYPE, SECONDARY_DATATYPE_TYPE)) {
                return null;
            }
            return safeWrap(node, eg, anonymousDatarangeFactories);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_FACTORY.canWrap(node, eg);
            }
            if (!node.isBlank()) {
                return false;
            }
            if (eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)) {
                return canWrap(node, eg, anonymousDatarangeFactories);
            }
            return canWrap(node, eg, ONE_OF_FACTORY);
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_FACTORY.wrap(node, eg);
            }
            OntJenaException.Conversion ex = new OntJenaException.Conversion("Can't convert node " + node +
                    " to Data Range Expression.");
            if (!node.isBlank()) {
                throw ex;
            }
            if (!eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)
                    && !eg.asGraph().contains(node, TYPE, SECONDARY_DATATYPE_TYPE)) {
                throw ex;
            }
            return wrap(node, eg, ex, anonymousDatarangeFactories);
        }
    }

    public enum Type {
        ONE_OF,
        UNION_OF,
        INTERSECTION_OF,
        COMPLEMENT_OF,
        RESTRICTION,
    }
}
