package r01hp.lod.urihandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.url.UrlPath;
import r01f.util.types.collections.Lists;

/**
 * The URI types
 * 	NTI: http://data.euskadi.eus/id/{Sector}/{Domain}/{ClassName}/{Identifier}
 * 	ELI: http://euskadi.eus/eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}/{version}/{pointintime}/{language}/{format}
 * 
 *	Dataset in a DCAT file: http://data.euskadi.eus/dataset/{NamedGraph}.
 *	Distribution in a DCAT file: http://data.euskadi.eus/distribution/{NamedGraph}/[lang]/format. lang is optional.
 *	Named Graph in a DCAT file or Triple Store: http://data.euskadi.eus/graph/{NamedGraph}.
 *
 *  OWL Classes: http://data.euskadi.eus/def/{OntologyName}#{ClassName}.
 *	OWL properties: http://data.euskadi.eus/def/{OntologyName}#{PropertyName}.
 *	OWL Ontology: http://data.euskadi.eus/def/{OntologyName}.
 *	SKOS Concept: http://data.euskadi.eus/kos/{ConceptName}.
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) 
public enum R01HLODURIType {
	// basic
	SPARQL			(Pattern.compile("/sparql/?.*"),  		UrlPath.from("sparql")),		// SPARQL endpoint url
	DOC				(Pattern.compile("/doc/.*"),			UrlPath.from("doc")),			// URL of a resource served as HTML by ELDA
	DATA			(Pattern.compile("/data/.*"),			UrlPath.from("data")),			// URL of a resource served as RDF by the triple-store
	
	// has representation
	ID				(Pattern.compile("/id/.*"),				UrlPath.from("id")),			// main URI type
	ELI				(Pattern.compile("/eli/.*"),			UrlPath.from("eli")),			// URL of an ELI resource
	DATASET			(Pattern.compile("/dataset/.*"),		UrlPath.from("dataset")),		// DataSet resource 		(used in DCAT files)
	DISTRIBUTION	(Pattern.compile("/distribution/.*"),	UrlPath.from("distribution")),	// DataSet distribution 	(used in DCAT files)
	GRAPH			(Pattern.compile("/graph/.*"),			UrlPath.from("graph")),			// graph
	
	SKOS			(Pattern.compile("/kos/.*"),			UrlPath.from("kos")),			// URL of a SKOS
	
	ONTOLOGY_DEF	(Pattern.compile("/def/.*"),			UrlPath.from("def"));			// URL of an ontology definition file (.owl)
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final Pattern _urlPathRegExPattern;
	@Getter private final UrlPath _pathToken;
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	public static R01HLODURIType of(final UrlPath urlPath) {
		R01HLODURIType outType = null;
		for (R01HLODURIType type : R01HLODURIType.values()) {
			Matcher m = type.getUrlPathRegExPattern()
							.matcher(urlPath.asAbsoluteString());
			if (m.matches()) {
				outType = type;
				break;
			}
		}
		if (outType == null) throw new IllegalArgumentException("The url path token '" + urlPath + "' is NOT one of the handled types: " + Lists.newArrayList(R01HLODURIType.values()));
		return outType;
	}
}