package com.github.sszuev.jena.ontapi.model;

import java.util.stream.Stream;

/**
 * A technical interface to provide {@link OntDisjoint}.
 *
 * @param <E> either {@link OntClass}, {@link OntIndividual}, {@link OntObjectProperty} or {@link OntDataProperty}
 */
interface HasDisjoint<E extends OntObject> {

    /**
     * Lists all {@code OntDisjoint} sections where this object is a member.
     *
     * @return a {@code Stream} of {@link OntDisjoint}s
     */
    Stream<? extends OntDisjoint<E>> disjoints();
}
