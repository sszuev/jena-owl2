package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDisjoint;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation for anonymous {@code owl:AllDisjointProperties}, {@code owl:AllDisjointClasses}, {@code owl:AllDifferent} sections.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntDisjointImpl<O extends OntObject> extends OntObjectImpl implements OntDisjoint<O> {

    public OntDisjointImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Classes createDisjointClasses(OntGraphModelImpl model, Stream<OntClass> classes) {
        return create(model, OWL.AllDisjointClasses, Classes.class, OntClass.class, classes, OWL.members);
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
        Property membersPredicate = OntGraphModelImpl.configValue(model, OntModelControls.USE_OWL1_DISTINCT_MEMBERS_PREDICATE_FEATURE) ?
                OWL.distinctMembers :
                OWL.members;
        return create(model, OWL.AllDifferent, Individuals.class, OntIndividual.class, individuals, membersPredicate);
    }

    public static ObjectProperties createDisjointObjectProperties(OntGraphModelImpl model, Stream<OntObjectProperty> properties) {
        return create(model, OWL.AllDisjointProperties, ObjectProperties.class, OntObjectProperty.class, properties, OWL.members);
    }

    public static DataProperties createDisjointDataProperties(OntGraphModelImpl model, Stream<OntDataProperty> properties) {
        return create(model, OWL.AllDisjointProperties, DataProperties.class, OntDataProperty.class, properties, OWL.members);
    }

    public static <R extends OntDisjoint<?>, E extends OntObject> R create(OntGraphModelImpl model,
                                                                           Resource type,
                                                                           Class<R> resultType,
                                                                           Class<E> memberType,
                                                                           Stream<E> members,
                                                                           Property membersPredicate) {
        OntJenaException.notNull(members, "Null " + OntEnhNodeFactories.viewAsString(memberType) + " members stream.");
        RDFList items = model.createList(members
                .peek(x -> OntJenaException.notNull(x,
                        "OntDisjoint: Null " + OntEnhNodeFactories.viewAsString(memberType) + " is specified"))
                .iterator());
        Resource res = model.createResource()
                .addProperty(RDF.type, type)
                .addProperty(membersPredicate, items);
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
        public Class<? extends OntObject> objectType() {
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
        private final boolean useMembers;
        private final boolean useDistinctMembers;

        public IndividualsImpl(Node n, EnhGraph m, boolean useMembers, boolean useDistinctMembers) {
            super(n, m);
            this.useMembers = useMembers;
            this.useDistinctMembers = useDistinctMembers;
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
            if (useDistinctMembers) {
                return Iterators.of(getAlternativePredicate());
            }
            if (useMembers) {
                return Iterators.of(getPredicate());
            } else {
                return Iterators.of(getPredicate(), getAlternativePredicate());
            }
        }

        public ExtendedIterator<OntListImpl<OntIndividual>> lists() {
            return listPredicates()
                    .mapWith(this::findList)
                    .filterKeep(Optional::isPresent)
                    .mapWith(Optional::get);
        }

        @Override
        public OntListImpl<OntIndividual> getList() {
            if (useDistinctMembers) {
                return findList(getAlternativePredicate())
                        .orElseThrow(() -> new OntJenaException.IllegalState("Can't find owl:distinctMembers"));
            }
            if (useMembers) {
                return findList(getPredicate())
                        .orElseThrow(() -> new OntJenaException.IllegalState("Can't find owl:members"));
            }
            Optional<OntListImpl<OntIndividual>> p = findList(getPredicate());
            Optional<OntListImpl<OntIndividual>> a = findList(getAlternativePredicate());
            if (p.isPresent() && a.isPresent()) {
                return p.get();
            }
            if (p.isPresent()) {
                return p.get();
            }
            if (a.isPresent()) {
                return a.get();
            }
            throw new OntJenaException.IllegalState("Can't find owl:members or owl:distinctMembers");
        }

        public Optional<OntListImpl<OntIndividual>> findList(Property predicate) {
            if (!hasProperty(predicate)) return Optional.empty();
            return Optional.of(getModel().asOntList(getRequiredObject(predicate, RDFList.class),
                    this, predicate, getComponentType()));
        }

        @Override
        public Class<? extends OntObject> objectType() {
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

    public abstract static class PropertiesImpl<P extends OntRelationalProperty>
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
        public Class<? extends OntObject> objectType() {
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
        public Class<? extends OntObject> objectType() {
            return DataProperties.class;
        }

        @Override
        protected Class<OntDataProperty> getComponentType() {
            return OntDataProperty.class;
        }
    }
}
