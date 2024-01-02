package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntID;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Ontology ID Implementation.
 * Created @ssz on 09.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntIDImpl extends OntObjectImpl implements OntID {

    public OntIDImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public String getVersionIRI() {
        try (Stream<OntStatement> versions = statements(OWL.versionIRI)) {
            Set<String> res = versions
                    .map(Statement::getObject)
                    .filter(RDFNode::isURIResource)
                    .map(it -> it.asResource().getURI())
                    .collect(Collectors.toSet());
            return res.size() == 1 ? res.iterator().next() : null;
        }
    }

    @Override
    public OntIDImpl setVersionIRI(String uri) throws OntJenaException {
        if (uri != null && isAnon()) {
            throw new OntJenaException.IllegalArgument("Attempt to add version IRI (" + uri +
                    ") to anonymous ontology (" + asNode().toString() + ").");
        }
        removeAll(OWL.versionIRI);
        if (uri != null) {
            addProperty(OWL.versionIRI, getModel().createResource(uri));
        }
        return this;
    }

    @Override
    public OntIDImpl addImport(String uri) throws OntJenaException {
        if (OntJenaException.notNull(uri, "Null uri specified.").equals(getURI())) {
            throw new OntJenaException.IllegalArgument("Can't import itself: " + uri);
        }
        addImportResource(getModel().createResource(uri));
        return this;
    }

    @Override
    public OntIDImpl removeImport(String uri) {
        Resource r = getModel().createResource(OntJenaException.notNull(uri, "Null uri specified."));
        removeImportResource(r);
        return this;
    }

    @Override
    public Stream<String> imports() {
        return Iterators.asStream(listImportResources().mapWith(Resource::getURI), getCharacteristics());
    }

    public ExtendedIterator<Resource> listImportResources() {
        return listObjects(OWL.imports)
                .filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource);
    }

    public void addImportResource(Resource uri) {
        addProperty(OWL.imports, uri);
    }

    public void removeImportResource(Resource uri) {
        getModel().remove(this, OWL.imports, uri);
    }

    @Override
    public String toString() {
        String iri = asNode().toString();
        String ver = getVersionIRI();
        if (ver != null) {
            return iri + "(" + ver + ")";
        }
        return iri;
    }
}
