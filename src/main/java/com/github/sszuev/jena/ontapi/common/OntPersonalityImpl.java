package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A default implementation of {@link OntPersonality}.
 * Mappings from [interface] Class objects of RDFNode to {@link Implementation} factories.
 * <p>
 * Created by @ssz on 10.11.2016.
 */
@SuppressWarnings({"WeakerAccess"})
public class OntPersonalityImpl extends Personality<RDFNode> implements OntPersonality {
    private final Punnings punnings;
    private final Builtins builtins;
    private final Reserved reserved;

    public OntPersonalityImpl(Personality<RDFNode> other, Punnings punnings, Builtins builtins, Reserved reserved) {
        super(Objects.requireNonNull(other, "Null personalities"));
        this.builtins = Objects.requireNonNull(builtins, "Null builtins vocabulary");
        this.punnings = Objects.requireNonNull(punnings, "Null punnings vocabulary");
        this.reserved = Objects.requireNonNull(reserved, "Null reserved vocabulary");
    }

    protected OntPersonalityImpl(OntPersonalityImpl other) {
        this(other, other.getPunnings(), other.getBuiltins(), other.getReserved());
    }

    @Override
    public Builtins getBuiltins() {
        return builtins;
    }

    @Override
    public Punnings getPunnings() {
        return punnings;
    }

    @Override
    public Reserved getReserved() {
        return reserved;
    }

    /**
     * Registers new OntObject if needed
     *
     * @param type    Interface (OntObject)
     * @param factory Factory to crete object
     */
    public void register(Class<? extends OntObject> type, EnhNodeFactory factory) {
        super.add(Objects.requireNonNull(type, "Null type."), EnhNodeFactory.asJenaImplementation(factory));
    }

    /**
     * Removes the factory.
     *
     * @param view Interface (OntObject)
     */
    public void unregister(Class<? extends OntObject> view) {
        getMap().remove(view);
    }

    @Override
    public Stream<Class<? extends RDFNode>> types() {
        return getMap().keySet().stream();
    }

    /**
     * Gets factory for {@link OntObject}.
     *
     * @param type Interface (OntObject type)
     * @return {@link EnhNodeFactory} factory
     */
    @Override
    public EnhNodeFactory getObjectFactory(Class<? extends RDFNode> type) {
        return (EnhNodeFactory) OntJenaException.notNull(getImplementation(type),
                "Unsupported object type " + OntEnhNodeFactories.viewAsString(type));
    }

    @Override
    public boolean supports(Class<? extends RDFNode> type) {
        return getMap().containsKey(type);
    }

    @Override
    public OntPersonalityImpl add(Personality<RDFNode> other) {
        super.add(other);
        return this;
    }

    @Override
    public OntPersonalityImpl copy() {
        return new OntPersonalityImpl(this);
    }

}
