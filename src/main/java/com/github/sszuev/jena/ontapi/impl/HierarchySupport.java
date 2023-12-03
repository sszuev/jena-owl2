package com.github.sszuev.jena.ontapi.impl;

import com.github.sszuev.jena.ontapi.OntModelConfig;
import com.github.sszuev.jena.ontapi.common.OntConfig;
import com.github.sszuev.jena.ontapi.impl.objects.OntObjectImpl;
import com.github.sszuev.jena.ontapi.model.OntObject;
import com.github.sszuev.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to handle resource hierarchy.
 */
public final class HierarchySupport {

    /**
     * Answers {@code true} if the specified {@code test} node is in the closure of the specified {@code root} nodes
     */
    public static <X extends OntObject> boolean contains(
            X root,
            X test,
            Function<X, Stream<X>> listChildren,
            boolean direct) {
        // TODO: optimize
        return treeNodes(root, listChildren, direct).anyMatch(test::equals);
    }

    /**
     * Lists tree nodes for the given root using {@code listChildren} function, which provides adjacent nodes.
     * see {@code org.apache.jena.ontology.impl.OntResourceImpl#listDirectPropertyValues(Property, String, Class, Property, boolean, boolean)}
     */
    public static <X extends OntObject> Stream<X> treeNodes(
            X object,
            Function<X, Stream<X>> listChildren,
            boolean direct) {
        OntConfig config = OntObjectImpl.config(object.getModel());
        boolean useBuiltinReasoner = config != null && config.getBoolean(OntModelConfig.USE_BUILTIN_HIERARCHY_SUPPORT);
        if (direct) {
            return directNodesAsStream(object, useBuiltinReasoner, listChildren);
        }
        if (useBuiltinReasoner) {
            return allTreeNodes(object, listChildren);
        }
        return listChildren.apply(object).filter(x -> !object.equals(x));
    }

    /**
     * For the given object returns a {@code Set} of objects the same type,
     * that are its children which is determined by the operation {@code listChildren}.
     *
     * @param object       {@link X}
     * @param listChildren a {@code Function} that returns {@code Iterator} for an object of type {@link X}
     * @param <X>          subtype of {@link Resource}
     * @return {@code Set} of {@link X}
     */
    static <X extends Resource> Stream<X> allTreeNodes(X object, Function<X, Stream<X>> listChildren) {
        return Iterators.fromSet(() -> {
            Set<X> res = new HashSet<>();
            collectIndirect(object, listChildren, res);
            res.remove(object);
            return res;
        });
    }

    /**
     * For the given object recursively collects all children determined by the operation {@code listChildren}.
     *
     * @param root         {@link X}
     * @param listChildren a {@code Function} that returns {@code Iterator} for an object of type {@link X}
     * @param res          {@code Set} to store result
     * @param <X>          any subtype of {@link Resource}
     */
    static <X extends Resource> void collectIndirect(X root,
                                                     Function<X, Stream<X>> listChildren,
                                                     Set<X> res) {
        Deque<X> queue = new ArrayDeque<>();
        queue.add(root);
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        while (!queue.isEmpty()) {
            X next = queue.removeFirst();
            getChildren(next, listChildren, childrenNodesCache).forEach(it -> {
                        if (res.add(it)) {
                            queue.add(it);
                        }
                    }
            );
        }
    }

    public static <X extends Resource> Stream<X> directNodesAsStream(X object,
                                                                     boolean useBuiltinHierarchySupport,
                                                                     Function<X, Stream<X>> listChildren) {
        return Iterators.fromSet(() ->
                useBuiltinHierarchySupport ? directNodesAsSetWithBuiltinInf(object, listChildren) : directNodesAsSetStandard(object, listChildren)
        );
    }

    public static <X extends Resource> Set<X> directNodesAsSetStandard(X root,
                                                                       Function<X, Stream<X>> listChildren) {
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Function<X, Set<X>> getChildren = it -> getChildren(it, listChildren, childrenNodesCache);
        return getChildren.apply(root).stream()
                .filter(x -> !equivalent(x, root, getChildren) && !hasAnotherPath(x, root, getChildren))
                .collect(Collectors.toSet());
    }

    public static <X extends Resource> Set<X> directNodesAsSetWithBuiltinInf(X root,
                                                                             Function<X, Stream<X>> listChildren) {
        Map<X, Node<X>> tree = collectTree(root, listChildren);
        Node<X> theRoot = tree.get(root);
        return theRoot.childrenWithEquivalents()
                .flatMap(it -> collectDirect(theRoot, it))
                .collect(Collectors.toSet());
    }

    private static <X extends Resource> boolean hasAnotherPath(X given,
                                                               X root,
                                                               Function<X, Set<X>> getChildren) {
        return getChildren.apply(root).stream()
                .filter(it -> !equivalent(it, root, getChildren))
                .flatMap(it ->
                        getChildren.apply(it).stream().filter(x -> !equivalent(x, it, getChildren))
                )
                .anyMatch(given::equals);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static <X extends Resource> boolean equivalent(X left, X right, Function<X, Set<X>> getChildren) {
        return getChildren.apply(right).contains(left) && getChildren.apply(left).contains(right);
    }

    private static <X extends Resource> Stream<X> collectDirect(Node<X> rootNode, Node<X> it) {
        Set<X> equivalents = it.equivalents();
        if (!equivalents.contains(rootNode.node)) {
            Set<X> siblings = new HashSet<>(equivalents);
            siblings.remove(it.node);
            if (it.hasMoreThanOnePathTo(rootNode, siblings)) {
                return Stream.empty();
            } else {
                equivalents.add(it.node);
                return equivalents.stream();
            }
        } else {
            return Stream.empty();
        }
    }

    private static <X extends Resource> Map<X, Node<X>> collectTree(X root, Function<X, Stream<X>> listChildren) {
        Map<X, Set<X>> childrenNodesCache = new HashMap<>();
        Map<X, Node<X>> res = new HashMap<>();
        Set<X> visited = new HashSet<>();
        Deque<X> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            X next = queue.removeFirst();
            if (!visited.add(next)) {
                continue;
            }
            Set<X> nextChildren = getChildren(next, listChildren, childrenNodesCache);
            Node<X> nextNode = res.computeIfAbsent(next, Node::new);
            nextChildren.forEach(child -> {
                Node<X> childNode = res.computeIfAbsent(child, Node::new);
                nextNode.children.add(childNode);
                queue.add(child);
            });
        }
        return res;
    }

    private static <X extends Resource> Set<X> getChildren(
            X root,
            Function<X, Stream<X>> listChildren,
            Map<X, Set<X>> childrenNodesCache
    ) {
        return childrenNodesCache.computeIfAbsent(root, it -> {
            try (Stream<X> children = listChildren.apply(it)) {
                return children.collect(Collectors.toSet());
            }
        });
    }

    /**
     * Auxiliary class, tree node that is used for builtin hierarchy support.
     *
     * @param <X> resource
     */
    private static class Node<X extends Resource> {
        final X node;
        final Set<Node<X>> children = new HashSet<>();

        Node(X node) {
            this.node = node;
        }

        Stream<Node<X>> childrenWithEquivalents() {
            Set<X> equivalents = this.equivalents();
            return children.stream().flatMap(ch -> {
                if (equivalents.contains(ch.node)) {
                    return ch.children.stream().filter(ech -> !ech.equals(Node.this));
                } else {
                    return Stream.of(ch);
                }
            });
        }

        boolean hasMoreThanOnePathTo(Node<X> given, Set<X> exclude) {
            Deque<Node<X>> queue = new ArrayDeque<>();
            Set<X> visited = new HashSet<>();
            int res = 0;
            Iterator<Node<X>> firstLevelChildren = given.childrenWithEquivalents().iterator();
            while (firstLevelChildren.hasNext()) {
                Node<X> child = firstLevelChildren.next();
                if (exclude.contains(child.node)) {
                    continue;
                }
                if (child.node.equals(this.node)) {
                    res++;
                } else {
                    queue.add(child);
                }
            }
            while (!queue.isEmpty()) {
                Node<X> next = queue.removeFirst();
                if (exclude.contains(next.node)) {
                    continue;
                }
                if (next.node.equals(given.node)) {
                    continue;
                }
                if (next.node.equals(this.node)) {
                    if (++res > 1) {
                        return true;
                    }
                }
                if (!visited.add(next.node)) {
                    continue;
                }
                next.childrenWithEquivalents().forEach(queue::add);
            }
            return false;
        }

        Set<X> equivalents() {
            Deque<Node<X>> queue = new ArrayDeque<>();
            queue.add(this);
            Set<X> visited = new HashSet<>();
            Map<X, Set<X>> paths = new HashMap<>();
            Set<X> res = new HashSet<>();
            while (!queue.isEmpty()) {
                Node<X> next = queue.removeFirst();
                Set<X> nextPaths = paths.computeIfAbsent(next.node, it -> new HashSet<>());
                if (!nextPaths.isEmpty() && next.node.equals(this.node)) {
                    // cycle, all nodes in cycle are equivalent
                    res.addAll(nextPaths);
                    nextPaths.forEach(p -> {
                        Set<X> other = paths.get(p);
                        if (other != null) {
                            res.addAll(other);
                        }
                    });
                    continue;
                }
                if (!visited.add(next.node)) {
                    continue;
                }
                next.children.forEach(child -> {
                    Set<X> childPaths = paths.computeIfAbsent(child.node, it -> new HashSet<>());
                    childPaths.add(next.node);
                    childPaths.addAll(nextPaths);
                    queue.add(child);
                });
            }
            return res;
        }

        @Override
        public String toString() {
            return node.asNode().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            return node.equals(((Node<?>) o).node);
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }
    }

}
