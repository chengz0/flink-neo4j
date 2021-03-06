/**
 * 
 */
package org.apache.flink.streaming.connectors.neo4j;

import java.util.Map;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.mapping.neo4j.Neo4JSerializationMappingStrategy;
import org.apache.flink.mapping.neo4j.SerializationMapper;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alberto De Lazzari
 *
 */
public class Neo4JSource<T> extends RichSourceFunction<T> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JSource.class);

	protected transient Neo4JDriverWrapper driver;

	/**
	 * The mapping strategy that we use to map data from Flink to Neo4J
	 * 
	 * @see Neo4JSerializationMappingStrategy
	 */
	private Neo4JSerializationMappingStrategy<T, SerializationMapper<T>> mappingStrategy;

	private Map<String, String> config;

	/**
	 * Default source constructor
	 * 
	 * @param mappingStrategy
	 *            a mapping strategy that will be used to map stream data from
	 *            Neo4J data
	 * @param config
	 *            the connection configurations for Neo4J
	 */
	public Neo4JSource(final Neo4JSerializationMappingStrategy<T, SerializationMapper<T>> mappingStrategy,
			final Map<String, String> config) {
		this.mappingStrategy = mappingStrategy;
		this.config = config;
	}

	/**
	 * Initialize the connection to Neo4J. As the Elasticsearch connector we can
	 * use and embedded instance or a cluster
	 */
	@Override
	public void open(Configuration parameters) throws Exception {
		driver = new Neo4JDriverWrapper(this.config);
	}

	@Override
	public void cancel() {
		driver.close();
	}

	@Override
	public void run(SourceContext<T> sourceContext) throws Exception {
		Statement queryStatement = mappingStrategy.getStatement();
		Session session = driver.session();

		// We should use a statement with parameters
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("running statement {}", queryStatement.text());
		}
		StatementResult result = session.run(queryStatement);
		while (result.hasNext()) {
			Record record = result.next();

			T item = mappingStrategy.map(record);
			sourceContext.collect(item);
		}

		session.close();
	}
}