URI policy test
==================================================================

## Uri Types
Euskadi.eus has normalized several URI types:
### Spanish NTI-like URIs
(NTI stands for "Norma tablecnica de Interoperabilidad = Interop Technical Norm")  
`http://data.euskadi.eus/id/{Sector}/{Domain}/{ClassName}/{Identifier}`

	  			{Sector}: one of the sectors provided by the NTI (e.g. environment),
	  			{Domain}: the realm to which the resource belongs, defined by Open Data Euskadi (e.g. air-quality)
	  			{ClassName}: the name of the class to which this resource belongs
	  			{Identifier}: a unique identifier, generated from the original data

	  		Example: http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi

### ELI: European Legislation Identifier   
Legislation resources can roughly be defined in three levels
  - Legislation Resource:`/eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}`
	- Version: `/{version}/{pointintime}/{language}`
		- Format `{format}`

where:

	{jurisdiction}: the territory
	{type}: the legislation type (e.g.; law)
	{year}/{month}/{day}: yyyy/mm/dd
	{naturalIdentifier}:
	{version};
	{pointintime}
	{language}: i.e. "eus" for basque, "spa" for spanish

so URIs can be like:
- Legislation resouce: `http://data.euskadi.eus/eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}`
  - Version: `http://data.euskadi.eus/eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}/{version}/{pointintime}/{language}/`
    - Format: `http://data.euskadi.eus/eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}/{version}/{pointintime}/{language}/{format}`

### DataSet:
	http://data.euskadi.eus/dataset/{NamedGraph}

### DataSet distribution
	http://data.euskadi.eus/distribution/{NamedGraph}/[lang]/format

### Named graph
	http://data.euskadi.eus/graph/{NamedGraph}


## Resource URIs and Representation URLs			

In general, a distinction is made between [resource URIs] and their [representation]
- A [resource URI] uniquely designates a resource
- A [representation URL] is an URL for a one of the multiple resource's representation, be it RDF, HTML, JSON, etc.

A [resource]'s [representation] can be accessed by two means;
- Using the [resource URI] + content negotiation
- Using the [resource]'s [representation URL]

How each of the above URIs is handled is explained bellow

The [URI]s can be grouped in:
- [resources] that CAN have a WEB [representation] (i.e.: can have an associated web page):
  - Spanish NTI-like URIs
  - ELI: European Legislation Identifier
  - Datasets

- [resources] that DO NOT have a WEB [representation]:
  - Dataset distributions
  - Graphs

- Other URIs

[resources] that CAN have a WEB [representation]
------------------------------------------------
The URI is handled differently whether the requested MIME is HTML or RDF (or turtle, or whatever)
- If the requested MIME is HTML, the URI can belong to a [resource] that has an associated web page   
... to guess if the [resource] has an associates web page, query the [triple-store] to check the main-entity-of-page attribute
   - if main-entity-of-page exists, a CLIENT REDIR to {mainEntityOfPage} is issued
   - if main-entity-of-page DOES NOT exists, it's an entity that DOES NOT have an associated web page so the [triple-store] data is "painted" in HTML format by ELDA: a CLIENT REDIR to /doc/{resource} is issued

- If the requested MIME is RDF, the URI is for a [triple-store] data so a CLIENT REDIR to /data/{resource} is issued

            ^
            |                                                      +------------------+
            |                                                      |  /id/{resource}  |
        Resource                                                   +--------+---------+
          URIs                                                              |
            |                     Is main entity                            |
            |                     of page?------------MIME=HTML-------------+-------MIME=RDF--------+
            |                            |                                                          |
            |              +-----NO------+-----YES-----+                                            |
            |              |                           |                                            |
            |              |                           |                                            |
            |              |                           |                                            |
        +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
            |              |                           |                                           |
            |   +----------v---------+       +---------v----------+                     +----------v----------+
            |   | /doc/id/{resource} |       |     {web page}     |                     | /data/id/{resource} |
            |   +--------+-----------+       +---------+----------+                     +----------+----------+
            |            |                             |                                           |
      Representation     |                             |                                      /id/{resource}    
          URLs           |                             |                                           |
            |   +--------v---------+         +---------v--------+                         +--------v--------+
            |   |                  |         |                  |                         |                 |
            |   |      ELDA        |         |        Web       |                         |   Triple-Store  |
            |   |                  |         |                  |                         |                 |
            v   +------------------+         +------------------+                         +--------^--------+
                         |                                                                         |
                         +--------------------------  /id/{resource}  -----------------------------+

## Testing:
Content negotiation:
* [CASE 1]
				  URL: http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
		Accept header: html
	  Expected result: since the [resource] has an associated [web page] (www.euskadi.eus), a CLIENT-REDIR
	  				   to www.euskadi.eus is issued

	  			 CURL: 	curl -X GET http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi \
  							 -H 'accept: application/xhtml+xml' \

			   Result: 	euskadi.eus web page
* [CASE 2]:
				  URL: http://data.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof
		Accept header: html
	  Expected result: since the [resource] DOES NOT hava an associated [web page] the only option is to render
	  				   the [triple-store] data as HTML using [ELDA] so a CLIENT-REDIR to
	  				   http://data.euskadi.eus/do/eli/es-pv/l/1979/03/20/(0)/dof is issued

				 CURL: 	curl -X GET http://data.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof \
  						     -H 'accept: application/xhtml+xml'
			   Result: An [ELDA]-generated web page for the [resource]

* [CASE 3]
				  URL: http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
		Accept header: rdf
	  Expected result: a CLIENT-REDIR to the [triple-store] date is issued: http://localhost/data/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi
	  				   ... and the RDF data is returned

				 CURL: 	curl -X GET http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi \
  							 -H 'accept: application/rdf+xml'
			   Result: 	The [resource] in RDF format
* [CASE 4]
				URL: http://data.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof
		Accept header: rdf
	  Expected result: a CLIENT-REDIR to the [triple-store] date is issued: http://localhost/eli/es-pv/l/1979/03/20/(0)/dof
	  				   ... and the RDF data is returned

				 CURL: 	curl -X GET http://data.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof \
  							 -H 'accept: application/rdf+xml'
			   Result: 	The [resource] in RDF format
