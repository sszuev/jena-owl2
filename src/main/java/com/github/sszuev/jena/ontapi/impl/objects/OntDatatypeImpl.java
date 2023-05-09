package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;

/**
 * Named entity with rdf:type = rdfs:Datatype
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntDatatypeImpl extends OntObjectImpl implements OntDataRange.Named {

    public OntDatatypeImpl(Node n, EnhGraph g) {
        super(checkNamed(n), g);
    }

    @Override
    public Class<Named> getActualClass() {
        return Named.class;
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, RDFS.Datatype);
    }

    @Override
    public RDFDatatype toRDFDatatype() {
        return getModel().getRDFDatatype(getURI());
    }
}
