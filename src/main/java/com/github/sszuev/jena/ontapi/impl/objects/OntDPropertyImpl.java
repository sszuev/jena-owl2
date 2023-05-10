package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ontology object implementation with declarative type {@link OWL#DatatypeProperty owl:DatatypeProperty}.
 * <p>
 * Created @ssz on 03.11.2016.
 */
public class OntDPropertyImpl extends OntPEImpl implements OntDataProperty {

    public OntDPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntDataProperty> getActualClass() {
        return OntDataProperty.class;
    }

    @Override
    public Stream<OntDataProperty> superProperties(boolean direct) {
        return superProperties(this, OntDataProperty.class, direct);
    }

    @Override
    public Stream<OntDataProperty> subProperties(boolean direct) {
        return subProperties(this, OntDataProperty.class, direct);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return declaringClasses(this, direct);
    }

    @Override
    public OntNegativeAssertion.WithDataProperty addNegativeAssertion(OntIndividual source, Literal target) {
        return OntNPAImpl.create(getModel(), source, this, target);
    }

    @Override
    public OntDPropertyImpl setFunctional(boolean functional) {
        changeRDFType(OWL.FunctionalProperty, functional);
        return this;
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }

    @Override
    public Property inModel(Model m) {
        return getModel() == m ? this : m.createProperty(getURI());
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, OWL.DatatypeProperty);
    }

    @Override
    public int getOrdinal() {
        return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
    }
}
