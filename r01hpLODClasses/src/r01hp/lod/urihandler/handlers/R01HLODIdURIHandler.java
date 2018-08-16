package r01hp.lod.urihandler.handlers;

import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForClientRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODResourceType;
import r01hp.lod.urihandler.R01HLODURIHandlerEngine;
import r01hp.lod.urihandler.R01HMIMEType;


/**
 * Handles uris that have a web representation like:
 * 		- Dataset in a DCAT file: <pre>http://idSite/dataset/{NamedGraph}</pre>
 * 		- ELI resources: <pre>http://idSite/{eliResource}</pre>
 * 		- Any resource: <pre>http://idSite/{resource}</pre>
 * 
 * These URIs ALLWAYS are resolved to a CLIENT-REDIRs depending on the MIME (see {@link R01HLODURIHandlerEngine})
 * <pre>
 *                     ^
 *                     |                                                  +--------------------------+
 *                     +                                                  | http://idsite/{resource} |
 *                 Resource                                               +-------------+------------+
 *                   URIs                                                               |
 *                     +                             Is main entity                     |
 *                     |                           +---+ of page? +-----+MIME=HTML+-----+-----+MIME=RDF+--------+
 *                     |                           |                                                            |
 *                     |              +------------+-------------+                                              |
 *                     |              |                          |                                              |
 *                     |              |                          |                                              |
 *                     |              |                          |                                              |
 *                 +---------------------------------------------------+[***** CLIENT REDIR ****]+----------------------------------+
 *                     |              |                          |                                              |
 *                                    |                          |                                              |
 *                     |   +----------v--------------+ +---------v---------------+                 +------------v-------------+
 *                     |   |http://docsite/{resource}| |http://website/{resource}|                 |http://datasite/{resource}|
 *                     +   +----------+--------------+ +---------+---------------+                 +------------+-------------+
 *                                    |                          |                                              |
 *               Representation       |                          |                                              |
 *                   URLs             |                          |                                              |
 *                     +       +------v-------+        +---------v--------+                            +--------v--------+
 *                     |       |              |        |                  |                            |                 |
 *                     |       |     ELDA     |        |        Web       |                            |   Triple+Store  |
 *                     |       |              |        |                  |                            |                 |
 *                     v       +--------------+        +------------------+                            +-----------------+
 * </pre> 
 * 
 * The URI is handled differently whether the requested MIME is HTML or RDF (or turtle, or whatever)
 * 	- If the requested MIME is HTML, the URI can belong to a [resource] that has an associated web page 
 * 	  ... to guess if the [resource] has an associates web page, query the [triple-store] to check the main-entity-of-page attribute
 * 			- if main-entity-of-page exists, a CLIENT REDIR to {mainEntityOfPage} is issued
 * 			- if main-entity-of-page DOES NOT exists, it's an entity that DOES NOT have an associated web page so the [triple-store] data 
 * 			  is "painted" in HTML format by ELDA: a CLIENT REDIR to http://docSite/{resource} is issued
 *
 * 	- If the requested MIME is RDF, the URI is for a [triple-store] data so a CLIENT REDIR to http://dataSite/{resource} is issued
 * 
 * All the above also means that:
 * 		- All http://docSite/{resource} calls MUST have a MIME=HTML (although ELDA supports other MIMES)
 * 		- All http://dataSite/{resource} calls MUST hava a MIME=RDF 
 * 
 * BEWARE that there are URIs that have NOT a web representation like:
 * 		- dataset distribution in a DCAT file: <pre>http://idSite/distribution/{NamedGraph}/[lang]/format</pre>
 * 		- graph: http://idSite/graph/{graph}
 * 
 * These URIs ALLWAYS are resolved to a CLIENT-REDIRs depending on the MIME (see {@link R01HLODURIHandlerEngine})
 * <pre>
 *        ^
 *        |
 *        +
 *    Resource                    +------------------------+
 *      URIs                      |http://idsite/{resource}|
 *        +                       +-------------+----------+
 *        |                                     |
 *        |                                     |
 *        |                  +----+MIME=HTML+---+---+MIME=RDF+----+
 *        |                  |                                    |
 *        |                  |                                    |
 *        |     +--------------+[*****+CLIENT REDIR ****]+------------+
 *        +                  |                                    |
 *                           |                                    |
 * +--------------------------------------------------------------------------+
 *                           |                                    |
 *        +   +--------------v-------------+       +--------------v--------------+
 *        |   |                            |       |                             |
 *        |   |  http://docsite/{resource} |       |  http://datasite/{resource} |
 *        |   |                            |       |                             |
 *        +   +--------------+-------------+       +--------------+--------------+
 * Representation            |                                    |
 *      URLs                 |                             ¿ {resource} ?
 *        +                  |                                    |
 *        |         +--------v--------+                 +---------v---------+
 *        |         |                 |                 |                   |
 *        |         |       ELDA      |                 |   TRIPLE+STORE    |
 *        |         |                 |                 |                   |
 *        v         +---------+-------+                 +---------^---------+
 *                            |                                   |
 *                            +--------+¿ {resource ?+------------+
 * 
 * </pre>
 */
public class R01HLODIdURIHandler 
	   extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELD
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HLODMainEntityOfPageResolver _mainEntityOfPageResolver;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODIdURIHandler(final R01HLODURIHandlerConfig config,
							   final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver) {
		super(config);
		_mainEntityOfPageResolver = isMainEntityOfPageResolver;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		R01HLODHandledURIData outUriData = null;
		switch(data.getResourceType()) {
		case ONTOLOGY_DEF:
			outUriData = _handleOntolotyDefinition(data);
			break;
		case SKOS:
			break;
		// resources that DO NOT have a web representation for sure
		case DISTRIBUTION:
		case GRAPH:
		case PROPERTY:
		case ELI:
		case DATASET:
		default:
			outUriData = _handle(data);
			break;
		}
		return outUriData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private R01HLODHandledURIData _handle(final R01HLODRequestedURIData data) {
		// [1] The ACCEPT header is HTML:
		if (data.isRequestingHTMLFromMimes()) {
			if (data.getResourceType().isIn(R01HLODResourceType.DISTRIBUTION,
										    R01HLODResourceType.GRAPH,
										    R01HLODResourceType.PROPERTY)) {
				// NO web representatcion: there's NO need to issue a {main entity of page}? query
				// ...Client redir to elda
				return new R01HLODHandledURIDataForClientRedirect(_config.getDocSite(),
																  data.getRequestedResourceUrlPath(), 
																  data.getRequestQueryString(),
																  data.getRequestedUrlAnchor());
			} 
			else {
				// The resource CAN have a web representation: issue a {main entity of page}? query
				// ...check if the resource has an associated web page
				Url webPageUrl = _mainEntityOfPageResolver.mainEntityOfPage(Url.from(data.getHost(),data.getPort(),
																			   		 data.getRequestedResourceUrlPath()));
				if (webPageUrl != null) {
					// Client redirect to page
					return new R01HLODHandledURIDataForClientRedirect(webPageUrl);
				} else {
					// Client redir to elda
					return new R01HLODHandledURIDataForClientRedirect(_config.getDocSite(),
																	  data.getRequestedResourceUrlPath(), 
																	  data.getRequestQueryString(),
																	  data.getRequestedUrlAnchor());
				}
			}
		}
		// [2] The accept header is RDF
		else {
			// client redir to the [triple-store]
			return new R01HLODHandledURIDataForClientRedirect(_config.getDataSite(),
															  data.getRequestedResourceUrlPath(), 
															  data.getRequestQueryString(),
															  data.getRequestedUrlAnchor());
		}
	}
	private R01HLODHandledURIData _handleOntolotyDefinition(final R01HLODRequestedURIData data) {
		// Get the ontology name from the url:
		//		http://site/def/{ontologyName}
		String ontologyName = data.getRequestedResourceUrlPath().getPathElementAt(0);
		

		if (ontologyName.endsWith(".owl")
		 || data.isAcceptingMime(R01HMIMEType.RDFXML)) {
			// server redirect to /owl/{ontology}
			return new R01HLODHandledURIDataForServerRedirect(UrlPath.from("/owl/" + ontologyName),
																	  data.getRequestQueryString(),
																	  R01HMIMEType.RDFXML);
		} else if (ontologyName.endsWith(".html")
		 || data.isRequestingHTMLFromMimes()) {
			// client redirect to /pages/{ontology}
			return new R01HLODHandledURIDataForClientRedirect(_config.getDataSite(),
															  UrlPath.from("/pages/" + ontologyName),
															  data.getRequestQueryString(),
															  data.getRequestedUrlAnchor());
		} else {
			throw new IllegalArgumentException("NOT a valid ontology request!");
		}
	}
}
