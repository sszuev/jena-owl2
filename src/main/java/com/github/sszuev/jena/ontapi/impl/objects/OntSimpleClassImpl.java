package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code owl:Class} Implementation.
 * Instance of this class as a class with unknown nature is only available in a spec with corresponding permissions
 * ({@link com.github.sszuev.jena.ontapi.OntModelConfig}).
 * Specialized classes have their own implementations (see {@link OntClassImpl}).
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntSimpleClassImpl extends OntObjectImpl implements OntClass.Named {

    public OntSimpleClassImpl(Node n, EnhGraph eg) {
        super(n, eg);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, OWL.Class);
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }

    @Override
    public Class<? extends OntClass> getActualClass() {
        return OntClass.class;
    }

    @Override
    public OntIndividual.Anonymous createIndividual() {
        return OntClassImpl.createAnonymousIndividual(getModel(), this);
    }

    @Override
    public OntIndividual.Named createIndividual(String uri) {
        return OntClassImpl.createNamedIndividual(getModel(), this, uri);
    }

    @Override
    public Stream<OntClass> superClasses(boolean direct) {
        return OntClassImpl.superClasses(this, direct);
    }

    @Override
    public Stream<OntClass> subClasses(boolean direct) {
        return OntClassImpl.subClasses(this, direct);
    }

    @Override
    public Stream<OntIndividual> individuals(boolean direct) {
        return OntClassImpl.individuals(this, direct);
    }

    @Override
    public boolean hasDeclaredProperty(OntRealProperty property, boolean direct) {
        return OntClassImpl.testDomain(this, property, direct);
    }

    @Override
    public Stream<OntProperty> declaredProperties(boolean direct) {
        return OntClassImpl.declaredProperties(this, direct);
    }

    @Override
    public boolean isHierarchyRoot() {
        return OntClassImpl.isHierarchyRoot(this);
    }

    @Override
    public OntList<OntRealProperty> createHasKey(Collection<OntObjectProperty> ope, Collection<OntDataProperty> dpe) {
        return OntClassImpl.createHasKey(getModel(), this, Stream.of(ope, dpe).flatMap(Collection::stream));
    }

    @Override
    public OntStatement addHasKeyStatement(OntRealProperty... properties) {
        return OntClassImpl.createHasKey(getModel(), this, Arrays.stream(properties)).getMainStatement();
    }

    @Override
    public Stream<OntList<OntRealProperty>> hasKeys() {
        return OntClassImpl.listHasKeys(getModel(), this);
    }

    @Override
    public OntSimpleClassImpl removeHasKey(Resource list) throws OntJenaException.IllegalArgument {
        OntClassImpl.removeHasKey(getModel(), this, list);
        return this;
    }

    @Override
    public boolean isDisjoint(Resource candidate) {
        return OntClassImpl.isDisjoint(this, candidate);
    }

    @Override
    public OntList<OntClass> createDisjointUnion(Collection<OntClass> classes) {
        return getModel().createOntList(this, OWL.disjointUnionOf, OntClass.class,
                Objects.requireNonNull(classes).stream().distinct().iterator());
    }

    @Override
    public Stream<OntList<OntClass>> disjointUnions() {
        return OntListImpl.stream(getModel(), this, OWL.disjointUnionOf, OntClass.class);
    }

    @Override
    public OntSimpleClassImpl removeDisjointUnion(Resource rdfList) throws OntJenaException.IllegalArgument {
        getModel().deleteOntList(this, OWL.disjointUnionOf, findDisjointUnion(rdfList).orElse(null));
        return this;
    }

    /**
     * Primary (named) class ({@code <uri> a owl:Class}).
     * This is also {@link com.github.sszuev.jena.ontapi.model.OntEntity}.
     * Note:
     * In jena OWL1, class expressions, such as {@link OntClass.ComplementOf},
     * can also be named,
     * but in this case specialized implementation is used (i.e., {@link OntClassImpl.ComplementOfImpl}).
     */
    public static class NamedImpl extends OntSimpleClassImpl implements OntClass.Named {

        public NamedImpl(Node n, EnhGraph eg) {
            super(n, eg);
        }

        @Override
        public OntClass.Named asNamed() {
            return this;
        }

        @Override
        public Class<Named> getActualClass() {
            return Named.class;
        }
    }
}
