package com.github.sszuev.jena.ontapi;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.shared.JenaException;

/**
 * A base jena exception that is used inside ONT RDF Model subsystem (i.e. inside package {@link com.github.sszuev.jena.ontapi}).
 * <p>
 * Created by @ssz on 24.11.2016.
 */
@SuppressWarnings({"unused"})
public class OntJenaException extends JenaException {

    public OntJenaException() {
        super();
    }

    public OntJenaException(String message) {
        super(message);
    }

    public OntJenaException(Throwable cause) {
        super(cause);
    }

    public OntJenaException(String message, Throwable cause) {
        super(message, cause);
    }

    public static <T> T notNull(T obj) {
        return notNull(obj, null);
    }

    public static <T> T notNull(T obj, String message) {
        if (obj == null)
            throw message == null ? new IllegalArgument() : new IllegalArgument(message);
        return obj;
    }

    /**
     * Stub for TO-DO.
     */
    public static <X> X TODO(String message) {
        throw new RuntimeException(message);
    }

    /**
     * @throws IllegalStateException if {@link X} is {@code null}
     * @see java.util.Objects#requireNonNull(Object)
     */
    public static <X> X checkNotNull(X object) {
        if (object == null) {
            throw new IllegalStateException();
        }
        return object;
    }

    /**
     * @throws IllegalStateException if {@link X} is {@code null}
     * @see java.util.Objects#requireNonNull(Object)
     */
    public static <X> X checkNotNull(X object, String message) {
        checkTrue(object != null, message);
        return object;
    }

    /**
     * @throws IllegalStateException if parameter condition is {@code true}
     */
    public static void checkFalse(Boolean mustBeFalse, String message) {
        if (mustBeFalse) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * @throws IllegalStateException if parameter condition is {@code false}
     */
    public static void checkTrue(Boolean mustBeTrue, String message) {
        if (!mustBeTrue) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Exception that is thrown when an ontology resource is converted to another facet,
     * using {@link org.apache.jena.rdf.model.RDFNode#as as()},
     * and the requested conversion is not possible.
     * This is an analogue of {@link org.apache.jena.ontology.ConversionException},
     * and it is used mostly by {@link EnhNodeFactory}.
     */
    public static class Conversion extends OntJenaException {
        public Conversion(String message, Throwable cause) {
            super(message, cause);
        }

        public Conversion(String message) {
            super(message);
        }
    }

    /**
     * Exception, which may happen while creation of ont-object.
     */
    public static class Creation extends OntJenaException {
        public Creation(String message, Throwable cause) {
            super(message, cause);
        }

        public Creation(String message) {
            super(message);
        }
    }

    /**
     * An exception to indicate that a feature is not supported right now
     * or by design for current conditions.
     */
    public static class IllegalCall extends OntJenaException {
        public IllegalCall() {
            super();
        }

        public IllegalCall(String message) {
            super(message);
        }
    }

    /**
     * An exception that is thrown if a recursion is found in the graph.
     * Example of such graph recursion:
     * <pre>{@code  _:b0 a owl:Class ; owl:complementOf  _:b0 .}</pre>
     */
    public static class Recursion extends OntJenaException {

        public Recursion(String message) {
            super(message);
        }
    }

    /**
     * A Jena exception that indicates wrong input.
     */
    public static class IllegalArgument extends OntJenaException {
        public IllegalArgument() {
            super();
        }

        public IllegalArgument(String message) {
            super(message);
        }

        public IllegalArgument(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * A Jena exception that indicates that Jena-object state is broken,
     * which may happen in multithreading or in other uncommon situations.
     */
    public static class IllegalState extends OntJenaException {
        public IllegalState() {
            super();
        }

        public IllegalState(String message) {
            super(message);
        }

        public IllegalState(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception that is raised when an ontology operation is attempted that is
     * not present in the configuration (e.g. language profile) for the current ontology model.
     */
    public static class Unsupported extends OntJenaException {
        public Unsupported(String message) {
            super(message);
        }
    }


}
