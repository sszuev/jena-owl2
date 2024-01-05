package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Graph repository.
 * Each {@link Graph} is associated with ID.
 * For OWL Ontology Graphs, Graph ID can be {@link com.github.sszuev.jena.ontapi.model.OntID#getImportsIRI()}.
 *
 * @see org.apache.jena.ontology.OntDocumentManager
 */
public interface GraphRepository {

    /**
     * A factory method to creates {@link GraphRepository} instance
     * that loads graphs on demand from the location to memory.
     * The location is specified by the method {@link DocumentGraphRepository#addMapping(String, String)}.
     *
     * @return {@link DocumentGraphRepository}
     */
    static DocumentGraphRepository createGraphDocumentRepositoryMem() {
        return createGraphDocumentRepository(GraphMemFactory::createDefaultGraph);
    }

    /**
     * A factory method to creates {@link GraphRepository} instance
     * that loads graphs on demand from the location.
     * The location is specified by the method {@link DocumentGraphRepository#addMapping(String, String)}.
     *
     * @param factory {@link Supplier} to produce new {@link Graph}, {@code null} for default
     * @return {@link DocumentGraphRepository}
     */
    static DocumentGraphRepository createGraphDocumentRepository(Supplier<Graph> factory) {
        return new DocumentGraphRepository(Objects.requireNonNull(factory, "Null graph factory"));
    }

    /**
     * Gets Graph by ID.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph}
     */
    Graph get(String id);

    /**
     * @return {@code Stream} of Graph's identifiers
     */
    Stream<String> ids();

    /**
     * Associates the specified graph with the specified ID,
     * returning the previous association or {@code null} if there was no association.
     *
     * @param id    {@code String} Graph's identifier
     * @param graph {@link Graph}
     * @return {@link Graph} or {@code null}
     */
    Graph put(String id, Graph graph);

    /**
     * Removes graph.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph}
     */
    Graph remove(String id);

    /**
     * Removes all graphs.
     */
    void clear();

}
