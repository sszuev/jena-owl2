package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import com.github.sszuev.jena.ontapi.vocabulary.SWRLB;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Ontology Vocabulary.
 * This is a generic interface that simply maps an {@code uri}-key to a {@code Set} of
 * {@link Resource RDF Resources} or {@link Property RDF Properties},
 * that are defined in vocabulary schemas and represent some family determined by that key.
 * A schema is a java class containing public static final constants.
 * Schemas are usually located inside the packages
 * {@link com.github.sszuev.jena.ontapi.vocabulary} and {@link org.apache.jena.vocabulary}.
 * There are two kinds of property/resources described by this vocabulary: system and builtin.
 * A system resource/property is simply a URI defined in any scheme.
 * A builtin resource/property is a URI with a known type that does not require explicit declaration.
 * Note that all methods of this interface return unmodifiable {@code Set}s.
 * <p>
 * Created by @ssz on 04.04.2017.
 *
 * @see Factory
 */
public interface OntVocabulary {

    /**
     * Answers a {@code Set} of system/builtin {@link Resource}s for the specified URI-{@code key}.
     * A URI-{@code key} - is a schema URI that determines a family of desired resources.
     * For example, to get all resources a key {@link RDFS#Resource rdfs:Resource} should be used,
     * because it is a supertype of everything.
     *
     * @param key String, not {@code null}
     * @return a {@code Set} of {@link Resource}s (possibly empty)
     */
    Set<? extends Resource> get(String key);

    /**
     * Answers a {@code Set} of system/builtin {@link Resource}s for the specified URI-key.
     *
     * @param uri a URI-{@link Resource}, not {@code null}
     * @param <X> either {@link Resource} or {@link Property}
     * @return a {@code Set} of {@link X}s, not {@code null} but possibly empty
     */
    @SuppressWarnings("unchecked")
    default <X extends Resource> Set<X> get(Resource uri) {
        return (Set<X>) get(uri.getURI());
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL#AnnotationProperty owl:AnnotationProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinAnnotationProperties() {
        return get(OWL.AnnotationProperty);
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL#DatatypeProperty owl:DatatypeProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinDatatypeProperties() {
        return get(OWL.DatatypeProperty);
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL#ObjectProperty owl:ObjectProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinObjectProperties() {
        return get(OWL.ObjectProperty);
    }

    /**
     * Returns a collection of all built-in uri-resources
     * with implicit {@code rdf:type} equal to {@link RDFS#Datatype rdfs:Datatype}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinDatatypes() {
        return get(RDFS.Datatype);
    }

    /**
     * Returns a collection of all built-in uri resources
     * with implicit {@code rdf:type} equal to {@link OWL#Class owl:Class}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinClasses() {
        return get(OWL.Class);
    }

    /**
     * Returns a collection of all built-in uri resources
     * with implicit {@code rdf:type} equal to {@link SWRL#Builtin swrl:Builtin}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinSWRLs() {
        return get(SWRL.Builtin);
    }

    /**
     * Returns all reserved resources:
     * OWL entities cannot have an uri belonging to the return collection.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getSystemResources() {
        return get(RDFS.Resource);
    }

    /**
     * Returns all reserved properties:
     * OWL2 ontology cannot contain assertion with predicate belonging to the return collection.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getSystemProperties() {
        return get(RDF.Property);
    }

    /**
     * A factory-helper to work with {@link OntVocabulary} instances, that wrap constant-holders
     * from the packages {@link com.github.sszuev.jena.ontapi.vocabulary}
     * and {@link org.apache.jena.vocabulary} (such as {@link OWL}).
     * <p>
     * In ONT-API, a {@link OntVocabulary} singleton is used
     * to build {@link OntPersonality}
     * and, also, in {@code com.github.owlcs.ontapi.transforms} subsystem.
     * <p>
     * Created by @ssz on 21.12.2016.
     */
    class Factory {

        public static final OntVocabulary RDFS_VOCABULARY = new RDFSImpl();
        public static final OntVocabulary OWL2_VOCABULARY = new OWL2Impl();
        public static final OntVocabulary OWL1_VOCABULARY = new OWL1Impl(true);
        public static final OntVocabulary OWL1_LITE_VOCABULARY = new OWL1Impl(false);
        public static final OntVocabulary DC_VOCABULARY = new DCImpl();
        public static final OntVocabulary SKOS_VOCABULARY = new SKOSImpl();
        public static final OntVocabulary SWRL_VOCABULARY = new SWRLImpl();
        public static final OntVocabulary OWL2_DC_SKOS_SWRL_VOCABULARY = create(OWL2_VOCABULARY, DC_VOCABULARY, SKOS_VOCABULARY, SWRL_VOCABULARY);


        /**
         * Creates a fresh union vocabulary that combines the given ones.
         *
         * @param vocabularies an {@code Array} of {@link OntVocabulary}s
         * @return {@link OntVocabulary}
         * @see #create(String, Collection)
         */
        public static OntVocabulary create(OntVocabulary... vocabularies) {
            return new Impl(Arrays.stream(vocabularies)
                    .map(Factory::asMap)
                    .flatMap(x -> x.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                        Set<Resource> res = new HashSet<>(a);
                        res.addAll(b);
                        return res;
                    })));
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    a URI-{@link Resource}, not {@code null}
         * @param values an {@code Array} with {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single (specified) mapping
         */
        public static OntVocabulary create(Resource key, Resource... values) {
            return create(key, Arrays.stream(values).collect(Collectors.toUnmodifiableSet()));
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    a URI-{@link Resource}, not {@code null}
         * @param values a {@code Collection} of {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single (specified) mapping
         */
        public static OntVocabulary create(Resource key, Collection<? extends Resource> values) {
            return create(Objects.requireNonNull(key).getURI(), values);
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    {@code String}, a URI of resource-family, not {@code null}
         * @param values a {@code Collection} of {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single mapping
         * @see #create(OntVocabulary...)
         */
        public static OntVocabulary create(String key, Collection<? extends Resource> values) {
            Map<String, Set<? extends Resource>> map = new HashMap<>();
            map.put(Objects.requireNonNull(key), toUnmodifiableSet(Objects.requireNonNull(values)));
            return new Impl(map);
        }

        /**
         * Creates a {@link OntVocabulary} with mapping for system resource/properties.
         *
         * @param schemas an {@code Array} of schemas
         *                - constant-holders with {@link Resource} and {@link Property} public static final fields,
         *                not {@code null}
         * @return a {@link OntVocabulary} with mapping for system resources and properties
         * (keys: {@link RDFS#Resource rdfs:Resource} and {@link RDF#Property rdf:Property})
         */
        public static OntVocabulary create(Class<?>... schemas) {
            return new Impl(getConstants(Property.class, schemas), getConstants(Resource.class, schemas));
        }

        private static Stream<Field> directFields(Class<?> vocabulary, Class<?> type) {
            return Arrays.stream(vocabulary.getDeclaredFields())
                    .filter(x -> Modifier.isPublic(x.getModifiers()))
                    .filter(x -> Modifier.isStatic(x.getModifiers()))
                    .filter(x -> type.equals(x.getType()));
        }

        private static Stream<Field> fields(Class<?> vocabulary, Class<?> type) {
            Stream<Field> res = directFields(vocabulary, type);
            return vocabulary.getSuperclass() != null ? Stream.concat(res, fields(vocabulary.getSuperclass(), type)) : res;
        }

        private static <T> Stream<T> constants(Class<?> vocabulary, Class<T> type) {
            return fields(vocabulary, type).map(x -> getValue(x, type)).filter(Objects::nonNull);
        }

        private static <T> T getValue(Field field, Class<T> type) {
            try {
                return type.cast(field.get(null));
            } catch (IllegalAccessException e) {
                throw new OntJenaException.IllegalState("Unable to get an object of the type " + type.getSimpleName() +
                        " from the field " + field.getName(), e);
            }
        }

        protected static <T> Set<T> getConstants(Class<? extends T> type, Class<?>... vocabularies) {
            return Arrays.stream(vocabularies)
                    .flatMap(x -> constants(x, type))
                    .collect(Collectors.toUnmodifiableSet());
        }

        private static Map<String, Set<? extends Resource>> asMap(OntVocabulary voc) {
            if (voc instanceof Impl) {
                return ((Impl) voc).map;
            }
            Map<String, Set<? extends Resource>> res = new HashMap<>();
            Stream.of(OWL.AnnotationProperty, OWL.DatatypeProperty, OWL.ObjectProperty,
                            RDFS.Datatype, OWL.Class, SWRL.Builtin, RDF.Property, RDFS.Resource)
                    .forEach(x -> res.put(x.getURI(), voc.get(x)));
            return res;
        }

        private static <X> Set<X> toUnmodifiableSet(Collection<X> input) {
            if (input instanceof Set && input.getClass().getName().equals("java.util.Collections$UnmodifiableSet")) {
                return (Set<X>) input;
            }
            return input.stream().peek(Objects::requireNonNull).collect(Collectors.toUnmodifiableSet());
        }

        protected static class RDFSImpl extends Impl {
            public static final Set<Property> ANNOTATION_PROPERTIES = Set.of(RDFS.label, RDFS.comment, RDFS.seeAlso, RDFS.isDefinedBy);
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{RDF.class, RDFS.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected RDFSImpl() {
                super(
                        /* annotation properties */ ANNOTATION_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * Access to the {@link OWL OWL2} (including RDFS & RDF & XSD) vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class OWL2Impl extends Impl {
            /**
             * The list of datatypes from owl-2 specification (35 types)
             * (see <a href="https://www.w3.org/TR/owl2-quick-reference/">Quick References, 3.1 Built-in Datatypes</a>).
             * It seems it is not full:
             */
            public static final Set<Resource> OWL2_DATATYPES =
                    Set.of(OWL.real, OWL.rational,
                            RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                            RDFS.Literal, XSD.xstring, XSD.normalizedString,
                            XSD.token, XSD.language, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                            XSD.xdouble, XSD.xfloat, XSD.xboolean,
                            XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                            XSD.xlong, XSD.xint, XSD.xshort, XSD.xbyte,
                            XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort, XSD.unsignedByte,
                            XSD.hexBinary, XSD.base64Binary,
                            XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
                    );
            public static final Set<Resource> ALL_KNOWN_DATATYPES = initOWL2BuiltInRDFDatatypes(TypeMapper.getInstance())
                    .stream()
                    .map(RDFDatatype::getURI)
                    .map(ResourceFactory::createResource)
                    .collect(Collectors.toUnmodifiableSet());
            public static final Set<Resource> OWL2_BUILTIN_CLASSES = Set.of(OWL.Nothing, OWL.Thing);
            public static final Set<Property> OWL2_BUILTIN_ANNOTATION_PROPERTIES =
                    Set.of(RDFS.label, RDFS.comment, RDFS.seeAlso, RDFS.isDefinedBy, OWL.versionInfo,
                            OWL.backwardCompatibleWith, OWL.priorVersion, OWL.incompatibleWith, OWL.deprecated);
            public static final Set<Property> OWL2_BUILTIN_DATA_PROPERTIES = Set.of(OWL.topDataProperty, OWL.bottomDataProperty);
            public static final Set<Property> OWL2_BUILTIN_OBJECT_PROPERTIES = Set.of(OWL.topObjectProperty, OWL.bottomObjectProperty);
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{XSD.class, RDF.class, RDFS.class, OWL.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected OWL2Impl() {
                super(
                        OWL2_BUILTIN_ANNOTATION_PROPERTIES,
                        OWL2_BUILTIN_DATA_PROPERTIES,
                        OWL2_BUILTIN_OBJECT_PROPERTIES,
                        OWL2_BUILTIN_CLASSES,
                        ALL_KNOWN_DATATYPES,
                        /*SWRL*/ null,
                        PROPERTIES,
                        RESOURCES
                );
            }

            private static Set<RDFDatatype> initOWL2BuiltInRDFDatatypes(TypeMapper types) {
                Stream.of(OWL.real, OWL.rational).forEach(d -> types.registerDatatype(new BaseDatatype(d.getURI())));
                OWL2_DATATYPES.forEach(iri -> types.getSafeTypeByName(iri.getURI()));
                Set<RDFDatatype> res = new HashSet<>();
                types.listTypes().forEachRemaining(res::add);
                return Collections.unmodifiableSet(res);
            }
        }

        /**
         * Access to the {@link org.apache.jena.vocabulary.OWL OWL1} (including RDFS & RDF & XSD) vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class OWL1Impl extends Impl {
            private static final Set<Resource> OWL1_DATATYPES =
                    Set.of(RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                            RDFS.Literal, XSD.xstring, XSD.normalizedString,
                            XSD.token, XSD.language, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                            XSD.xdouble, XSD.xfloat, XSD.xboolean,
                            XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                            XSD.xlong, XSD.xint, XSD.xshort, XSD.xbyte,
                            XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort, XSD.unsignedByte,
                            XSD.hexBinary, XSD.base64Binary,
                            XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
                    );
            public static final Set<Resource> ALL_KNOWN_BUILTIN_DATATYPES = initOWL1BuiltInRDFDatatypes(TypeMapper.getInstance())
                    .stream()
                    .map(RDFDatatype::getURI)
                    .map(ResourceFactory::createResource)
                    .collect(Collectors.toUnmodifiableSet());
            public static final Set<Resource> OWL1_FULL_BUILTIN_CLASSES = Set.of(
                    org.apache.jena.vocabulary.OWL.Nothing, org.apache.jena.vocabulary.OWL.Thing
            );
            public static final Set<Resource> OWL1_LITE_BUILTIN_CLASSES = Set.of(
                    org.apache.jena.vocabulary.OWL.Thing
            );
            public static final Set<Property> OWL1_BUILTIN_ANNOTATION_PROPERTIES = Set.of(
                    RDFS.label, RDFS.comment, RDFS.seeAlso, RDFS.isDefinedBy,
                    org.apache.jena.vocabulary.OWL.versionInfo, org.apache.jena.vocabulary.OWL.backwardCompatibleWith,
                    org.apache.jena.vocabulary.OWL.priorVersion, org.apache.jena.vocabulary.OWL.incompatibleWith
            );
            public static final Set<Property> OWL1_BUILTIN_DATA_PROPERTIES = Set.of();
            public static final Set<Property> OWL1_BUILTIN_OBJECT_PROPERTIES = Set.of();
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{XSD.class, RDF.class, RDFS.class, org.apache.jena.vocabulary.OWL.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected OWL1Impl(boolean full) {
                super(
                        OWL1_BUILTIN_ANNOTATION_PROPERTIES,
                        OWL1_BUILTIN_DATA_PROPERTIES,
                        OWL1_BUILTIN_OBJECT_PROPERTIES,
                        full ? OWL1_FULL_BUILTIN_CLASSES : OWL1_LITE_BUILTIN_CLASSES,
                        ALL_KNOWN_BUILTIN_DATATYPES,
                        /*SWRL*/ null,
                        PROPERTIES,
                        RESOURCES
                );
            }

            private static Set<RDFDatatype> initOWL1BuiltInRDFDatatypes(TypeMapper types) {
                OWL1_DATATYPES.forEach(iri -> types.getSafeTypeByName(iri.getURI()));
                Set<String> exclude = OWL2Impl.OWL2_DATATYPES.stream()
                        .filter(it -> !OWL1_DATATYPES.contains(it))
                        .map(Resource::getURI)
                        .collect(Collectors.toUnmodifiableSet());
                Set<RDFDatatype> res = new HashSet<>();
                types.listTypes().forEachRemaining(it -> {
                    if (!exclude.contains(it.getURI())) {
                        res.add(it);
                    }
                });
                return Collections.unmodifiableSet(res);
            }
        }

        /**
         * Access to {@link DC} vocabulary.
         */
        protected static class DCImpl extends Impl {
            public static final Set<Property> ALL_PROPERTIES = getConstants(Property.class, DC.class);

            protected DCImpl() {
                super(
                        /* annotation properties */ ALL_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ ALL_PROPERTIES,
                        /* system resources */ null
                );
            }
        }

        /**
         * Access to {@link SKOS} vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class SKOSImpl extends Impl {
            public static final Set<Property> ANNOTATION_PROPERTIES =
                    Set.of(SKOS.altLabel, SKOS.changeNote, SKOS.definition,
                            SKOS.editorialNote, SKOS.example, SKOS.hiddenLabel, SKOS.historyNote,
                            SKOS.note, SKOS.prefLabel, SKOS.scopeNote);
            public static final Set<Property> OBJECT_PROPERTIES =
                    Set.of(SKOS.broadMatch, SKOS.broader, SKOS.broaderTransitive,
                            SKOS.closeMatch, SKOS.exactMatch, SKOS.hasTopConcept, SKOS.inScheme,
                            SKOS.mappingRelation, SKOS.member, SKOS.memberList, SKOS.narrowMatch,
                            SKOS.narrower, SKOS.narrowerTransitive, SKOS.related,
                            SKOS.relatedMatch, SKOS.semanticRelation, SKOS.topConceptOf);
            /**
             * NOTE: In the OWLAPI-api {@code org.semanticweb.owlapi.vocab.SKOSVocabulary} there is also skos:TopConcept
             * But in fact there is no such resource in the <a href="https://www.w3.org/2009/08/skos-reference/skos.htm">specification</a>.
             */
            public static final Set<Resource> CLASSES =
                    Set.of(SKOS.Collection, SKOS.Concept, SKOS.ConceptScheme, SKOS.OrderedCollection);

            public static final Set<Property> PROPERTIES = getConstants(Property.class, SKOS.class);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, SKOS.class);


            protected SKOSImpl() {
                super(
                        /* annotation properties */ ANNOTATION_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ OBJECT_PROPERTIES,
                        /* named classes */ CLASSES,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * For SWRL modeling.
         *
         * @see SWRL
         * @see SWRLB
         */
        protected static class SWRLImpl extends Impl {
            public static final Set<Resource> BUILTINS = getConstants(Property.class, SWRLB.class);
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{SWRL.class, SWRLB.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected SWRLImpl() {
                super(
                        /* annotation properties */ null,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ BUILTINS,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * The base implementation.
         */
        protected static class Impl implements OntVocabulary {
            private final Map<String, Set<? extends Resource>> map;

            private Impl(Set<Property> properties,
                         Set<Resource> resources) {
                this(
                        /* annotation properties */ null,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ properties,
                        /* system resources */ resources
                );
            }

            protected Impl(Set<Property> annotationProperties,
                           Set<Property> dataProperties,
                           Set<Property> objectProperties,
                           Set<Resource> classes,
                           Set<Resource> datatypes,
                           Set<Resource> swrlBuiltins,
                           Set<Property> allProperties,
                           Set<Resource> allResources) {
                this.map = collectBuiltIns(
                        annotationProperties,
                        dataProperties,
                        objectProperties,
                        classes,
                        datatypes,
                        swrlBuiltins,
                        allProperties,
                        allResources);
            }

            private static Map<String, Set<? extends Resource>> collectBuiltIns(Set<Property> annotationProperties,
                                                                                Set<Property> dataProperties,
                                                                                Set<Property> objectProperties,
                                                                                Set<Resource> classes,
                                                                                Set<Resource> datatypes,
                                                                                Set<Resource> swrlBuiltins,
                                                                                Set<Property> allProperties,
                                                                                Set<Resource> allResources) {
                return Stream.of(
                        pair(OWL.AnnotationProperty, annotationProperties),
                        pair(OWL.DatatypeProperty, dataProperties),
                        pair(OWL.ObjectProperty, objectProperties),
                        pair(OWL.Class, classes),
                        pair(RDFS.Datatype, datatypes),
                        pair(SWRL.Builtin, swrlBuiltins),
                        pair(RDF.Property, allProperties),
                        pair(RDFS.Resource, allResources)
                ).filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(
                        it -> it.getKey().getURI(),
                        it -> Set.copyOf(it.getValue()))
                );
            }

            private static Map.Entry<Resource, Set<? extends Resource>> pair(Resource key, Set<? extends Resource> value) {
                return value == null ? null : Map.entry(key, value);
            }

            protected Impl(Map<String, Set<? extends Resource>> map) {
                this.map = Map.copyOf(Objects.requireNonNull(map));
            }

            @Override
            public Set<? extends Resource> get(String key) {
                return map.getOrDefault(key, Collections.emptySet());
            }
        }
    }
}
