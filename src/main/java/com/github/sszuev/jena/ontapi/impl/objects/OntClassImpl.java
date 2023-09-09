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
 * owl:Class Implementation
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntClassImpl extends OntObjectImpl implements OntClass.Named {

    public OntClassImpl(Node n, EnhGraph eg) {
        super(checkNamed(n), eg);
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
    public Class<Named> getActualClass() {
        return Named.class;
    }

    @Override
    public OntIndividual.Anonymous createIndividual() {
        return OntCEImpl.createAnonymousIndividual(getModel(), this);
    }

    @Override
    public OntIndividual.Named createIndividual(String uri) {
        return OntCEImpl.createNamedIndividual(getModel(), this, uri);
    }

    @Override
    public Stream<OntClass> superClasses(boolean direct) {
        return OntCEImpl.superClasses(this, direct);
    }

    @Override
    public Stream<OntClass> subClasses(boolean direct) {
        return OntCEImpl.subClasses(this, direct);
    }

    @Override
    public Stream<OntIndividual> individuals(boolean direct) {
        return OntCEImpl.individuals(this, direct);
    }

    @Override
    public boolean hasDeclaredProperty(OntRealProperty property, boolean direct) {
        return OntCEImpl.testDomain(this, property, direct);
    }

    @Override
    public Stream<OntProperty> declaredProperties(boolean direct) {
        return OntCEImpl.declaredProperties(this, direct);
    }

    @Override
    public boolean isHierarchyRoot() {
        return OntCEImpl.isHierarchyRoot(this);
    }

    @Override
    public OntList<OntRealProperty> createHasKey(Collection<OntObjectProperty> ope, Collection<OntDataProperty> dpe) {
        return OntCEImpl.createHasKey(getModel(), this, Stream.of(ope, dpe).flatMap(Collection::stream));
    }

    @Override
    public OntStatement addHasKeyStatement(OntRealProperty... properties) {
        return OntCEImpl.createHasKey(getModel(), this, Arrays.stream(properties)).getMainStatement();
    }

    @Override
    public Stream<OntList<OntRealProperty>> hasKeys() {
        return OntCEImpl.listHasKeys(getModel(), this);
    }

    @Override
    public OntClassImpl removeHasKey(Resource list) throws OntJenaException.IllegalArgument {
        OntCEImpl.removeHasKey(getModel(), this, list);
        return this;
    }

    @Override
    public boolean isDisjoint(Resource candidate) {
        return OntCEImpl.isDisjoint(this, candidate);
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
    public OntClassImpl removeDisjointUnion(Resource rdfList) throws OntJenaException.IllegalArgument {
        getModel().deleteOntList(this, OWL.disjointUnionOf, findDisjointUnion(rdfList).orElse(null));
        return this;
    }
}
