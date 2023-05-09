package com.github.sszuev.jena.ontapi.model;

import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A base for SWRL addition.
 * <p>
 * Created by @ssz on 02.11.2016.
 *
 * @see SWRL
 * @see <a href='https://www.w3.org/Submission/SWRL'>specification</a>
 */
public interface OntSWRL extends OntObject {

    /**
     * @see OntModel#createSWRLImp(Collection, Collection)
     */
    @SuppressWarnings("rawtypes")
    interface Imp extends OntSWRL {

        /**
         * Gets the head ONT-List.
         * The list <b>is</b> typed:
         * each of its items has the type {@link SWRL#AtomList swrl:AtomList}.
         *
         * @return {@link OntList} of {@link Atom}
         */
        OntList<Atom> getHeadList();

        /**
         * Gets the body ONT-List.
         * The list <b>is</b> typed:
         * each of its items has the type {@link SWRL#AtomList swrl:AtomList}.
         *
         * @return {@link OntList} of {@link Atom}
         */
        OntList<Atom> getBodyList();

        default Stream<Atom> head() {
            return getHeadList().members().distinct();
        }

        default Stream<Atom> body() {
            return getBodyList().members().distinct();
        }
    }

    /**
     * Represents {@link SWRL#Builtin} entity.
     * Must be a URI {@link Resource}.
     */
    interface Builtin extends OntSWRL {
    }

    /**
     * Represents {@link SWRL#Variable} entity.
     *
     * @see OntModel#createSWRLVariable(String)
     */
    interface Variable extends OntSWRL, DArg, IArg {
    }

    /**
     * It is not a SWRL Object, but just a plain {@link OntObject}.
     * An interface that represents either {@link org.apache.jena.rdf.model.Literal},
     * {@link Variable} or {@link OntIndividual}.
     */
    interface Arg extends OntObject {
    }

    /**
     * An interface that represents either {@link org.apache.jena.rdf.model.Literal} or {@link Variable}.
     */
    interface DArg extends Arg {
    }

    /**
     * An interface that represents either {@link OntIndividual} or {@link Variable}.
     */
    interface IArg extends Arg {
    }

    /**
     * A base abstraction for SWRL-Atom.
     *
     * @param <P> subtype of {@link OntObject}
     */
    interface Atom<P extends OntObject> extends OntSWRL {

        /**
         * Returns the atom predicate, which can be one of the following:
         * {@link OntDataRange}, {@link OntObjectProperty}, {@link OntDataProperty}, {@link OntClass}, URI-{@link Resource}, {@link Property}.
         *
         * @return RDFNode
         */
        P getPredicate();

        /**
         * Lists all arguments from this {@code Atom}.
         *
         * @return Stream of {@link Arg}s
         */
        Stream<? extends Arg> arguments();

        /**
         * @see OntModel#createBuiltInSWRLAtom(Resource, Collection)
         */
        interface WithBuiltin extends Atom<Builtin> {
            /**
             * Gets the argument's ONT-List.
             * Note that the returned list is <b>not</b> expected to be typed,
             * i.e. there is neither {@code _:x rdf:type rdf:List}
             * or {@code _:x rdf:type swrl:AtomList} statements for each its items.
             *
             * @return {@link OntList} of {@link DArg}s
             */
            OntList<DArg> getArgList();

            @Override
            default Stream<DArg> arguments() {
                return getArgList().members();
            }
        }

        /**
         * @see OntModel#createClassSWRLAtom(OntClass, IArg)
         */
        interface WithClass extends Unary<OntClass, IArg> {
        }

        /**
         * @see OntModel#createDataRangeSWRLAtom(OntDataRange, DArg)
         */
        interface WithDataRange extends Unary<OntDataRange, DArg> {
        }

        /**
         * @see OntModel#createDataPropertySWRLAtom(OntDataProperty, IArg, DArg)
         */
        interface WithDataProperty extends Binary<OntDataProperty, IArg, DArg> {
        }

        /**
         * @see OntModel#createObjectPropertySWRLAtom(OntObjectProperty, IArg, IArg)
         */
        interface WithObjectProperty extends Binary<OntObjectProperty, IArg, IArg> {
        }

        /**
         * @see CreateSWRL#createDifferentIndividualsSWRLAtom(IArg, IArg)
         */
        interface WithDifferentIndividuals extends Binary<OntObjectProperty.Named, IArg, IArg> {
        }

        /**
         * @see CreateSWRL#createSameIndividualsSWRLAtom(IArg, IArg)
         */
        interface WithSameIndividuals extends Binary<OntObjectProperty.Named, IArg, IArg> {
        }

        /**
         * A binary atom abstraction.
         *
         * @param <P> the predicate - either {@link Arg} or {@link OntRealProperty}
         * @param <F> {@link Arg} the first argument
         * @param <S> {@link Arg} the second argument
         */
        interface Binary<P extends OntObject, F extends Arg, S extends Arg> extends Atom<P> {
            F getFirstArg();

            S getSecondArg();

            @Override
            default Stream<Arg> arguments() {
                return Stream.of(getFirstArg(), getSecondArg());
            }
        }

        /**
         * A unary atom abstraction.
         *
         * @param <P> the predicate, either {@link OntClass} or {@link OntDataRange}
         * @param <A> {@link Arg}, the argument
         */
        interface Unary<P extends OntObject, A extends Arg> extends Atom<P> {
            A getArg();

            @Override
            default Stream<A> arguments() {
                return Stream.of(getArg());
            }
        }
    }
}
