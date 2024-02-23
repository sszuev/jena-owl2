# Apache Jena OWL2 Model


### Summary

This is enhanced [Apache Jena](https://github.com/apache/jena) Inference [RDF](https://www.w3.org/TR/rdf11-concepts/) Model with [OWL2](https://www.w3.org/TR/owl2-syntax/) support.
The original code is taken from [ONT-API](https://github.com/owlcs/ont-api).

### Motivation

[com.github.owlcs.ontapi.jena.model.OntModel](https://github.com/owlcs/ont-api/blob/3.x.x/jena-impl/src/main/java/com/github/owlcs/ontapi/jena/model/OntModel.java)
covers OWL2 syntax but not inference and OWL profiles.     
[org.apache.jena.ontology.OntModel](https://github.com/apache/jena/blob/main/jena-core/src/main/java/org/apache/jena/ontology/OntModel.java) 
supports inference, but only follows OWL1 specification.

This project closes this gap by providing a fully functional ontology RDF model for working with OWL2. 

### Major components

- [OntModel](src/main/java/com/github/sszuev/jena/ontapi/model/OntModel.java) - the main interface to work with OWL2 & OWL1 & RDFS ontologies
- [OntModelFactory](src/main/java/com/github/sszuev/jena/ontapi/OntModelFactory.java) - factory for creating different `OntModel` types
- [GraphRepository](src/main/java/com/github/sszuev/jena/ontapi/GraphRepository.java) - graph store accessor
- [OntSpecification](src/main/java/com/github/sszuev/jena/ontapi/OntSpecification.java) - encapsulates `ReasonerFactory`, `OntConfig`, `OntPersonality`
- [UnionGraph](src/main/java/com/github/sszuev/jena/ontapi/UnionGraph.java) - a Graph implementation with support hierarchy
- [OntModelConfig](src/main/java/com/github/sszuev/jena/ontapi/OntModelControls.java) - model configuration, defines
  what a model can do and what it cannot do
- [OntPersonality](src/main/java/com/github/sszuev/jena/ontapi/common/OntPersonality.java) - a configuration class
  to conduct Jena's polymorphism for `OntObject`s

### Supported specifications

- OWL2 DL & Full: NO_INF, RULES_INF, RDFS_INF, TRANS_INF + BUILTIN_INF (default)
- OWL2 EL: NO_INF, RULES_INF, RDFS_INF, TRANS_INF
- OWL2 QL: NO_INF, RULES_INF, RDFS_INF, TRANS_INF
- OWL1 DL & Full: NO_INF, RULES_INF, RDFS_INF, TRANS_INF, MICRO_RULES, MINI_RULES
- OWL1 Lite: NO_INF, RULES_INF, RDFS_INF, TRANS_INF
- RDFS: NO_INF, RDFS_INF, TRANS_INF

### Example

```java
GraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();

OntModel m = OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM_BUILTIN_INF, repository)
        .setNsPrefixes(OntModelFactory.STANDARD);

m.setID("ont").setVersionIRI("ont#v1");

OntObjectProperty p = m.createObjectProperty("p");
OntClass a = m.createOntClass("a");
OntIndividual i = a.createIndividual("i");
OntClass b = m.createOntClass("b")
        .addSuperClass(m.createObjectIntersectionOf(a, m.createObjectHasValue(p, i)));

m.ontObjects(OntClass.class).forEach(System.out::println);
m.ontObjects(OntIndividual.class).forEach(System.out::println);

m.write(System.out, "ttl");
```

### Available via [jitpack](https://jitpack.io/#sszuev/jena-owl2/)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sszuev</groupId>
        <artifactId>jena-owl2</artifactId>
        <version>{{latest-version}}</version>
    </dependency>
</dependencies>
```

### Apache License Version 2.0