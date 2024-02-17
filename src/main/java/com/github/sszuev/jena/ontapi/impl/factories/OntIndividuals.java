package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntIndividuals {
    private static final String FORBIDDEN_SUBJECTS = OntIndividual.Anonymous.class.getName() + ".InSubject";
    private static final String FORBIDDEN_OBJECTS = OntIndividual.Anonymous.class.getName() + ".InObject";
    // allowed predicates for a subject (the pattern '_:x p ANY'):
    private static final Set<Node> FOR_SUBJECT = Stream.of(OWL.sameAs, OWL.differentFrom)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
    // allowed predicates for an object (the pattern 'ANY p _:x'):
    private static final Set<Node> FOR_OBJECT = Stream.of(OWL.sameAs, OWL.differentFrom,
                    OWL.sourceIndividual, OWL.targetIndividual, OWL.hasValue,
                    OWL.annotatedSource, OWL.annotatedTarget,
                    RDF.first, SWRL.argument1, SWRL.argument2)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());

    public static boolean testAnonymousIndividual(Node node, EnhGraph eg) {
        if (!node.isBlank()) {
            return false;
        }
        boolean hasType = false;
        // class-assertion:
        ExtendedIterator<Node> types = eg.asGraph().find(node, RDF.Nodes.type, Node.ANY).mapWith(Triple::getObject);
        try {
            while (types.hasNext()) {
                if (OntEnhGraph.canAs(OntClass.class, types.next(), eg)) return true;
                hasType = true;
            }
        } finally {
            types.close();
        }
        // any other typed statement (builtin, such as owl:AllDifferent):
        if (hasType) {
            return false;
        }
        // all known predicates whose subject definitely cannot be an individual
        Set<Node> forbiddenSubjects = reserved(eg, FORBIDDEN_SUBJECTS, FOR_SUBJECT);
        // _:x @built-in-predicate @any:
        ExtendedIterator<Node> bySubject = eg.asGraph().find(node, Node.ANY, Node.ANY).mapWith(Triple::getPredicate);
        try {
            while (bySubject.hasNext()) {
                if (forbiddenSubjects.contains(bySubject.next()))
                    return false;
            }
        } finally {
            bySubject.close();
        }
        // all known predicates whose object definitely cannot be an individual
        Set<Node> forbiddenObjects = reserved(eg, FORBIDDEN_OBJECTS, FOR_OBJECT);
        // @any @built-in-predicate _:x
        ExtendedIterator<Node> byObject = eg.asGraph().find(Node.ANY, Node.ANY, node).mapWith(Triple::getPredicate);
        try {
            while (byObject.hasNext()) {
                if (forbiddenObjects.contains(byObject.next()))
                    return false;
            }
        } finally {
            byObject.close();
        }
        // tolerantly allow any other blank node to be treated as anonymous individual:
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Set<Node> reserved(EnhGraph eg, String key, Set<Node> forbiddenProperties) {
        OntPersonality personality = OntEnhGraph.asPersonalityModel(eg).getOntPersonality();
        OntPersonality.Builtins builtins = personality.getBuiltins();
        OntPersonality.Reserved reserved = personality.getReserved();
        Set<Node> builtinProperties = builtins.getOntProperties();
        if (eg instanceof OntGraphModelImpl) {
            Map<String, Object> store = ((OntGraphModelImpl) eg).propertyStore;
            Object res = store.get(key);
            if (res != null) {
                return (Set<Node>) res;
            }
            Set<Node> forbidden = reserved.getProperties().stream()
                    .filter(n -> !builtinProperties.contains(n) && !forbiddenProperties.contains(n))
                    .collect(Collectors.toUnmodifiableSet());
            store.put(key, forbidden);
            return forbidden;
        }
        return reserved.getProperties().stream()
                .filter(n -> !builtinProperties.contains(n) && !forbiddenProperties.contains(n))
                .collect(Collectors.toUnmodifiableSet());
    }
}
