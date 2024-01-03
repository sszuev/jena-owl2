package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntObjectPersonalityBuilder;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.impl.UnionGraphImpl;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.utils.Graphs;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.util.Objects;

/**
 * A factory to produce {@link OntModel OWL2 model}s, {@link Model Common model}s and {@link Graph graph}s.
 * Currently, OWL2 models support only direct semantic with no additional entailment reasoning.
 * This corresponds to {@link org.apache.jena.ontology.OntModelSpec#OWL_MEM OWL_MEM spec} for {@link org.apache.jena.ontology.OntModel Jena OWL model}.
 * <p>
 * Created @ssz on 14.02.2017.
 */
public class OntModelFactory {

    /**
     * A {@code PrefixMapping} that contains the "standard" for OWL2 prefixes we know about, viz rdf, rdfs, xsd, and owl.
     */
    public static final PrefixMapping STANDARD = PrefixMapping.Factory.create()
            .setNsPrefix("owl", OWL.NS)
            .setNsPrefix("rdfs", RDFS.uri)
            .setNsPrefix("rdf", RDF.uri)
            .setNsPrefix("xsd", XSD.NS)
            .lock();

    static {
        init();
    }

    /**
     * Initializes Jena System.
     */
    public static void init() {
        JenaSystem.init();
    }

    /**
     * Creates default (in-memory) graph implementation.
     *
     * @return {@code GraphMem}
     */
    public static Graph createDefaultGraph() {
        return GraphMemFactory.createGraphMem();
    }

    /**
     * Wraps the given {@code base} graph as {@link UnionGraph}
     *
     * @param base {@code Graph}
     * @return {@link UnionGraph}
     */
    public static UnionGraph createUnionGraph(Graph base) {
        // non-distinct graph since requiring distinct is expensive;
        // usually OWL imports-closure does not contain duplicate data and
        // cyclic imports are resolved by the UnionGraph itself and do non lead to duplication data
        return new UnionGraphImpl(base, false);
    }

    /**
     * Creates default (in-memory) RDF Model implementation.
     *
     * @return {@link Model}
     * @see org.apache.jena.rdf.model.ModelFactory#createDefaultModel()
     */
    public static Model createDefaultModel() {
        return new ModelCom(createDefaultGraph());
    }

    /**
     * Creates a fresh in-memory Ontology RDF Model with default personalities.
     *
     * @return {@link OntModel}
     */
    public static OntModel createModel() {
        return createModel(createDefaultGraph());
    }

    /**
     * Creates an Ontology RDF Model wrapper around the given graph with default personalities.
     *
     * @param graph {@link Graph}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph) {
        return createModel(graph, OntSpecification.OWL2_DL_MEM_BUILTIN_INF);
    }

    /**
     * Creates an Ontology RDF Model wrapper around the given graph with given personalities.
     *
     * @param graph       {@link Graph}
     * @param personality {@link OntPersonality}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph, OntPersonality personality) {
        OntPersonality withBuiltinHierarchySupport = OntObjectPersonalityBuilder.from(personality)
                .setConfig(OntPersonalities.OWL2_CONFIG.setTrue(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT))
                .build();
        return createModel(graph, new OntSpecification(withBuiltinHierarchySupport, null));
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     *
     * @param spec {@link OntSpecification}
     * @return {@link OntModel}
     */
    public static OntModel createModel(OntSpecification spec) {
        return createModel(createDefaultGraph(), spec);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     *
     * @param spec {@link OntSpecification}
     * @param data {@link Graph} (base graph)
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph data, OntSpecification spec) {
        Objects.requireNonNull(data);
        ReasonerFactory reasonerFactory = spec.getReasonerFactory();
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(Graphs.makeOntUnionFrom(data, OntModelFactory::createUnionGraph), spec.getPersonality());
        }
        return createModel(data, spec.getPersonality(), reasonerFactory.create(null));
    }

    /**
     * Creates an {@link OntModel Ontology Model} which is {@link org.apache.jena.rdf.model.InfModel Inference Model}.
     *
     * @param data        {@link Graph} to wrap (base graph)
     * @param personality {@link OntPersonality}
     * @param reasoner    {@link Reasoner}
     * @return {@link OntModel}
     * @see OntModel#asInferenceModel()
     */
    public static OntModel createModel(Graph data, OntPersonality personality, Reasoner reasoner) {
        if (Graphs.dataGraphs(Objects.requireNonNull(data)).anyMatch(it -> it instanceof InfGraph)) {
            throw new IllegalArgumentException("InfGraph detected");
        }
        UnionGraph unionGraph = Graphs.makeOntUnionFrom(data, OntModelFactory::createUnionGraph);
        InfGraph infGraph = reasoner.bind(unionGraph);
        return new OntGraphModelImpl(infGraph, personality);
    }
}
