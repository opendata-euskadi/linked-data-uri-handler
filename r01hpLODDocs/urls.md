
TEST: PRE-REQUISITES:
================================================================================================
see test.setup.read.me

[TripleStore console]
================================================================================================
(for BLAZEGRAPH)


                        |
               http:/site/read/blazegraph
                        |         
            +-----------v-----------+
            |                       |
            |          WEB          |
            +-----------+-----------+
                      proxy
                        |
                        | http:/wlhost:wlport/r01hpLODWar/blazegraph
                        |
            +-----------v-----------+
            |APP SERVER             |
            |   +----------------+  |
            |   |    LOD WAR     |  |
            +-----------+-----------+
                      proxy
                        |
                        | http:/triplestorehost:triplestoreport/blazegraph
                        |
            +-----------v------------+
            |                        |
            |       TripleStore      |
            +------------------------+

	Using the web browser
		Through the web server:
			LOCAL: http://localhost/read/triplestore/    or     http://localhost/write/triplestore/
			 DESA: http:/data.euskadi.ejiedes.eus/read/triplestore/     or   http:/data.euskadi.ejiedes.eus/write/triplestore/
			 PROD: http:/data.euskadi.eus/read/triplestore/    or    http:/data.euskadi.eus/write/triplestore/

		Skipping the web server: request the app server
			LOCAL: http://localhost:8080/r01hpLODWar/read/blazegraph/   or   http://localhost:8080/r01hpLODWar/write/blazegraph/
		 	 DESA: http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/read/blazegraph/     or   http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/write/blazegraph/

		Request the [triple-store] server (Skipping the web server and app server)
			LOCAL: http://localhost:9999/blazegraph/
			 DESA:  Read: http:/(ejld1255|ejld1256):(18080|28080)/blazegraph/ 			(4 blazegraph servers, 2 for each host)
			 	   Write: http:/www.integracion.jakina.ejiedes.net/blazegraph/


[SPARQL GUI: YASGUI]
================================================================================================

```
                |
         http:/site/sparql/    (MIME=HTML)
                |
	+-----------------------+
	|                       |
	|          WEB          |
	+-----------+-----------+
	          proxy
	            |
	            | http:/wlhost:wlport/r01hpLODWar/sparql
	            |
	+-----------v-----------+
	|APP SERVER             |
	|   +----------------+  |
	|   |    LOD WAR     |  |
	|   |       |        |  |
	|   |   server redir |  |
	|   |       |        |  |
	|   /_pages/yasgui.jsp  |
	|   +----------------+  |
	+-----------------------+
```

	Using the web browser
		Through the web server:
		    LOCAL: http://localhost/sparql
			 DESA: http:/data.euskadi.ejiedes.eus/sparql/
			 PROD: http:/data.euskadi.eus/sparql/

		Skipping the web server: request the app server
		    LOCAL: http://localhost:8080/r01hpLODWar/sparql
			 DESA: http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/sparql/			

[SPARQL QUERY]
================================================================================================
```
                |
         http:/site/sparql/    (MIME=RDF)
                |
	+-----------------------+
	|                       |
	|          WEB          |
	+-----------+-----------+
	          proxy
	            |
	            | http:/wlhost:wlport/r01hpLODWar/sparql
	            |
	+-----------v-----------+
	|APP SERVER             |
	|   +----------------+  |
	|   |    LOD WAR     |  |
	+-----------+-----------+
	          proxy
	            |
	            | http:/triplestorehost:triplestoreport/blazegraph/namespace/euskadi_db/sparql
	            |
	        +---v----+
	        | SPARQL |
	+------------------------+
	|                        |
	|       TripleStore      |
	+------------------------+
```

	Using POSTMAN with Accept=application/rdf+xml  GET or POST with BODY TYPE = x-wwww-form-urlencoded
		directly querying the [triple-store]
			LOCAL: http://localhost:9999/blazegraph/namespace/euskadi_db/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
			 DESA: Read servers: http:/(ejld1255|ejld1256):(18080|28080)/blazegraph/namespace/euskadi_db/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
			 	   Write server: http:/www.integracion.jakina.ejiedes.net/blazegraph/namespace/euskadi_db/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>


		Skipping the web server: request the app server
			LOCAL: http://localhost:8080/r01hpLODWar/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
			 DESA: http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
			 	   ... or after the LOD URIHandler filter:
			 	   http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/read/blazegraph/namespace/euskadi_db/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>

		Through the web server
			LOCAL: http://localhost/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
			 DESA: http:/data.euskadi.ejiedes.eus/sparql/?query=DESCRIBE <http:/data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>


[ELDA GUI: /DOC]		
================================================================================================
```
                |
      http:/site/doc/{resource}    (MIME=HTML)
                |
	+-----------------------+
	|                       |
	|          WEB          |
	+-----------+-----------+
	          proxy
	            |
	            | http:/wlhost:wlport/r01hpLODWar/doc/{resource}
	            |
	+-----------v-----------+
	|APP SERVER             |
	|   +----------------+  |
	|   |    LOD WAR     |  |
	|   |       |        |  |
	|   | chain.doFilter |  |
	|   |   +---------+  |  |
	|   |   |  ELDA   |  |  |
	|   |   +---------+  |  |
	|   +----------------+  |
	+-----------------------+
```

	Using the web browser
		Through the web server
			LOCAL: http://localhost/doc/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
			 DESA: http:/data.euskadi.ejiedes.eus/doc/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi

		Skipping the web server: request the app server
			LOCAL: http://localhost:8080/r01hpLODWar/doc/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
			 DESA: http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/doc/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi


[TripleStore Resource: /DATA]
================================================================================================
```
                |
     http:/site/data/{resource}    (MIME=RDF)
                |         
	+-----------v-----------+
	|                       |
	|          WEB          |
	+-----------+-----------+
	          proxy
	            |
	            | http:/wlhost:wlport/r01hpLODWar/data/{resource}
	            |
	+-----------v-----------+
	|APP SERVER             |
	|   +----------------+  |
	|   |    LOD WAR     |  |
	+-----------+-----------+
	          proxy
	            |
	            | http:/triplestorehost:triplestoreport/blazegraph/namespace/euskadi_db/sparql?query=DESCRIBE <{resource}>
	            |
  +-----------v------------+
	|                        |
	|       TripleStore      |
	+------------------------+
```

	Using POSTMAN with Accept=application/rdf+xml GET
		Through the web server
		    LOCAL: http://localhost/data/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
			 DESA: http:/data.euskadi.ejiedes.eus/data/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi

		Skipping the web server: request the app server
		    LOCAL: http://localhost:8080/r01hpLODWar/data/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
			 DESA: http:/wl11vf00(37|39).ejiedes.net:7001/r01hpLODWar/data/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi

ID URIs
================================================================================================
```
		             ^
		             |                                                      +------------------+
		             |                                                      |  /id/{resource}  |
		         Resource                                                   +--------+---------+
		           URIs                                                              |
		             |                     Is main entity                            |
		             |                     of page?------------MIME=HTML-------------+-------MIME=RDF---------+
		             |                          |                                                             |
		             |            +-----NO------+------YES-----+                                              |
		             |            |                            |                                              |
		             |            |                            |                                              |
		             |            |                            |                                              |
		         +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
		             |            |                            |                                              |
		             |   +--------v--------+         +---------v---------+                           +--------v--------+
		             |   | /doc/{resource} |         | {mainEntityOfPage}|                           | /data/{resource}|
		             |   +--------+--------+         +---------+---------+                           +--------+--------+
		                          |                            |                                              |
		       Representation     |                           303                                             |
		           URLs           |                            |                                              |
		             |   +--------v---------+        +---------v--------+                            +--------v--------+
		             |   |                  |        |                  |                            |                 |
		             |   |      ELDA        |        |        Web       |                            |   Triple-Store  |
		             |   |                  |        |                  |                            |                 |
		             v   +------------------+        +------------------+                            +-----------------+
```

	TEST 1: /id/{resource} with MIME=RDF > should do a [client-redir] to /data/{resource} and return an RDF result
		Using POSTMAN with Accept=application/rdf+xml GET
			LOCAL: http://localhost/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
		     DESA: http:/data.euskadi.ejiedes.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi

	TEST 2: /id/{resource} with MIME=HTML > should do a [client-redir] to /doc/{resource} (elda)
		Using the web browser:
			LOCAL: http://localhost/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
		     DESA: http:/data.euskadi.ejiedes.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
