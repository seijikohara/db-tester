// Neo4j Integration Test Schema Setup
// Creates nodes that can be queried via JDBC in tabular format

// Clean up existing data
MATCH (n:TABLE1) DELETE n;

// Note: Neo4j stores data as nodes with properties
// The JDBC driver returns Cypher query results as JDBC ResultSets
// Labels like :TABLE1 allow organizing nodes similarly to tables
