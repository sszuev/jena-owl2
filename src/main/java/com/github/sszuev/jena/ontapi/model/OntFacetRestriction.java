package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.XSD;
import org.apache.jena.rdf.model.Literal;

/**
 * Interface encapsulating an Ontology Facet Restriction abstraction.
 * <p>
 * Created by @ssz on 02.11.2016.
 *
 * @see <a href='https://www.w3.org/TR/owl2-quick-reference/#Facets'>3.2 Facets</a>
 * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-facets'>4.3 Constraining Facets</a>
 * @see XSD
 * @see OntModel#createFacetRestriction(Class, Literal)
 */
public interface OntFacetRestriction extends OntObject {
    /**
     * Answers the value of this Facet Restriction.
     *
     * @return {@link Literal}
     */
    Literal getValue();

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-pattern'>4.3.4 pattern</a>
     */
    interface Pattern extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-length'>4.3.1 length</a>
     */
    interface Length extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-minLength'>4.3.2 minLength</a>
     */
    interface MinLength extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-maxLength'>4.3.3 maxLength</a>
     */
    interface MaxLength extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-minInclusive'>4.3.10 minInclusive</a>
     */
    interface MinInclusive extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-maxInclusive'>4.3.7 maxInclusive</a>
     */
    interface MaxInclusive extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-minExclusive'>4.3.9 minExclusive</a>
     */
    interface MinExclusive extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-maxExclusive'>4.3.8 maxExclusive</a>
     */
    interface MaxExclusive extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-totalDigits'>4.3.11 totalDigits</a>
     */
    interface TotalDigits extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/xmlschema11-2/#rf-fractionDigits'>4.3.12 fractionDigits</a>
     */
    interface FractionDigits extends OntFacetRestriction {
    }

    /**
     * @see <a href='https://www.w3.org/TR/rdf-plain-literal/#langRange'>Table 1. The Facet Space of rdf:PlainLiteral</a>
     */
    interface LangRange extends OntFacetRestriction {
    }

}
