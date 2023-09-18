package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation for anonymous {@code owl:AllDisjointProperties}, {@code owl:AllDisjointClasses}, {@code owl:AllDifferent} sections.
 * <p>
 * Created by @ssz on 15.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntDisjointImpl<O extends OntObject> extends OntObjectImpl implements OntDisjoint<O> {
    public static final EnhNodeFinder PROPERTIES_FINDER = new EnhNodeFinder.ByType(OWL.AllDisjointProperties);
    public static final EnhNodeFinder DISJOINT_FINDER = OntEnhNodeFactories.createFinder(OWL.AllDisjointClasses,
            OWL.AllDifferent, OWL.AllDisjointProperties);

    public OntDisjointImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static EnhNodeFactory createFactory(Class<? extends OntDisjointImpl<?>> impl,
                                                Resource type,
                                                Class<? extends RDFNode> view,
                                                boolean allowEmptyList,
                                                Property... predicates) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, type);
        EnhNodeFinder finder = new EnhNodeFinder.ByType(type);
        EnhNodeFilter filter = EnhNodeFilter.BLANK.and(new EnhNodeFilter.HasType(type));
        return OntEnhNodeFactories.createCommon(maker, finder, filter
                .and(getHasPredicatesFilter(predicates))
                .and(getHasMembersOfFilter(view, allowEmptyList, predicates)));
    }

    private static EnhNodeFilter getHasPredicatesFilter(Property... predicates) {
        EnhNodeFilter res = EnhNodeFilter.FALSE;
        for (Property p : predicates) {
            res = res.or(new EnhNodeFilter.HasPredicate(p));
        }
        return res;
    }

    private static EnhNodeFilter getHasMembersOfFilter(Class<? extends RDFNode> view,
                                                       boolean allowEmptyList,
                                                       Property... predicates) {
        return (node, eg) -> {
            ExtendedIterator<Node> res = listRoots(node, eg.asGraph(), predicates);
            try {
                while (res.hasNext()) {
                    if (testList(res.next(), eg, view, allowEmptyList)) return true;
                }
            } finally {
                res.close();
            }
            return false;
        };
    }

    private static ExtendedIterator<Node> listRoots(Node node, Graph graph, Property... predicates) {
        return Iterators.flatMap(Iterators.of(predicates),
                p -> graph.find(node, p.asNode(), Node.ANY).mapWith(Triple::getObject));
    }

    private static boolean testList(Node node, EnhGraph graph, Class<? extends RDFNode> view, boolean allowEmptyList) {
        if (!RDFListImpl.factory.canWrap(node, graph)) return false;
        if (view == null) return true;
        RDFList list = RDFListImpl.factory.wrap(node, graph).as(RDFList.class);
        return (list.isEmpty() && allowEmptyList) ||
                Iterators.anyMatch(list.iterator().mapWith(RDFNode::asNode), n -> OntEnhGraph.canAs(view, n, graph));
    }

    public static Classes createDisjointClasses(OntGraphModelImpl model, Stream<OntClass> classes) {
        return create(model, OWL.AllDisjointClasses, Classes.class, OntClass.class, classes);
    }

    /**
     * Creates blank node {@code _:x rdf:type owl:AllDifferent. _:x owl:members (a1 ... an).}
     * <p>
     * Note: the predicate is {@link OWL#members owl:members},
     * not {@link OWL#distinctMembers owl:distinctMembers} (but the last one is correct also)
     * It is chosen as the preferred from considerations of uniformity.
     *
     * @param model       {@link OntGraphModelImpl}
     * @param individuals stream of {@link OntIndividual}
     * @return {@link Individuals}
     * @see <a href='https://www.w3.org/TR/owl2-quick-reference/#Additional_Vocabulary_in_OWL_2_RDF_Syntax'>4.2 Additional Vocabulary in OWL 2 RDF Syntax</a>
     */
    public static Individuals createDifferentIndividuals(OntGraphModelImpl model, Stream<OntIndividual> individuals) {
        return create(model, OWL.AllDifferent, Individuals.class, OntIndividual.class, individuals);
    }

    public static ObjectProperties createDisjointObjectProperties(OntGraphModelImpl model, Stream<OntObjectProperty> properties) {
        return create(model, OWL.AllDisjointProperties, ObjectProperties.class, OntObjectProperty.class, properties);
    }

    public static DataProperties createDisjointDataProperties(OntGraphModelImpl model, Stream<OntDataProperty> properties) {
        return create(model, OWL.AllDisjointProperties, DataProperties.class, OntDataProperty.class, properties);
    }

    public static <R extends OntDisjoint<?>, E extends OntObject> R create(OntGraphModelImpl model,
                                                                           Resource type,
                                                                           Class<R> resultType,
                                                                           Class<E> memberType,
                                                                           Stream<E> members) {
        OntJenaException.notNull(members, "Null " + OntEnhNodeFactories.viewAsString(memberType) + " members stream.");
        Resource res = model.createResource()
                .addProperty(RDF.type, type)
                .addProperty(OWL.members, model.createList(members
                        .peek(x -> OntJenaException.notNull(x,
                                "OntDisjoint: Null " + OntEnhNodeFactories.viewAsString(memberType) + " is specified"))
                        .iterator()));
        return model.getNodeAs(res.asNode(), resultType);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, getResourceType());
    }

    protected Property getPredicate() {
        return OWL.members;
    }

    protected abstract Class<O> getComponentType();

    protected abstract Resource getResourceType();

    @Override
    public Stream<O> members() {
        return Iterators.asStream(listMembers());
    }

    public ExtendedIterator<O> listMembers() {
        return getList().listMembers();
    }

    @Override
    public OntListImpl<O> getList() {
        return getModel().asOntList(getRequiredObject(getPredicate(), RDFList.class),
                this, getPredicate(), getComponentType());
    }

    @Override
    public ExtendedIterator<OntStatement> listSpec() {
        return Iterators.concat(super.listSpec(), getList().listContent());
    }

    public static class ClassesImpl extends OntDisjointImpl<OntClass> implements Classes {
        public ClassesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return Classes.class;
        }

        @Override
        protected Class<OntClass> getComponentType() {
            return OntClass.class;
        }

        @Override
        protected Resource getResourceType() {
            return OWL.AllDisjointClasses;
        }
    }

    public static class IndividualsImpl extends OntDisjointImpl<OntIndividual> implements Individuals {
        public IndividualsImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public ExtendedIterator<OntIndividual> listMembers() {
            return Iterators.flatMap(lists(), OntListImpl::listMembers);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), Iterators.flatMap(lists(), OntListImpl::listContent));
        }

        public ExtendedIterator<Property> listPredicates() {
            return Iterators.of(getPredicate(), getAlternativePredicate());
        }

        public ExtendedIterator<OntListImpl<OntIndividual>> lists() {
            return listPredicates()
                    .mapWith(this::findList)
                    .filterKeep(Optional::isPresent)
                    .mapWith(Optional::get);
        }

        @Override
        public OntListImpl<OntIndividual> getList() {
            Optional<OntListImpl<OntIndividual>> p = findList(getPredicate());
            Optional<OntListImpl<OntIndividual>> a = findList(getAlternativePredicate());
            if (p.isPresent() && a.isPresent()) {
                if (p.get().size() > a.get().size()) return p.get();
                if (p.get().size() < a.get().size()) return a.get();
            }
            if (p.isPresent()) {
                return p.get();
            }
            if (a.isPresent()) {
                return a.get();
            }
            throw new OntJenaException.IllegalState("Neither owl:members or owl:distinctMembers could be found");
        }

        public Optional<OntListImpl<OntIndividual>> findList(Property predicate) {
            if (!hasProperty(predicate)) return Optional.empty();
            return Optional.of(getModel().asOntList(getRequiredObject(predicate, RDFList.class),
                    this, predicate, getComponentType()));
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return Individuals.class;
        }

        protected Property getAlternativePredicate() {
            return OWL.distinctMembers;
        }

        @Override
        protected Class<OntIndividual> getComponentType() {
            return OntIndividual.class;
        }

        @Override
        protected Resource getResourceType() {
            return OWL.AllDifferent;
        }
    }

    public abstract static class PropertiesImpl<P extends OntRealProperty>
            extends OntDisjointImpl<P> implements Properties<P> {

        public PropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        protected Resource getResourceType() {
            return OWL.AllDisjointProperties;
        }
    }

    public static class ObjectPropertiesImpl extends PropertiesImpl<OntObjectProperty> implements ObjectProperties {
        public ObjectPropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return ObjectProperties.class;
        }

        @Override
        protected Class<OntObjectProperty> getComponentType() {
            return OntObjectProperty.class;
        }
    }

    public static class DataPropertiesImpl extends PropertiesImpl<OntDataProperty> implements DataProperties {
        public DataPropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return DataProperties.class;
        }

        @Override
        protected Class<OntDataProperty> getComponentType() {
            return OntDataProperty.class;
        }
    }
}
