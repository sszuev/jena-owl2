# Apache Jena OWL2 Model


## Summary

This is enhanced [Apache Jena](https://github.com/apache/jena) Inference [RDF](https://www.w3.org/TR/rdf11-concepts/) Model with [OWL2](https://www.w3.org/TR/owl2-syntax/) support.
The original code is taken from [ONT-API](https://github.com/owlcs/ont-api).

[com.github.owlcs.ontapi.jena.model.OntModel](https://github.com/owlcs/ont-api/blob/3.x.x/jena-impl/src/main/java/com/github/owlcs/ontapi/jena/model/OntModel.java)
covers OWL2 syntax but not inference and OWL profiles.     
[org.apache.jena.ontology.OntModel](https://github.com/apache/jena/blob/main/jena-core/src/main/java/org/apache/jena/ontology/OntModel.java) 
supports inference, but only follows OWL1 specification.   

The goal of this project is to provide a fully functional OWL2 model.

See also [ONT-API/issue#45](https://github.com/owlcs/ont-api/issues/45)

**Under developing**
