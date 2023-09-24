package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;

/**
 * Named entity with {@code rdf:type = rdfs:Datatype}.
 * Supported by OWL2 only.
 * In OWL1.1 and RDFS specifications {@code rdfs:Datatype} represents as {@link com.github.sszuev.jena.ontapi.model.OntClass} object.
 * <p>
 * Created @ssz on 03.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntNamedDataRangeImpl extends OntObjectImpl implements OntDataRange.Named {

    public OntNamedDataRangeImpl(Node n, EnhGraph g) {
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
