package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.rdf.model.Model;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A technical interface that describes model I/O operations.
 * Contains overridden read/write methods inherited from {@link Model}.
 *
 * @param <R> - subtype of {@link Model}, the model to return
 * @see <a href="http://jena.apache.org/documentation/io/index.html">"Reading and Writing RDF in Apache Jena"</a>
 */
interface IOModel<R extends Model> extends Model {

    @Override
    R read(String url);

    @Override
    R read(InputStream in, String base);

    @Override
    R read(InputStream in, String base, String lang);

    @Override
    R read(Reader reader, String base);

    @Override
    R read(String url, String lang);

    @Override
    R read(Reader reader, String base, String lang);

    @Override
    R read(String url, String base, String lang);

    @Override
    R write(Writer writer);

    @Override
    R write(Writer writer, String lang);

    @Override
    R write(Writer writer, String lang, String base);

    @Override
    R write(OutputStream out);

    @Override
    R write(OutputStream out, String lang);

    @Override
    R write(OutputStream out, String lang, String base);

}
