package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
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
 * ({@link OntModelControls}).
 * Specialized classes have their own implementations ({@link NamedImpl} or {@link OntClassImpl}).
 */
@SuppressWarnings("WeakerAccess")
public class OntSimpleClassImpl extends OntObjectImpl implements OntClass {

    public OntSimpleClassImpl(Node n, EnhGraph eg) {
        super(n, eg);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, OWL.Class);
    }

    @Override
    public Class<? extends OntClass> objectType() {
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
    public boolean hasDeclaredProperty(OntProperty property, boolean direct) {
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
    public OntList<OntRelationalProperty> createHasKey(Collection<OntObjectProperty> ope, Collection<OntDataProperty> dpe) {
        return OntClassImpl.createHasKey(getModel(), this, Stream.of(ope, dpe).flatMap(Collection::stream));
    }

    @Override
    public OntStatement addHasKeyStatement(OntRelationalProperty... properties) {
        return OntClassImpl.createHasKey(getModel(), this, Arrays.stream(properties)).getMainStatement();
    }

    @Override
    public Stream<OntList<OntRelationalProperty>> hasKeys() {
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
    public Stream<OntClass> disjointClasses() {
        return OntClassImpl.disjointClasses(getModel(), this);
    }

    @Override
    public Stream<OntClass> equivalentClasses() {
        return OntClassImpl.equivalentClasses(getModel(), this);
    }

    @Override
    public OntClass addDisjointClass(OntClass other) {
        OntClassImpl.addDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public OntStatement addDisjointWithStatement(OntClass other) {
        return OntClassImpl.addDisjointWithStatement(getModel(), this, other);
    }

    @Override
    public OntClass removeDisjointClass(Resource other) {
        OntClassImpl.removeDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public OntStatement addEquivalentClassStatement(OntClass other) {
        return OntClassImpl.addEquivalentClass(getModel(), this, other);
    }

    @Override
    public OntClass removeEquivalentClass(Resource other) {
        OntClassImpl.removeEquivalentClass(getModel(), this, other);
        return this;
    }

    /**
     * A version of Named class for RL specification.
     * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_RL_2">OWL2 RL</a>
     */
    public static class RLNamedImpl extends NamedImpl {

        public RLNamedImpl(Node n, EnhGraph eg) {
            super(n, eg);
        }

        @Override
        public OntClass asSubClass() {
            return OWL.Thing.equals(this) ? null : this;
        }

        @Override
        public OntClass asSuperClass() {
            return OWL.Thing.equals(this) ? null : this;
        }

        @Override
        public OntClass asEquivalentClass() {
            return OWL.Thing.equals(this) ? null : this;
        }

        @Override
        public OntClass asDisjointClass() {
            return OWL.Thing.equals(this) ? null : this;
        }
    }

    /**
     * Primary (named) class ({@code <uri> a owl:Class}).
     * This is also {@link com.github.sszuev.jena.ontapi.model.OntEntity}.
     * Note:
     * In jena OWL1, class expressions, such as {@link OntClass.ComplementOf}
     * can also be named.
     */
    public static class NamedImpl extends OntSimpleClassImpl implements OntClass.Named {

        public NamedImpl(Node n, EnhGraph eg) {
            super(checkNamed(n), eg);
        }

        @Override
        public OntClass.Named asNamed() {
            return this;
        }

        @Override
        public Class<Named> objectType() {
            return Named.class;
        }

        @Override
        public boolean isBuiltIn() {
            return getModel().isBuiltIn(this);
        }

        @Override
        public Stream<OntList<OntClass>> disjointUnions() {
            if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)) {
                return Stream.empty();
            }
            return OntListImpl.stream(getModel(), this, OWL.disjointUnionOf, OntClass.class);
        }

        @Override
        public OntList<OntClass> createDisjointUnion(Collection<OntClass> classes) {
            OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE, "owl:disjointUnionOf");
            return getModel().createOntList(this, OWL.disjointUnionOf, OntClass.class,
                    Objects.requireNonNull(classes).stream().distinct().iterator());
        }

        @Override
        public OntClass.Named removeDisjointUnion(Resource rdfList) throws OntJenaException.IllegalArgument {
            OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE, "owl:disjointUnionOf");
            getModel().deleteOntList(this, OWL.disjointUnionOf, findDisjointUnion(rdfList).orElse(null));
            return this;
        }

        @Override
        public OntClass.Named removeDisjointClass(Resource other) {
            super.removeDisjointClass(other);
            return this;
        }

        @Override
        public OntClass.Named removeEquivalentClass(Resource other) {
            super.removeEquivalentClass(other);
            return this;
        }

        @Override
        public OntClass.Named addDisjointClass(OntClass other) {
            super.addDisjointClass(other);
            return this;
        }
    }
}
