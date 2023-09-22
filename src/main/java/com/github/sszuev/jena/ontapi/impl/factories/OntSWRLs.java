package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFilter;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.common.OntPersonality;
import com.github.sszuev.jena.ontapi.impl.objects.OntSWRLImpl;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

final class OntSWRLs {
    public static final EnhNodeFilter VARIABLE_FILTER = EnhNodeFilter.URI.and(new EnhNodeFilter.HasType(SWRL.Variable));
    public static final EnhNodeFilter BUILTIN_FILTER = (n, g) -> {
        if (!n.isURI())
            return false;
        OntPersonality p = OntEnhGraph.asPersonalityModel(g).getOntPersonality();
        if (p.getBuiltins().get(OntSWRL.Builtin.class).contains(n)) {
            return true;
        }
        return Iterators.findFirst(g.asGraph().find(n, RDF.Nodes.type, SWRL.Builtin.asNode())).isPresent();
    };

    public static EnhNodeFactory makeAtomFactory(Class<? extends OntSWRLImpl.AtomImpl<?>> view, Resource type) {
        return OntEnhNodeFactories.createCommon(new EnhNodeProducer.Default(view, null),
                new EnhNodeFinder.ByType(type), EnhNodeFilter.BLANK.and(new EnhNodeFilter.HasType(type)));
    }

    public static class SWRLImplFactory extends BaseEnhNodeFactoryImpl {
        private static final Node IMP = SWRL.Imp.asNode();
        private static final Node BODY = SWRL.body.asNode();
        private static final Node HEAD = SWRL.head.asNode();
        private static final Node LIST = SWRL.AtomList.asNode();

        private static final Implementation LIST_FACTORY = RDFListImpl.factory;

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return eg.asGraph().find(Node.ANY, RDF.Nodes.type, IMP)
                    .filterKeep(t -> hasAtomList(HEAD, t.getSubject(), eg) && hasAtomList(BODY, t.getSubject(), eg))
                    .mapWith(t -> createInstance(t.getSubject(), eg));
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return eg.asGraph().contains(node, RDF.Nodes.type, IMP)
                    && hasAtomList(HEAD, node, eg)
                    && hasAtomList(BODY, node, eg);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntSWRLImpl.ImpImpl(node, eg);
        }

        private boolean hasAtomList(Node p, Node node, EnhGraph eg) {
            return Iterators.anyMatch(eg.asGraph().find(node, p, Node.ANY), t -> isAtomList(t.getObject(), eg));
        }

        private boolean isAtomList(Node n, EnhGraph eg) {
            if (RDF.Nodes.nil.equals(n)) return true;
            return eg.asGraph().contains(n, RDF.Nodes.type, LIST) && LIST_FACTORY.canWrap(n, eg);
        }
    }
}
