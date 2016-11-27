/**
 * 
 */
package org.apache.flink.streaming.connectors.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.connectors.neo4j.mapper.Neo4JSourceMappingStrategy;
import org.apache.flink.streaming.connectors.neo4j.mapper.SerializationMapper;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;

/**
 * @author Alberto De Lazzari
 *
 */
public class Neo4JSourceMock<T> extends Neo4JSource<T> {

	private static final long serialVersionUID = 1L;

	public Neo4JSourceMock(final Neo4JSourceMappingStrategy<T, SerializationMapper<T>> mappingStrategy,
			final Map<String, String> config) {
		super(mappingStrategy, config);
	}

	@Override
	public void open(Configuration parameters) throws Exception {
		Map<String, String> mockConfig = new HashMap<String, String>();
		mockConfig.put(Neo4JDriverWrapper.USERNAME_PARAM, "user");
		mockConfig.put(Neo4JDriverWrapper.PASSWORD_PARAM, "password");
		mockConfig.put(Neo4JDriverWrapper.URL, "localhost");
		driver = new Neo4JDriverWrapperMock(mockConfig);

		Session session = Mockito.mock(Session.class);
		Mockito.when(driver.session()).thenReturn(session);
		StatementResult result = Mockito.mock(StatementResult.class);
		Mockito.when(result.hasNext()).thenReturn(Boolean.TRUE);
		Mockito.when(result.next()).thenAnswer(new Answer<Record>() {

			@Override
			public Record answer(InvocationOnMock invocation) throws Throwable {
				Mockito.when(result.hasNext()).thenReturn(Boolean.FALSE);
				Record record = Mockito.mock(Record.class);

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("i.description", "an item");
				Mockito.when(record.asMap()).thenReturn(map);

				return record;
			}
		});
		Mockito.when(session.run(Mockito.any(Statement.class))).thenReturn(result);
	}
}
