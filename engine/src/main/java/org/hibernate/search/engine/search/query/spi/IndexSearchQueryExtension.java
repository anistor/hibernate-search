/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.query.spi;


import java.util.Optional;

import org.hibernate.search.engine.search.loading.context.spi.LoadingContext;

/**
 * An extension to the search query, allowing to wrap a query.
 * <p>
 * <strong>WARNING:</strong> while this type is API, because instances should be manipulated by users,
 * all of its methods are considered SPIs and therefore should never be called or implemented directly by users.
 * In short, users are only expected to get instances of this type from an API ({@code SomeExtension.get()})
 * and pass it to another API.
 *
 * @param <Q> The type of search queries wrappers created by this extension.
 * @param <T> The type of hits in the query to be wrapped.
 *
 * @see IndexSearchQuery#extension(IndexSearchQueryExtension)
 */
public interface IndexSearchQueryExtension<Q, T> {

	/**
	 * Attempt to extend a given query, returning an empty {@link Optional} in case of failure.
	 * <p>
	 * <strong>WARNING:</strong> this method is not API, see comments at the type level.
	 *
	 * @param original The original, non-extended {@link IndexSearchQuery}.
	 * @param loadingContext The {@link LoadingContext} used by the original query.
	 * @return An optional containing the extended search query ({@link Q}) in case
	 * of success, or an empty optional otherwise.
	 */
	Optional<Q> extendOptional(IndexSearchQuery<T> original, LoadingContext<?, ?> loadingContext);

}