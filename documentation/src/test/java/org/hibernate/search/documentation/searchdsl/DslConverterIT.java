/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.documentation.searchdsl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.search.documentation.testsupport.BackendSetupStrategy;
import org.hibernate.search.engine.search.predicate.DslConverter;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ValueBridgeRef;
import org.hibernate.search.util.impl.integrationtest.orm.OrmSetupHelper;
import org.hibernate.search.util.impl.integrationtest.orm.OrmUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DslConverterIT {
	@Parameterized.Parameters(name = "{0}")
	public static Object[] backendSetups() {
		return BackendSetupStrategy.simple().toArray();
	}

	@Rule
	public OrmSetupHelper setupHelper = new OrmSetupHelper();

	private final BackendSetupStrategy backendSetupStrategy;

	private EntityManagerFactory entityManagerFactory;

	public DslConverterIT(BackendSetupStrategy backendSetupStrategy) {
		this.backendSetupStrategy = backendSetupStrategy;
	}

	@Before
	public void setup() {
		entityManagerFactory = backendSetupStrategy.withSingleBackend( setupHelper ).setup( AuthenticationEvent.class );
		initData();
	}

	@Test
	public void dslConverterEnabled() {
		OrmUtils.withinJPATransaction( entityManagerFactory, entityManager -> {
			SearchSession searchSession = Search.getSearchSession( entityManager );

			// tag::dsl-converter-enabled[]
			SearchQuery<AuthenticationEvent> query = searchSession.search( AuthenticationEvent.class )
					.asEntity()
					.predicate( f -> f.match().onField( "outcome" )
							.matching( AuthenticationOutcome.INVALID_PASSWORD ) )
					.toQuery();
			// end::dsl-converter-enabled[]

			List<AuthenticationEvent> result = query.fetchHits();

			assertThat( result )
					.extracting( "id" )
					.containsExactly( 2 );
		} );
	}

	@Test
	public void dslConverterDisabled() {
		OrmUtils.withinJPATransaction( entityManagerFactory, entityManager -> {
			SearchSession searchSession = Search.getSearchSession( entityManager );

			// tag::dsl-converter-disabled[]
			SearchQuery<AuthenticationEvent> query = searchSession.search( AuthenticationEvent.class )
					.asEntity()
					.predicate( f -> f.match().onField( "outcome" )
							.matching( "Invalid password", DslConverter.DISABLED ) )
					.toQuery();
			// end::dsl-converter-disabled[]

			List<AuthenticationEvent> result = query.fetchHits();

			assertThat( result )
					.extracting( "id" )
					.containsExactly( 2 );
		} );
	}

	private void initData() {
		OrmUtils.withinJPATransaction( entityManagerFactory, entityManager -> {
			AuthenticationEvent event1 = new AuthenticationEvent( 1 );
			event1.setOutcome( AuthenticationOutcome.USER_NOT_FOUND );
			AuthenticationEvent event2 = new AuthenticationEvent( 2 );
			event2.setOutcome( AuthenticationOutcome.INVALID_PASSWORD );

			entityManager.persist( event1 );
			entityManager.persist( event2 );
		} );
	}

	@Entity(name = "AuthenticationEvent")
	@Indexed
	public static class AuthenticationEvent {
		@Id
		private Integer id;
		@Basic
		@Enumerated
		@FullTextField(
				analyzer = "myAnalyzer",
				valueBridge = @ValueBridgeRef(type = AuthenticationOutcomeBridge.class)
		)
		private AuthenticationOutcome outcome;

		protected AuthenticationEvent() {
			// For Hibernate ORM
		}

		public AuthenticationEvent(int id) {
			this.id = id;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public AuthenticationOutcome getOutcome() {
			return outcome;
		}

		public void setOutcome(AuthenticationOutcome outcome) {
			this.outcome = outcome;
		}
	}

	private enum AuthenticationOutcome {
		USER_NOT_FOUND( "User not found" ),
		INVALID_PASSWORD( "Invalid password" );

		private final String text;

		private AuthenticationOutcome(String text) {
			this.text = text;
		}
	}

	public static class AuthenticationOutcomeBridge implements ValueBridge<AuthenticationOutcome, String> {
		@Override
		public String toIndexedValue(AuthenticationOutcome value, ValueBridgeToIndexedValueContext context) {
			return value == null ? null : value.text;
		}

		@Override
		public AuthenticationOutcome cast(Object value) {
			return (AuthenticationOutcome) value;
		}
	}

}