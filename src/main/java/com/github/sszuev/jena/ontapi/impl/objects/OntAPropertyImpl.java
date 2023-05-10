package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
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
 * Implementation of Annotation Property abstraction (an URI-{@link Resource} with {@link OWL#AnnotationProperty owl:AnnotationProperty} type).
 * <p>
 * Created @ssz on 03.11.2016.
 */
public class OntAPropertyImpl extends OntPEImpl implements OntAnnotationProperty {

    public OntAPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntAnnotationProperty> getActualClass() {
        return OntAnnotationProperty.class;
    }

    @Override
    public Stream<OntAnnotationProperty> superProperties(boolean direct) {
        if (direct) {
            return adjacentChildren(this, x -> actualAdjacentSubProperties(x, OntAnnotationProperty.class, true));
        }
        return treeAsStream(this, x -> explicitSuperProperties(x, OntAnnotationProperty.class));
    }

    @Override
    public Stream<OntAnnotationProperty> subProperties(boolean direct) {
        if (direct) {
            return adjacentChildren(this, x -> actualAdjacentSubProperties(x, OntAnnotationProperty.class, false));
        }
        return treeAsStream(this, x -> explicitSubProperties(x, OntAnnotationProperty.class));
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
