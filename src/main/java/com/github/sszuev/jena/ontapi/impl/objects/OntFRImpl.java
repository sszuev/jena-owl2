package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntFacetRestriction;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
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
public abstract class OntFRImpl extends OntObjectImpl implements OntFacetRestriction {
    public static final EnhNodeFactory OWL2_LENGTH_FR_FACTORY = OntEnhNodeFactories.createCommon(LengthImpl.class,
            makeFinder(XSD.length), makeFilter(XSD.length));
    public static final EnhNodeFactory OWL2_MIN_LENGTH_FR_FACTORY = OntEnhNodeFactories.createCommon(MinLengthImpl.class,
            makeFinder(XSD.minLength), makeFilter(XSD.minLength));
    public static final EnhNodeFactory OWL2_MAX_LENGTH_FR_FACTORY = OntEnhNodeFactories.createCommon(MaxLengthImpl.class,
            makeFinder(XSD.maxLength), makeFilter(XSD.maxLength));
    public static final EnhNodeFactory OWL2_MIN_INCLUSIVE_FR_FACTORY = OntEnhNodeFactories.createCommon(MinInclusiveImpl.class,
            makeFinder(XSD.minInclusive), makeFilter(XSD.minInclusive));
    public static final EnhNodeFactory OWL2_MAX_INCLUSIVE_FR_FACTORY = OntEnhNodeFactories.createCommon(MaxInclusiveImpl.class,
            makeFinder(XSD.maxInclusive), makeFilter(XSD.maxInclusive));
    public static final EnhNodeFactory OWL2_MIN_EXCLUSIVE_FR_FACTORY = OntEnhNodeFactories.createCommon(MinExclusiveImpl.class,
            makeFinder(XSD.minExclusive), makeFilter(XSD.minExclusive));
    public static final EnhNodeFactory OWL2_MAX_EXCLUSIVE_FR_FACTORY = OntEnhNodeFactories.createCommon(MaxExclusiveImpl.class,
            makeFinder(XSD.maxExclusive), makeFilter(XSD.maxExclusive));
    public static final EnhNodeFactory OWL2_TOTAL_DIGITS_FR_FACTORY = OntEnhNodeFactories.createCommon(TotalDigitsImpl.class,
            makeFinder(XSD.totalDigits), makeFilter(XSD.totalDigits));
    public static final EnhNodeFactory OWL2_FRACTION_DIGITS_FR_FACTORY = OntEnhNodeFactories.createCommon(FractionDigitsImpl.class,
            makeFinder(XSD.fractionDigits), makeFilter(XSD.fractionDigits));
    public static final EnhNodeFactory OWL2_PATTERN_FR_FACTORY = OntEnhNodeFactories.createCommon(PatternImpl.class,
            makeFinder(XSD.pattern), makeFilter(XSD.pattern));
    public static final EnhNodeFactory OWL2_LANG_RANGE_FR_FACTORY = OntEnhNodeFactories.createCommon(LangRangeImpl.class,
            makeFinder(RDF.langRange), makeFilter(RDF.langRange));

    public static final EnhNodeFactory OWL2_FR_FACTORY = OntEnhNodeFactories.createFrom(EnhNodeFinder.ANY_BLANK_SUBJECT
            , Length.class
            , MinLength.class
            , MaxLength.class
            , MinInclusive.class
            , MaxInclusive.class
            , MinExclusive.class
            , MaxExclusive.class
            , TotalDigits.class
            , FractionDigits.class
            , Pattern.class
            , LangRange.class);

    public OntFRImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    private static EnhNodeFinder makeFinder(Property predicate) {
        return new EnhNodeFinder.ByPredicate(predicate);
    }

    private static EnhNodeFilter makeFilter(Property predicate) {
        return EnhNodeFilter.BLANK.and(
                (n, g) -> Iterators.anyMatch(g.asGraph().find(n, predicate.asNode(), Node.ANY)
                        .mapWith(Triple::getObject), Node::isLiteral)
        );
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
        throw new OntJenaException.Unsupported("Unsupported facet restriction " + view);
    }

    public static <T extends OntFacetRestriction> T create(OntGraphModelImpl model, Class<T> view, Literal literal) {
        Resource res = model.createResource();
        res.addProperty(predicate(view), literal);
        return model.getNodeAs(res.asNode(), view);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return Optional.of(getModel().createStatement(this, predicate(getActualClass()), getValue()));//.asRootStatement();
    }

    @Override
    public Literal getValue() {
        return getRequiredObject(predicate(getActualClass()), Literal.class);
    }

    public static class LengthImpl extends OntFRImpl implements Length {
        public LengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<Length> getActualClass() {
            return Length.class;
        }
    }

    public static class MinLengthImpl extends OntFRImpl implements MinLength {
        public MinLengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinLength> getActualClass() {
            return MinLength.class;
        }
    }

    public static class MaxLengthImpl extends OntFRImpl implements MaxLength {
        public MaxLengthImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxLength> getActualClass() {
            return MaxLength.class;
        }
    }

    public static class MinInclusiveImpl extends OntFRImpl implements MinInclusive {
        public MinInclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinInclusive> getActualClass() {
            return MinInclusive.class;
        }
    }

    public static class MaxInclusiveImpl extends OntFRImpl implements MaxInclusive {
        public MaxInclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxInclusive> getActualClass() {
            return MaxInclusive.class;
        }
    }

    public static class MinExclusiveImpl extends OntFRImpl implements MinExclusive {
        public MinExclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MinExclusive> getActualClass() {
            return MinExclusive.class;
        }
    }

    public static class MaxExclusiveImpl extends OntFRImpl implements MaxExclusive {
        public MaxExclusiveImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<MaxExclusive> getActualClass() {
            return MaxExclusive.class;
        }
    }

    public static class PatternImpl extends OntFRImpl implements Pattern {
        public PatternImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<Pattern> getActualClass() {
            return Pattern.class;
        }
    }

    public static class TotalDigitsImpl extends OntFRImpl implements TotalDigits {
        public TotalDigitsImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<TotalDigits> getActualClass() {
            return TotalDigits.class;
        }
    }

    public static class FractionDigitsImpl extends OntFRImpl implements FractionDigits {
        public FractionDigitsImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<FractionDigits> getActualClass() {
            return FractionDigits.class;
        }
    }

    public static class LangRangeImpl extends OntFRImpl implements LangRange {
        public LangRangeImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<LangRange> getActualClass() {
            return LangRange.class;
        }
    }
}
