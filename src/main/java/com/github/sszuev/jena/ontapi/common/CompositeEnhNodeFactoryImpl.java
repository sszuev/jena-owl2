package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link EnhNodeFactory Ontology Object Factory} implementation to combine several other factories.
 * <p>
 * Created @ssz on 07.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class CompositeEnhNodeFactoryImpl extends BaseEnhNodeFactoryImpl {
    private final List<EnhNodeFactory> factories;
    private final EnhNodeFinder finder;
    private final EnhNodeFilter fittingFilter;

    /**
     * Creates a factory instance.
     *
     * @param finder        {@link EnhNodeFinder}, optional, if {@code null} then uses only provided sub-factories to search
     * @param fittingFilter {@link EnhNodeFilter}, optional, to trim searching
     * @param factories     array of factories to combine, must not be {@code null} or empty
     */
    public CompositeEnhNodeFactoryImpl(EnhNodeFinder finder, EnhNodeFilter fittingFilter, EnhNodeFactory... factories) {
        this.finder = finder;
        this.fittingFilter = fittingFilter;
        if (factories.length == 0)
            throw new IllegalArgumentException("Empty factory array");
        this.factories = unbend(factories);
    }

    private static List<EnhNodeFactory> unbend(EnhNodeFactory... factories) {
        return Arrays.stream(factories)
                .flatMap(f -> f instanceof CompositeEnhNodeFactoryImpl ? ((CompositeEnhNodeFactoryImpl) f).factories.stream() : Stream.of(f))
                .collect(Collectors.toList());
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        EnhNode res = createInstance(node, eg);
        if (res != null) return res;
        throw new OntJenaException.Conversion("Can't wrap node " + node + ". Use direct factory.");
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return !(fittingFilter != null && !fittingFilter.test(node, eg))
                && Iterators.anyMatch(listFactories(), f -> f.canWrap(node, eg));
    }

    @Override
    public EnhNode createInstance(Node node, EnhGraph eg) {
        if (fittingFilter != null && !fittingFilter.test(node, eg)) return null;
        return Iterators.findFirst(Iterators.filter(listFactories(), f -> f.canWrap(node, eg))
                .mapWith(f -> f.createInstance(node, eg))).orElse(null);
    }

    @Override
    public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
        if (finder != null) {
            return finder.iterator(eg).mapWith(n -> createInstance(n, eg)).filterDrop(Objects::isNull);
        }
        return Iterators.distinct(Iterators.flatMap(listFactories(), f -> f.iterator(eg)));
    }

    public EnhNodeFinder getFinder() {
        return finder;
    }

    public EnhNodeFilter getFilter() {
        return fittingFilter;
    }

    /**
     * Lists all sub-factories.
     *
     * @return {@link ExtendedIterator} of {@link EnhNodeFactory}
     */
    public ExtendedIterator<? extends EnhNodeFactory> listFactories() {
        return Iterators.create(factories);
    }

}
