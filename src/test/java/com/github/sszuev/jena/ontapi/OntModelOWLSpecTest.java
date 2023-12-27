package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.impl.objects.OntClassImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNamedProperty;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.utils.OntModels;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * To test {@link OntModel} and all its related functionality.
 * <p>
 * Created @ssz on 07.11.2016.
 */
public class OntModelOWLSpecTest {

    @SafeVarargs
    private static <X> Set<X> toSet(Collection<? extends X>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

    private static void assertOntObjectsCount(OntModel m, Class<? extends OntObject> type, long expected) {
        Assertions.assertEquals(expected, m.ontObjects(type).count());
    }

    private static void testListObjects(OntModel m, Map<Class<? extends OntObject>, Integer> expected) {
        expected.keySet().forEach(it -> {
            try {
                List<OntObject> found = m.ontObjects(it).collect(Collectors.toList());
                Assertions.assertEquals(expected.get(it), found.size(), "Wrong objects count for " + it.getSimpleName());
            } catch (Exception e) {
                throw new AssertionError("Can't list objects for " + it.getSimpleName(), e);
            }
        });
    }

    private static void testHasPredicate(Model m, Property predicate, List<? extends OntClass> ces) {
        String type = ces.isEmpty() ? null : ((OntClassImpl) ces.get(0)).objectType().getSimpleName();
        Assertions.assertEquals(m.listSubjectsWithProperty(predicate).toSet().size(), ces.size(), "Incorrect count of " + type);
    }

    @SuppressWarnings("rawtypes")
    static void simplePropertiesValidation(OntModel ont) {
        Model jena = ModelFactory.createModelForGraph(ont.getGraph());
        Set<Resource> annotationProperties = jena.listStatements(null, RDF.type, OWL.AnnotationProperty)
                .mapWith(Statement::getSubject).toSet();
        Set<Resource> datatypeProperties = jena.listStatements(null, RDF.type, OWL.DatatypeProperty)
                .mapWith(Statement::getSubject).toSet();
        Set<Resource> namedObjectProperties = jena.listStatements(null, RDF.type, OWL.ObjectProperty)
                .mapWith(Statement::getSubject).toSet();
        Set<Resource> inverseObjectProperties = jena.listStatements(null, OWL.inverseOf, (RDFNode) null)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isAnon).toSet();
        Set<Statement> inverseStatements = jena.listStatements(null, OWL.inverseOf, (RDFNode) null)
                .filterKeep(s -> s.getSubject().isURIResource()).filterKeep(s -> s.getObject().isURIResource()).toSet();

        List<OntProperty> actualPEs = ont.ontObjects(OntProperty.class).collect(Collectors.toList());

        Set<Resource> expectedNamed = toSet(annotationProperties, datatypeProperties, namedObjectProperties);
        Set<Resource> expectedPEs = toSet(expectedNamed, inverseObjectProperties);
        Assertions.assertEquals(expectedPEs.size(), actualPEs.size());

        List<OntNamedProperty> actualNamed = ont.ontObjects(OntNamedProperty.class).collect(Collectors.toList());
        Assertions.assertEquals(expectedNamed.size(), actualNamed.size());

        List<OntProperty> actualDOs = ont.ontObjects(OntRealProperty.class).collect(Collectors.toList());
        Set<Resource> expectedDOs = toSet(datatypeProperties, namedObjectProperties, inverseObjectProperties);
        Assertions.assertEquals(expectedDOs.size(), actualDOs.size());

        Assertions.assertEquals(inverseStatements.size(), ont.objectProperties()
                .flatMap(OntObjectProperty::inverseProperties).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testPizzaObjects1a(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);

        Map<Class<? extends OntObject>, Integer> expected = new HashMap<>();
        expected.put(OntClass.ObjectSomeValuesFrom.class, 155);
        expected.put(OntClass.DataSomeValuesFrom.class, 0);
        expected.put(OntClass.ObjectAllValuesFrom.class, 26);
        expected.put(OntClass.DataAllValuesFrom.class, 0);
        expected.put(OntClass.ObjectHasValue.class, 6);
        expected.put(OntClass.DataHasValue.class, 0);
        expected.put(OntClass.ObjectMinCardinality.class, 1);
        expected.put(OntClass.DataMinCardinality.class, 0);
        expected.put(OntClass.ObjectMaxCardinality.class, 0);
        expected.put(OntClass.DataMaxCardinality.class, 0);
        expected.put(OntClass.ObjectCardinality.class, 0);
        expected.put(OntClass.DataCardinality.class, 0);
        expected.put(OntClass.HasSelf.class, 0);
        expected.put(OntClass.UnionOf.class, 25);
        expected.put(OntClass.OneOf.class, 1);
        expected.put(OntClass.IntersectionOf.class, 15);
        expected.put(OntClass.ComplementOf.class, 3);
        expected.put(OntClass.NaryDataAllValuesFrom.class, 0);
        expected.put(OntClass.NaryDataSomeValuesFrom.class, 0);
        expected.put(OntClass.LogicalExpression.class, 44);
        expected.put(OntClass.CollectionOf.class, 41);
        expected.put(OntClass.ValueRestriction.class, 187);
        expected.put(OntClass.CardinalityRestriction.class, 1);
        expected.put(OntClass.ComponentRestriction.class, 188);
        expected.put(OntClass.UnaryRestriction.class, 188);
        expected.put(OntClass.Restriction.class, 188);
        expected.put(OntClass.class, 332);

        testListObjects(m, expected);

        List<OntClass.Named> classes = m.ontObjects(OntClass.Named.class).collect(Collectors.toList());
        int expectedClassesCount = m.listStatements(null, RDF.type, OWL.Class)
                .mapWith(Statement::getSubject).filterKeep(RDFNode::isURIResource).toSet().size();
        int actualClassesCount = classes.size();
        Assertions.assertEquals(expectedClassesCount, actualClassesCount);

        List<OntClass> ces = m.ontObjects(OntClass.class).collect(Collectors.toList());
        int expectedCEsCount = m.listStatements(null, RDF.type, OWL.Class)
                .andThen(m.listStatements(null, RDF.type, OWL.Restriction)).toSet().size();
        int actualCEsCount = ces.size();
        Assertions.assertEquals(expectedCEsCount, actualCEsCount);

        List<OntClass.Restriction> restrictions = m.ontObjects(OntClass.Restriction.class).collect(Collectors.toList());
        Assertions.assertEquals(m.listStatements(null, RDF.type, OWL.Restriction).toSet().size(), restrictions.size());

        List<OntClass.ObjectSomeValuesFrom> objectSomeValuesFromCEs = m.ontObjects(OntClass.ObjectSomeValuesFrom.class)
                .collect(Collectors.toList());
        List<OntClass.ObjectAllValuesFrom> objectAllValuesFromCEs = m.ontObjects(OntClass.ObjectAllValuesFrom.class)
                .collect(Collectors.toList());
        List<OntClass.ObjectHasValue> objectHasValueCEs = m.ontObjects(OntClass.ObjectHasValue.class)
                .collect(Collectors.toList());
        List<OntClass.UnionOf> unionOfCEs = m.ontObjects(OntClass.UnionOf.class).collect(Collectors.toList());
        List<OntClass.IntersectionOf> intersectionOfCEs = m.ontObjects(OntClass.IntersectionOf.class)
                .collect(Collectors.toList());
        List<OntClass.ComplementOf> complementOfCEs = m.ontObjects(OntClass.ComplementOf.class).collect(Collectors.toList());
        List<OntClass.OneOf> oneOfCEs = m.ontObjects(OntClass.OneOf.class).collect(Collectors.toList());
        List<OntClass.ObjectMinCardinality> objectMinCardinalityCEs = m.ontObjects(OntClass.ObjectMinCardinality.class)
                .collect(Collectors.toList());

        testHasPredicate(m, OWL.someValuesFrom, objectSomeValuesFromCEs);
        testHasPredicate(m, OWL.allValuesFrom, objectAllValuesFromCEs);
        testHasPredicate(m, OWL.hasValue, objectHasValueCEs);
        testHasPredicate(m, OWL.unionOf, unionOfCEs);
        testHasPredicate(m, OWL.intersectionOf, intersectionOfCEs);
        testHasPredicate(m, OWL.complementOf, complementOfCEs);
        testHasPredicate(m, OWL.oneOf, oneOfCEs);
        testHasPredicate(m, OWL.minCardinality, objectMinCardinalityCEs);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testPizzaLoadProperties(TestSpec spec) {
        simplePropertiesValidation(OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testFamilyLoadProperties(TestSpec spec) {
        simplePropertiesValidation(OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(), spec.inst));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testPizzaLoadIndividuals(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph(), spec.inst);
        List<OntIndividual> individuals = m.ontObjects(OntIndividual.class).collect(Collectors.toList());
        Map<OntIndividual, Set<OntClass>> classes = individuals.stream()
                .collect(Collectors.toMap(Function.identity(), i -> i.classes().collect(Collectors.toSet())));
        classes.forEach((i, c) -> c.forEach(x -> Assertions.assertEquals(1, x.individuals()
                .filter(it -> Objects.equals(it, i)).count())));

        Set<Resource> namedIndividuals = m.listSubjectsWithProperty(RDF.type, OWL.NamedIndividual).toSet();
        Set<Resource> anonIndividuals = m.listStatements(null, RDF.type, (RDFNode) null)
                .filterKeep(s -> s.getSubject().isAnon())
                .filterKeep(s -> s.getObject().isResource() && m.contains(s.getObject()
                        .asResource(), RDF.type, OWL.Class))
                .mapWith(Statement::getSubject).toSet();
        Set<Resource> expected = new HashSet<>(namedIndividuals);
        expected.addAll(anonIndividuals);
        Assertions.assertEquals(expected.size(), individuals.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testKoalaCommon(TestSpec spec) throws IOException {
        // koala has 4 cardinality restrictions with wrong 'xsd:int' instead of 'xsd:nonNegativeInteger'
        // see issue #56
        // OntClass => 20,
        // OntCE$ObjectSomeValuesFrom => 3,
        // OntCE$ObjectAllValuesFrom => 1,
        // OntCE$OneOf => 1,
        // OntCE$IntersectionOf => 4,
        // OntCE$ObjectHasValue => 4,
        // OntCE$DataHasValue => 3
        long numClasses = 36;

        OntModel m = OntModelFactory.createModel(spec.inst);
        try (InputStream in = OntModelOWLSpecTest.class.getResourceAsStream("/koala.owl")) {
            m.read(in, null, Lang.RDFXML.getName());
        }


        long statementsCount = m.statements().count();

        Map<OntClass, Set<OntProperty>> props = new HashMap<>();
        m.ontObjects(OntClass.class)
                .forEach(x -> props.computeIfAbsent(x, c -> new HashSet<>())
                        .addAll(x.properties().collect(Collectors.toSet())));

        Assertions.assertEquals(numClasses, props.keySet().size());
        Assertions.assertEquals(5, props.values().stream().mapToLong(Collection::size).sum());

        String ns = m.getID().getURI() + "#";
        OntClass.Named animal = m.getOntClass(ns + "Animal");
        Assertions.assertNotNull(animal);
        Assertions.assertEquals(3, animal.properties().count());
        OntClass.Named person = m.getOntClass(ns + "Person");
        Assertions.assertNotNull(person);
        Assertions.assertEquals(2, person.properties().count());

        OntDataProperty isHardWorking = m.getDataProperty(ns + "isHardWorking");
        Assertions.assertNotNull(isHardWorking);
        Set<OntObjectProperty> objProperties = m.ontObjects(OntObjectProperty.Named.class).collect(Collectors.toSet());
        Assertions.assertEquals(4, objProperties.size());

        OntStatement statement = person.createHasKey(objProperties, Collections.singleton(isHardWorking)).getMainStatement();
        Assertions.assertTrue(statement.getObject().canAs(RDFList.class));
        statement.addAnnotation(m.getRDFSComment(), "These are keys", "xz");


        Assertions.assertEquals(5, person.hasKeys().findFirst().orElseThrow(AssertionError::new).members().count());
        Assertions.assertEquals(numClasses, m.ontObjects(OntClass.class).distinct().count());
        Assertions.assertEquals(statementsCount + 16, m.statements().count());
        Assertions.assertNotNull(statement.deleteAnnotation(m.getRDFSComment()));

        Assertions.assertEquals(statementsCount + 11, m.statements().count());
        person.clearHasKeys();
        Assertions.assertEquals(statementsCount, m.statements().count());

        OntClass.Named marsupials = m.getOntClass(ns + "Marsupials");
        Assertions.assertNotNull(marsupials);
        Assertions.assertEquals(marsupials, person.disjointClasses().findFirst().orElse(null));
        Assertions.assertEquals(person, marsupials.disjointClasses().findAny().orElse(null));

        marsupials.addDisjointClass(animal);
        Assertions.assertEquals(2, marsupials.disjointClasses().count());
        Assertions.assertEquals(0, animal.disjointClasses().count());
        Assertions.assertEquals(1, person.disjointClasses().count());
        marsupials.removeDisjointClass(animal);
        Assertions.assertEquals(1, marsupials.disjointClasses().count());
        Assertions.assertEquals(0, animal.disjointClasses().count());
        Assertions.assertEquals(1, person.disjointClasses().count());

        person.addSuperClass(marsupials);
        Assertions.assertEquals(2, person.superClasses().count());
        person.removeSuperClass(marsupials);
        Assertions.assertEquals(1, person.superClasses().count());

        Assertions.assertEquals(statementsCount, m.statements().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testKoalaProperties(TestSpec spec) throws IOException {
        OntModel m = OntModelFactory.createModel(spec.inst);
        try (InputStream in = OntModelOWLSpecTest.class.getResourceAsStream("/koala.owl")) {
            m.read(in, null, Lang.RDFXML.getName());
        }
        simplePropertiesValidation(m);
        OntObjectProperty p1 = m.objectProperties().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertFalse(p1.inverseProperty().isPresent());
        OntObjectProperty p2 = m.createResource().addProperty(OWL.inverseOf, p1).as(OntObjectProperty.class);
        Assertions.assertTrue(p2.inverseProperty().isPresent());
        Assertions.assertEquals(1, p2.inverseProperties().count());
        Assertions.assertEquals(p1.asProperty(), p2.asProperty());
        Assertions.assertEquals(p1, p2.inverseProperty().orElseThrow(AssertionError::new));
        Assertions.assertEquals(1, m.ontObjects(OntObjectProperty.Inverse.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testCreateImports(TestSpec spec) {
        String baseURI = "http://test.com/graph/5";
        String baseNS = baseURI + "#";
        OntModel base = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD)
                .setID(baseURI).getModel();
        OntClass.Named cl1 = base.createOntClass(baseNS + "Class1");
        OntClass.Named cl2 = base.createOntClass(baseNS + "Class2");

        String childURI = "http://test.com/graph/6";
        String childNS = childURI + "#";
        OntModel child = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
                .setID(childURI).getModel().addImport(base);
        OntClass.Named cl3 = child.createOntClass(childNS + "Class3");
        cl3.addSuperClass(child.createObjectIntersectionOf(cl1, cl2));
        cl3.createIndividual(childNS + "Individual1");

        base = child.imports().findFirst().orElse(null);
        Assertions.assertNotNull(base, "Null base");

        Set<String> imports = child.getID().imports().collect(Collectors.toSet());
        Assertions.assertEquals(imports, Stream.of(baseURI).collect(Collectors.toSet()));
        Assertions.assertEquals(4, child.ontEntities().count());
        Assertions.assertEquals(2, child.ontEntities().filter(OntEntity::isLocal).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testAssemblySimplestOntology(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        m.setID("http://example.com/xxx");

        String schemaNS = m.getID().getURI() + "#";
        String dataNS = m.getID().getURI() + "/data#";
        m.setNsPrefix("schema", schemaNS).setNsPrefix("data", dataNS);

        OntDataRange.Named email = m.createDatatype(schemaNS + "email");
        OntDataRange.Named phone = m.createDatatype(schemaNS + "phone");
        OntDataRange.Named skype = m.createDatatype(schemaNS + "skype");
        OntDataProperty contactInfo = m.createDataProperty(schemaNS + "info");
        OntClass.Named contact = m.createOntClass(schemaNS + "Contact");
        OntClass.Named person = m.createOntClass(schemaNS + "Person");
        OntObjectProperty.Named hasContact = m.createObjectProperty(schemaNS + "contact");

        hasContact.addDomain(person).addRange(contact);

        contactInfo.addDomain(contact)
                .addRange(email)
                .addRange(phone)
                .addRange(skype);

        // data:
        OntIndividual bobs = contact.createIndividual(dataNS + "bobs");
        bobs.addAssertion(contactInfo, email.createLiteral("bob@x-email.com"))
                .addAssertion(m.getRDFSLabel(), m.createLiteral("Bob's contacts"))
                .addAssertion(contactInfo, phone.createLiteral(98_968_78_98_792L));
        OntIndividual bob = person.createIndividual(dataNS + "Bob").addAssertion(hasContact, bobs)
                .addAssertion(m.getRDFSLabel(), m.createLiteral("Bob Label"));

        OntIndividual jhons = contact.createIndividual(dataNS + "jhons")
                .addAssertion(contactInfo, skype.createLiteral("john-skype-id"));
        person.createIndividual(dataNS + "Jhon").addAssertion(hasContact, jhons);
        bob.addNegativeAssertion(hasContact, jhons)
                .addNegativeAssertion(contactInfo, phone.createLiteral("212 85 06"))
                .addNegativeAssertion(hasContact.createInverse(), bobs);

        Assertions.assertEquals(2, bob.positiveAssertions().count());
        Assertions.assertEquals(3, bob.negativeAssertions().count());

        Assertions.assertEquals(42, m.statements().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testCreateSimpleEntities(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        createSimpleEntityTest(m, "a-p", OntAnnotationProperty.class);
        createSimpleEntityTest(m, "o-p", OntObjectProperty.Named.class);
        createSimpleEntityTest(m, "d-p", OntDataProperty.class);
        createSimpleEntityTest(m, "c", OntClass.Named.class);
        if (!spec.isOWL1()) {
            createSimpleEntityTest(m, "d", OntDataRange.Named.class);
        } else {
            // no such type in OWL1
            Assertions.assertThrows(OntJenaException.Unsupported.class,
                    () -> createSimpleEntityTest(m, "d", OntDataRange.Named.class));
        }
        if (!spec.isOWL1()) {
            createSimpleEntityTest(m, "I", OntIndividual.Named.class);
        } else {
            // can't create naked individual in OWL1 (there is no default class-type)
            Assertions.assertThrows(OntJenaException.Creation.class,
                    () -> createSimpleEntityTest(m, "I", OntIndividual.Named.class));
        }
    }

    private <E extends OntEntity> void createSimpleEntityTest(OntModel m, String uri, Class<E> type) {
        E e = m.createOntEntity(type, uri);
        Assertions.assertEquals(1, e.statements().count());
        Assertions.assertSame(e, e.as(type));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testCreateAnnotatedEntities(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        createAnnotatedEntityTest(m, "a-p", OntAnnotationProperty.class);
        createAnnotatedEntityTest(m, "o-p", OntObjectProperty.Named.class);
        createAnnotatedEntityTest(m, "d-p", OntDataProperty.class);
        createAnnotatedEntityTest(m, "c", OntClass.Named.class);
        createAnnotatedEntityTest(m, "d", OntDataRange.Named.class);
        createAnnotatedEntityTest(m, "I", OntIndividual.Named.class);
    }

    private <E extends OntEntity> void createAnnotatedEntityTest(OntModel m, String uri, Class<E> type) {
        String pref = "Annotation[" + uri + "]:::";
        E e = m.createOntEntity(type, uri);
        e.addAnnotation(m.getRDFSComment(), pref + "entity of type " + type.getSimpleName())
                .addAnnotation(m.getRDFSLabel(), pref + "label");
        m.asStatement(e.getMainStatement().asTriple()).addAnnotation(m.getRDFSComment(), pref + "comment");
        Assertions.assertEquals(2, e.annotations().count());
        Assertions.assertEquals(2, e.statements().count());
        Assertions.assertSame(e, e.as(type));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
    })
    public void testObjectsContent(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        // properties:
        OntDataProperty p1 = m.createDataProperty("p1");
        OntObjectProperty.Named p2 = m.createObjectProperty("p2");
        // classes:
        OntClass.Named class1 = m.createOntClass("c");
        OntClass.UnionOf class2 = m.createObjectUnionOf(m.createOntClass("c1"), m.createOntClass("c2"));
        OntClass.DataHasValue class3 = m.createDataHasValue(p1, m.createLiteral("2"));
        OntClass.DataMinCardinality class4 = m.createDataMinCardinality(p1, 2,
                m.getDatatype(XSD.xdouble));
        OntClass.Named class5 = m.getOWLThing();
        OntClass.ObjectCardinality class6 = m.createObjectCardinality(p2, 1234, class5);
        OntClass.HasSelf class7 = m.createHasSelf(p2);
        class3.addComment("The Restriction");
        class1.addSuperClass(class2).addSuperClass(class3).addDisjointClass(class4);
        class2.addSuperClass(m.createObjectComplementOf(class5));
        class5.addEquivalentClass(m.getOWLNothing());
        // data-ranges:
        OntDataRange.Named dr1 = m.getDatatype(XSD.xint);
        OntDataRange.IntersectionOf dr2 = m.createDataIntersectionOf(dr1, m.getDatatype(XSD.xdouble));
        OntDataRange.ComplementOf dr3 = m.createDataComplementOf(dr2);
        dr3.addComment("Data range: complement of intersection int and double");
        // individuals:
        OntIndividual i1 = class5.createIndividual("i1");
        OntIndividual i2 = class6.createIndividual();
        // nap:
        OntNegativeAssertion<?, ?> npa1 = p1.addNegativeAssertion(i1, m.createLiteral("xxx"));


        Assertions.assertEquals(1, class1.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(4, class1.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(6, class2.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(7, class2.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(3, class3.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(3, class3.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(4, class4.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(4, class4.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(0, class5.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(1, class5.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(3, class6.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(3, class6.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(3, class7.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(3, class7.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(0, dr1.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(0, dr1.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(6, dr2.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(6, dr2.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(2, dr3.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(2, dr3.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(1, i1.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(6, i1.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(0, i2.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(1, i2.content().map(ModelUtils::toString).count());

        Assertions.assertEquals(4, npa1.spec().map(ModelUtils::toString).count());
        Assertions.assertEquals(4, npa1.content().map(ModelUtils::toString).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testRemoveObjects(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);

        OntClass class1 = m.createOntClass("C-1");
        OntClass class2 = m.createOntClass("C-2");
        OntClass class3 = m.createOntClass("C-3");
        OntObjectProperty p = m.createObjectProperty("P");
        OntClass class4 = m.createObjectComplementOf(class3);
        OntClass class5 = m.createObjectSomeValuesFrom(p, class4);
        OntClass class6 = m.createObjectIntersectionOf(m.getOWLThing(), class2, class4, class5);
        Assertions.assertEquals(6, m.ontObjects(OntClass.class).count());
        long size = m.size();
        OntDisjoint<?> d = m.createDisjointClasses(m.getOWLNothing(), class1, class6);

        m.removeOntObject(d);

        Assertions.assertEquals(size, m.statements().count());

        m.removeOntObject(class6).removeOntObject(class5).removeOntObject(class4).removeOntObject(p);

        Assertions.assertEquals(3, m.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    }, mode = EnumSource.Mode.EXCLUDE)
    public void testModelPrefixes(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        m.setID("http://x");
        Assertions.assertEquals(4, m.numPrefixes());
        Assertions.assertEquals(4, m.getBaseGraph().getPrefixMapping().numPrefixes());
        String txt = RDFIOTestUtils.asString(m, Lang.TURTLE);
        Assertions.assertEquals(6, txt.split("\n").length);

        m.setNsPrefix("x", "http://x#");
        Assertions.assertEquals(5, m.numPrefixes());
        Assertions.assertEquals(5, m.getBaseGraph().getPrefixMapping().numPrefixes());
        txt = RDFIOTestUtils.asString(m, Lang.TURTLE);
        Assertions.assertEquals(7, txt.split("\n").length);

        m.removeNsPrefix("x");
        Assertions.assertEquals(4, m.numPrefixes());
        Assertions.assertEquals(4, m.getBaseGraph().getPrefixMapping().numPrefixes());
        txt = RDFIOTestUtils.asString(m, Lang.TURTLE);
        Assertions.assertEquals(6, txt.split("\n").length);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
    })
    public void testAdvancedModelImports(TestSpec spec) {
        OntModel av1 = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD)
                .setID("a").setVersionIRI("v1").getModel();
        OntModel av2 = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD)
                .setID("a").setVersionIRI("v2").getModel();
        OntModel b = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD)
                .setID("b").getModel();
        OntModel c = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD)
                .setID("c").getModel();

        try {
            c.addImport(av1).addImport(av1);
            Assertions.fail("Can add the same model");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertTrue(c.hasImport(av1));
        Assertions.assertFalse(c.hasImport(av2));
        Assertions.assertEquals(1, c.imports().count());

        c.removeImport(av1).addImport(av2);
        Assertions.assertTrue(c.hasImport(av2));
        Assertions.assertFalse(c.hasImport(av1));
        Assertions.assertEquals(1, c.imports().count());

        b.addImport(c);
        Assertions.assertEquals(1, b.imports().count());
        Assertions.assertTrue(b.hasImport(c));
        Assertions.assertFalse(b.hasImport(av1));
        Assertions.assertFalse(b.hasImport(av2));

        String tree = Graphs.importsTreeAsString(b.getGraph());
        Assertions.assertEquals(Arrays.asList("<b>", "<c>", "<a[v2]>"),
                Arrays.stream(tree.split("\n")).map(String::trim).collect(Collectors.toList()));

        c.removeImport(av1);
        tree = Graphs.importsTreeAsString(b.getGraph());
        Assertions.assertEquals(Arrays.asList("<b>", "<c>", "<a[v2]>"),
                Arrays.stream(tree.split("\n")).map(String::trim).collect(Collectors.toList()));

        c.removeImport(av2).addImport(av1);
        tree = Graphs.importsTreeAsString(b.getGraph());
        Assertions.assertEquals(Arrays.asList("<b>", "<c>", "<a[v1]>"),
                Arrays.stream(tree.split("\n")).map(String::trim).collect(Collectors.toList()));

        // sync imports:
        ((UnionGraph) c.getGraph()).addGraph(av2.getGraph());
        OntModels.syncImports(b);
        tree = Graphs.importsTreeAsString(b.getGraph());
        Assertions.assertEquals(4, OntModels.importsClosure(b).count());
        Assertions.assertEquals(3, OntModels.importsClosure(c).count());
        Assertions.assertEquals(Arrays.asList("<b>", "<c>", "<a[v1]>", "<a[v2]>"),
                Arrays.stream(tree.split("\n")).map(String::trim).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("v1", "v2"), c.statements(null, OWL.imports, null)
                .map(Statement::getResource)
                .map(Resource::getURI)
                .sorted()
                .collect(Collectors.toList()));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
            "OWL1_MEM",
            "OWL1_LITE_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testCycleModelImports(TestSpec spec) {
        OntModel a = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntModel b = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntModel c = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        a.createOntClass("A");
        b.createOntClass("B");
        c.createOntClass("C");
        a.setID("a");
        b.setID("b");
        c.setID("c");

        a.addImport(b);
        Assertions.assertEquals(1, a.imports().count());
        Assertions.assertEquals(0, b.imports().count());
        Assertions.assertEquals(0, c.imports().count());
        Assertions.assertEquals(2, a.ontEntities().count());
        Assertions.assertEquals(1, b.ontEntities().count());
        Assertions.assertEquals(1, c.ontEntities().count());

        b.addImport(c);
        Assertions.assertEquals(1, a.imports().count());
        Assertions.assertEquals(1, b.imports().count());
        Assertions.assertEquals(0, c.imports().count());
        Assertions.assertEquals(3, a.ontEntities().count());
        Assertions.assertEquals(2, b.ontEntities().count());
        Assertions.assertEquals(1, c.ontEntities().count());

        // add cycle import:
        c.addImport(a);
        Assertions.assertEquals(1, a.imports().count());
        Assertions.assertEquals(1, b.imports().count());
        Assertions.assertEquals(1, c.imports().count());
        Assertions.assertEquals(3, a.ontEntities().count());
        Assertions.assertEquals(3, b.ontEntities().count());
        Assertions.assertEquals(3, c.ontEntities().count());

        // add more entities:
        a.createOntClass("B");
        b.createOntClass("X");
        Assertions.assertEquals(4, a.ontEntities().count());
        Assertions.assertEquals(4, b.ontEntities().count());
        Assertions.assertEquals(4, c.ontEntities().count());

        // remove cycle import
        b.removeImport(c);
        Assertions.assertEquals(1, a.imports().count());
        Assertions.assertEquals(0, b.imports().count());
        Assertions.assertEquals(1, c.imports().count());
        Assertions.assertEquals(3, a.ontEntities().count());
        Assertions.assertEquals(2, b.ontEntities().count());
        Assertions.assertEquals(4, c.ontEntities().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    }, mode = EnumSource.Mode.EXCLUDE)
    public void testOntPropertyOrdinal(TestSpec spec) {
        Graph g = RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph();
        OntModel m = OntModelFactory.createModel(g, spec.inst);
        OntNamedProperty<?> p = m.getOntEntity(OntNamedProperty.class, m.expandPrefix(":isIngredientOf"));
        Assertions.assertNotNull(p);
        Assertions.assertEquals(0, p.getOrdinal());
        Assertions.assertEquals(0, m.getRDFSComment().getOrdinal());
        Assertions.assertEquals(0, m.getOWLBottomDataProperty().getOrdinal());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testFamilyListObjectsOWL2(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());

        assertOntObjectsCount(m, OntObject.class, 1684);
        assertOntObjectsCount(m, OntEntity.class, 656);
        assertOntObjectsCount(m, OntNamedProperty.class, 90);

        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntIndividual.Named.class, 508);
        assertOntObjectsCount(m, OntObjectProperty.Named.class, 80);
        assertOntObjectsCount(m, OntAnnotationProperty.class, 1);
        assertOntObjectsCount(m, OntDataProperty.class, 9);

        assertOntObjectsCount(m, OntObjectProperty.class, 80);
        assertOntObjectsCount(m, OntRealProperty.class, 89);
        assertOntObjectsCount(m, OntProperty.class, 90);

        assertOntObjectsCount(m, OntDataRange.class, 0);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntDataRange.OneOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Restriction.class, 0);
        assertOntObjectsCount(m, OntDataRange.UnionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.ComplementOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.IntersectionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Combination.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        assertOntObjectsCount(m, OntClass.class, 289);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 114);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 117);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 117);
        assertOntObjectsCount(m, OntClass.Restriction.class, 117);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 117);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 4);
        assertOntObjectsCount(m, OntClass.OneOf.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectHasValue.class, 6);
        assertOntObjectsCount(m, OntClass.ObjectSomeValuesFrom.class, 111);
        assertOntObjectsCount(m, OntClass.ObjectAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.DataSomeValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.HasSelf.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataSomeValuesFrom.class, 0);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testFamilyListObjectsOWL1(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertInstanceOf(OntClass.IntersectionOf.class, equivalentToWife.get(0));
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());
        List<OntClass> equivalentToSex = m.getOntClass(ns + "Sex").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToSex.size());
        Assertions.assertInstanceOf(OntClass.UnionOf.class, equivalentToSex.get(0));
        Assertions.assertEquals(OntClass.UnionOf.class, equivalentToSex.get(0).objectType());

        assertOntObjectsCount(m, OntObject.class, 1684);
        assertOntObjectsCount(m, OntEntity.class, 151);
        assertOntObjectsCount(m, OntNamedProperty.class, 90);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        // owl:NamedIndividual is not valid class-type in OWL1:
        assertOntObjectsCount(m, OntIndividual.Named.class, 3);
        assertOntObjectsCount(m, OntObjectProperty.Named.class, 80);
        assertOntObjectsCount(m, OntAnnotationProperty.class, 1);
        assertOntObjectsCount(m, OntDataProperty.class, 9);

        assertOntObjectsCount(m, OntObjectProperty.class, 80);
        assertOntObjectsCount(m, OntRealProperty.class, 89);
        assertOntObjectsCount(m, OntProperty.class, 90);

        assertOntObjectsCount(m, OntDataRange.class, 0);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntDataRange.OneOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Restriction.class, 0);
        assertOntObjectsCount(m, OntDataRange.UnionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.ComplementOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.IntersectionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Combination.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        assertOntObjectsCount(m, OntClass.class, 289);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 114);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.Restriction.class, 111);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 113);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 4);
        assertOntObjectsCount(m, OntClass.OneOf.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectSomeValuesFrom.class, 111);
        assertOntObjectsCount(m, OntClass.ObjectAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.DataSomeValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.HasSelf.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataSomeValuesFrom.class, 0);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testFamilyListObjectsOWL1Lite(TestSpec spec) {
        String ns = "http://www.co-ode.org/roberts/family-tree.owl#";
        OntModel m = OntModelFactory.createModel(
                RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                spec.inst);

        List<OntClass> equivalentToWife = m.getOntClass(ns + "Wife").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToWife.size());
        Assertions.assertInstanceOf(OntClass.IntersectionOf.class, equivalentToWife.get(0));
        Assertions.assertEquals(OntClass.IntersectionOf.class, equivalentToWife.get(0).objectType());
        List<OntClass> equivalentToSex = m.getOntClass(ns + "Sex").equivalentClasses().collect(Collectors.toList());
        Assertions.assertEquals(1, equivalentToSex.size());
        // generic class:
        Assertions.assertFalse(equivalentToSex.get(0) instanceof OntClass.UnionOf);
        Assertions.assertNotEquals(OntClass.UnionOf.class, equivalentToSex.get(0).objectType());

        assertOntObjectsCount(m, OntObject.class, 1684);
        assertOntObjectsCount(m, OntEntity.class, 151);
        assertOntObjectsCount(m, OntNamedProperty.class, 90);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        // owl:NamedIndividual is not valid class-type in OWL1:
        assertOntObjectsCount(m, OntIndividual.Named.class, 3);
        assertOntObjectsCount(m, OntObjectProperty.Named.class, 80);
        assertOntObjectsCount(m, OntAnnotationProperty.class, 1);
        assertOntObjectsCount(m, OntDataProperty.class, 9);

        assertOntObjectsCount(m, OntObjectProperty.class, 80);
        assertOntObjectsCount(m, OntRealProperty.class, 89);
        assertOntObjectsCount(m, OntProperty.class, 90);

        assertOntObjectsCount(m, OntDataRange.class, 0);
        assertOntObjectsCount(m, OntDataRange.Named.class, 0);
        assertOntObjectsCount(m, OntDataRange.OneOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Restriction.class, 0);
        assertOntObjectsCount(m, OntDataRange.UnionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.ComplementOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.IntersectionOf.class, 0);
        assertOntObjectsCount(m, OntDataRange.Combination.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        assertOntObjectsCount(m, OntClass.class, 289);
        assertOntObjectsCount(m, OntClass.Named.class, 58);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.LogicalExpression.class, 109);
        assertOntObjectsCount(m, OntClass.ValueRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.UnaryRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.Restriction.class, 111);
        assertOntObjectsCount(m, OntClass.NaryRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.ComponentRestriction.class, 111);
        assertOntObjectsCount(m, OntClass.CardinalityRestriction.class, 0);
        assertOntObjectsCount(m, OntClass.CollectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.IntersectionOf.class, 109);
        assertOntObjectsCount(m, OntClass.UnionOf.class, 0);
        assertOntObjectsCount(m, OntClass.OneOf.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.ObjectSomeValuesFrom.class, 111);
        assertOntObjectsCount(m, OntClass.ObjectAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataMinCardinality.class, 0);
        assertOntObjectsCount(m, OntClass.DataHasValue.class, 0);
        assertOntObjectsCount(m, OntClass.DataSomeValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.DataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.HasSelf.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataAllValuesFrom.class, 0);
        assertOntObjectsCount(m, OntClass.NaryDataSomeValuesFrom.class, 0);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testRemoveStatement(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named c = m.createOntClass("c");
        OntDataProperty d = m.createDataProperty("d");
        OntStatement s = d.addDomainStatement(c);
        s.addAnnotation(m.getRDFSLabel(), "a1").addAnnotation(m.getRDFSComment(), "a2");
        s.addAnnotation(m.getRDFSComment(), "a3");

        Assertions.assertEquals(14, m.size());

        d.removeDomain(c);
        Assertions.assertEquals(2, m.size());

        d.removeRange(c);
        Assertions.assertEquals(2, m.size());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testDisjointComponents(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst).setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named c1 = m.createOntClass("C1");
        OntClass.Named c2 = m.createOntClass("C1");
        OntObjectProperty.Named op1 = m.createObjectProperty("OP1");
        OntObjectProperty.Named op2 = m.createObjectProperty("OP2");
        OntObjectProperty.Named op3 = m.createObjectProperty("OP3");
        OntDataProperty dp1 = m.createDataProperty("DP1");
        OntDataProperty dp2 = m.createDataProperty("DP2");
        OntDataProperty dp3 = m.createDataProperty("DP3");
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = c1.createIndividual("I2");
        OntIndividual i3 = c2.createIndividual();

        List<OntIndividual> list1 = Arrays.asList(i1, i2);
        OntDisjoint.Individuals d1 = m.createDifferentIndividuals(list1);
        Assertions.assertEquals(list1, d1.getList().members().collect(Collectors.toList()));
        Assertions.assertEquals(2, d1.members().count());
        Assertions.assertSame(d1, d1.setComponents(i2, i3));
        Assertions.assertEquals(Arrays.asList(i2, i3), d1.members().collect(Collectors.toList()));

        OntDisjoint.ObjectProperties d2 = m.createDisjointObjectProperties(op1, op2, op3);
        Assertions.assertEquals(3, d2.getList().members().count());
        Assertions.assertTrue(d2.setComponents().getList().isEmpty());

        OntDisjoint.DataProperties d3 = m.createDisjointDataProperties(dp1, dp2);
        Assertions.assertEquals(2, d3.setComponents(Arrays.asList(dp3, m.getOWLBottomDataProperty())).members().count());


        Set<RDFNode> expected = new HashSet<>(Arrays.asList(i2, i3, dp3, OWL.bottomDataProperty));
        Set<RDFNode> actual = m.ontObjects(OntDisjoint.class)
                .map(x -> x.getList())
                .map(x -> x.as(RDFList.class))
                .map(RDFList::asJavaList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testCreateDifferentExpressions(TestSpec spec) {
        String uri = "http://test.com/graph/3";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel(spec.inst)
                .setNsPrefix("test", ns)
                .setNsPrefixes(OntModelFactory.STANDARD)
                .setID(uri)
                .getModel();

        OntDataProperty ndp1 = m.createDataProperty(ns + "dataProperty1");
        OntDataRange dt1 = m.createOntEntity(OntDataRange.Named.class, ns + "dataType1");
        dt1.asNamed().addEquivalentClass(m.getDatatype(XSD.dateTime));

        OntDataRange.Named dt2 = m.createOntEntity(OntDataRange.Named.class, ns + "dataType2");

        OntFacetRestriction fr1 = m.createFacetRestriction(OntFacetRestriction.MaxExclusive.class,
                ResourceFactory.createTypedLiteral(12));
        OntFacetRestriction fr2 = m.createFacetRestriction(OntFacetRestriction.LangRange.class,
                ResourceFactory.createTypedLiteral("\\d+"));

        OntDataRange dr1 = m.createDataRestriction(dt1.asNamed(), fr1, fr2);

        OntClass ce1 = m.createDataSomeValuesFrom(ndp1, dr1);

        OntDataRange dr2 = m.createDataIntersectionOf(dt1, dt2);
        OntIndividual i1 = ce1.createIndividual(ns + "individual1");
        OntClass ce2 = m.createDataMaxCardinality(ndp1, 343434, dr2);
        i1.attachClass(ce2).attachClass(m.createOntClass(ns + "Class1"));

        OntDataRange dr3 = m.createDataOneOf(m.getDatatype(XSD.integer).createLiteral(1),
                dt1.asNamed().createLiteral(2));
        OntDataRange dr4 = m.createDataComplementOf(dr3);
        m.createOntEntity(OntDataRange.Named.class, ns + "dataType3")
                .addEquivalentClass(m.createDataUnionOf(dr1, dr2, m.createDataIntersectionOf(dr1, dr4)));

        OntIndividual i2 = ce2.createIndividual();
        i2.addStatement(ndp1, ResourceFactory.createPlainLiteral("individual value"));

        m.createObjectOneOf(i1, i2, ce2.createIndividual());


        Assertions.assertEquals(3, m.ontObjects(OntIndividual.class).count(), "Incorrect count of individuals");
        Assertions.assertEquals(4, m.ontObjects(OntClass.class).count(), "Incorrect count of class expressions");
        Assertions.assertEquals(2, m.ontObjects(OntClass.Restriction.class).count(), "Incorrect count of restrictions");
        Assertions.assertEquals(1, m.ontObjects(OntClass.CardinalityRestriction.class).count(),
                "Incorrect count of cardinality restrictions");
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.Named.class).count(), "Incorrect count of datatype entities");
        Assertions.assertEquals(1, m.ontObjects(OntDataProperty.class).count(), "Incorrect count of data properties");
        Assertions.assertEquals(2, m.ontObjects(OntFacetRestriction.class).count(), "Incorrect count of facet restrictions");
        Assertions.assertEquals(9, m.ontObjects(OntDataRange.class).count(), "Incorrect count of data ranges");
        Assertions.assertEquals(6, m.ontObjects(OntEntity.class).count(), "Incorrect count of entities");
    }

    @Test
    public void testRecursionOnComplementOf() {
        // test there is no StackOverflowError
        Assertions.assertThrows(OntJenaException.Recursion.class, () -> {
            Model m = OntModelFactory.createDefaultModel().setNsPrefixes(OntModelFactory.STANDARD);
            Resource anon = m.createResource().addProperty(RDF.type, OWL.Class);
            anon.addProperty(OWL.complementOf, anon);
            OntModel ont = OntModelFactory.createModel(m.getGraph());
            List<OntClass> ces = ont.ontObjects(OntClass.class).collect(Collectors.toList());
            Assertions.assertEquals(0, ces.size());
        });
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_TRANS_INF",
    })
    public void testOneOfDataRangeForOWL2(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        OntDataRange.OneOf d1 = m.createDataOneOf(m.createTypedLiteral(42));
        OntDataRange.OneOf d2 = m.createDataOneOf(m.createTypedLiteral("A"), m.createLiteral("B"));
        OntDataRange.OneOf d3 = m.createResource(null, OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C")))
                .as(OntDataRange.OneOf.class);
        m.createResource("X", RDFS.Datatype) // treated as named data range
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("42")));
        Assertions.assertEquals(
                List.of(42),
                d1.getList().members().map(Literal::getInt).collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("A", "B"),
                d2.getList().members().map(Literal::getString).sorted().collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("C"),
                d3.getList().members().map(Literal::getString).collect(Collectors.toList())
        );

        Assertions.assertEquals(4, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(1, m.datatypes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
    })
    public void testDataRangesForOWL1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        OntDataRange.OneOf d1 = m.createDataOneOf(m.createTypedLiteral(42));
        OntDataRange.OneOf d2 = m.createDataOneOf(m.createTypedLiteral("A"), m.createLiteral("B"));
        OntDataRange.OneOf d3 = m.createResource("X", OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C"))).as(OntDataRange.OneOf.class);
        m.createResource("X", RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C")));
        Assertions.assertEquals(
                List.of(42),
                d1.getList().members().map(Literal::getInt).collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("A", "B"),
                d2.getList().members().map(Literal::getString).sorted().collect(Collectors.toList())
        );
        Assertions.assertEquals(
                List.of("C"),
                d3.getList().members().map(Literal::getString).collect(Collectors.toList())
        );

        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataUnionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataIntersectionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataComplementOf(d1));

        Assertions.assertEquals(3, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_LITE_MEM",
    })
    public void testDataRangesForOWL1Lite(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());

        m.createResource("X", OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("A")));
        m.createResource(null, OWL.DataRange)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("B")));
        m.createResource(null, RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("C")));
        m.createResource("Q", RDFS.Datatype)
                .addProperty(OWL.oneOf, m.createList(m.createLiteral("D")));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataOneOf(m.createTypedLiteral(42)));
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataUnionOf);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDataIntersectionOf);

        Assertions.assertEquals(0, m.ontObjects(OntDataRange.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Combination.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.OneOf.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntDataRange.Restriction.class).count());
        Assertions.assertEquals(0, m.datatypes().count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_TRANS_INF",
            "OWL1_LITE_MEM",
    })
    public void testDisjointIndividualsForOWL1(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        OntIndividual i1 = m.getOWLThing().createIndividual("A");
        OntIndividual i2 = m.getOWLThing().createIndividual("B");
        OntDisjoint.Individuals d = m.createDifferentIndividuals(i1, i2);
        Assertions.assertEquals(
                List.of("A", "B"),
                d.members().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        Assertions.assertEquals(8, m.statements().count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.class).count());

        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointClasses);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointDataProperties);
        Assertions.assertThrows(OntJenaException.Unsupported.class, m::createDisjointObjectProperties);

        Assertions.assertEquals(8, m.statements().count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.Individuals.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntDisjoint.class).count());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL2_DL_MEM_RDFS_BUILTIN_INF",
            "OWL2_MEM",
            "OWL2_MEM_RDFS_INF",
            "OWL2_MEM_TRANS_INF",
    })
    public void testHasSelfClassExpression1a(TestSpec spec) {
        Model g = ModelFactory.createDefaultModel();
        //_:x rdf:type owl:Restriction.
        //_:x owl:onProperty P.
        //_:x owl:hasSelf "true"^^xsd:boolean.
        Resource r = g.createResource().addProperty(RDF.type, OWL.Restriction)
                .addProperty(OWL.onProperty, g.createResource("P", OWL.ObjectProperty))
                .addLiteral(OWL.hasSelf, true);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst);
        Stream.of(OntClass.class, OntClass.HasSelf.class).forEach(t -> {
            List<OntClass> ces = m.ontObjects(t).collect(Collectors.toList());
            Assertions.assertEquals(1, ces.size());
            OntClass.HasSelf hasSelf = ces.get(0).as(OntClass.HasSelf.class);
            Assertions.assertEquals(m.getObjectProperty("P"), hasSelf.getProperty());
        });


        // can create & delete individual
        OntClass.HasSelf hasSelf = r.inModel(m).as(OntClass.HasSelf.class);
        OntIndividual i = hasSelf.as(OntClass.HasSelf.class).createIndividual("I");
        Assertions.assertEquals(hasSelf, i.classes(false).findFirst().orElseThrow());
        hasSelf.removeIndividual(i);
        Assertions.assertTrue(i.classes(false).findFirst().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(names = {
            "OWL1_MEM",
            "OWL1_MEM_RDFS_INF",
            "OWL1_MEM_TRANS_INF",
    })
    public void testHasSelfClassExpression1b(TestSpec spec) {
        Model g = ModelFactory.createDefaultModel();
        //_:x rdf:type owl:Restriction.
        //_:x owl:onProperty P.
        //_:x owl:hasSelf "true"^^xsd:boolean.
        g.createResource().addProperty(RDF.type, OWL.Restriction)
                .addProperty(OWL.onProperty, g.createResource("P", OWL.ObjectProperty))
                .addLiteral(OWL.hasSelf, true);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst);
        List<OntClass> ces1 = m.ontObjects(OntClass.HasSelf.class).collect(Collectors.toList());
        Assertions.assertEquals(0, ces1.size());
        List<OntClass> ces2 = m.ontObjects(OntClass.class).collect(Collectors.toList());
        Assertions.assertEquals(1, ces2.size());
        OntClass hasSelf = ces2.get(0);
        Assertions.assertThrows(UnsupportedPolymorphismException.class, () -> hasSelf.as(OntClass.HasSelf.class));

        OntIndividual i = hasSelf.createIndividual("I");
        Assertions.assertEquals(hasSelf, i.classes(false).findFirst().orElseThrow());
        hasSelf.removeIndividual(i);
        Assertions.assertTrue(i.classes(false).findFirst().isEmpty());
    }
}

