package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
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
 * Implementation of Annotation Property abstraction
 * (a URI-{@link Resource} with {@link OWL#AnnotationProperty owl:AnnotationProperty} type).
 */
public class OntAnnotationPropertyImpl extends OntPropertyImpl implements OntAnnotationProperty {

    public OntAnnotationPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntAnnotationProperty> objectType() {
        return OntAnnotationProperty.class;
    }

    @Override
    public Stream<OntAnnotationProperty> superProperties(boolean direct) {
        return superProperties(this, OntAnnotationProperty.class, direct);
    }

    @Override
    public Stream<OntAnnotationProperty> subProperties(boolean direct) {
        return subProperties(this, OntAnnotationProperty.class, direct);
    }

    @Override
    public OntStatement addDomainStatement(Resource domain) {
        return addStatement(RDFS.domain, checkNamed(domain));
    }

    @Override
    public OntStatement addRangeStatement(Resource range) {
        return addStatement(RDFS.range, checkNamed(range));
    }

    @Override
    public Stream<Resource> domains() {
        return objects(RDFS.domain, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<Resource> ranges() {
        return objects(RDFS.range, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return OntPropertyImpl.declaringClasses(this, direct);
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
        return getOptionalRootStatement(this, OWL.AnnotationProperty);
    }

    @Override
    public int getOrdinal() {
        return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
    }
}
