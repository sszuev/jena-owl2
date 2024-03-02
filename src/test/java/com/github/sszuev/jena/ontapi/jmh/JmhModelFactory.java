package com.github.sszuev.jena.ontapi.jmh;

import com.github.sszuev.jena.ontapi.OntModelFactory;
import com.github.sszuev.jena.ontapi.OntSpecification;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/*
@Param({
        "OWL2_FULL_MEM",
        "OWL2_EL_MEM",
        "OWL1_LITE_MEM",
        "OWL2_FULL_MEM_RDFS_INF",
        "OWL2_DL_MEM_BUILTIN_INF",
        "LEGACY_OWL_MEM",
        "LEGACY_OWL_DL_MEM_RDFS_INF",
})
 */
public enum JmhModelFactory {
    OWL2_FULL_MEM {
        @Override
        public Model createModel(Graph graph) {
            return OntModelFactory.createModel(graph, OntSpecification.OWL2_FULL_MEM);
        }
    },
    OWL2_EL_MEM {
        @Override
        public Model createModel(Graph graph) {
            return OntModelFactory.createModel(graph, OntSpecification.OWL2_EL_MEM);
        }
    },
    OWL1_LITE_MEM {
        @Override
        public Model createModel(Graph graph) {
            return OntModelFactory.createModel(graph, OntSpecification.OWL1_LITE_MEM);
        }
    },
    OWL2_FULL_MEM_RDFS_INF {
        @Override
        public Model createModel(Graph graph) {
            return OntModelFactory.createModel(graph, OntSpecification.OWL2_FULL_MEM_RDFS_INF);
        }
    },
    OWL2_DL_MEM_BUILTIN_INF {
        @Override
        public Model createModel(Graph graph) {
            return OntModelFactory.createModel(graph, OntSpecification.OWL2_DL_MEM_BUILTIN_INF);
        }
    },

    LEGACY_OWL_MEM {
        @Override
        public Model createModel(Graph graph) {
            return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, ModelFactory.createModelForGraph(graph));
        }
    },
    LEGACY_OWL_DL_MEM_RDFS_INF {
        @Override
        public Model createModel(Graph graph) {
            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, ModelFactory.createModelForGraph(graph));
        }
    },

    ;

    public abstract Model createModel(Graph graph);

}
