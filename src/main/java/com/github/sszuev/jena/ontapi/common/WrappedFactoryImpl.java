package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Objects;

/**
 * A factory wrapper.
 * It is a facility to provide implicit links between {@link EnhNodeFactory object factories}.
 * For more details see the {@link OntEnhGraph} description and also the description for
 * the method {@link PersonalityBuilder#add(Class, EnhNodeFactory)}.
 * <p>
 * Created by @ssz on 18.01.2019.
 *
 * @see OntEnhNodeFactories
 */
@SuppressWarnings("WeakerAccess")
public class WrappedFactoryImpl extends BaseEnhNodeFactoryImpl {
    private final Class<? extends RDFNode> type;

    public WrappedFactoryImpl(Class<? extends RDFNode> type) {
        this.type = Objects.requireNonNull(type);
    }

    public static WrappedFactoryImpl of(Class<? extends RDFNode> type) {
        return new WrappedFactoryImpl(type);
    }

    /**
     * Finds and returns the {@link EnhNodeFactory} instance for the encapsulated {@link OntObject object} type.
     * This factory and the returned one are synonymous: both have the same behaviour.
     *
     * @param g {@link EnhGraph}, the model. Must be instance of {@link OntEnhGraph}
     * @return {@link EnhNodeFactory}, not {@code null}
     * @throws OntJenaException in case nothing is found
     */
    public EnhNodeFactory getDelegate(EnhGraph g) throws OntJenaException {
        return getFactory(g);
    }

    protected EnhNodeFactory getFactory(EnhGraph g) throws OntJenaException {
        EnhNodeFactory res = OntEnhGraph.asPersonalityModel(g).getOntPersonality().getObjectFactory(type);
        if (res == null) {
            throw new OntJenaException.IllegalState("Unable to find factory for " + OntEnhNodeFactories.viewAsString(type));
        }
        return res;
    }

    @Override
    public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
        return getDelegate(eg).iterator(eg);
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return getDelegate(eg).canWrap(node, eg);
    }

    @Override
    public EnhNode createInstance(Node node, EnhGraph eg) {
        return getDelegate(eg).createInstance(node, eg);
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        return getDelegate(eg).wrap(node, eg);
    }

    @Override
    public String toString() {
        return String.format("Factory[%s]", OntEnhNodeFactories.viewAsString(type));
    }
}
