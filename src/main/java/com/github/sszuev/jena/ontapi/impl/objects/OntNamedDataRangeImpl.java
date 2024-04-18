package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Named entity with {@code rdf:type = rdfs:Datatype}.
 * Supported by OWL2 only.
 * In OWL1.1 and RDFS specifications {@code rdfs:Datatype} represents as {@link com.github.sszuev.jena.ontapi.model.OntClass} object.
 */
@SuppressWarnings("WeakerAccess")
public class OntNamedDataRangeImpl extends OntObjectImpl implements OntDataRange.Named {

    public OntNamedDataRangeImpl(Node n, EnhGraph g) {
        super(checkNamed(n), g);
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
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, RDFS.Datatype);
    }

    @Override
    public Stream<OntDataRange> equivalentClasses() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE)) {
            return Stream.empty();
        }
        return objects(OWL.equivalentClass, OntDataRange.class);
    }

    @Override
    public OntStatement addEquivalentClassStatement(OntDataRange other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        return addStatement(OWL.equivalentClass, other);
    }

    @Override
    public Named removeEquivalentClass(Resource other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_CLASS_EQUIVALENT_FEATURE, "owl:equivalentClass");
        remove(OWL.equivalentClass, other);
        return this;
    }

    @Override
    public RDFDatatype toRDFDatatype() {
        return getModel().getRDFDatatype(getURI());
    }
}
