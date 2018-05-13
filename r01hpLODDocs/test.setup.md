# CREATE THE BLAZEGRAPH euskadi_db NAMESPACE:


1.- Goto the console of the [writable Blazegraph]:
		http://localhost/write/triplestore

2.- Create the database

		a) Goto [namespaces] tab: http://localhost/write/triplestore/#namespaces
		b) Fill the [create namespace] data:
				Name: euskadi_db
				Mode: quads    <--- very important
				Isolatable indices: checked
				   Full text index: checked
				 Enable geospatial: checked
		c) Set the [in-use namespace] to [euskadi_db]  

# LOAD TEST DATA

1.- Goto the console of the [writable Blazegraph]:
		http://localhost/write/triplestore 		   

2.- Goto [UPDATE]: http://localhost/write/triplestore/#update

3.- Using the [file browse] utility upload all files at [test-data] folder:
    (the data format is auto-detected)

		- ejgv.ttl:     [Euskadi] resource
		- eli.ttl:      An [eli] resource (the basque autonomous region constitution law)
		- eli-dcat.rdf: The [eli] dataset [DCAT]

# TEST
a) goto the [triple store] sparql endpoint
		http://localhost/write/triplestore/#query

b) Paste the query and exec:
```sparql
	DESCRIBE <http://data.euskadi.eus/id/public-sector/government/GovernmentalAdministrativeRegion/euskadi>
```

... should return the previously inserted data for [euskadi] resource				
