package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelCon;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.List;

/**
 * A technical interface that describes model modify operations.
 * Contains overridden methods inherited from {@link Model} and {@link ModelCon}:
 * Created by @ssz on 15.03.2020.
 *
 * @param <R> - a subtype of {@link Model}, the type to return
 */
interface MutableModel<R extends Model> extends Model {

    @Override
    R add(Statement s);

    @Override
    R add(Resource s, Property p, RDFNode o);

    @Override
    R add(Model m);

    @Override
    R add(StmtIterator it);

    @Override
    R add(Statement[] statements);

    @Override
    R add(List<Statement> statements);

    @Override
    R remove(Statement s);

    @Override
    R remove(Resource s, Property p, RDFNode o);

    @Override
    R remove(Model m);

    @Override
    R remove(StmtIterator it);

    @Override
    R removeAll(Resource s, Property p, RDFNode o);

    @Override
    R remove(Statement[] statements);

    @Override
    R remove(List<Statement> statements);

    @Override
    R removeAll();

    @Override
    R addLiteral(Resource s, Property p, boolean v);

    @Override
    R addLiteral(Resource s, Property p, long v);

    @Override
    R addLiteral(Resource s, Property p, int v);

    @Override
    R addLiteral(Resource s, Property p, char v);

    @Override
    R addLiteral(Resource s, Property p, float v);

    @Override
    R addLiteral(Resource s, Property p, double v);

    @Override
    R addLiteral(Resource s, Property p, Literal o);

    @Override
    R add(Resource s, Property p, String lex);

    @Override
    R add(Resource s, Property p, String lex, RDFDatatype datatype);

    @Override
    R add(Resource s, Property p, String lex, String lang);
}
