package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.impl.objects.OntAnnotationImpl;
import com.github.sszuev.jena.ontapi.model.OntAnnotation;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Set;

final class OntAnnotations {
    public static final Set<Node> REQUIRED_PROPERTY_NODES = ModelUtils.asUnmodifiableNodeSet(OntAnnotationImpl.REQUIRED_PROPERTIES);
    public static final Node AXIOM = OWL.Axiom.asNode();
    public static final Node ANNOTATION = OWL.Annotation.asNode();
    public static final Set<Node> EXTRA_ROOT_TYPES_AS_NODES = ModelUtils.asUnmodifiableNodeSet(OntAnnotationImpl.EXTRA_ROOT_TYPES);

    /**
     * Lists all root {@link Node}s of top-level {@link OntAnnotation}s in the given model.
     * In OWL2 a top-level annotation must have one of the following {@code rdf:type}s:
     * {@link OWL#Axiom owl:Axiom}, {@link OWL#AllDisjointClasses owl:AllDisjointClasses},
     * {@link OWL#AllDisjointProperties owl:AllDisjointProperties}, {@link OWL#AllDifferent owl:AllDifferent} or
     * {@link OWL#NegativePropertyAssertion owl:NegativePropertyAssertion}
     *
     * @param eg {@link EnhGraph} model to search in
     * @return {@link ExtendedIterator} of {@link Node}s
     */
    public static ExtendedIterator<Node> listRootAnnotations(EnhGraph eg) {
        return Iterators.flatMap(Iterators.of(AXIOM).andThen(EXTRA_ROOT_TYPES_AS_NODES.iterator()),
                        t -> eg.asGraph().find(Node.ANY, RDF.Nodes.type, t))
                .mapWith(Triple::getSubject);
    }

    public static boolean testAnnotation(Node node, EnhGraph graph) {
        return testAnnotation(node, graph.asGraph());
    }

    public static boolean testAnnotation(Node node, Graph graph) {
        if (!node.isBlank()) return false;
        ExtendedIterator<Node> types = graph.find(node, RDF.Nodes.type, Node.ANY).mapWith(Triple::getObject);
        try {
            while (types.hasNext()) {
                Node t = types.next();
                if (AXIOM.equals(t) || ANNOTATION.equals(t)) {
                    // test spec
                    Set<Node> props = graph.find(node, Node.ANY, Node.ANY).mapWith(Triple::getPredicate).toSet();
                    return props.containsAll(REQUIRED_PROPERTY_NODES);
                }
                // special cases: owl:AllDisjointClasses, owl:AllDisjointProperties,
                // owl:AllDifferent or owl:NegativePropertyAssertion
                if (EXTRA_ROOT_TYPES_AS_NODES.contains(t)) {
                    return true;
                }
            }
        } finally {
            types.close();
        }
        return false;
    }
}
