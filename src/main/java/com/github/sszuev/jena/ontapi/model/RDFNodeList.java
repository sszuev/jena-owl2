package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A parameterized analogue of the {@link RDFList Jena RDF []-List} that provides read-only access to its items.
 * <p>
 * Created by @ssz on 08.05.2019.
 *
 * @param <E> the type of element-nodes in this []-list
 * @see RDFList
 */
public interface RDFNodeList<E extends RDFNode> extends Resource {

    /**
     * Lists all elements of the type {@link E} from this list.
     * Note: a real RDF-list may contain nodes with incompatible type,
     * in this case they will not be included in the result {@code Stream}.
     * To get all {@link RDFNode RDF Node}s use the standard list representation:
     * the expression {@code Iter.asStream(this.as(RDFList.class).iterator())} will return a {@code Stream} of nodes.
     *
     * @return {@code Stream} of {@link E}-elements
     * @see #as(Class)
     * @see Iterators#asStream(java.util.Iterator)
     */
    Stream<E> members();

    /**
     * Answers {@code true} if the []-list contains the specified {@code element}.
     * More formally, returns {@code true} if and only if
     * this RDF-list contains at least one element {@code e} of the type {@link E} such that {@code element.equals(e)}.
     *
     * @param element {@link E}, not {@code null}
     * @return boolean
     */
    default boolean contains(E element) {
        Objects.requireNonNull(element);
        try (Stream<E> members = members()) {
            return members.anyMatch(element::equals);
        }
    }

    /**
     * Answers the first element of the type {@link E}.
     *
     * @return {@code Optional} around the {@link E}-item
     */
    default Optional<E> first() {
        try (Stream<E> members = members()) {
            return members.findFirst();
        }
    }

    /**
     * Answers the last element of the type {@link E}.
     *
     * @return {@code Optional} around the {@link E}-item
     */
    default Optional<E> last() {
        return members().reduce((f, s) -> s);
    }

    /**
     * Answers the number of {@link RDFNode rdf-node}s in the list.
     * Note: in general, this operation is not equivalent to the expression {@code this.members().count()},
     * since the list may contain items of incompatible type.
     *
     * @return the real size of the []-list as an integer
     */
    default long size() {
        return as(RDFList.class).size();
    }

    /**
     * Answers {@code true} if it is a nil []-list.
     * Please note: a non-nil list may also not contain elements of the type {@link E}
     * and, therefore, be {@link #isEmpty()} empty}.
     *
     * @return boolean
     * @see #isEmpty()
     */
    default boolean isNil() {
        return as(RDFList.class).isEmpty();
    }

    /**
     * Answers {@code true} if this list contains no elements of the type {@link E}.
     * A {@link #isNil() nil}-list is always empty, but the reverse is not true.
     *
     * @return boolean
     * @see #isNil()
     */
    default boolean isEmpty() {
        return first().isEmpty();
    }

}
