package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelConfig;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation for Data Range Expressions.
 * <p>
 * Created by @ssz on 16.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntDataRangeImpl extends OntObjectImpl implements OntDataRange {

    public OntDataRangeImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    private static Resource create(OntGraphModelImpl model) {
        Resource type = configValue(model, OntModelConfig.USE_OWL_V1_VOCABULARY) ?
                OWL.DataRange :
                RDFS.Datatype;
        return model.createResource().addProperty(RDF.type, type);
    }

    public static OneOf createOneOf(OntGraphModelImpl model, Stream<Literal> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null literal.")).iterator());
        Resource res = create(model).addProperty(OWL.oneOf, items);
        return model.getNodeAs(res.asNode(), OneOf.class);
    }

    public static Restriction createRestriction(OntGraphModelImpl model, Named dataType, Stream<OntFacetRestriction> values) {
        OntJenaException.notNull(dataType, "Null data-type.");
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null faced restriction."))
                .iterator());
        Resource res = create(model)
                .addProperty(OWL.onDatatype, dataType)
                .addProperty(OWL.withRestrictions, items);
        return model.getNodeAs(res.asNode(), Restriction.class);
    }

    public static ComplementOf createComplementOf(OntGraphModelImpl model, OntDataRange other) {
        OntJenaException.notNull(other, "Null data range.");
        Resource res = create(model).addProperty(OWL.datatypeComplementOf, other);
        return model.getNodeAs(res.asNode(), ComplementOf.class);
    }

    public static UnionOf createUnionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                .iterator());
        Resource res = create(model)
                .addProperty(OWL.unionOf, items);
        return model.getNodeAs(res.asNode(), UnionOf.class);
    }

    public static IntersectionOf createIntersectionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                .iterator());
        Resource res = create(model).addProperty(OWL.intersectionOf, items);
        return model.getNodeAs(res.asNode(), IntersectionOf.class);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        Resource type = configValue(getModel(), OntModelConfig.USE_OWL_V1_VOCABULARY) ? OWL.DataRange : RDFS.Datatype;
        return getRequiredRootStatement(this, type);
    }

    public static class ComplementOfImpl extends OntDataRangeImpl implements ComplementOf {
        public ComplementOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntDataRange getValue() {
            return getRequiredObject(OWL.datatypeComplementOf, OntDataRange.class);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(OWL.datatypeComplementOf));
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return ComplementOf.class;
        }

        @Override
        public ComplementOf setValue(OntDataRange value) {
            Objects.requireNonNull(value);
            removeAll(OWL.datatypeComplementOf).addProperty(OWL.datatypeComplementOf, value);
            return this;
        }
    }

    public static class OneOfImpl extends CombinationImpl<Literal> implements OneOf {
        public OneOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.oneOf, Literal.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return OneOf.class;
        }
    }

    public static class RestrictionImpl extends CombinationImpl<OntFacetRestriction> implements Restriction {
        public RestrictionImpl(Node n, EnhGraph m) {
            super(n, m, OWL.withRestrictions, OntFacetRestriction.class);
        }

        @Override
        public Class<Restriction> objectType() {
            return Restriction.class;
        }

        @Override
        public Named getValue() {
            return getRequiredObject(OWL.onDatatype, Named.class);
        }

        @Override
        public RestrictionImpl setValue(Named value) {
            Objects.requireNonNull(value);
            removeAll(OWL.onDatatype).addProperty(OWL.onDatatype, value);
            return this;
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(listDeclaration(), listRequired(OWL.onDatatype), withRestrictionsSpec());
        }

        public ExtendedIterator<OntStatement> withRestrictionsSpec() {
            return Iterators.flatMap(getList().listContent(), s -> {
                if (!s.getObject().canAs(OntFacetRestriction.class)) {
                    return Iterators.of(s);
                }
                return Iterators.of(s, s.getObject().as(OntFacetRestriction.class).getMainStatement());
            });
        }

    }

    public static class UnionOfImpl extends CombinationImpl<OntDataRange> implements UnionOf {
        public UnionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.unionOf, OntDataRange.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return UnionOf.class;
        }
    }

    public static class IntersectionOfImpl extends CombinationImpl<OntDataRange> implements IntersectionOf {

        public IntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.intersectionOf, OntDataRange.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return IntersectionOf.class;
        }
    }

    /**
     * An abstract superclass for {@link OneOf}, {@link Restriction}, {@link UnionOf}, {@link IntersectionOf}.
     *
     * @param <N> {@link RDFNode}
     */
    protected static abstract class CombinationImpl<N extends RDFNode> extends OntDataRangeImpl implements Combination<N> {
        protected final Property predicate;
        protected final Class<N> type;

        protected CombinationImpl(Node n, EnhGraph m, Property predicate, Class<N> type) {
            super(n, m);
            this.predicate = OntJenaException.notNull(predicate, "Null predicate.");
            this.type = OntJenaException.notNull(type, "Null view.");
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(listDeclaration(), getList().listContent());
        }

        public ExtendedIterator<OntStatement> listDeclaration() {
            return super.listSpec();
        }

        @Override
        public OntListImpl<N> getList() {
            return getModel().asOntList(getRequiredObject(predicate, RDFList.class), this, predicate, true, null, type);
        }
    }

}
