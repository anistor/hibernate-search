/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge;

/**
 * A bridge from a POJO entity to a document routing key.
 *
 * @deprecated Implement {@link RoutingBridge} instead.
 * See the reference documentation for how to implement it and use it.
 */
@Deprecated
public interface RoutingKeyBridge extends AutoCloseable {

	/**
	 * Generate a routing key using the given {@code tenantIdentifier}, {@code entityIdentifier} and {@code bridgedElement}
	 * as input and transforming them as necessary.
	 * <p>
	 * <strong>Warning:</strong> Reading from {@code bridgedElement} should be done with care.
	 * Any read that was not declared during {@link org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.RoutingKeyBinder#bind(org.hibernate.search.mapper.pojo.bridge.binding.RoutingKeyBindingContext) binding}
	 * (by declaring dependencies using {@link org.hibernate.search.mapper.pojo.bridge.binding.RoutingKeyBindingContext#dependencies()}
	 * or (advanced use) creating an accessor using {@link org.hibernate.search.mapper.pojo.bridge.binding.RoutingKeyBindingContext#bridgedElement()})
	 * may lead to out-of-sync indexes,
	 * because Hibernate Search will consider the read property irrelevant to indexing
	 * and will not reindex entities when that property changes.
	 *
	 * @param tenantIdentifier The tenant identifier currently in use ({@code null} if none).
	 * @param entityIdentifier The value of the POJO property used to generate the document identifier,
	 * i.e. the same value that was passed to {@link IdentifierBridge#toDocumentIdentifier(Object, org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeToDocumentIdentifierContext)}.
	 * @param bridgedElement The element this bridge is applied to, from which data should be read.
	 * @param context A context that can be
	 * {@link org.hibernate.search.mapper.pojo.bridge.runtime.RoutingKeyBridgeToRoutingKeyContext#extension(org.hibernate.search.mapper.pojo.bridge.runtime.RoutingKeyBridgeToRoutingKeyContextExtension) extended}
	 * to a more useful type, giving access to such things as a Hibernate ORM Session (if using the Hibernate ORM mapper).
	 * @return The resulting routing key. Never null.
	 */
	String toRoutingKey(String tenantIdentifier, Object entityIdentifier, Object bridgedElement,
			org.hibernate.search.mapper.pojo.bridge.runtime.RoutingKeyBridgeToRoutingKeyContext context);

	/**
	 * Close any resource before the bridge is discarded.
	 */
	@Override
	default void close() {
	}

}
