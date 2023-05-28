package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link OntIndividual} implementation, both for anonymous and named individuals.
 * <p>
 * Created @ssz on 09.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntIndividualImpl extends OntObjectImpl implements OntIndividual {

    private static final String FORBIDDEN_SUBJECTS = Anonymous.class.getName() + ".InSubject";
    private static final String FORBIDDEN_OBJECTS = Anonymous.class.getName() + ".InObject";

    // allowed predicates for subject (the pattern '_:x p ANY'):
    private static final Set<Node> FOR_SUBJECT = Stream.of(OWL.sameAs, OWL.differentFrom)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
    // allowed predicates for object (the pattern 'ANY p _:x'):
    private static final Set<Node> FOR_OBJECT = Stream.of(OWL.sameAs, OWL.differentFrom,
                    OWL.sourceIndividual, OWL.targetIndividual, OWL.hasValue,
                    OWL.annotatedSource, OWL.annotatedTarget,
                    RDF.first, SWRL.argument1, SWRL.argument2)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());

    private static final EnhNodeFinder FINDER = EnhNodeFinder.ANY_SUBJECT_AND_OBJECT;
    public static final EnhNodeFactory OWL2_ANONYMOUS_INDIVIDUAL_FACTORY = OntEnhNodeFactories.createCommon(AnonymousImpl.class, FINDER,
            OntIndividualImpl::testAnonymousIndividual);

    public static final EnhNodeFactory OWL2_INDIVIDUAL_FACTORY = OntEnhNodeFactories.createFrom(FINDER, Named.class, Anonymous.class);

    public OntIndividualImpl(Node n, EnhGraph m) {
        super(n, m);
    }

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
        OntPersonality personality = OntEnhGraph.asPersonalityModel(eg).getOntPersonality();
        OntPersonality.Builtins builtins = personality.getBuiltins();
        OntPersonality.Reserved reserved = personality.getReserved();

        // all known predicates whose subject definitely cannot be an individual
        Set<Node> forbiddenSubjects = reserved.get(FORBIDDEN_SUBJECTS, () -> {
            Set<Node> bSet = builtins.getProperties();
            return reserved.getProperties().stream()
                    .filter(n -> !bSet.contains(n))
                    .filter(n -> !FOR_SUBJECT.contains(n))
                    .collect(Collectors.toUnmodifiableSet());
        });
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
        Set<Node> forbiddenObjects = reserved.get(FORBIDDEN_OBJECTS, () -> {
            Set<Node> bSet = builtins.getProperties();
            return reserved.getProperties().stream()
                    .filter(n -> !bSet.contains(n))
                    .filter(n -> !FOR_OBJECT.contains(n))
                    .collect(Collectors.toUnmodifiableSet());
        });
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

    public static Anonymous createAnonymousIndividual(RDFNode node) {
        if (OntJenaException.notNull(node, "Null node.").canAs(Anonymous.class))
            return node.as(Anonymous.class);
        if (node.isAnon()) {
            return new AnonymousImpl(node.asNode(), (EnhGraph) node.getModel());
        }
        throw new OntJenaException.Conversion(node + " could not be " + Anonymous.class);
    }

    static Stream<OntClass> explicitClassTypes(OntIndividual individual) {
        return individual.objects(RDF.type, OntClass.class);
    }

    @Override
    public Stream<OntClass> classes(boolean direct) {
        return Iterators.fromSet(() -> getClasses(direct));
    }

    /**
     * Lists all right parts from class assertion statements where this individual is at subject position.
     *
     * @return {@link ExtendedIterator} over all direct {@link OntClass class}-types
     */
    public ExtendedIterator<OntClass> listClasses() {
        return listObjects(RDF.type, OntClass.class);
    }


    /**
     * Returns a {@code Set} of all class-types,
     * including their super-classes if the parameter {@code direct} is {@code false}.
     *
     * @param direct if {@code true} returns only direct types, just like {@code #listClasses().toSet()}
     * @return a {@code Set} of all {@link OntClass class}-types
     */
    public Set<OntClass> getClasses(boolean direct) {
        Stream<OntClass> directClasses = explicitClassTypes(this)
                .flatMap(it -> Stream.concat(OntCEImpl.equivalentsBySubClassOf(it), Stream.of(it)));
        if (direct) {
            return directClasses.collect(Collectors.toSet());
        }
        return directClasses.flatMap(it -> Stream.concat(Stream.of(it), it.superClasses(false))).collect(Collectors.toSet());
    }

    @Override
    public boolean isLocal() {
        Optional<OntStatement> root = findRootStatement();
        return (root.isPresent() && root.get().isLocal()) || hasLocalClassAssertions();
    }

    protected boolean hasLocalClassAssertions() {
        return Iterators.findFirst(listClassAssertions().filterKeep(OntStatement::isLocal)).isPresent();
    }

    /**
     * Lists all class assertion statements.
     *
     * @return {@link ExtendedIterator} over all class assertions.
     */
    public ExtendedIterator<OntStatement> listClassAssertions() {
        return listStatements(RDF.type).filterKeep(s -> s.getObject().canAs(OntClass.class));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Stream<OntNegativeAssertion> negativeAssertions() {
        return Iterators.asStream(listNegativeAssertions(), getCharacteristics());
    }

    @SuppressWarnings("rawtypes")
    public ExtendedIterator<OntNegativeAssertion> listNegativeAssertions() {
        return listSubjects(OWL.sourceIndividual, OntNegativeAssertion.class);
    }

    @Override
    protected Set<OntStatement> getContent() {
        Set<OntStatement> res = super.getContent();
        listNegativeAssertions().forEachRemaining(x -> res.addAll(((OntObjectImpl) x).getContent()));
        return res;
    }

    /**
     * Represents a named individual.
     * Note: it may not have {@link OntObject#getMainStatement()} statement.
     */
    public static class NamedImpl extends OntIndividualImpl implements Named {
        public NamedImpl(Node n, EnhGraph m) {
            super(checkNamed(n), m);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.of(getModel().createStatement(this, RDF.type, OWL.NamedIndividual).asRootStatement())
                    .filter(r -> getModel().contains(r));
        }

        @Override
        public boolean isBuiltIn() {
            return false;
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return Named.class;
        }

        @Override
        public NamedImpl detachClass(Resource clazz) {
            OntGraphModelImpl m = getModel();
            m.listOntStatements(this, RDF.type, clazz)
                    .filterDrop(s -> OWL.NamedIndividual.equals(s.getObject()))
                    .toList()
                    .forEach(s -> m.remove(s.clearAnnotations()));
            return this;
        }
    }

    /**
     * See description to the interface {@link Anonymous}.
     * The current implementation allows treating b-node as anonymous individual
     * in any case except the following cases:
     * <ul>
     * <li>it is a subject in statement "_:x rdf:type s", where "s" is not a class expression ("C").</li>
     * <li>it is a subject in statement "_:x @predicate @any", where @predicate is from reserved vocabulary
     * but not object, data or annotation built-in property
     * and not owl:sameAs and owl:differentFrom.</li>
     * <li>it is an object in statement "@any @predicate _:x", where @predicate is from reserved vocabulary
     * but not object, data or annotation built-in property
     * and not owl:sameAs, owl:differentFrom, owl:hasValue, owl:sourceIndividual and rdf:first.</li>
     * </ul>
     * <p>
     * for notations and self-education see our main <a href="https://www.w3.org/TR/owl2-quick-reference/">OWL2 Quick Refs</a>
     */
    public static class AnonymousImpl extends OntIndividualImpl implements Anonymous {

        public AnonymousImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public boolean isLocal() {
            return hasLocalClassAssertions();
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.empty();
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return Anonymous.class;
        }

        @Override
        public AnonymousImpl detachClass(Resource clazz) {
            Set<OntClass> classes = classes().collect(Collectors.toSet());
            if (clazz == null && !classes.isEmpty()) {
                throw new OntJenaException.IllegalState("Detaching classes is prohibited: " +
                        "the anonymous individual (" + this + ") should contain at least one class assertion, " +
                        "otherwise it can be lost");
            }
            if (classes.size() == 1 && classes.iterator().next().equals(clazz)) {
                throw new OntJenaException.IllegalState("Detaching class (" + clazz + ") is prohibited: " +
                        "it is a single class assertion for the individual " + this + ".");
            }
            remove(RDF.type, clazz);
            return this;
        }

    }
}
