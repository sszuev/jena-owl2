package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.stream.Stream;

/**
 * A common super-type for all the abstractions in this ontology representation package.
 * This is an analogue of the {@link org.apache.jena.ontology.OntResource Jena OntResource} interface,
 * but for {@link OntModel OWL2 Ontology RDF Graph Model}.
 */
interface OntResource extends Resource {

    /**
     * Returns the ontology model associated with this resource.
     * If the Resource was not created by a Model, the result may be null.
     *
     * @return {@link OntModel}
     */
    OntModel getModel();

    /**
     * Returns the main triple (wrapped as {@link OntStatement})
     * which determines the nature of this ontological resource.
     * Usually it is a type-declaration
     * (i.e., a triple with predicate {@code rdf:type} and with this resource as a subject).
     * The result may be {@code null} in several boundary cases (e.g., for built-in OWL entities).
     * <p>
     * Note that a main statement differs from the others:
     * for a common {@link OntStatement}, its annotations go in the form of {@link OntAnnotation Annotation},
     * but a main-statement can have annotation assertions.
     *
     * @return {@link OntStatement} or {@code null}
     */
    OntStatement getMainStatement();

    /**
     * Determines if this Ontology Resource is locally defined.
     * This means that the resource definition (i.e., a the {@link #getMainStatement() root statement})
     * belongs to the base ontology graph.
     * If the ontology contains subgraphs (which should match {@code owl:imports} in OWL)
     * and the resource is defined in one of them,
     * than this method called from top-level interface will return {@code false}.
     *
     * @return {@code true} if this resource is local to the base model graph.
     */
    boolean isLocal();

    /**
     * Lists all characteristic statements of the ontology resource, i.e.,
     * all those statements which completely determine this object nature according to the OWL2 specification.
     * For non-composite objects the result might contain only the {@link #getMainStatement() root statement}.
     * For composite objects (usually anonymous resources: disjoint sections, class expression, etc.)
     * the result would contain all statements in the graph directly related to the object
     * but without statements that relate to the object components.
     * The return stream is ordered and, in most cases,
     * the expression {@code this.spec().findFirst().get()} returns the same statement as {@code this.getRoot()}.
     * Object annotations are not included in the resultant stream.
     *
     * @return Stream of {@link Statement Jena Statement}s that fully describe this object in OWL2 terms
     */
    Stream<? extends Statement> spec();

    /**
     * Safely converts this RDF resource to the given {@code type} interface, if it is possible.
     * Otherwise, returns {@code null}.
     * A calling of this method is effectively equivalent to
     * the expression {@code this.canAs(type) ? this.as(type) : null}.
     *
     * @param type a {@code Class}-type of the desired RDF view (interface)
     * @param <X>  any subtype of {@link RDFNode}
     * @return an instance of the type {@link X} or {@code null}
     * @see RDFNode#as(Class)
     * @see RDFNode#canAs(Class)
     */
    default <X extends RDFNode> X getAs(Class<X> type) {
        return canAs(type) ? as(type) : null;
    }

}
