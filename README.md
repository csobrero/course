== Spring Boot Neo4j JDBC - Movies Example Application

Even if http://neo4j.org[Neo4j] is all about graphs, its graph query language http://neo4j.org/learn/cypher[Cypher] is well suited to be used with JDBC (Java Database Connectivity).
As you probably know, JDBC is a common way to connect to a datastore, especially since there
are a lot of tooling and connectors written around it in the Business Intelligence, Data Migration, and ETL world.

The Neo4j JDBC driver works with Neo4j Server in version 2.x and with embedded and in-memory databases.
It allows you to (transactionally) execute parametrized Cypher statements against your Neo4j database to either create,
query or update data.

Here we integrate Neo4j-JDBC with Spring-Boot and the traditional Spring-JDBC (JDBCTemplate) and Spring Web-MVC to create the backend.


=== The Stack

These are the components of our mini Web Application:

* Application Type:         Java-Web Application
* Web framework:            Spring-Boot with Spring-WebMVC
* Persistence Access:       Spring-JDBC
* Neo4j Database Connector: https://github.com/neo4j-contrib/neo4j-jdbc#minimum-viable-snippet[Neo4j-JDBC] with Cypher
* Database:                 Neo4j-Server
* Frontend:                 jquery, bootstrap, http://d3js.org/[d3.js]

=== Endpoints:

Get Movie

----
// JSON object for single movie with cast
curl http://neo4j-movies.herokuapp.com/movie/The%20Matrix

// list of JSON objects for movie search results
curl http://neo4j-movies.herokuapp.com/search?q=matrix

// JSON object for whole graph viz (nodes, links - arrays)
curl http://neo4j-movies.herokuapp.com/graph
----
