package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ontology object implementation with declarative type {@link OWL#DatatypeProperty owl:DatatypeProperty}.
 * <p>
 * Created @ssz on 03.11.2016.
 */
public class OntDataPropertyImpl extends OntPropertyImpl implements OntDataProperty {

    public OntDataPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntDataProperty> objectType() {
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
    public Stream<OntDataProperty> disjointProperties() {
        return OntPropertyImpl.disjointProperties(getModel(), OntDataProperty.class, this);
    }

    @Override
    public OntStatement addPropertyDisjointWithStatement(OntDataProperty other) {
        return OntPropertyImpl.addDisjointWith(getModel(), OntDataProperty.class, this, other);
    }

    @Override
    public OntDataProperty removeDisjointProperty(Resource property) {
        OntPropertyImpl.removeDisjointWith(getModel(), this, property);
        return this;
    }

    @Override
    public Stream<OntDataProperty> equivalentProperties() {
        return OntPropertyImpl.equivalentProperties(getModel(), OntDataProperty.class, this);
    }

    @Override
    public OntStatement addEquivalentPropertyStatement(OntDataProperty other) {
        return OntPropertyImpl.addEquivalentProperty(getModel(), OntDataProperty.class, this, other);
    }

    @Override
    public OntDataProperty removeEquivalentProperty(Resource property) {
        OntPropertyImpl.removeEquivalentProperty(getModel(), this, property);
        return this;
    }

    @Override
    public OntNegativeAssertion.WithDataProperty addNegativeAssertion(OntIndividual source, Literal target) {
        return OntNegativePropertyAssertionImpl.create(getModel(), source, this, target);
    }

    @Override
    public boolean isFunctional() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE) &&
                hasType(OWL.FunctionalProperty);
    }

    @Override
    public OntDataPropertyImpl setFunctional(boolean functional) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        changeRDFType(OWL.FunctionalProperty, functional);
        return this;
    }

    @Override
    public OntStatement addFunctionalDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        return addStatement(RDF.type, OWL.FunctionalProperty);
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