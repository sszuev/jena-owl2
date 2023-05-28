package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Simple direct {@link OntProperty} implementation to use with RDFS Specifications.
 */
public class OntRPropertyImpl extends OntPEImpl implements OntProperty, OntEntity, Property {

    public OntRPropertyImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public Class<? extends OntObject> getActualClass() {
        return OntProperty.class;
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, RDF.Property);
    }

    @Override
    public Stream<OntProperty> superProperties(boolean direct) {
        return treeNodes(this,
                x -> actualAdjacentSubProperties(x, OntProperty.class, true),
                x -> actualAdjacentSubProperties(x, OntProperty.class, true),
                direct);
    }

    @Override
    public Stream<OntProperty> subProperties(boolean direct) {
        return treeNodes(this,
                x -> actualAdjacentSubProperties(x, OntProperty.class, false),
                x -> actualAdjacentSubProperties(x, OntProperty.class, false),
                direct);
    }

    @Override
    public Stream<? extends Resource> domains() {
        return objects(RDFS.domain, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<? extends Resource> ranges() {
        return objects(RDFS.range, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public boolean isProperty() {
        return true;
    }

    @Override
    public Property inModel(Model m) {
        return getModel() == m ? this : m.createProperty(getURI());
    }

    @Override
    public int getOrdinal() {
        return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }
}
