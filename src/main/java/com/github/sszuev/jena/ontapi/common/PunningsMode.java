package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.model.OntAnnotationProperty;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntDataRange;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A standard personality mode to manage punnings.
 *
 * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
 */
public enum PunningsMode {
    /**
     * For OWL1 DL.
     * OWL1 DL required a strict separation between the names of, e.g., classes and individuals.
     */
    DL1,
    /**
     * For OWL2 DL.
     * Personality with four kinds of restriction on a {@code rdf:type} intersection (i.e. "illegal punnings"):
     * <ul>
     * <li>Named owl:Class &lt;-&gt; Named rdfs:Datatype</li>
     * <li>Named owl:ObjectProperty &lt;-&gt; owl:DatatypeProperty</li>
     * <li>Named owl:ObjectProperty &lt;-&gt; owl:AnnotationProperty</li>
     * <li>owl:AnnotationProperty &lt;-&gt; owl:DatatypeProperty</li>
     * </ul>
     * each of the pairs above can't exist in the form of OWL-Entity in the same model at the same time.
     * From specification: "OWL 2 DL imposes certain restrictions:
     * it requires that a name cannot be used for both a class and a datatype and
     * that a name can only be used for one kind of property."
     */
    DL2,
    /**
     * Forbidden intersections of rdf-declarations:
     * <ul>
     * <li>Class &lt;-&gt; Datatype</li>
     * <li>ObjectProperty &lt;-&gt; DataProperty</li>
     * </ul>
     */
    DL_WEAK,
    /**
     * Allow any entity type intersections.
     */
    FULL,
    ;

    private static final Set<Resource> OWL2_OBJECT_PROPERTIES = Set.of(
            OWL.ObjectProperty,
            OWL.InverseFunctionalProperty,
            OWL.ReflexiveProperty,
            OWL.IrreflexiveProperty,
            OWL.SymmetricProperty,
            OWL.AsymmetricProperty,
            OWL.TransitiveProperty
    );
    private static final Set<Resource> OWL1_OBJECT_PROPERTIES = Set.of(
            OWL.ObjectProperty,
            OWL.InverseFunctionalProperty,
            OWL.SymmetricProperty,
            OWL.TransitiveProperty
    );
    private static final Set<Resource> DATATYPE_PROPERTIES = Set.of(OWL.DatatypeProperty);
    private static final Set<Resource> ANNOTATION_PROPERTIES = Set.of(OWL.AnnotationProperty);
    private static final Set<Resource> RDF_PROPERTIES = Set.of(RDF.Property);
    private static final Set<Resource> OWL2_CLASSES = Set.of(OWL.Class);
    private static final Set<Resource> OWL1_CLASSES = Set.of(OWL.Class, OWL.Restriction, RDFS.Class, RDFS.Datatype);
    private static final Set<Resource> OWL2_DATATYPES = Set.of(RDFS.Datatype);
    private static final Set<Resource> OWL2_INDIVIDUALS = Set.of(OWL.NamedIndividual);

    static Map<Class<? extends OntObject>, Set<Node>> toMap(PunningsMode mode) {
        Map<Class<? extends OntObject>, Set<Set<Resource>>> res;
        if (PunningsMode.DL1 == mode) {
            res = OWL1_DL();
        } else if (PunningsMode.DL2 == mode) {
            res = OWL2_DL();
        } else if (PunningsMode.DL_WEAK == mode) {
            res = OWL2_DL_WEAK();
        } else if (PunningsMode.FULL == mode) {
            res = FULL();
        } else {
            throw new IllegalStateException();
        }
        return res.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        it -> it.getValue().stream()
                                .flatMap(Collection::stream)
                                .map(FrontsNode::asNode)
                                .collect(Collectors.toUnmodifiableSet())
                )
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL1_DL() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(
                        OWL1_OBJECT_PROPERTIES, DATATYPE_PROPERTIES, OWL1_CLASSES
                ),
                OntObjectProperty.Named.class, Set.of(
                        DATATYPE_PROPERTIES, ANNOTATION_PROPERTIES, OWL1_CLASSES
                ),
                OntDataProperty.class, Set.of(
                        OWL1_OBJECT_PROPERTIES, ANNOTATION_PROPERTIES, OWL1_CLASSES
                ),
                OntDataRange.Named.class, Set.of(),
                OntClass.Named.class, Set.of(
                        ANNOTATION_PROPERTIES, OWL1_OBJECT_PROPERTIES, DATATYPE_PROPERTIES
                ),
                OntIndividual.Named.class, Set.of(
                        ANNOTATION_PROPERTIES, OWL1_OBJECT_PROPERTIES, DATATYPE_PROPERTIES, OWL1_CLASSES, RDF_PROPERTIES
                )
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL2_DL() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(
                        OWL2_OBJECT_PROPERTIES, DATATYPE_PROPERTIES
                ),
                OntObjectProperty.Named.class, Set.of(
                        DATATYPE_PROPERTIES, ANNOTATION_PROPERTIES
                ),
                OntDataProperty.class, Set.of(
                        OWL2_OBJECT_PROPERTIES, ANNOTATION_PROPERTIES
                ),
                OntDataRange.Named.class, Set.of(
                        OWL2_CLASSES
                ),
                OntClass.Named.class, Set.of(
                        OWL2_DATATYPES
                ),
                OntIndividual.Named.class, Set.of()
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL2_DL_WEAK() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(),
                OntObjectProperty.Named.class, Set.of(DATATYPE_PROPERTIES),
                OntDataProperty.class, Set.of(OWL2_OBJECT_PROPERTIES),
                OntDataRange.Named.class, Set.of(OWL2_CLASSES),
                OntClass.Named.class, Set.of(OWL2_DATATYPES),
                OntIndividual.Named.class, Set.of()
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> FULL() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(),
                OntObjectProperty.Named.class, Set.of(),
                OntDataProperty.class, Set.of(),
                OntDataRange.Named.class, Set.of(),
                OntClass.Named.class, Set.of(),
                OntIndividual.Named.class, Set.of()
        );
    }
}
