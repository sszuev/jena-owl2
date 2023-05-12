package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntID;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.testutils.ModelTestUtils;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.utils.OntModels;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * To test {@link ModelUtils} and {@link OntModels} utilities.
 * <p>
 * Created by @ssz on 25.04.2018.
 */
public class ModelUtilsTest {
    @SuppressWarnings("SameParameterValue")
    private static void testStatementsComparator(Model m, Comparator<Statement> comp) {
        List<Statement> first = Iterators.asStream(m.listStatements()).sorted(comp).collect(Collectors.toList());
        List<Statement> tmp;
        Collections.shuffle(tmp = m.listStatements().toList());
        List<Statement> second = tmp.stream().sorted(comp).collect(Collectors.toList());
        Assertions.assertEquals(first, second);
    }

    @Test
    public void testDeleteResources() {
        OntModel m = OntModelFactory.createModel()
                .read(ModelUtilsTest.class.getResourceAsStream("/testdata/recursive-graph.ttl"), null, "ttl");
        String ns = m.getID().getURI() + "#";
        OntObject d = m.createDisjointClasses(
                m.createOntClass(ns + "CL1"),
                m.createOntClass(ns + "CL2"),
                m.createObjectUnionOf(Arrays.asList(
                        m.createOntClass(ns + "CL4"),
                        m.createOntClass(ns + "CL5"),
                        m.createOntClass(ns + "CL6"))),
                m.createOntClass(ns + "CL3"));


        Assertions.assertEquals(40, m.localStatements().count());

        Resource r = m.statements(null, RDFS.subClassOf, null)
                .map(Statement::getObject)
                .filter(RDFNode::isAnon)
                .map(RDFNode::asResource)
                .filter(s -> s.hasProperty(OWL.someValuesFrom))
                .findFirst().orElseThrow(IllegalStateException::new);

        ModelTestUtils.deleteAll(r);
        ModelTestUtils.deleteAll(d);
        List<OntClass> classes = m.classes()
                .filter(s -> s.getLocalName().contains("CL"))
                .collect(Collectors.toList());
        classes.forEach(ModelTestUtils::deleteAll);

        Assertions.assertEquals(10, m.statements().count());
    }

    @Test
    public void testListLangValues() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntID id = m.getID()
                .addVersionInfo("lab1")
                .addVersionInfo("lab1", "e1")
                .addVersionInfo("lab2", "e2")
                .addVersionInfo("lab3", "language3")
                .addVersionInfo("lab4", "e2")
                .addVersionInfo("lab5", "e2")
                .addVersionInfo("lab5");

        Property p = OWL.versionInfo;
        Assertions.assertEquals(2, ModelUtils.langValues(id, p, null).count());
        Assertions.assertEquals(3, ModelUtils.langValues(id, p, "e2").count());
        Assertions.assertEquals(1, ModelUtils.langValues(id, p, "language3").count());
        Assertions.assertEquals(7, m.listObjectsOfProperty(id, p).toSet().size());
    }

    @Test
    public void testInsertModel() {
        OntModel a1 = OntModelFactory.createModel().setID("http://a").getModel();
        OntModel a2 = OntModelFactory.createModel().setID("http://a").getModel();
        OntClass c1 = a1.createOntClass("http://a#Class-a1");
        OntClass c2 = a2.createOntClass("http://a#Class-a2");

        // collection depending on a1
        OntModel m1 = OntModelFactory.createModel().setID("http://m1").getModel().addImport(a1);
        OntModel m2 = OntModelFactory.createModel().setID("http://m2").getModel().addImport(a1);
        Assertions.assertTrue(ModelFactory.createModelForGraph(m1.getGraph()).containsResource(c1));
        Assertions.assertFalse(ModelFactory.createModelForGraph(m1.getGraph()).containsResource(c2));
        Assertions.assertTrue(ModelFactory.createModelForGraph(m2.getGraph()).containsResource(c1));
        Assertions.assertFalse(ModelFactory.createModelForGraph(m2.getGraph()).containsResource(c2));

        OntModels.insert(() -> Stream.of(m1, m2), a2, true);
        Assertions.assertTrue(ModelFactory.createModelForGraph(m1.getGraph()).containsResource(c2));
        Assertions.assertFalse(ModelFactory.createModelForGraph(m1.getGraph()).containsResource(c1));
        Assertions.assertTrue(ModelFactory.createModelForGraph(m2.getGraph()).containsResource(c2));
        Assertions.assertFalse(ModelFactory.createModelForGraph(m2.getGraph()).containsResource(c1));
    }

    @Test
    public void testMiscModelsFunctionality() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        Resource t = m.getResource("type");
        RDFList list = ModelUtils.createTypedList(m, t, Arrays.asList(a, b));
        Assertions.assertNotNull(list);

        Assertions.assertEquals(8, m.size());
        Assertions.assertEquals(2, m.listStatements(null, RDF.type, t).toList().size());

        Assertions.assertTrue(ModelUtils.isInList(m, a));
        Assertions.assertTrue(ModelUtils.isInList(m, b));

        Assertions.assertEquals(6, Iterators.peek(ModelTestUtils.listDescendingStatements(list),
                s -> Assertions.assertTrue(RDF.type.equals(s.getPredicate()) || ModelUtils.isInList(s))).toList().size());

        Assertions.assertEquals(2, ModelUtils.subjects(t).count());
        Assertions.assertEquals(2, ModelTestUtils.listAscendingStatements(RDF.nil.inModel(m)).toList().size());
    }

    @Test
    public void testStatementsComparators() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass a = m.createOntClass("A");
        OntClass b = m.createOntClass("B");
        m.createObjectSomeValuesFrom(m.createObjectProperty("P"), m.createObjectComplementOf(m.createObjectUnionOf(a, b)));

        testStatementsComparator(m, ModelUtils.STATEMENT_COMPARATOR);
    }

    @Test
    public void testTripleToStatement() {
        OntModel m = OntModelFactory.createModel();
        m.createDisjointClasses(m.createOntClass("X1").addComment("A1"))
                .addComment("A2")
                .addComment("A3");

        Triple t1 = Iterators.findFirst(m.getBaseGraph().find(Node.ANY, RDF.type.asNode(), OWL.AllDisjointClasses.asNode()))
                .orElseThrow(AssertionError::new);
        OntStatement s1 = OntModels.toOntStatement(t1, m);
        Assertions.assertEquals(2, s1.annotations().count());

        Triple t2 = Iterators.findFirst(m.getBaseGraph().find(Node.ANY, RDF.type.asNode(), OWL.Class.asNode()))
                .orElseThrow(AssertionError::new);
        OntStatement s2 = OntModels.toOntStatement(t2, m);
        Assertions.assertEquals(1, s2.annotations().count());
    }
}
