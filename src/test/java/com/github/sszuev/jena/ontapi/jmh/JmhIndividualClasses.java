package com.github.sszuev.jena.ontapi.jmh;

import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
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
public class JmhIndividualClasses {

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
        model = factory.createModel(JmhGraphs.WINE);
    }

    @Benchmark
    @Group("LIST_INDIVIDUAL_CLASSES_DIRECT")
    public void jmhClassListDeclaredClassesDirect(Blackhole blackhole) {
        List<? extends Resource> classes;
        if (model instanceof OntModel) {
            classes = ((OntModel) model)
                    .getIndividual("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Light")
                    .classes(true).collect(Collectors.toList());
        } else {
            classes = ((org.apache.jena.ontology.OntModel) model)
                    .getIndividual("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Light")
                    .listOntClasses(true).toList();
        }
        blackhole.consume(classes);
    }

    @Benchmark
    @Group("LIST_INDIVIDUAL_CLASSES_INDIRECT")
    public void jmhClassListDeclaredClassesIndirect(Blackhole blackhole) {
        List<? extends Resource> classes;
        if (model instanceof OntModel) {
            classes = ((OntModel) model)
                    .getIndividual("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling")
                    .classes(false).collect(Collectors.toList());
        } else {
            classes = ((org.apache.jena.ontology.OntModel) model)
                    .getIndividual("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling")
                    .listOntClasses(false).toList();
        }
        blackhole.consume(classes);
    }
}
