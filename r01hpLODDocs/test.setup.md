# CREATE THE BLAZEGRAPH euskadi_db NAMESPACE:


1.- Goto the console of the [writable Blazegraph]:
		http://api.localhost/write/triplestore/

2.- Create the database

		a) Goto [namespaces] tab: http://api.localhost/write/triplestore/#namespaces
		b) Fill the [create namespace] data:
				Name: euskadi_db
				Mode: quads    <--- very important
				Isolatable indices: checked
				   Full text index: checked
				 Enable geospatial: checked
		c) Set the [in-use namespace] to [euskadi_db]  

# LOAD TEST DATA

1.- Goto the console of the [writable Blazegraph]:
		http://api.localhost/write/triplestore 		   

2.- Goto [UPDATE]: http://api.localhost/write/triplestore/#update

3.- Using the [file browse] utility upload all files at [test-data] folder:
    (the data format is auto-detected)

		- ejgv.ttl:     [Euskadi] resource
		- eli.ttl:      An [eli] resource (the basque autonomous region constitution law)
		- eli-dcat.rdf: The [eli] dataset [DCAT]

# TEST
a) goto the [triple store] sparql endpoint
		http://api.localhost/write/triplestore/#query

b) Paste the query and exec:
```sparql
	DESCRIBE <http://id.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
```

... should return the previously inserted data for [euskadi] resource				

subject	predicate	object	context
<http://id.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>	schema:mainEntityOfPage	<http://www.euskadi.eus>	
<http://id.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>	owl:sameAs	<http://datos.gob.es/recurso/sector-publico/territorio/Autonomia/Pais-Vasco>	
