package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.OntModelControls;
import com.github.sszuev.jena.ontapi.impl.HierarchySupport;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Property Expression base impl-class.
 * No functionality, just a collection of factories related to all OWL property-expressions.
 * <p>
 * Created by @ssz on 08.11.2016.
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

    static <X extends OntProperty> Stream<X> subProperties(X property, Class<X> type, boolean direct) {
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

    static <X extends OntProperty> Stream<X> superProperties(X property, Class<X> type, boolean direct) {
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
