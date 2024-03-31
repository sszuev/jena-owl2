package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.impl.GraphListenerBase;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OntModelMiscTest {
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
    public void testCheckCreate() {
        OntModel m = OntModelFactory.createModel();
        List<Triple> triples = new ArrayList<>();
        m.getGraph().getEventManager().register(new GraphListenerBase() {
            @Override
            protected void addTripleEvent(Graph g, Triple t) {
                triples.add(t);
            }

            @Override
            protected void deleteTripleEvent(Graph g, Triple t) {
                Assertions.fail();
            }
        });
        m.createObjectUnionOf(m.getOWLThing());
        Assertions.assertEquals(4, triples.size());

        UnionGraph ug = (UnionGraph) m.getGraph();
        Assertions.assertEquals(0L, ug.superGraphs().count());
    }
}
