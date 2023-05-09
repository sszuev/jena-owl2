package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.testutils.RDFIOUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The simplest reasoner test.
 * Modified copy-paste from jena-core-tests (org.apache.jena.reasoner.test.ManualExample)
 * <p>
 * Created @ssz on 26.04.2017.
 */
public class JenaReasonerTest {

    /**
     * Illustrate different ways of finding a reasoner
     */
    @Test
    public void testFinding() {
        String NS = "urn:example/";

        // Build a trivial example data set
        OntModel example = OntModelFactory.createModel();
        OntDataProperty p = example.createDataProperty(NS + "p");
        OntDataProperty q = example.createDataProperty(NS + "q");
        p.addSuperProperty(q);
        example.createIndividual(NS + "a").addProperty(p, "foo");
        example.setNsPrefixes(OntModelFactory.STANDARD);

        Resource config = OntModelFactory.createDefaultModel()
                .createResource()
                .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
        Reasoner reasoner = RDFSRuleReasonerFactory.theInstance().create(config);

        InfModel inf = example.getInferenceModel(reasoner);

        Resource a = inf.getResource(NS + "a");
        Statement s = a.getProperty(q);
        Assertions.assertNotNull(s, "Null statement");
    }

    @Test
    public void testValidation1() {
        validationTest("/testdata/dttest1.nt", false);
    }

    @Test
    public void testValidation2() {
        validationTest("/testdata/dttest2.nt", false);
    }

    @Test
    public void testValidation3() {
        validationTest("/testdata/dttest3.nt", true);
    }


    private void validationTest(String resource, boolean result) {
        OntModel data = OntModelFactory.createModel(RDFIOUtils.loadResourceAsModel(resource, Lang.NTRIPLES).getGraph());
        InfModel inf = data.getInferenceModel(ReasonerRegistry.getRDFSReasoner());
        ValidityReport validity = inf.validate();
        List<ValidityReport.Report> reports = new ArrayList<>();
        if (!validity.isValid()) {
            for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = i.next();
                reports.add(report);
            }
        }
        Assertions.assertEquals(result, validity.isValid(), "Conflicts: " + reports);
    }

    /**
     * Illustrate generic rules and derivation tracing
     */
    @Test
    public void testDerivation() {
        // Test data
        String egNS = "urn:x-hp:eg/";
        OntModel rawData = OntModelFactory.createModel();
        Property p = rawData.createProperty(egNS, "p");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        Resource D = rawData.createResource(egNS + "D");
        A.addProperty(p, B);
        B.addProperty(p, C);
        C.addProperty(p, D);

        // Rule example
        String rules = "[rule1: (?a eg:p ?b) (?b eg:p ?c) -> (?a eg:p ?c)]";
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        InfModel inf = rawData.getInferenceModel(reasoner);

        List<Statement> statements = inf.listStatements(A, p, D).toList();
        Assertions.assertEquals(1, statements.size());
        StringWriter res = new StringWriter();
        PrintWriter out = new PrintWriter(res, true);
        Iterator<Derivation> id = inf.getDerivation(statements.get(0));
        while (id.hasNext()) {
            id.next().printTrace(out, true);
        }
        String expected = "Rule rule1 concluded (eg:A eg:p eg:D) <-\n" +
                "    Rule rule1 concluded (eg:A eg:p eg:C) <-\n" +
                "        Fact (eg:A eg:p eg:B)\n" +
                "        Fact (eg:B eg:p eg:C)\n" +
                "    Fact (eg:C eg:p eg:D)\n";
        Assertions.assertEquals(expected, res.toString().replace("\r", ""));
    }

    /**
     * Another generic rules illustration
     */
    @Test
    public void testGenericRules() {
        // Test data
        String egNS = "urn:x-hp:eg/";
        OntModel rawData = OntModelFactory.createModel();
        Property first = rawData.createProperty(egNS, "concatFirst");
        Property second = rawData.createProperty(egNS, "concatSecond");
        Property p = rawData.createProperty(egNS, "p");
        Property q = rawData.createProperty(egNS, "q");
        Property r = rawData.createProperty(egNS, "r");
        Resource A = rawData.createResource(egNS + "A");
        Resource B = rawData.createResource(egNS + "B");
        Resource C = rawData.createResource(egNS + "C");
        A.addProperty(p, B);
        B.addProperty(q, C);
        r.addProperty(first, p);
        r.addProperty(second, q);

        String data = RDFIOUtils.asString(rawData, Lang.TURTLE);

        // Rule example for
        String rules = "[r1: (?c eg:concatFirst ?p), (?c eg:concatSecond ?q) -> [r1b: (?x ?c ?y) <- (?x ?p ?z) (?z ?q ?y)]]";
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel inf = rawData.getInferenceModel(reasoner);
        Assertions.assertTrue(inf.contains(A, p, B));
        Assertions.assertTrue(inf.contains(A, r, C));

        Assertions.assertEquals(data, RDFIOUtils.asString(rawData, Lang.TURTLE), "Data has been changed");
    }

}
