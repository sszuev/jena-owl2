package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Optional;

/**
 * Implementation of Facet Restrictions
 * <p>
 * Created by @ssz on 16.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntFacetRestrictionImpl extends OntObjectImpl implements OntFacetRestriction {

    public OntFacetRestrictionImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    private static Property predicate(Class<?> view) {
        if (Length.class.equals(view)) return XSD.length;
        if (MinLength.class.equals(view)) return XSD.minLength;
        if (MaxLength.class.equals(view)) return XSD.maxLength;
        if (MinInclusive.class.equals(view)) return XSD.minInclusive;
        if (MaxInclusive.class.equals(view)) return XSD.maxInclusive;
        if (MinExclusive.class.equals(view)) return XSD.minExclusive;
        if (MaxExclusive.class.equals(view)) return XSD.maxExclusive;
        if (TotalDigits.class.equals(view)) return XSD.totalDigits;
        if (FractionDigits.class.equals(view)) return XSD.fractionDigits;
        if (Pattern.class.equals(view)) return XSD.pattern;
        if (LangRange.class.equals(view)) return RDF.langRange;
        throw new OntJenaException.IllegalArgument("Unsupported facet restriction " + view);
    }

    public static <T extends OntFacetRestriction> T create(OntGraphModelImpl model, Class<T> view, Literal literal) {
        Resource res = model.createResource();
        res.addProperty(predicate(view), literal);
        return model.getNodeAs(res.asNode(), view);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return Optional.of(getModel().createStatement(this, predicate(objectType()), getValue()));//.asRootStatement();
    }

    @Override
    public Literal getValue() {
        return getRequiredObject(predicate(objectType()), Literal.class);
    }

    public static class LengthImpl extends OntFacetRestrictionImpl implements Length {
        public LengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<Length> objectType() {
            return Length.class;
        }
    }

    public static class MinLengthImpl extends OntFacetRestrictionImpl implements MinLength {
        public MinLengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinLength> objectType() {
            return MinLength.class;
        }
    }

    public static class MaxLengthImpl extends OntFacetRestrictionImpl implements MaxLength {
        public MaxLengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxLength> objectType() {
            return MaxLength.class;
        }
    }

    public static class MinInclusiveImpl extends OntFacetRestrictionImpl implements MinInclusive {
        public MinInclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinInclusive> objectType() {
            return MinInclusive.class;
        }
    }

    public static class MaxInclusiveImpl extends OntFacetRestrictionImpl implements MaxInclusive {
        public MaxInclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxInclusive> objectType() {
            return MaxInclusive.class;
        }
    }

    public static class MinExclusiveImpl extends OntFacetRestrictionImpl implements MinExclusive {
        public MinExclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinExclusive> objectType() {
            return MinExclusive.class;
        }
    }

    public static class MaxExclusiveImpl extends OntFacetRestrictionImpl implements MaxExclusive {
        public MaxExclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxExclusive> objectType() {
            return MaxExclusive.class;
        }
    }

    public static class PatternImpl extends OntFacetRestrictionImpl implements Pattern {
        public PatternImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<Pattern> objectType() {
            return Pattern.class;
        }
    }

    public static class TotalDigitsImpl extends OntFacetRestrictionImpl implements TotalDigits {
        public TotalDigitsImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<TotalDigits> objectType() {
            return TotalDigits.class;
        }
    }

    public static class FractionDigitsImpl extends OntFacetRestrictionImpl implements FractionDigits {
        public FractionDigitsImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<FractionDigits> objectType() {
            return FractionDigits.class;
        }
    }

    public static class LangRangeImpl extends OntFacetRestrictionImpl implements LangRange {
        public LangRangeImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<LangRange> objectType() {
            return LangRange.class;
        }
    }
}
