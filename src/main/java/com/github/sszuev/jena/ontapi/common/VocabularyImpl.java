package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Vocabulary} abstraction and a holder for some particular vocabulary {@link OntPersonality}'s implementations.
 * Each of the impl has a simple in-memory cache to speed-up,
 * since it is important to have quick access to desired types.
 * <p>
 * Created by @ssz on 18.01.2019.
 */
abstract class VocabularyImpl<T extends Resource> implements Vocabulary<T> {
    private final Map<Class<? extends T>, Set<Node>> map;

    VocabularyImpl(Map<Class<? extends T>, Set<Node>> map) {
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public Set<Node> get(Class<? extends T> key) throws OntJenaException {
        Set<Node> res = map.get(OntJenaException.notNull(key, "Null key"));
        if (res == null) {
            return Collections.emptySet();
        }
        return res;
    }

    @Override
    public boolean supports(Class<? extends T> type) {
        return map.containsKey(type);
    }

    static class EntitiesImpl extends VocabularyImpl<OntObject> implements OntPersonality.Builtins, OntPersonality.Punnings {
        private Set<Node> classes;
        private Set<Node> datatypes;
        private Set<Node> objectProperties;
        private Set<Node> datatypeProperties;
        private Set<Node> annotationProperties;
        private Set<Node> allProperties;
        private Set<Node> individuals;

        EntitiesImpl(Map<Class<? extends OntObject>, Set<Node>> map) {
            super(map);
        }

        @Override
        public Set<Node> getClasses() {
            return classes == null ? classes = get(OntClass.Named.class) : classes;
        }

        @Override
        public Set<Node> getDatatypes() {
            return datatypes == null ? datatypes = get(OntDataRange.Named.class) : datatypes;
        }

        @Override
        public Set<Node> getObjectProperties() {
            return objectProperties == null ? objectProperties = get(OntObjectProperty.Named.class) : objectProperties;
        }

        @Override
        public Set<Node> getDatatypeProperties() {
            return datatypeProperties == null ? datatypeProperties = get(OntDataProperty.class) : datatypeProperties;
        }

        @Override
        public Set<Node> getAnnotationProperties() {
            return annotationProperties == null ? annotationProperties = get(OntAnnotationProperty.class) : annotationProperties;
        }

        @Override
        public Set<Node> getIndividuals() {
            return individuals == null ? individuals = get(OntIndividual.Named.class) : individuals;
        }

        @Override
        public Set<Node> getProperties() {
            if (allProperties != null) return allProperties;
            return allProperties = Stream.of(getObjectProperties(), getAnnotationProperties(), getDatatypeProperties())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    static class ReservedIml extends VocabularyImpl<Resource> implements OntPersonality.Reserved {
        private final Map<String, Set<Node>> nodes = new HashMap<>();
        private Set<Node> resources;
        private Set<Node> properties;

        ReservedIml(Map<Class<? extends Resource>, Set<Node>> map) {
            super(map);
        }

        @Override
        public Set<Node> get(String key, Supplier<Set<Node>> loader) {
            return nodes.computeIfAbsent(key, k -> loader.get());
        }

        @Override
        public Set<Node> getResources() {
            return resources == null ? resources = get(Resource.class) : resources;
        }

        @Override
        public Set<Node> getProperties() {
            return properties == null ? properties = get(Property.class) : properties;
        }
    }
}
