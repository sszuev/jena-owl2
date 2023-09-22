package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Property Expression base impl-class.
 * No functionality, just a collection of factories related to all OWL property-expressions.
 * <p>
 * Created by @ssz on 08.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntPEImpl extends OntObjectImpl implements OntProperty {

    public OntPEImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Stream<OntClass> declaringClasses(OntRealProperty property, boolean direct) {
        Set<OntClass> domains = property.domains()
                .flatMap(clazz -> Stream.concat(Stream.of(clazz), clazz.subClasses(false)))
                .filter(OntCEImpl::isNotBuiltin)
                .collect(Collectors.toSet());
        if (domains.isEmpty()) {
            Stream<OntClass> res = property.getModel().ontObjects(OntClass.class).filter(OntCEImpl::isNotBuiltin);
            if (!direct) {
                return res;
            } else {
                return res.filter(OntClass::isHierarchyRoot);
            }
        }
        return domains.stream().filter(clazz -> clazz.hasDeclaredProperty(property, direct));
    }

    static <X extends OntProperty> Stream<X> subProperties(X property, Class<X> type, boolean direct) {
        return treeNodes(property,
                x -> actualAdjacentSubProperties(x, type, false),
                x -> explicitSubProperties(x, type),
                direct);
    }

    static <X extends OntProperty> Stream<X> superProperties(X property, Class<X> type, boolean direct) {
        return treeNodes(property,
                x -> actualAdjacentSubProperties(x, type, true),
                x -> explicitSuperProperties(x, type),
                direct);
    }

    static <X extends OntProperty> Stream<X> actualAdjacentSubProperties(X property, Class<X> type, boolean inverse) {
        Set<X> equivalents = equivalentsBySubPropertyOf(property, type).collect(Collectors.toSet());
        equivalents.add(property);
        return equivalents.stream()
                .flatMap(x -> inverse ? explicitSuperProperties(x, type) : explicitSubProperties(x, type))
                .filter(x -> !equivalents.contains(x))
                .flatMap(x -> Stream.concat(Stream.of(x), equivalentsBySubPropertyOf(x, type)))
                .distinct();
    }

    static <X extends OntProperty> Stream<X> explicitSubProperties(X property, Class<X> type) {
        return subjects(RDFS.subPropertyOf, property, type);
    }

    static <X extends OntProperty> Stream<X> explicitSuperProperties(X property, Class<X> type) {
        return property.objects(RDFS.subPropertyOf, type);
    }

    static <X extends OntProperty> Stream<X> equivalentsBySubPropertyOf(X property, Class<X> type) {
        return explicitSubProperties(property, type).filter(x -> x.getModel().contains(property, RDFS.subPropertyOf, x));
    }

    @Override
    public Property asProperty() {
        if (!isURIResource()) throw new OntJenaException.IllegalState();
        return as(Property.class);
    }

}
