package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
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
public class OntDRImpl extends OntObjectImpl implements OntDataRange {

    public OntDRImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    private static Resource create(OntGraphModelImpl model) {
        return model.createResource().addProperty(RDF.type, RDFS.Datatype);
    }

    public static OneOf createOneOf(OntGraphModelImpl model, Stream<Literal> values) {
        OntJenaException.notNull(values, "Null values stream.");
        Resource res = create(model)
                .addProperty(OWL.oneOf, model.createList(values
                        .peek(f -> OntJenaException.notNull(f, "OntDR: null literal.")).iterator()));
        return model.getNodeAs(res.asNode(), OneOf.class);
    }

    public static Restriction createRestriction(OntGraphModelImpl model, Named dataType, Stream<OntFacetRestriction> values) {
        OntJenaException.notNull(dataType, "Null data-type.");
        OntJenaException.notNull(values, "Null values stream.");
        Resource res = create(model)
                .addProperty(OWL.onDatatype, dataType)
                .addProperty(OWL.withRestrictions, model.createList(values
                        .peek(f -> OntJenaException.notNull(f, "OntDR: null faced restriction."))
                        .iterator()));
        return model.getNodeAs(res.asNode(), Restriction.class);
    }

    public static ComplementOf createComplementOf(OntGraphModelImpl model, OntDataRange other) {
        OntJenaException.notNull(other, "Null data range.");
        Resource res = create(model).addProperty(OWL.datatypeComplementOf, other);
        return model.getNodeAs(res.asNode(), ComplementOf.class);
    }

    public static UnionOf createUnionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        Resource res = create(model)
                .addProperty(OWL.unionOf, model.createList(values
                        .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                        .iterator()));
        return model.getNodeAs(res.asNode(), UnionOf.class);
    }

    public static IntersectionOf createIntersectionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        Resource res = create(model).addProperty(OWL.intersectionOf, model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                .iterator()));
        return model.getNodeAs(res.asNode(), IntersectionOf.class);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, RDFS.Datatype);
    }

    public static class ComplementOfImpl extends OntDRImpl implements ComplementOf {
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
        public Class<? extends OntObject> getActualClass() {
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
        public Class<? extends OntObject> getActualClass() {
            return OneOf.class;
        }
    }

    public static class RestrictionImpl extends CombinationImpl<OntFacetRestriction> implements Restriction {
        public RestrictionImpl(Node n, EnhGraph m) {
            super(n, m, OWL.withRestrictions, OntFacetRestriction.class);
        }

        @Override
        public Class<Restriction> getActualClass() {
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
        public Class<? extends OntObject> getActualClass() {
            return UnionOf.class;
        }
    }

    public static class IntersectionOfImpl extends CombinationImpl<OntDataRange> implements IntersectionOf {

        public IntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL.intersectionOf, OntDataRange.class);
        }

        @Override
        public Class<? extends OntObject> getActualClass() {
            return IntersectionOf.class;
        }
    }

    /**
     * An abstract super-class for {@link OneOf}, {@link Restriction}, {@link UnionOf}, {@link IntersectionOf}.
     *
     * @param <N> {@link RDFNode}
     */
    protected static abstract class CombinationImpl<N extends RDFNode> extends OntDRImpl implements Combination<N> {
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
