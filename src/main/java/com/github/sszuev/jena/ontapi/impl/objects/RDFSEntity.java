package com.github.sszuev.jena.ontapi.impl.objects;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntNamedProperty;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

/**
 * see also {@link org.apache.jena.ontology.impl.RDFSProfile}
 */
public enum RDFSEntity {
    CLASS {
        @Override
        public EnhNodeFactory createFactory() {
            return createFactory(OntClassImpl.class, OntClass.Named.class, RDFS.Class, createFilter());
        }

        private EnhNodeFilter createFilter() {
            return (n, g) -> { // from RDFSProfile
                return g.asGraph().contains(n, RDF.type.asNode(), RDFS.Class.asNode()) ||
                        g.asGraph().contains(n, RDF.type.asNode(), RDFS.Datatype.asNode()) ||
                        // These are common cases that we should support
                        n.equals(RDFS.Resource.asNode()) ||
                        g.asGraph().contains(Node.ANY, RDFS.domain.asNode(), n) ||
                        g.asGraph().contains(Node.ANY, RDFS.range.asNode(), n)
                        ;
            };
        }
    },
    PROPERTY {
        @Override
        public EnhNodeFactory createFactory() {
            return createFactory(OntRPropertyImpl.class, OntNamedProperty.class, RDF.Property, createFilter());
        }

        private EnhNodeFilter createFilter() {
            return (n, g) -> n.isURI() && g.asGraph().contains(n, RDF.type.asNode(), RDF.Property.asNode());
        }
    },
    ;

    public static final EnhNodeFinder ENTITY_FINDER = OntEnhNodeFactories.createFinder(RDF.Property, RDFS.Class);

    static EnhNodeFactory createFactory(
            Class<? extends OntObjectImpl> impl,
            Class<? extends OntEntity> classType,
            Resource resourceType,
            EnhNodeFilter filter) {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(resourceType);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType);
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }

    public abstract EnhNodeFactory createFactory();
}
