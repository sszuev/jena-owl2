package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.common.Factories;
import com.github.sszuev.jena.ontapi.common.ObjectFactory;
import com.github.sszuev.jena.ontapi.common.OntFilter;
import com.github.sszuev.jena.ontapi.common.OntFinder;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntNegativeAssertion;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Optional;

/**
 * Implementation of the Negative Property Assertion.
 * <p>
 * Created by @ssz on 15.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntNPAImpl<P extends OntRealProperty, T extends RDFNode>
        extends OntObjectImpl implements OntNegativeAssertion<P, T> {

    private static final OntFinder NPA_FINDER = new OntFinder.ByType(OWL.NegativePropertyAssertion);
    private static final OntFilter NPA_FILTER = OntFilter.BLANK
            .and(new OntFilter.HasPredicate(OWL.sourceIndividual))
            .and(new OntFilter.HasPredicate(OWL.assertionProperty));

    public static final ObjectFactory OWL2_OBJECT_NPA_FACTORY = Factories.createCommon(ObjectAssertionImpl.class,
            NPA_FINDER, NPA_FILTER, new OntFilter.HasPredicate(OWL.targetIndividual));
    public static final ObjectFactory OWL2_DATA_NPA_FACTORY = Factories.createCommon(DataAssertionImpl.class,
            NPA_FINDER, NPA_FILTER, new OntFilter.HasPredicate(OWL.targetValue));
    public static final ObjectFactory OWL2_NPA_FACTORY = Factories.createFrom(NPA_FINDER,
            WithObjectProperty.class, WithDataProperty.class);

    public OntNPAImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static WithDataProperty create(OntGraphModelImpl model,
                                          OntIndividual source,
                                          OntDataProperty property,
                                          Literal target) {
        Resource res = create(model, source).addProperty(OWL.assertionProperty, property)
                .addProperty(OWL.targetValue, target);
        return model.getNodeAs(res.asNode(), WithDataProperty.class);
    }

    public static WithObjectProperty create(OntGraphModelImpl model,
                                            OntIndividual source,
                                            OntObjectProperty property,
                                            OntIndividual target) {
        Resource res = create(model, source)
                .addProperty(OWL.assertionProperty, property)
                .addProperty(OWL.targetIndividual, target);
        return model.getNodeAs(res.asNode(), WithObjectProperty.class);
    }

    private static Resource create(OntModel model, OntIndividual source) {
        Resource res = model.createResource();
        res.addProperty(RDF.type, OWL.NegativePropertyAssertion);
        res.addProperty(OWL.sourceIndividual, source);
        return res;
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, OWL.NegativePropertyAssertion);
    }

    @Override
    public ExtendedIterator<OntStatement> listSpec() {
        return Iterators.concat(super.listSpec(), listRequired(OWL.sourceIndividual, OWL.assertionProperty, targetPredicate()));
    }

    abstract Class<P> propertyClass();

    abstract Property targetPredicate();

    @Override
    public OntIndividual getSource() {
        return getRequiredObject(OWL.sourceIndividual, OntIndividual.class);
    }

    @Override
    public P getProperty() {
        return getRequiredObject(OWL.assertionProperty, propertyClass());
    }

    public static class ObjectAssertionImpl extends OntNPAImpl<OntObjectProperty, OntIndividual> implements WithObjectProperty {
        public ObjectAssertionImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        Class<OntObjectProperty> propertyClass() {
            return OntObjectProperty.class;
        }

        @Override
        Property targetPredicate() {
            return OWL.targetIndividual;
        }

        @Override
        public Class<WithObjectProperty> getActualClass() {
            return WithObjectProperty.class;
        }


        @Override
        public OntIndividual getTarget() {
            return getRequiredObject(targetPredicate(), OntIndividual.class);
        }

    }

    public static class DataAssertionImpl extends OntNPAImpl<OntDataProperty, Literal> implements WithDataProperty {
        public DataAssertionImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        Class<OntDataProperty> propertyClass() {
            return OntDataProperty.class;
        }

        @Override
        Property targetPredicate() {
            return OWL.targetValue;
        }

        @Override
        public Class<WithDataProperty> getActualClass() {
            return WithDataProperty.class;
        }


        @Override
        public Literal getTarget() {
            return getRequiredObject(targetPredicate(), Literal.class);
        }
    }
}
