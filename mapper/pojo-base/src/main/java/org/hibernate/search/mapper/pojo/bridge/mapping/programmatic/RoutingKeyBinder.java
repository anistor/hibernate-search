/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge.mapping.programmatic;

/**
 * A binder from a POJO type to a routing key.
 * <p>
 * This binder takes advantage of provided metadata
 * to pick, configure and create a {@link org.hibernate.search.mapper.pojo.bridge.RoutingKeyBridge}.
 *
 * @see org.hibernate.search.mapper.pojo.bridge.RoutingKeyBridge
 * @deprecated Implement {@link RoutingBinder} instead.
 * See the reference documentation for how to implement it and use it.
 */
@Deprecated
public interface RoutingKeyBinder {

	/**
	 * Binds a type to routing keys.
	 * <p>
	 * The context passed in parameter provides various information about the type being bound.
	 * Implementations are expected to take advantage of that information
	 * and to call one of the {@code bridge(...)} methods on the context
	 * to set the bridge.
	 * <p>
	 * Implementations are also expected to declare dependencies, i.e. the properties
	 * that will later be used in the
	 * {@link org.hibernate.search.mapper.pojo.bridge.RoutingKeyBridge#toRoutingKey(String, Object, Object, org.hibernate.search.mapper.pojo.bridge.runtime.RoutingKeyBridgeToRoutingKeyContext)} method,
	 * using {@link org.hibernate.search.mapper.pojo.bridge.binding.RoutingKeyBindingContext#dependencies()}.
	 * Failing that, Hibernate Search will not reindex entities properly when an indexed property is modified.
	 *
	 * @param context A context object providing information about the type being bound,
	 * and expecting a call to one of its {@code bridge(...)} methods.
	 */
	void bind(org.hibernate.search.mapper.pojo.bridge.binding.RoutingKeyBindingContext context);

}
