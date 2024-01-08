package com.github.sszuev.jena.ontapi.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEvents;

import java.util.Objects;

/**
 * Events that called on {@link com.github.sszuev.jena.ontapi.model.OntModel}'s level.
 */
public class OntModelEvent extends GraphEvents {
    public static final String START_CHANGE_ID = "startChangeID";
    public static final String FINISH_CHANGE_ID = "endChangeID";
    public static final String START_ADD_DATA_GRAPH = "startAddDataGraph";
    public static final String START_DELETE_DATA_GRAPH = "startDeleteDataGraph";
    public static final String FINISH_ADD_DATA_GRAPH = "finishAddDataGraph";
    public static final String FINISH_DELETE_DATA_GRAPH = "finishDeleteDataGraph";
    public static final String START_READ_DATA_GRAPH = "startReadDataGraph";
    public static final String FINISH_READ_DATA_GRAPH = "endReadDataGraph";

    private final String type;

    public OntModelEvent(String title, Object content) {
        super(title, content);
        this.type = title;
    }

    public static boolean isEventOfType(Object any, String type) {
        if (any instanceof OntModelEvent) {
            return Objects.equals(((OntModelEvent) any).type, type);
        }
        if (any instanceof GraphEvents) {
            if (any == startRead && START_READ_DATA_GRAPH.equals(type)) {
                return true;
            }
            if (any == finishRead && FINISH_READ_DATA_GRAPH.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static OntModelEvent startChangeIDEvent() {
        return new OntModelEvent(START_CHANGE_ID, "CHANGE-ID");
    }

    public static OntModelEvent finishChangeIDEvent() {
        return new OntModelEvent(FINISH_CHANGE_ID, "CHANGE-ID");
    }

    public static OntModelEvent startAddDataGraphEvent(Graph graph) {
        return new OntModelEvent(START_ADD_DATA_GRAPH, graph);
    }

    public static OntModelEvent finishAddDataGraphEvent(Graph graph) {
        return new OntModelEvent(FINISH_ADD_DATA_GRAPH, graph);
    }

    public static OntModelEvent startDeleteDataGraphEvent(Graph graph) {
        return new OntModelEvent(START_DELETE_DATA_GRAPH, graph);
    }

    public static OntModelEvent finishDeleteDataGraphEvent(Graph graph) {
        return new OntModelEvent(FINISH_DELETE_DATA_GRAPH, graph);
    }

    public static GraphEvents startReadDataGraphEvent() {
        return startRead;
    }

    public static GraphEvents finishReadDataGraphEvent() {
        return finishRead;
    }
}
