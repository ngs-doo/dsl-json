package com.dslplatform.json.runtime;

/**
 * Marker for converters.
 * When collections are encoding elements, interfaces and abstract classes
 * would check each element for its actual signature and use that instead.
 *
 * To prevent lookup on each elements and use explicitly provided converter,
 * use descriptions which have this marker even when dealing with unknown types.
 */
public interface ExplicitDescription {
}
