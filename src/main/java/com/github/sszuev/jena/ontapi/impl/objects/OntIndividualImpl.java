package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelConfig;
import com.github.sszuev.jena.ontapi.impl.HierarchySupport;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link OntIndividual} implementation, both for anonymous and named individuals.
 * <p>
 * Created @ssz on 09.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntIndividualImpl extends OntObjectImpl implements OntIndividual {

    public OntIndividualImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Anonymous createAnonymousIndividual(RDFNode node) {
        if (OntJenaException.notNull(node, "Null node.").canAs(Anonymous.class))
            return node.as(Anonymous.class);
        if (node.isAnon()) {
            return new AnonymousImpl(node.asNode(), (EnhGraph) node.getModel());
        }
        throw new OntJenaException.Conversion(node + " can't be presented as an anonymous individual");
    }

    /**
     * Returns a {@code Stream} of all class-types,
     * including their super-classes if the parameter {@code direct} is {@code false}.
     *
     * @param direct if {@code true} returns only direct types
     * @return a {@code Stream} of all {@link OntClass class}-types
     */
    @Override
    public Stream<OntClass> classes(boolean direct) {
        return classes(this, direct);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasOntClass(OntClass clazz, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(getModel(), RDF.type);
            if (reasonerProperty != null) {
                return getModel().contains(this, reasonerProperty, clazz);
            }
        }
        AtomicBoolean isIndividual = new AtomicBoolean(true);
        return HierarchySupport.contains(
                this,
                (OntObject) clazz,
                it -> (Stream<OntObject>) ((Stream<?>) listClassesFor(it, isIndividual)),
                direct,
                configValue(getModel(), OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    /**
     * Lists all right parts from class assertion statements where this individual is at subject position.
     *
     * @return {@link ExtendedIterator} over all direct {@link OntClass class}-types
     */
    public ExtendedIterator<OntClass> listClasses() {
        return listObjects(RDF.type, OntClass.class);
    }

    @SuppressWarnings("unchecked")
    static Stream<OntClass> classes(OntObject individual, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(individual.getModel(), RDF.type);
            if (reasonerProperty != null) {
                return individual.objects(reasonerProperty, OntClass.class);
            }
        }
        AtomicBoolean isIndividual = new AtomicBoolean(true);
        Stream<?> res = HierarchySupport.treeNodes(individual,
                it -> (Stream<OntObject>) ((Stream<?>) listClassesFor(it, isIndividual)),
                direct,
                configValue(individual.getModel(), OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
        return (Stream<OntClass>) res;
    }

    static Stream<OntClass> listClassesFor(OntObject resource, AtomicBoolean isFirstLevel) {
        if (isFirstLevel.get()) {
            isFirstLevel.set(false);
            return resource.objects(RDF.type, OntClass.class);
        }
        return resource.objects(RDFS.subClassOf, OntClass.class);
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
     * for notations see <a href="https://www.w3.org/TR/owl2-quick-reference/">OWL2 Quick Refs</a>
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
