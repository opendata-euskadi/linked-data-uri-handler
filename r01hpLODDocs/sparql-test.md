SPARQL test
==================================================================
The [SPARQL] endpoint is:
	http://data.euskadi.eus/sparql

The [SPARQL] endpoint can be queried via GET or POST and the expected result
differs depending on the accedpted mime-type (the Accept HTTP header)

[SPARQL] endpoint GUI
==================================================================
If [accept] header is HTML (for example) using a web browser, a [SPARQL] GUI is presented
Using this GUI anyone can issue [SPARQL] queries to the [triple-store]

Test:

a) using a web browser enter: ` http://api.euskadi.eus/sparql/ `

b) Enter the following [SPARQL] query:

```sparql
		DESCRIBE <http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
```

The expected result is (Prefixes not shown for brevity):

		<http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi> <http://schema.org/mainEntityOfPage> <http://www.euskadi.eus> ;
			owl:sameAs <http://datos.gob.es/recurso/sector-publico/territorio/Autonomia/Pais-Vasco> .

[SPARQL] endpoint as a service
==================================================================
If [accept] header is RDF (or turtle or whatever), the query is just directed (proxied) to the [triple-store]
[SPARQL] endpoint and executed

Tes: Using an HTTP client GUI, for example POSTMAN or CURL

If using GET http method

	          Url: http://api.euskadi.eus/sparql/?query=DESCRIBE <http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
	       Method: GET
	Accept header: Accept=rdf


			 CURL: 
```	
			 curl -X GET 'http://api.euskadi.eus/sparql/?query=DESCRIBE <http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi>' \
					     -H 'accept: application/rdf+xml'
```
		   Result: 	An RDF representation of the query result

If using POST http method

	          Url: http://api.euskadi.eus/sparql/
	       Method: POST
	Accept header: Accept=rdf
	         Body: form url encoded
	         	   query=DESCRIBE <http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi>			   

			 CURL: 	
```
			 	curl -X POST http://api.euskadi.eus/sparql/ \
					     -H 'accept: application/rdf+xml' \
						 -H 'content-type: application/x-www-form-urlencoded' \
						 -d query=DESCRIBE <http://id.euskadi.eus/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
```
		   Result: 	An RDF representation of the query result
