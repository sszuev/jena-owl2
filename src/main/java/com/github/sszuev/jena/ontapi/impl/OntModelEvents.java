package com.github.sszuev.jena.ontapi.impl;

import org.apache.jena.graph.GraphEvents;

public class OntModelEvents {
    public static final GraphEvents ON_ONT_ID_CHANGE = new GraphEvents("changeIDStart", "XXX");
    public static final GraphEvents NOTIFY_ONT_ID_CHANGED = new GraphEvents("changeIDEnd", "XXX");
}
