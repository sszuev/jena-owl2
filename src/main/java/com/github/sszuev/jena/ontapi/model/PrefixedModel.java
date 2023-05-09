package com.github.sszuev.jena.ontapi.model;

import org.apache.jena.shared.PrefixMapping;

import java.util.Map;

/**
 * A technical interface that describes model prefix operations.
 * Contains overridden methods inherited from {@link PrefixMapping}.
 * Created by @ssz on 15.03.2020.
 *
 * @param <R> - subtype of {@link PrefixMapping}, the type to return
 */
interface PrefixedModel<R extends PrefixMapping> extends PrefixMapping {

    @Override
    R setNsPrefix(String prefix, String uri);

    @Override
    R removeNsPrefix(String prefix);

    @Override
    R clearNsPrefixMap();

    @Override
    R setNsPrefixes(PrefixMapping other);

    @Override
    R setNsPrefixes(Map<String, String> map);

    @Override
    R withDefaultMappings(PrefixMapping map);

    @Override
    R lock();
}
