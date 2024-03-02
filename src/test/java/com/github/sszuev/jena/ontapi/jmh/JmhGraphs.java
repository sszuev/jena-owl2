package com.github.sszuev.jena.ontapi.jmh;

import com.github.sszuev.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;

public class JmhGraphs {

    public static final Graph SIMPLE = RDFIOTestUtils.readResourceToModel(
            ModelFactory.createDefaultModel(), "/simple.ttl", Lang.TURTLE).getGraph();
    public static final Graph PIZZA = RDFIOTestUtils.readResourceToModel(
            ModelFactory.createDefaultModel(), "/pizza.ttl", Lang.TURTLE).getGraph();
    public static final Graph FAMILY = RDFIOTestUtils.readResourceToModel(
            ModelFactory.createDefaultModel(), "/family.ttl", Lang.TURTLE).getGraph();
    public static final Graph KOALA = RDFIOTestUtils.readResourceToModel(
            ModelFactory.createDefaultModel(), "/koala.owl", Lang.RDFXML).getGraph();
    public static final Graph WINE = RDFIOTestUtils.readResourceToModel(
            ModelFactory.createDefaultModel(), "/wine.ttl", Lang.TURTLE).getGraph();
}
