package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.OntObjectPersonalityBuilder;
import com.github.sszuev.jena.ontapi.common.OntPersonalities;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.impl.UnionGraphImpl;
import com.github.sszuev.jena.ontapi.impl.repositories.OntUnionGraphRepository;
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
     * @return {@code Graph}
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
     * @param data {@link Graph} (base graph)
     * @param spec {@link OntSpecification}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph data, OntSpecification spec) {
        Objects.requireNonNull(data);
        ReasonerFactory reasonerFactory = Objects.requireNonNull(spec).getReasonerFactory();
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(Graphs.makeOntUnionFrom(data, OntModelFactory::createUnionGraph), spec.getPersonality());
        }
        return createModel(data, spec.getPersonality(), reasonerFactory.create(null));
    }

    /**
     * Creates an {@link OntModel Ontology Model} which is {@link org.apache.jena.rdf.model.InfModel Inference Model}.
     * The specified graph and its subgraphs (if any) must not be {@link InfGraph}.
     *
     * @param graph        {@link Graph}
     * @param personality {@link OntPersonality}
     * @param reasoner    {@link Reasoner}
     * @return {@link OntModel}
     * @see OntModel#asInferenceModel()
     */
    public static OntModel createModel(Graph graph, OntPersonality personality, Reasoner reasoner) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(reasoner);
        Objects.requireNonNull(personality);
        if (Graphs.dataGraphs(graph).anyMatch(it -> it instanceof InfGraph)) {
            throw new IllegalArgumentException("InfGraph in the hierarchy detected");
        }
        UnionGraph unionGraph = Graphs.makeOntUnionFrom(graph, OntModelFactory::createUnionGraph);
        InfGraph infGraph = reasoner.bind(unionGraph);
        return new OntGraphModelImpl(infGraph, personality);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     * The {@code repository} manages all the dependencies.
     * See {@link #createModel(Graph, OntSpecification, GraphRepository)}.
     *
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}
     * @return {@link OntModel}
     */
    public static OntModel createModel(OntSpecification spec, GraphRepository repository) {
        return createModel(createDefaultGraph(), spec, repository);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     * The {@code repository} manages all the dependencies (imports closure).
     * Note that for consistency it is necessary to work only
     * through the {@link OntModel} or {@link UnionGraph} interfaces.
     * Working directly with the {@link UnionGraph#getBaseGraph()} or {@code repository} may break the state.
     * Imports closure control is performed via {@link UnionGraph.Listener},
     * any ontological graphs from the {@code repository} which are in use, will be wrapped as {@link UnionGraph}.
     * When adding subgraph using the {@link UnionGraph#addSubGraph(Graph)} method
     * a statement {@code a owl:import b} will be added.
     * In turns, adding a statement {@code a owl:import b} will cause adding a subgraph.
     * If a subgraph cannot be found in the {@code repository},
     * an empty ontology graph will be associated with the corresponding {@code owl:import}.
     * The specified graph and its subgraphs (if any) must not be {@link InfGraph}.
     * Note that the method adds ontology headers to each subgraph of the specified graph, including itself.
     *
     * @param graph       {@link Graph}
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph, OntSpecification spec, GraphRepository repository) {
        Objects.requireNonNull(spec);
        Objects.requireNonNull(repository);
        Objects.requireNonNull(graph);
        if (Graphs.dataGraphs(graph).anyMatch(it -> it instanceof InfGraph)) {
            throw new IllegalArgumentException("InfGraph in the hierarchy detected");
        }
        OntUnionGraphRepository ontUnionGraphRepository = new OntUnionGraphRepository(
                repository,
                OntModelFactory::createUnionGraph,
                OntModelFactory::createDefaultGraph,
                /*ignoreUnresolvedImports*/ true);
        ReasonerFactory reasonerFactory = spec.getReasonerFactory();
        UnionGraph union;
        if (graph instanceof UnionGraph) {
            union = (UnionGraph) graph;
            Graphs.flatTree((UnionGraph) graph).forEach(it -> {
                Graphs.findOntologyNameNode(it.getBaseGraph()).orElseGet(() -> Graphs.createOntologyHeaderNode(it, null));
                ontUnionGraphRepository.put(it);
            });
        } else {
            Graphs.dataGraphs(graph).forEach(it -> {
                String name = Graphs.findOntologyNameNode(it)
                        .orElseGet(() -> Graphs.createOntologyHeaderNode(it, null))
                        .toString();
                repository.put(name, it);
            });
            union = ontUnionGraphRepository.put(Graphs.getBase(graph));
        }
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(union, spec.getPersonality());
        }
        InfGraph inf = reasonerFactory.create(null).bind(union);
        return new OntGraphModelImpl(inf, spec.getPersonality());
    }

}
