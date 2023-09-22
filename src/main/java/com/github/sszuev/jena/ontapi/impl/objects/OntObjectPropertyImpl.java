package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntList;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the Object Property Expression abstraction.
 * Named instances should have a {@link OWL#ObjectProperty owl:ObjectProperty} type declarations.
 * Anonymous instances should have {@link OWL#inverseOf owl:inverseOf} predicate.
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntObjectPropertyImpl extends OntPropertyImpl implements OntObjectProperty {

    public OntObjectPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Stream<OntObjectProperty> superProperties(boolean direct) {
        return superProperties(this, OntObjectProperty.class, direct);
    }

    @Override
    public Stream<OntObjectProperty> subProperties(boolean direct) {
        return subProperties(this, OntObjectProperty.class, direct);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return OntPropertyImpl.declaringClasses(this, direct);
    }

    @Override
    public OntNegativeAssertion.WithObjectProperty addNegativeAssertion(OntIndividual source, OntIndividual target) {
        return OntNegativePropertyAssertionImpl.create(getModel(), source, this, target);
    }

    @Override
    public OntList<OntObjectProperty> createPropertyChain(Collection<OntObjectProperty> properties) {
        return getModel().createOntList(this, OWL.propertyChainAxiom, OntObjectProperty.class, properties.iterator());
    }

    @Override
    public Stream<OntList<OntObjectProperty>> propertyChains() {
        return OntListImpl.stream(getModel(), this, OWL.propertyChainAxiom, OntObjectProperty.class);
    }

    @Override
    public OntObjectPropertyImpl removePropertyChain(Resource rdfList) throws OntJenaException.IllegalArgument {
        getModel().deleteOntList(this, OWL.propertyChainAxiom, findPropertyChain(rdfList).orElse(null));
        return this;
    }

    @Override
    protected OntObjectPropertyImpl changeRDFType(Resource type, boolean add) {
        super.changeRDFType(type, add);
        return this;
    }

    @Override
    public OntObjectPropertyImpl setFunctional(boolean functional) {
        return changeRDFType(OWL.FunctionalProperty, functional);
    }

    @Override
    public OntObjectPropertyImpl setInverseFunctional(boolean inverseFunctional) {
        return changeRDFType(OWL.InverseFunctionalProperty, inverseFunctional);
    }

    @Override
    public OntObjectPropertyImpl setSymmetric(boolean symmetric) {
        return changeRDFType(OWL.SymmetricProperty, symmetric);
    }

    @Override
    public OntObjectPropertyImpl setAsymmetric(boolean asymmetric) {
        return changeRDFType(OWL.AsymmetricProperty, asymmetric);
    }

    @Override
    public OntObjectPropertyImpl setTransitive(boolean transitive) {
        return changeRDFType(OWL.TransitiveProperty, transitive);
    }

    @Override
    public OntObjectPropertyImpl setReflexive(boolean reflexive) {
        return changeRDFType(OWL.ReflexiveProperty, reflexive);
    }

    @Override
    public OntObjectPropertyImpl setIrreflexive(boolean irreflexive) {
        return changeRDFType(OWL.IrreflexiveProperty, irreflexive);
    }

    public static class NamedPropertyImpl extends OntObjectPropertyImpl implements Named {

        public NamedPropertyImpl(Node n, EnhGraph g) {
            super(n, g);
        }

        @Override
        public Inverse createInverse() {
            OntGraphModelImpl m = getModel();
            List<Node> nodes = m.localStatements(null, OWL.inverseOf, this)
                    .map(OntStatement::getSubject)
                    .filter(RDFNode::isAnon)
                    .map(FrontsNode::asNode)
                    .distinct()
                    .collect(Collectors.toList());
            if (nodes.size() > 1) {
                throw new OntJenaException.IllegalState("More than one inverse-of object properties found: [" +
                        nodes + " owl:inverseOf " + this + "]");
            }
            Node n = nodes.isEmpty() ?
                    m.createResource().addProperty(OWL.inverseOf, NamedPropertyImpl.this).asNode() :
                    nodes.get(0);
            return m.getNodeAs(n, Inverse.class);
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
        public Property inModel(Model m) {
            return getModel() == m ? this : m.createProperty(getURI());
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getOptionalRootStatement(this, OWL.ObjectProperty);
        }

        @Override
        public int getOrdinal() {
            return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
        }
    }

    public static class InversePropertyImpl extends OntObjectPropertyImpl implements Inverse {

        public InversePropertyImpl(Node n, EnhGraph g) {
            super(n, g);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.of(getModel().createStatement(this, OWL.inverseOf, getDirect()).asRootStatement());
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return Inverse.class;
        }

        @Override
        public Named getDirect() {
            OntGraphModelImpl m = getModel();
            List<Resource> res = Iterators.distinct(listObjects(OWL.inverseOf, Resource.class)
                    .filterKeep(RDFNode::isURIResource)).toList();
            if (res.size() != 1)
                throw new OntJenaException.IllegalState("Expected one and only one owl:inverseOf statement, but found: [" +
                        this + " owl:inverseOf " + res + "]");
            return m.getNodeAs(res.get(0).asNode(), Named.class);
        }

        @Override
        public Property asProperty() {
            return getDirect().asProperty();
        }
    }

}

