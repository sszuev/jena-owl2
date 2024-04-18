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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link ResourceVocabulary} abstraction and a holder for some particular vocabulary {@link OntPersonality}'s implementations.
 * Each of the impl has a simple in-memory cache to speed-up,
 * since it is important to have quick access to desired types.
 */
abstract class ResourceVocabularyImpl<T extends Resource> implements ResourceVocabulary<T> {
    private final Map<Class<? extends T>, Set<Node>> map;

    ResourceVocabularyImpl(Map<Class<? extends T>, Set<Node>> map) {
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public Set<Node> get(Class<? extends T> key) throws OntJenaException {
        return map.getOrDefault(OntJenaException.notNull(key, "Null key"), Set.of());
    }

    @Override
    public boolean supports(Class<? extends T> type) {
        return map.containsKey(type);
    }

    static class BuiltinsImpl extends EntitiesImpl implements OntPersonality.Builtins {
        BuiltinsImpl(Map<Class<? extends OntObject>, Set<Node>> map) {
            super(map);
        }
    }

    static class PunningsImpl extends EntitiesImpl implements OntPersonality.Punnings {
        PunningsImpl(Map<Class<? extends OntObject>, Set<Node>> map) {
            super(map);
        }
    }

    static abstract class EntitiesImpl extends ResourceVocabularyImpl<OntObject> {
        final Set<Node> classes;
        final Set<Node> datatypes;
        final Set<Node> objectProperties;
        final Set<Node> datatypeProperties;
        final Set<Node> annotationProperties;
        final Set<Node> ontProperties;
        final Set<Node> individuals;

        EntitiesImpl(Map<Class<? extends OntObject>, Set<Node>> map) {
            super(map);
            this.classes = get(OntClass.Named.class);
            this.datatypes = get(OntDataRange.Named.class);
            this.individuals = get(OntIndividual.Named.class);
            this.objectProperties = get(OntObjectProperty.Named.class);
            this.datatypeProperties = get(OntDataProperty.class);
            this.annotationProperties = get(OntAnnotationProperty.class);
            this.ontProperties = collect(OntObjectProperty.Named.class, OntDataProperty.class, OntAnnotationProperty.class);
        }

        public Set<Node> getNamedClasses() {
            return classes;
        }

        public Set<Node> getDatatypes() {
            return datatypes;
        }

        public Set<Node> getObjectProperties() {
            return objectProperties;
        }

        public Set<Node> getDatatypeProperties() {
            return datatypeProperties;
        }

        public Set<Node> getAnnotationProperties() {
            return annotationProperties;
        }

        public Set<Node> getNamedIndividuals() {
            return individuals;
        }

        public Set<Node> getOntProperties() {
            return ontProperties;
        }

        @SafeVarargs
        private Set<Node> collect(Class<? extends OntObject>... types) {
            return Arrays.stream(types).map(this::get)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    static class ReservedIml extends ResourceVocabularyImpl<Resource> implements OntPersonality.Reserved {
        private final Map<String, Set<Node>> nodes = new HashMap<>();
        private final Set<Node> resources;
        private final Set<Node> properties;
        private final Set<Node> allResources;

        ReservedIml(Map<Class<? extends Resource>, Set<Node>> map) {
            super(map);
            this.properties = get(Property.class);
            this.resources = get(Resource.class);
            this.allResources = Stream.of(properties, resources)
                    .flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
        }

        @Override
        public Set<Node> getResources() {
            return resources;
        }

        @Override
        public Set<Node> getProperties() {
            return properties;
        }

        @Override
        public Set<Node> getAllResources() {
            return allResources;
        }
    }
}
