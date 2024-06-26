package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A parameterized analogue of the {@link RDFList Jena []-List} that behaves like a java collection.
 * Please note: currently it is not a Personality resource and, therefore, Jena Polymorphism is not supported for it.
 * The latter means that attempt to cast any {@link RDFNode RDF Node} to this view
 * will cause {@link org.apache.jena.enhanced.UnsupportedPolymorphismException UnsupportedPolymorphismException},
 * but it is possible do the opposite: cast an instance of this interface to the {@link RDFList Jena []-List} view
 * using the expression {@code OntList.as(RDFList.class)}.
 * Also note: switching to nil-list (by any of the add/remove/clean operations) from a not-empty list and vice verse
 * violates a Jena invariant, this means that this {@link OntResource} behaves not always like pure {@link Resource Jena Resource}
 * and all the methods may throw {@link OntJenaException.IllegalState}
 * in case of usage different instances encapsulating the same resource-list.
 * <p>
 * Unlike the standard {@link RDFList []-List} implementation, ONT-List can be typed.
 * This means that each resource-member of []-List may have an {@link RDF#type rdf:type} declaration,
 * while the standard RDF []-List impl does not support typing.
 * See below for an example of a typed []-list in Turtle format:
 * <pre>{@code
 * [ rdf:type   <type> ;
 *   rdf:first  <A> ;
 *   rdf:rest   [ rdf:type   <type> ;
 *                rdf:first  <B> ;
 *                rdf:rest   rdf:nil
 *              ]
 * ] .
 * }</pre>
 * Note, that an empty []-list (i.e. {@link RDF#nil nil}-list) cannot be typed.
 * <p>
 * Using the method {@link #getMainStatement()} it is possible to add annotations with any nesting depth.
 *
 * @param <E> the type of {@link RDFNode rdf-node}s in this list
 * @see RDFNodeList
 */
public interface OntList<E extends RDFNode> extends RDFNodeList<E>, OntResource {

    /**
     * Adds the given value to the end of the list.
     *
     * @param e {@link E} rdf-node
     * @return this list instance
     * @see #add(RDFNode)
     */
    OntList<E> addLast(E e);

    /**
     * Removes the last element from this list.
     * No-op in case of nil-list.
     * Note: the removed element can be of any type, not necessarily of the type {@link E}.
     *
     * @return this list instance
     * @see #remove()
     */
    OntList<E> removeLast();

    /**
     * Inserts the specified element at the beginning of this list.
     * As a rule, this operation is faster than {@link #addLast(RDFNode)},
     * since it does not require iteration to the end of the list.
     *
     * @param e {@link E} rdf-node
     * @return this list instance
     */
    OntList<E> addFirst(E e);

    /**
     * Removes and the first element from this list.
     * No-op in case of empty list.
     * Note: the last element can be of any type, not necessarily of type {@link E}.
     * As a rule, this operation is faster than {@link #removeLast()} ,
     * since the last one requires iteration to the end of the list.
     *
     * @return the first element from this list
     */
    OntList<E> removeFirst();

    /**
     * Removes all elements from this list.
     * The list will be empty (nil) after this call returns.
     *
     * @return this (empty) instance
     */
    OntList<E> clear();

    /**
     * Answers the list that is the tail of this list starting from the given position.
     * Note: the returned list cannot be annotated.
     * This method can be used to insert/remove/clear the parent list at any position,
     * e.g. the operation {@code get(1).addFirst(e)} will insert the element {@code e} at second position.
     *
     * @param index int, not negative
     * @return new {@code OntList} instance
     * @throws OntJenaException.IllegalArgument if the specified index is out of list bounds
     */
    OntList<E> get(int index) throws OntJenaException;

    /**
     * Answers the resource-type of this ONT-list, if it is typed.
     * A standard RDF-list does not require any {@link RDF#type rdf:type}
     * in its RDF-deeps, since predicates {@link RDF#first rdf:first},
     * {@link RDF#rest rdf:rest}
     * and {@link RDF#nil rdf:nil} are sufficient for its description.
     * In this case the method returns {@link Optional#empty() empty} result.
     * But in some rare semantics (e.g. see {@link SWRL}),
     * the []-list must to be typed.
     * In that case this method returns a URI-{@code Resource} (that is wrapped as {@code Optional})
     * describing the []-list's type
     * (for SWRL it is {@link SWRL#AtomList swrl:AtomList}).
     *
     * @return {@link Optional} around the URI-{@link Resource}, can be empty.
     */
    Optional<Resource> type();

    /**
     * Lists all statements related to this list.
     * For nil-list an empty stream is expected.
     * Note: it returns all statements even if the list contains incompatible types.
     * <p>
     * See also inherit java-docs:
     * {@inheritDoc}
     *
     * @return Stream of {@link OntStatement Ontology Statement}s that does not support annotations
     */
    @Override
    Stream<OntStatement> spec();

    /**
     * Returns the root statement plus spec.
     * Please note: only the first item (root) is allowed to be annotated.
     *
     * @return {@code Stream} of {@link OntStatement Ontology Statement}s
     */
    default Stream<OntStatement> content() {
        return Stream.concat(Stream.of(getMainStatement()), spec());
    }

    /**
     * Adds the given value to the end of the list.
     * This is a synonym for the {@code this.addLast(e)}.
     *
     * @param e {@link E} rdf-node
     * @return this list instance
     * @see #addLast(RDFNode)
     */
    default OntList<E> add(E e) {
        return addLast(e);
    }

    /**
     * Removes the last element from this list.
     * This is a synonym for the {@code this.removeLast(e)}.
     *
     * @return this list instance
     * @see #removeLast()
     */
    default OntList<E> remove() {
        return removeLast();
    }

    /**
     * Appends all the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's iterator.
     *
     * @param c Collection of {@link E}-elements
     * @return this list instance
     */
    default OntList<E> addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isLocal() {
        return getMainStatement().isLocal();
    }

}
