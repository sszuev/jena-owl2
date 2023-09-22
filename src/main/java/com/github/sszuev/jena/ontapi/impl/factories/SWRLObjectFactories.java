package com.github.sszuev.jena.ontapi.impl.factories;

import com.github.sszuev.jena.ontapi.common.EnhNodeFactory;
import com.github.sszuev.jena.ontapi.common.EnhNodeFinder;
import com.github.sszuev.jena.ontapi.common.EnhNodeProducer;
import com.github.sszuev.jena.ontapi.common.OntEnhGraph;
import com.github.sszuev.jena.ontapi.common.OntEnhNodeFactories;
import com.github.sszuev.jena.ontapi.impl.objects.OntSWRLImpl;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.model.OntSWRL;
import com.github.sszuev.jena.ontapi.vocabulary.SWRL;
import org.apache.jena.rdf.model.impl.LiteralImpl;

/**
 * A helper-factory to produce (SWRL) {@link EnhNodeFactory} factories;
 * for {@link com.github.sszuev.jena.ontapi.common.OntPersonality ont-personalities}
 */
public final class SWRLObjectFactories {

    public static final EnhNodeFactory VARIABLE_SWRL = OntEnhNodeFactories.createCommon(
            new EnhNodeProducer.WithType(OntSWRLImpl.VariableImpl.class, SWRL.Variable),
            new EnhNodeFinder.ByType(SWRL.Variable),
            OntSWRLs.VARIABLE_FILTER
    );
    public static final EnhNodeFactory DARG_SWRL = OntEnhNodeFactories.createCommon(
            OntSWRLImpl.DArgImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRLs.VARIABLE_FILTER.or(LiteralImpl.factory::canWrap)
    );
    public static final EnhNodeFactory IARG_SWRL = OntEnhNodeFactories.createCommon(
            OntSWRLImpl.IArgImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRLs.VARIABLE_FILTER.or((n, g) -> OntEnhGraph.canAs(OntIndividual.class, n, g))
    );
    public static final EnhNodeFactory BUILTIN_SWRL = OntEnhNodeFactories.createCommon(
            new EnhNodeProducer.WithType(OntSWRLImpl.BuiltinImpl.class, SWRL.Builtin),
            new EnhNodeFinder.ByType(SWRL.Builtin),
            OntSWRLs.BUILTIN_FILTER
    );
    public static final EnhNodeFactory BUILT_IN_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.BuiltInAtomImpl.class,
            SWRL.BuiltinAtom
    );
    public static final EnhNodeFactory CLASS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.OntClassAtomImpl.class,
            SWRL.ClassAtom
    );
    public static final EnhNodeFactory DATA_RANGE_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DataRangeAtomImpl.class,
            SWRL.DataRangeAtom
    );
    public static final EnhNodeFactory DATA_VALUED_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DataPropertyAtomImpl.class,
            SWRL.DatavaluedPropertyAtom
    );
    public static final EnhNodeFactory INDIVIDUAL_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.ObjectPropertyAtomImpl.class,
            SWRL.IndividualPropertyAtom
    );
    public static final EnhNodeFactory DIFFERENT_INDIVIDUALS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DifferentIndividualsAtomImpl.class,
            SWRL.DifferentIndividualsAtom
    );
    public static final EnhNodeFactory SAME_INDIVIDUALS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.SameIndividualsAtomImpl.class,
            SWRL.SameIndividualAtom
    );
    public static final EnhNodeFactory IMPL_SWRL = new OntSWRLs.SWRLImplFactory();
    public static final EnhNodeFactory ANY_ARG_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRL.DArg.class,
            OntSWRL.IArg.class
    );
    public static final EnhNodeFactory ANY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.TYPED,
            OntSWRL.Atom.WithBuiltin.class,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class
    );
    public static final EnhNodeFactory ANY_BINARY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.TYPED,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class
    );
    public static final EnhNodeFactory ANY_UNARY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.TYPED,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class
    );
    public static final EnhNodeFactory ANY_OBJECT_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.TYPED,
            OntSWRL.Atom.WithBuiltin.class,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class,
            OntSWRL.Builtin.class,
            OntSWRL.Variable.class,
            OntSWRL.Imp.class
    );
}
