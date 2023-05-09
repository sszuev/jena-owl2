package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link OntPersonality} builder.
 * This must be the only place to create various {@code OntPersonality} objects.
 * <p>
 * Created by @szz on 17.01.2019.
 */
@SuppressWarnings("WeakerAccess")
public class PersonalityBuilder {
    private final Map<Class<? extends OntObject>, ObjectFactory> map;

    private final Personality<RDFNode> base;
    private OntPersonality.Punnings punnings;
    private OntPersonality.Builtins builtins;
    private OntPersonality.Reserved reserved;

    public PersonalityBuilder() {
        this(new LinkedHashMap<>());
    }

    protected PersonalityBuilder(Map<Class<? extends OntObject>, ObjectFactory> factories) {
        this.map = Objects.requireNonNull(factories);
        this.base = new Personality<>();
    }

    /**
     * Makes a full copy of the given {@link OntPersonality}
     * in the form of modifiable {@link PersonalityBuilder builder}.
     *
     * @param from {@link OntPersonality} to copy settings, not {@code null}
     * @return {@link PersonalityBuilder}
     */
    public static PersonalityBuilder from(OntPersonality from) {
        return new PersonalityBuilder()
                .addPersonality(OntPersonality.asJenaPersonality(from))
                .setPunnings(from.getPunnings())
                .setBuiltins(from.getBuiltins())
                .setReserved(from.getReserved());
    }

    @SuppressWarnings("rawtypes")
    private static <X extends Vocabulary> X require(X obj, Class<X> type) {
        if (obj == null) {
            throw new IllegalStateException("The " + type.getSimpleName() + " Vocabulary must be present in builder.");
        }
        return obj;
    }

    @SuppressWarnings("rawtypes")
    private static <V extends Vocabulary> V hasSpec(V voc, Class... types) {
        Objects.requireNonNull(voc);
        Set<?> errors = Arrays.stream(types).filter(x -> {
            try {
                //noinspection unchecked
                return voc.get(x) == null;
            } catch (OntJenaException e) {
                return true;
            }
        }).collect(Collectors.toSet());
        if (errors.isEmpty()) return voc;
        throw new IllegalArgumentException("The vocabulary " + voc + " has missed required types: " + errors);
    }

    /**
     * Makes a full copy of this builder.
     *
     * @return {@link PersonalityBuilder}, a copy
     */
    public PersonalityBuilder copy() {
        PersonalityBuilder res = new PersonalityBuilder(new LinkedHashMap<>(this.map));
        res.addPersonality(base.copy());
        if (punnings != null) res.setPunnings(punnings);
        if (builtins != null) res.setBuiltins(builtins);
        if (reserved != null) res.setReserved(reserved);
        return res;
    }

    /**
     * Associates the specified {@link ObjectFactory factory} with the specified {@link OntObject object} type.
     * If the builder previously contained a mapping for the object type (which is common situation),
     * the old factory is replaced by the specified factory.
     * <p>
     * Please note: the {@link ObjectFactory factory} must not explicitly refer to another factory,
     * instead it may contain implicit references through
     * {@link OntEnhGraph#asPersonalityModel(EnhGraph)} method.
     * For example if you need a check, that some {@link Node node} is an OWL-Class inside your factory,
     * you can use {@link OntEnhGraph#canAs(Class, Node, EnhGraph)}
     * with the type {@link OntClass.Named}.
     *
     * @param type    {@code Class}-type of the concrete {@link OntObject}.
     * @param factory {@link ObjectFactory} the factory to produce the instances of the {@code type},
     * @return this builder
     */
    public PersonalityBuilder add(Class<? extends OntObject> type, ObjectFactory factory) {
        map.put(type, factory);
        return this;
    }

    /**
     * Adds everything from the specified {@link Personality Jena Personality} to the existing internal collection.
     *
     * @param from {@link Personality} with generic type {@link RDFNode}, not {@code null}
     * @return this builder
     * @see Personality#add(Personality)
     */
    public PersonalityBuilder addPersonality(Personality<RDFNode> from) {
        this.base.add(Objects.requireNonNull(from));
        return this;
    }

    /**
     * Sets a new punnings personality vocabulary.
     *
     * @param punnings {@link OntPersonality.Punnings}, not {@code null}
     * @return this builder
     */
    public PersonalityBuilder setPunnings(OntPersonality.Punnings punnings) {
        this.punnings = hasSpec(punnings, getEntityTypes());
        return this;
    }

    /**
     * Sets a new builtins personality vocabulary.
     *
     * @param builtins {@link OntPersonality.Builtins}, not {@code null}
     * @return this builder
     */
    public PersonalityBuilder setBuiltins(OntPersonality.Builtins builtins) {
        this.builtins = hasSpec(builtins, getEntityTypes());
        return this;
    }

    private Class<?>[] getEntityTypes() {
        return OntEntity.entityTypes().toArray(Class[]::new);
    }

    /**
     * Sets a new reserved personality vocabulary.
     *
     * @param reserved {@link OntPersonality.Reserved}, not {@code null}
     * @return this builder
     */
    public PersonalityBuilder setReserved(OntPersonality.Reserved reserved) {
        this.reserved = hasSpec(reserved, Resource.class, Property.class);
        return this;
    }

    /**
     * Builds a new personality configuration.
     *
     * @return {@link OntPersonality}, fresh instance
     * @throws IllegalStateException in case the builder does not contain require components
     */
    public OntPersonality build() throws IllegalStateException {
        OntPersonalityImpl res = new OntPersonalityImpl(base, punnings(), builtins(), reserved());
        map.forEach(res::register);
        return res;
    }

    private OntPersonality.Punnings punnings() {
        return require(punnings, OntPersonality.Punnings.class);
    }

    private OntPersonality.Builtins builtins() {
        return require(builtins, OntPersonality.Builtins.class);
    }

    private OntPersonality.Reserved reserved() {
        return require(reserved, OntPersonality.Reserved.class);
    }

}
