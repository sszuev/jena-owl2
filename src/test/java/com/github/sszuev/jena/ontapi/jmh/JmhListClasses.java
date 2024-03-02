package com.github.sszuev.jena.ontapi.jmh;

import com.github.sszuev.jena.ontapi.model.OntClass;
import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class JmhListClasses {

    @Param({
            "OWL2_FULL_MEM",
            "OWL2_EL_MEM",
            "OWL1_LITE_MEM",
            "OWL2_FULL_MEM_RDFS_INF",
            "OWL2_DL_MEM_BUILTIN_INF",
            "LEGACY_OWL_MEM",
            "LEGACY_OWL_DL_MEM_RDFS_INF",
    })
    public JmhModelFactory factory;
    public Model model;

    @Setup(Level.Invocation)
    public void setup() {
        model = factory.createModel(JmhGraphs.PIZZA);
    }

    @Benchmark
    @Group("LIST_ALL_CLASSES")
    public void jmhListAllClasses(Blackhole blackhole) {
        List<? extends Resource> classes;
        if (model instanceof OntModel) {
            classes = ((OntModel) model).ontObjects(OntClass.class).collect(Collectors.toList());
        } else {
            classes = ((org.apache.jena.ontology.OntModel) model).listClasses().toList();
        }
        blackhole.consume(classes);
    }

    @Benchmark
    @Group("LIST_NAMED_CLASSES")
    public void jmhListNamedClasses(Blackhole blackhole) {
        List<? extends Resource> classes;
        if (model instanceof OntModel) {
            classes = ((OntModel) model).classes().collect(Collectors.toList());
        } else {
            classes = ((org.apache.jena.ontology.OntModel) model).listClasses().filterKeep(RDFNode::isURIResource).toList();
        }
        blackhole.consume(classes);
    }
}
