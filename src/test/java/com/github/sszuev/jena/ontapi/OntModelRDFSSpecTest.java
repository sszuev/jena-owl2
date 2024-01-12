package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntDataProperty;
import com.github.sszuev.jena.ontapi.model.OntEntity;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObjectProperty;
import com.github.sszuev.jena.ontapi.model.OntProperty;
import com.github.sszuev.jena.ontapi.model.OntRealProperty;
import com.github.sszuev.jena.ontapi.utils.ModelUtils;
import com.github.sszuev.jena.ontapi.vocabulary.OWL;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntModelRDFSSpecTest {

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testClasses(TestSpec spec) {
        Model base = ModelFactory.createDefaultModel();
        base.createResource("1", OWL.Class);
        base.createResource("2", RDFS.Datatype);
        base.createResource("3", RDFS.Class);
        base.createResource("4", RDFS.Class);
        base.createResource(null, RDFS.Class);

        OntModel m = OntModelFactory.createModel(base.getGraph(), spec.inst);

        List<OntClass.Named> res1 = m.classes().collect(Collectors.toList());
        Assertions.assertEquals(List.of("3", "4"),
                res1.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );
        List<OntEntity> res2 = m.ontEntities().collect(Collectors.toList());
        Assertions.assertEquals(List.of("3", "4"),
                res2.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );
        List<OntClass> res4 = m.ontObjects(OntClass.Named.class).collect(Collectors.toList());
        Assertions.assertEquals(List.of("3", "4"),
                res4.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntClass> res3 = m.ontObjects(OntClass.class).collect(Collectors.toList());
        Assertions.assertEquals(3, res3.size());
        Assertions.assertEquals(List.of("3", "4"),
                res3.stream().filter(RDFNode::isURIResource).map(Resource::getURI).sorted().collect(Collectors.toList())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testProperties(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource p1 = m.createResource("1", RDF.Property);
        Resource p2 = m.createResource("2", OWL.ObjectProperty);
        Resource p3 = m.createResource("3", OWL.DatatypeProperty);
        Resource p4 = m.createResource("4", OWL.AnnotationProperty);

        Assertions.assertEquals(List.of("1"),
                m.properties().map(Resource::getURI).collect(Collectors.toList())
        );
        Assertions.assertEquals(List.of("1"),
                m.ontObjects(OntProperty.class).map(Resource::getURI).collect(Collectors.toList())
        );

        Assertions.assertEquals(List.of(), m.ontObjects(OntObjectProperty.class).collect(Collectors.toList()));
        Assertions.assertEquals(List.of(), m.ontEntities().collect(Collectors.toList()));

        Assertions.assertTrue(p1.canAs(OntProperty.class));
        Assertions.assertFalse(p2.canAs(OntProperty.class));
        Assertions.assertFalse(p3.canAs(OntProperty.class));
        Assertions.assertFalse(p4.canAs(OntProperty.class));

        Stream.of(OntClass.class, OntRealProperty.class, OntDataProperty.class)
                .forEach(it -> Assertions.assertFalse(p1.canAs(it)));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_TRANS_INF",
    })
    public void testIndividuals(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource c1 = m.createResource("c1", RDFS.Class);
        Resource c2 = m.createResource("c2", RDFS.Class);
        Resource i1 = m.createResource("i1", c1);
        Resource i2 = m.createResource("i2", c2);
        Resource i3 = m.createResource(null, c2);

        List<OntIndividual> res1 = m.individuals().collect(Collectors.toList());
        Assertions.assertEquals(3, res1.size());
        Assertions.assertEquals(List.of("i1", "i2"),
                res1.stream().filter(RDFNode::isURIResource).map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntEntity> res2 = m.ontEntities().collect(Collectors.toList());
        Assertions.assertEquals(List.of("c1", "c2", "i1", "i2"),
                res2.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntEntity> res3 = m.ontObjects(OntIndividual.Named.class).collect(Collectors.toList());
        Assertions.assertEquals(List.of("i1", "i2"),
                res3.stream().map(Resource::getURI).sorted().collect(Collectors.toList())
        );

        List<OntIndividual> res5 = m.ontObjects(OntIndividual.Anonymous.class).collect(Collectors.toList());
        Assertions.assertEquals(1, res5.size());
        Assertions.assertTrue(res5.get(0).isAnon());

        Assertions.assertFalse(c1.canAs(OntIndividual.class));
        Assertions.assertFalse(c2.canAs(OntIndividual.class));
        Assertions.assertTrue(i1.canAs(OntIndividual.class));
        Assertions.assertTrue(i2.canAs(OntIndividual.class));
        Assertions.assertTrue(i3.canAs(OntIndividual.class));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testUnsupportedObjects(TestSpec spec) {
        OntModel m = OntModelFactory.createModel(spec.inst);
        Resource x = m.createResource("x", OWL.DatatypeProperty);
        Assertions.assertThrows(UnsupportedPolymorphismException.class, () -> x.as(OntDataProperty.class));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> m.createDataHasValue(null, null));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "RDFS_MEM",
            "RDFS_MEM_RDFS_INF",
            "RDFS_MEM_TRANS_INF",
    })
    public void testOntClassCastRDFS(TestSpec spec) {
        Model g = ModelFactory.createDefaultModel();
        Resource namedRdfsClass = g.createResource("rdfsClass", RDFS.Class);
        Resource namedRdfsDatatype = g.createResource("rdfsDatatype", RDFS.Datatype);
        Resource namedOwlClass = g.createResource("owlClass", OWL.Class);
        Resource anonRdfsClass = g.createResource(RDFS.Class);
        Resource anonRdfsDatatype = g.createResource(RDFS.Datatype);
        Resource anonOwlClass = g.createResource(OWL.Class);
        Resource anonRdfsDomain = g.createResource();
        Resource anonRdfsRange = g.createResource();
        Resource namedRdfsDomain = g.createResource("rdfsDomain");
        Resource namedRdfsRange = g.createResource("rdfsRange");
        g.createResource("p", RDF.Property).addProperty(RDFS.domain, anonRdfsDomain).addProperty(RDFS.range, namedRdfsRange);
        g.createResource(null, RDF.Property).addProperty(RDFS.domain, namedRdfsDomain).addProperty(RDFS.range, anonRdfsRange);

        OntModel m = OntModelFactory.createModel(g.getGraph(), spec.inst).setNsPrefixes(PrefixMapping.Standard);

        if (spec == TestSpec.RDFS_MEM_RDFS_INF) {
            Assertions.assertTrue(
                    m.ontObjects(OntClass.class).map(RDFNode::asResource).collect(Collectors.toSet())
                            .containsAll(Set.of(anonRdfsClass, namedRdfsClass))
            );
        } else {
            Assertions.assertEquals(
                    List.of(anonRdfsClass, namedRdfsClass),
                    m.ontObjects(OntClass.class).sorted(ModelUtils.RDF_NODE_COMPARATOR).collect(Collectors.toList())
            );
        }
        Stream.of(namedOwlClass, anonOwlClass).forEach(it -> Assertions.assertFalse(it.inModel(m).canAs(OntClass.class)));
        Stream.of(anonRdfsClass, anonRdfsDatatype, anonRdfsDomain, anonRdfsRange)
                .forEach(it ->
                        Assertions.assertTrue(it.inModel(m).canAs(OntClass.class) && !it.inModel(m).canAs(OntClass.Named.class))
                );
        Stream.of(namedRdfsClass, namedRdfsDatatype, namedRdfsDomain, namedRdfsRange)
                .forEach(it -> Assertions.assertTrue(it.inModel(m).canAs(OntClass.Named.class)));
    }

    @Test
    public void testDisabledFeatures() {
        OntModel d = OntModelFactory.createModel();
        d.createOntClass("X")
                .addHasKey(d.createObjectProperty("p"))
                .addDisjointUnion(d.createOntClass("Q"));
        d.createOntClass("Q").addDisjointClass(d.createOntClass("W"));
        d.createOntClass("Q").addEquivalentClass(d.createOntClass("F"));
        d.createResource("X", RDFS.Class);
        d.createResource("Q", RDFS.Class);
        d.createResource("W", RDFS.Class);
        d.createResource("F", RDFS.Class);

        OntModel m = OntModelFactory.createModel(d.getGraph(), OntSpecification.RDFS_MEM);
        OntClass.Named x = m.getOntClass("X");
        OntClass.Named q = m.getOntClass("Q");

        Assertions.assertThrows(OntJenaException.Unsupported.class, x::hasKeys);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeHasKey(m.createList()));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointUnion(m.createOntClass("Q")));
        Assertions.assertThrows(OntJenaException.Unsupported.class, x::disjointUnions);
        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.removeDisjointUnion(m.createList()));

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addDisjointClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, x::disjointClasses);

        Assertions.assertThrows(OntJenaException.Unsupported.class, () -> x.addEquivalentClass(q));
        Assertions.assertThrows(OntJenaException.Unsupported.class, x::equivalentClasses);
    }
}
