package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.impl.HierarchySupport;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRelationalProperty;
import com.github.sszuev.jena.ontapi.model.OntStatement;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Property Expression base impl-class.
 * No functionality, just a collection of factories related to all OWL property-expressions.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntPropertyImpl extends OntObjectImpl implements OntProperty {

    public OntPropertyImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Stream<OntClass> declaringClasses(OntProperty property, boolean direct) {
        Set<OntClass> domains = HierarchySupport.allTreeNodesSetInclusive(
                () -> property.domains()
                        .filter(it -> it.canAs(OntClass.class))
                        .map(it -> it.as(OntClass.class)).filter(it -> !isReservedOrBuiltin(it)),
                clazz -> OntClassImpl.explicitSubClasses(clazz).filter(it -> !isReservedOrBuiltin(it))
        );
        if (domains.isEmpty()) {
            if (!direct) {
                return property.getModel().ontObjects(OntClass.class).filter(it -> !isReservedOrBuiltin(it));
            } else {
                return property.getModel().hierarchyRoots();
            }
        }
        return domains.stream().filter(clazz -> clazz.hasDeclaredProperty(property, direct));
    }

    public static <X extends OntProperty> Stream<X> subProperties(X property, Class<X> type, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(property.getModel(), RDFS.subPropertyOf);
            if (reasonerProperty != null) {
                return explicitSubProperties(property, reasonerProperty, type).filter(x -> !property.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                property,
                it -> explicitSubProperties(it, RDFS.subPropertyOf, type),
                direct,
                OntGraphModelImpl.configValue(property.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static <X extends OntProperty> Stream<X> superProperties(X property, Class<X> type, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(property.getModel(), RDFS.subPropertyOf);
            if (reasonerProperty != null) {
                return explicitSuperProperties(property, reasonerProperty, type).filter(x -> !property.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                property,
                it -> explicitSuperProperties(it, RDFS.subPropertyOf, type),
                direct,
                OntGraphModelImpl.configValue(property.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static <X extends OntRelationalProperty> Stream<X> disjointProperties(OntGraphModelImpl m, Class<X> type, X property) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE)) {
            return Stream.empty();
        }
        return property.objects(OWL.propertyDisjointWith, type);
    }

    public static <X extends OntRelationalProperty> OntStatement addDisjointWith(OntGraphModelImpl m, Class<X> type, X property, X other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE, "owl:propertyDisjointWith");
        return property.addStatement(OWL.propertyDisjointWith, other);
    }

    public static <X extends OntRelationalProperty> void removeDisjointWith(OntGraphModelImpl m, X property, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE, "owl:propertyDisjointWith");
        property.remove(OWL.propertyDisjointWith, other);
    }

    public static <X extends OntRelationalProperty> Stream<X> equivalentProperties(OntGraphModelImpl m, Class<X> type, X property) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE)) {
            return Stream.empty();
        }
        return property.objects(OWL.equivalentProperty, type);
    }

    public static <X extends OntRelationalProperty> OntStatement addEquivalentProperty(OntGraphModelImpl m, Class<X> type, X property, X other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE, "owl:equivalentProperty");
        return property.addStatement(OWL.equivalentProperty, other);
    }

    public static <X extends OntRelationalProperty> void removeEquivalentProperty(OntGraphModelImpl m, X property, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE, "owl:equivalentProperty");
        property.remove(OWL.equivalentProperty, other);
    }

    static <X extends OntProperty> Stream<X> explicitSubProperties(X property, Property predicate, Class<X> type) {
        return subjects(predicate, property, type);
    }

    static <X extends OntProperty> Stream<X> explicitSuperProperties(X property, Property predicate, Class<X> type) {
        return property.objects(predicate, type);
    }

    @Override
    public Property asProperty() {
        if (!isURIResource()) {
            throw new OntJenaException.IllegalState();
        }
        return as(Property.class);
    }

}
