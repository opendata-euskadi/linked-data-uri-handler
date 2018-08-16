package r01hp.lod.urihandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;
import r01f.types.url.UrlPath;
import r01f.util.types.collections.Lists;

/**
 * The Resource types
 * 		NTI: /{Sector}/{Domain}/{ClassName}/{Identifier}
 * 		ELI: /eli/{jurisdiction}/{type}/{year}/{month}/{day}/{naturalidentifier}/{version}/{pointintime}/{language}/{format}
 *		
 *		Dataset in a DCAT file: /dataset/{NamedGraph}.
 *		Distribution in a DCAT file: /distribution/{NamedGraph}/[lang]/format. lang is optional.
 *		Named Graph in a DCAT file or Triple Store: /graph/{NamedGraph}.
 *
 *  	OWL Classes: /def/{OntologyName}#{ClassName}.
 *		OWL properties: /def/{OntologyName}#{PropertyName}.
 *		OWL Ontology: /def/{OntologyName}.
 *		SKOS Concept: /kos/{ConceptName}.
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) 
public enum R01HLODResourceType 
 implements EnumExtended<R01HLODResourceType> {
	READ_TRIPLE_STORE	(Pattern.compile("/read/triplestore/?.*"),	UrlPath.from("read/triplestore")),
	WRITE_TRIPLE_STORE	(Pattern.compile("/write/triplestore/?.*"),	UrlPath.from("write/triplestore")),
	
	DATASET			(Pattern.compile("/dataset/.+"),			UrlPath.from("dataset")),		// DataSet resource 		(used in DCAT files)
	DISTRIBUTION	(Pattern.compile("/distribution/.+"),		UrlPath.from("distribution")),	// DataSet distribution 	(used in DCAT files)
	GRAPH			(Pattern.compile("/graph/.+"),				UrlPath.from("graph")),			// graph
	PROPERTY		(Pattern.compile("/property/.+"),			UrlPath.from("property")),		// object's property
	
	ELI				(Pattern.compile("/eli/.+"),				UrlPath.from("eli")),			// ELI resource
	NTI				(Pattern.compile("/[^/]+/[^/]+/[^/]+/.+"),  null),
	
	SKOS			(Pattern.compile("/kos/.+"),				UrlPath.from("kos")),			// URL of a SKOS
	
	ONTOLOGY_DEF	(Pattern.compile("/def/.+"),				UrlPath.from("def")),			// URL of an ontology definition file (.owl)
	
	SPARQL			(Pattern.compile("/sparql/?.*"),			UrlPath.from("sparql"));		// spaql query
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Pattern _urlPathRegExPattern;
	@Getter private final UrlPath _pathToken;
	
	private static final EnumExtendedWrapper<R01HLODResourceType> DELEGATE = EnumExtendedWrapper.wrapEnumExtended(R01HLODResourceType.class);
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	public static R01HLODResourceType of(final UrlPath urlPath) {
		R01HLODResourceType outType = null;
		for (R01HLODResourceType type : R01HLODResourceType.values()) {
			Matcher m = type.getUrlPathRegExPattern()
							.matcher(urlPath.asAbsoluteString());
			if (m.matches()) {
				outType = type;
				break;
			}
		}
		if (outType == null) throw new IllegalArgumentException("The url path token '" + urlPath + "' is NOT one of the handled types: " + Lists.newArrayList(R01HLODResourceType.values()));
		return outType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isIn(final R01HLODResourceType... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01HLODResourceType el) {
		return DELEGATE.is(this,el);
	}
}