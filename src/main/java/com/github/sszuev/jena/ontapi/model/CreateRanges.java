package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Literal;

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to generate {@link OntDataRange Data Range Expression}s.
 * Created by @szz on 14.05.2019.
 */
interface CreateRanges {

    /**
     * Creates an Enumeration of Literals.
     * RDF (turtle) syntax:
     * <pre>{@code
     * _:x rdf:type rdfs:Datatype .
     * _:x owl:oneOf ( v1 ... vn ) .
     * }</pre>
     *
     * @param values {@code Collection} of {@link Literal literal}s, without {@code null}s
     * @return {@link OntDataRange.OneOf}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Enumeration_of_Literals'>7.4 Enumeration of Literals</a>
     */
    OntDataRange.OneOf createDataOneOf(Collection<Literal> values);

    /**
     * Creates a Datatype Restriction.
     * RDF (turtle) syntax:
     * <pre>{@code
     * _:x rdf:type rdfs:Datatype .
     * _:x owl:onDatatype DN .
     * _:x owl:withRestrictions ( _:x1 ... _:xn ) .
     * _:xj fj vj .
     * }</pre>
     *
     * @param other  {@link OntDataRange.Named}, not {@code null}
     * @param values {@code Collection} of {@link OntFacetRestriction facet restriction}s, without {@code null}s
     * @return {@link OntDataRange.Restriction}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Datatype_Restrictions'>7.5 Datatype Restrictions</a>
     * @see OntFacetRestriction
     * @see OntModel#createFacetRestriction(Class, Literal)
     */
    OntDataRange.Restriction createDataRestriction(OntDataRange.Named other, Collection<OntFacetRestriction> values);

    /**
     * Creates a Complement of Data Ranges.
     * RDF (turtle) syntax:
     * <pre>{@code
     * _:x rdf:type rdfs:Datatype .
     * _:x owl:datatypeComplementOf D .
     * }</pre>
     *
     * @param other {@link OntDataRange}, not {@code null}
     * @return {@link OntDataRange.ComplementOf}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Complement_of_Data_Ranges'>7.3 Complement of Data Ranges</a>
     */
    OntDataRange.ComplementOf createDataComplementOf(OntDataRange other);

    /**
     * Creates a Union of Data Ranges.
     * RDF (turtle) syntax:
     * <pre>{@code
     * _:x rdf:type rdfs:Datatype .
     * _:x owl:unionOf ( D1 ... Dn ) .
     * }</pre>
     *
     * @param values {@code Collection} of {@link OntDataRange data range}s, without {@code null}s
     * @return {@link OntDataRange.UnionOf}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Union_of_Data_Ranges'>7.2 Union of Data Ranges</a>
     */
    OntDataRange.UnionOf createDataUnionOf(Collection<OntDataRange> values);

    /**
     * Creates an Intersection of Data Ranges.
     * RDF (turtle) syntax:
     * <pre>{@code
     * _:x rdf:type rdfs:Datatype .
     * _:x owl:intersectionOf ( D1 ... Dn ) .
     * }</pre>
     *
     * @param values {@code Collection} of {@link OntDataRange data range}s, without {@code null}s
     * @return {@link OntDataRange.IntersectionOf}
     * @see <a href='https://www.w3.org/TR/owl-syntax/#Intersection_of_Data_Ranges'>7.1 Intersection of Data Ranges</a>
     */
    OntDataRange.IntersectionOf createDataIntersectionOf(Collection<OntDataRange> values);

    /**
     * Creates an Enumeration of Literals.
     *
     * @param values Array of {@link Literal literal}s, without {@code null}-elements
     * @return {@link OntDataRange.OneOf}
     * @see #createDataOneOf(Collection)
     */
    default OntDataRange.OneOf createDataOneOf(Literal... values) {
        return createDataOneOf(Arrays.asList(values));
    }

    /**
     * Creates a Datatype Restriction.
     *
     * @param other  {@link OntDataRange.Named Named Data Range}, not {@code null}
     * @param values Array of {@link OntFacetRestriction facet restriction}s, without {@code null}s
     * @return {@link OntDataRange.Restriction}
     * @see #createDataRestriction(OntDataRange.Named, Collection)
     */
    default OntDataRange.Restriction createDataRestriction(OntDataRange.Named other, OntFacetRestriction... values) {
        return createDataRestriction(other, Arrays.asList(values));
    }

    /**
     * Creates a Union of Data Ranges.
     *
     * @param values {@code Collection} of {@link OntDataRange data range}s, without {@code null}-elements
     * @return {@link OntDataRange.UnionOf}
     * @see #createDataUnionOf(Collection)
     */
    default OntDataRange.UnionOf createDataUnionOf(OntDataRange... values) {
        return createDataUnionOf(Arrays.asList(values));
    }

    /**
     * Creates an Intersection of Data Ranges.
     *
     * @param values Array of {@link OntDataRange data range}s, without {@code null}-elements
     * @return {@link OntDataRange.IntersectionOf}
     * @see #createDataIntersectionOf(Collection)
     */
    default OntDataRange.IntersectionOf createDataIntersectionOf(OntDataRange... values) {
        return createDataIntersectionOf(Arrays.asList(values));
    }
}
