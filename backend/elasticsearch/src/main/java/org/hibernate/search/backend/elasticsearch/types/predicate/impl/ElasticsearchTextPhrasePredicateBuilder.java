/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.types.predicate.impl;

import org.hibernate.search.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.backend.elasticsearch.gson.impl.JsonObjectAccessor;
import org.hibernate.search.backend.elasticsearch.search.predicate.impl.AbstractElasticsearchSearchPredicateBuilder;
import org.hibernate.search.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateBuilder;
import org.hibernate.search.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateContext;
import org.hibernate.search.engine.search.predicate.spi.PhrasePredicateBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ElasticsearchTextPhrasePredicateBuilder extends AbstractElasticsearchSearchPredicateBuilder
		implements PhrasePredicateBuilder<ElasticsearchSearchPredicateBuilder> {

	private static final JsonObjectAccessor MATCH_PHRASE_ACCESSOR = JsonAccessor.root().property( "match_phrase" ).asObject();

	private static final JsonAccessor<Integer> SLOP_ACCESSOR = JsonAccessor.root().property( "slop" ).asInteger();
	private static final JsonAccessor<JsonElement> QUERY_ACCESSOR = JsonAccessor.root().property( "query" );

	private final String absoluteFieldPath;

	private Integer slop;
	private JsonElement phrase;

	ElasticsearchTextPhrasePredicateBuilder(String absoluteFieldPath) {
		this.absoluteFieldPath = absoluteFieldPath;
	}

	@Override
	public void slop(int slop) {
		this.slop = slop;
	}

	@Override
	public void phrase(String phrase) {
		this.phrase = new JsonPrimitive( phrase );
	}

	@Override
	protected JsonObject doBuild(ElasticsearchSearchPredicateContext context,
			JsonObject outerObject, JsonObject innerObject) {
		QUERY_ACCESSOR.set( innerObject, phrase );
		if ( slop != null ) {
			SLOP_ACCESSOR.set( innerObject, slop );
		}

		JsonObject middleObject = new JsonObject();
		middleObject.add( absoluteFieldPath, innerObject );

		MATCH_PHRASE_ACCESSOR.set( outerObject, middleObject );
		return outerObject;
	}

}