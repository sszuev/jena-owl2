package com.github.sszuev.jena.ontapi.common;

import com.github.sszuev.jena.ontapi.OntJenaException;
import com.github.sszuev.jena.ontapi.impl.OntGraphModelImpl;
import com.github.sszuev.jena.ontapi.model.OntModel;
import com.github.sszuev.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An abstraction to work with {@link OntPersonality}
 * and an interface-analogue of the {@link EnhGraph Jena Enhanced Graph},
 * and also a facility to provide implicit links between different
 * {@link EnhNodeFactory} factories within a model.
 * A .orElse(null) is assumed to be {@link OntEnhGraph}.
 * <p>
 * Explicit links between object factories are undesirable, since replacing one of the factories will affect others.
 * But using this interface, it is possible to build safe implicit links and
 * replacing one factory with a custom implementation will not break the whole model.
 * More about this see in the description for
 * the method {@link OntObjectPersonalityBuilder#add(Class, EnhNodeFactory)}.
 */
@SuppressWarnings("unused")
public interface OntEnhGraph {

    /**
     * Represents the given {@code EnhGraph} as a {@link OntEnhGraph}.
     *
     * @param graph {@link EnhGraph enhanced graph},
     *              that is also assumed to be {@link OntModel}, not {@code null}
     * @return {@link OntEnhGraph}
     * @throws OntJenaException in case the conversion is not possible
     * @see OntPersonality#asJenaPersonality(OntPersonality)
     */
    static OntEnhGraph asPersonalityModel(EnhGraph graph) throws OntJenaException {
        if (graph instanceof OntEnhGraph) {
            return (OntEnhGraph) graph;
        }
        throw new OntJenaException.IllegalArgument("The given EnhGraph is not a PersonalityModel: " + graph);
    }

    /**
     * Represents the given {@code Ont[Graph]Model} as a {@link OntEnhGraph}.
     *
     * @param graph {@link OntModel OWL graph model},
     *              that is also assumed to be {@link EnhGraph}, not {@code null}
     * @return {@link OntEnhGraph}
     * @throws OntJenaException in case the conversion is not possible
     * @see OntPersonality#asJenaPersonality(OntPersonality)
     */
    static OntEnhGraph asPersonalityModel(OntModel graph) throws OntJenaException {
        if (graph instanceof OntEnhGraph) {
            return (OntEnhGraph) graph;
        }
        throw new OntJenaException.IllegalArgument("The given OntGraphModel is not a PersonalityModel: " + graph);
    }

    /**
     * Extracts {@link OntConfig} from the given enhanced graph.
     *
     * @param graph {@link OntModel OWL graph model},
     *              that is also assumed to be {@link EnhGraph}, not {@code null}
     * @return {@link OntConfig}
     */
    static OntConfig config(EnhGraph graph) {
        return asPersonalityModel(graph).getOntPersonality().getConfig();
    }

    /**
     * Checks if the given {@link Node node} can be viewed as the given type.
     * Opposite to the method {@link OntEnhGraph#findNodeAs(Node, Class)}, this method handles possible graph recursions.
     *
     * @param view  Class-type
     * @param node  {@link Node}
     * @param graph {@link EnhGraph}, assumed to be {@link OntGraphModelImpl}
     * @return {@code true} if the node can be safely cast to the specified type
     */
    static boolean canAs(Class<? extends RDFNode> view, Node node, EnhGraph graph) {
        return asPersonalityModel(graph).canNodeAs(view, node);
    }

    /**
     * Returns the model personality, that is unmodifiable model's configuration storage.
     *
     * @return {@link OntPersonality}
     */
    OntPersonality getOntPersonality();

    /**
     * Answers an enhanced node that wraps the given {@link Node node} and conforms to the given interface type.
     *
     * @param node a {@link Node node}, that is assumed to be in this graph
     * @param view a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node, not {@code null}
     * @throws OntJenaException in case no RDFNode match found
     * @see OntEnhGraph#findNodeAs(Node, Class)
     * @see EnhGraph#getNodeAs(Node, Class)
     */
    <N extends RDFNode> N getNodeAs(Node node, Class<N> view);

    /**
     * Answers an enhanced node that wraps the given {@link Node node} and conforms to the given interface type.
     * It works silently: no exception is thrown, instead returns {@code null}.
     *
     * @param node {@link Node}
     * @param type {@link Class}-type
     * @param <N>  any subtype of {@link RDFNode}
     * @return {@link RDFNode} or {@code null}
     * @see OntEnhGraph#getNodeAs(Node, Class)
     */
    <N extends RDFNode> N findNodeAs(Node node, Class<N> type);

    /**
     * Answers an enhanced node that wraps the given {@link Node node} and conforms to the given interface type,
     * taking into account possible graph recursions.
     *
     * @param node a {@link Node node}, that is assumed to be in this graph
     * @param view a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node or {@code null} if no match found
     * @throws OntJenaException.Recursion if a graph recursion is indicated
     * @see OntEnhGraph#getNodeAs(Node, Class)
     */
    <N extends RDFNode> N safeFindNodeAs(Node node, Class<N> view);

    /**
     * Equivalent to {@code safeFindNodeAs(node, view) != null}.
     *
     * @param view {@link RDFNode} type
     * @param node {@link Node}
     * @return boolean
     */
    default boolean canNodeAs(Class<? extends RDFNode> view, Node node) {
        return safeFindNodeAs(node, view) != null;
    }

    /**
     * @param type {@link X}
     * @param <X>  any {@link OntObject} type
     * @throws OntJenaException.Unsupported if the {@code type} is not supported by the configuration
     */
    default <X extends OntObject> void checkType(Class<X> type) {
        OntJenaException.checkSupported(getOntPersonality().supports(type),
                "Profile " + getOntPersonality().getName() + " does not support language construct " +
                        OntEnhNodeFactories.viewAsString(type));
    }
}
