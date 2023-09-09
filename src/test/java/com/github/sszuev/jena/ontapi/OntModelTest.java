package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.impl.objects.OntCEImpl;
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
import org.apache.jena.graph.Graph;
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
public class OntModelTest {

    @SafeVarargs
    private static <X> Set<X> toSet(Collection<? extends X>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

    private static void assertOntObjectsCount(OntModel m, Class<? extends OntObject> type, long expected) {
        Assertions.assertEquals(expected, m.ontObjects(type).count());
    }

    private static void testPizzaCEs(Model m, Property predicate, List<? extends OntClass> ces) {
        String type = ces.isEmpty() ? null : ((OntCEImpl) ces.get(0)).getActualClass().getSimpleName();
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

    @SuppressWarnings("rawtypes")
    @Test
    public void testPizzaLoadCE() {
        OntModel m = OntModelFactory.createModel(RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph());

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

        List<OntClass.RestrictionCE> restrictionCEs = m.ontObjects(OntClass.RestrictionCE.class).collect(Collectors.toList());
        Assertions.assertEquals(m.listStatements(null, RDF.type, OWL.Restriction).toSet().size(), restrictionCEs.size());

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

        testPizzaCEs(m, OWL.someValuesFrom, objectSomeValuesFromCEs);
        testPizzaCEs(m, OWL.allValuesFrom, objectAllValuesFromCEs);
        testPizzaCEs(m, OWL.hasValue, objectHasValueCEs);
        testPizzaCEs(m, OWL.unionOf, unionOfCEs);
        testPizzaCEs(m, OWL.intersectionOf, intersectionOfCEs);
        testPizzaCEs(m, OWL.complementOf, complementOfCEs);
        testPizzaCEs(m, OWL.oneOf, oneOfCEs);
        testPizzaCEs(m, OWL.minCardinality, objectMinCardinalityCEs);
    }

    @Test
    public void testPizzaLoadProperties() {
        simplePropertiesValidation(
                OntModelFactory.createModel(RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph())
        );
    }

    @Test
    public void testFamilyLoadProperties() {
        simplePropertiesValidation(
                OntModelFactory.createModel(RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph())
        );
    }

    @Test
    public void testPizzaLoadIndividuals() {
        OntModel m = OntModelFactory.createModel(RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph());
        List<OntIndividual> individuals = m.ontObjects(OntIndividual.class).collect(Collectors.toList());
        Map<OntIndividual, Set<OntClass>> classes = individuals.stream()
                .collect(Collectors.toMap(Function.identity(), i -> i.classes().collect(Collectors.toSet())));
        classes.forEach((i, c) -> c.forEach(x -> Assertions.assertEquals(1, x.individuals()
                .filter(_i -> Objects.equals(_i, i)).count())));

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

    @Test
    public void testKoalaCommon() throws IOException {
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

        OntModel m = OntModelFactory.createModel();
        try (InputStream in = OntModelTest.class.getResourceAsStream("/koala.owl")) {
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

    @Test
    public void testKoalaProperties() throws IOException {
        OntModel m = OntModelFactory.createModel();
        try (InputStream in = OntModelTest.class.getResourceAsStream("/koala.owl")) {
            m.read(in, null, Lang.RDFXML.getName());
        }
        simplePropertiesValidation(m);
        OntObjectProperty p1 = m.objectProperties().findFirst().orElseThrow(AssertionError::new);
        Assertions.assertFalse(p1.findInverseProperty().isPresent());
        OntObjectProperty p2 = m.createResource().addProperty(OWL.inverseOf, p1).as(OntObjectProperty.class);
        Assertions.assertTrue(p2.findInverseProperty().isPresent());
        Assertions.assertEquals(1, p2.inverseProperties().count());
        Assertions.assertEquals(p1.asProperty(), p2.asProperty());
        Assertions.assertEquals(p1, p2.findInverseProperty().orElseThrow(AssertionError::new));
        Assertions.assertEquals(1, m.ontObjects(OntObjectProperty.Inverse.class).count());
    }

    @Test
    public void testCreateImports() {
        String baseURI = "http://test.com/graph/5";
        String baseNS = baseURI + "#";
        OntModel base = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
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

    @Test
    public void testAssemblySimplestOntology() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testCreateEntities() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        createEntityTest(m, "a-p", OntAnnotationProperty.class);
        createEntityTest(m, "o-p", OntObjectProperty.Named.class);
        createEntityTest(m, "d-p", OntDataProperty.class);
        createEntityTest(m, "c", OntClass.Named.class);
        createEntityTest(m, "d", OntDataRange.Named.class);
        createEntityTest(m, "I", OntIndividual.Named.class);

    }

    private <E extends OntEntity> void createEntityTest(OntModel m, String uri, Class<E> type) {
        String pref = "Annotation[" + uri + "]:::";
        E e = m.createOntEntity(type, uri);
        e.addAnnotation(m.getRDFSComment(), pref + "entity of type " + type.getSimpleName())
                .addAnnotation(m.getRDFSLabel(), pref + "label");
        m.asStatement(e.getMainStatement().asTriple()).addAnnotation(m.getRDFSComment(), pref + "comment");
        Assertions.assertEquals(2, e.annotations().count());
        Assertions.assertEquals(2, e.statements().count());
        Assertions.assertSame(e, e.as(type));
    }

    @Test
    public void testObjectsContent() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testRemoveObjects() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);

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

    @Test
    public void testModelPrefixes() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testAdvancedModelImports() {
        OntModel av1 = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
                .setID("a").setVersionIRI("v1").getModel();
        OntModel av2 = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
                .setID("a").setVersionIRI("v2").getModel();
        OntModel b = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
                .setID("b").getModel();
        OntModel c = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD)
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

    @Test
    public void testCycleModelImports() {
        OntModel a = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntModel b = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntModel c = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testOntPropertyOrdinal() {
        Graph g = RDFIOTestUtils.loadResourceAsModel("/pizza.ttl", Lang.TURTLE).getGraph();
        OntModel m = OntModelFactory.createModel(g);
        OntNamedProperty<?> p = m.getOntEntity(OntNamedProperty.class, m.expandPrefix(":isIngredientOf"));
        Assertions.assertNotNull(p);
        Assertions.assertEquals(0, p.getOrdinal());
        Assertions.assertEquals(0, m.getRDFSComment().getOrdinal());
        Assertions.assertEquals(0, m.getOWLBottomDataProperty().getOrdinal());
    }

    @Test
    public void testFamilyListObjects() {
        OntModel m = OntModelFactory.createModel(RDFIOTestUtils.loadResourceAsModel("/family.ttl", Lang.TURTLE).getGraph(),
                OntPersonalities.OWL2_PERSONALITY_LAX);
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

        assertOntObjectsCount(m, OntDataRange.class, 0);

        assertOntObjectsCount(m, OntDisjoint.class, 1);
        assertOntObjectsCount(m, OntDisjoint.Classes.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Individuals.class, 1);
        assertOntObjectsCount(m, OntDisjoint.DataProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.ObjectProperties.class, 0);
        assertOntObjectsCount(m, OntDisjoint.Properties.class, 0);

        // todo: handle all other types
    }

    @Test
    public void testListIndividualTypes() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named a = m.createOntClass("A");
        OntClass.Named b = m.createOntClass("B");
        OntClass.Named c = m.createOntClass("C");
        OntClass.Named d = m.createOntClass("D");
        OntClass.Named e = m.createOntClass("E");

        b.addSuperClass(m.createObjectComplementOf(c)).addSuperClass(a);
        OntClass ae = m.createObjectIntersectionOf(a, e);
        d.addSuperClass(ae);
        a.addSuperClass(d);
        ae.addSuperClass(a).addSuperClass(b);

        OntIndividual i1 = a.createIndividual("i");
        OntIndividual i2 = d.createIndividual();
        i2.attachClass(b);
        i1.attachClass(d);


        Assertions.assertEquals(2, i2.classes(true).count());
        Assertions.assertEquals(5, i2.classes(false).count());

        Assertions.assertEquals(2, i1.classes(true).count());
        Assertions.assertEquals(5, i1.classes(false).count());
    }

    @Test
    public void testRemoveStatement() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testDisjointComponents() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
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

    @Test
    public void testCreateDifferentExpressions() {
        String uri = "http://test.com/graph/3";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel()
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
        Assertions.assertEquals(2, m.ontObjects(OntClass.RestrictionCE.class).count(), "Incorrect count of restrictions");
        Assertions.assertEquals(1, m.ontObjects(OntClass.CardinalityRestrictionCE.class).count(),
                "Incorrect count of cardinality restrictions");
        Assertions.assertEquals(3, m.ontObjects(OntDataRange.Named.class).count(), "Incorrect count of datatype entities");
        Assertions.assertEquals(1, m.ontObjects(OntDataProperty.class).count(), "Incorrect count of data properties");
        Assertions.assertEquals(2, m.ontObjects(OntFacetRestriction.class).count(), "Incorrect count of facet restrictions");
        Assertions.assertEquals(9, m.ontObjects(OntDataRange.class).count(), "Incorrect count of data ranges");
        Assertions.assertEquals(6, m.ontObjects(OntEntity.class).count(), "Incorrect count of entities");
    }

    @Test
    public void testHierarchyRoots() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.setNsPrefixes(OntModelFactory.STANDARD);
        OntClass c0 = m.createOntClass(":C0");
        OntClass c1 = m.createOntClass(":C1");
        OntClass c2 = m.createOntClass(":C2");
        OntClass c3 = m.createOntClass(":C3");
        OntClass c4 = m.createOntClass(":C4");
        OntClass c5 = m.createOntClass(":C5");
        OntClass c6 = m.createOntClass(":C6");
        OntClass c7 = m.createOntClass(":C7");
        OntClass c8 = m.createDataSomeValuesFrom(m.createDataProperty(":p1"), m.createDatatype(":dp"));
        OntClass c9 = m.createObjectOneOf(m.createIndividual(":i1"), m.createIndividual(":i2"));
        OntClass c10 = m.createObjectComplementOf(c6);
        OntClass c11 = m.getOWLThing();
        OntClass c12 = m.getOWLNothing();

        c1.addSuperClass(c2);
        c2.addSuperClass(c3);
        c3.addSuperClass(c4);
        c5.addSuperClass(c6);
        c6.addSuperClass(c12);
        c8.addSuperClass(c9);
        c9.addSuperClass(c5);
        c9.addSuperClass(c7);
        c10.addSuperClass(c11);

        Set<OntClass> roots = m.hierarchyRoots().collect(Collectors.toSet());
        Assertions.assertEquals(Set.of(c0, c4, c7, c10), roots);
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

    @Test
    public void testClassesRDFSMem() {
        Model base = ModelFactory.createDefaultModel();
        base.createResource("1", OWL.Class);
        base.createResource("2", RDFS.Datatype);
        base.createResource("3", RDFS.Class);
        base.createResource("4", RDFS.Class);

        OntModel m = OntModelFactory.createModel(base.getGraph(), OntSpecification.RDFS_MEM);

        List<OntClass.Named> res1 = m.classes().collect(Collectors.toList());
        List<OntEntity> res2 = m.ontEntities().collect(Collectors.toList());
        List<OntClass> res3 = m.ontObjects(OntClass.class).collect(Collectors.toList());
        List<OntClass> res4 = m.ontObjects(OntClass.Named.class).collect(Collectors.toList());

        Stream.of(res1, res2, res3, res4).forEach(x ->
                Assertions.assertEquals(List.of("3", "4"), x.stream().map(Resource::getURI).sorted().collect(Collectors.toList()))
        );
    }

}

