package r01hp.lod.urihandler.handlers;

import r01f.types.url.Url;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForClientRedirect;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolverByDefault;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODURIHandlerEngine;
import r01hp.lod.urihandler.R01HLODURIType;

/**
 * Handles uris like: <pre>/id/{resource}</pre>: uris that has a representation
 * These URIs ALLWAYS are resolved to a CLIENT-REDIRs depending on the MIME (see {@link R01HLODURIHandlerEngine})
 * <pre>
 *             ^
 *             |                                                      +------------------+
 *             |                                                      |  /id/{resource}  |
 *         Resource                                                   +--------+---------+
 *           URIs                                                              |
 *             |                     Is main entity                            |
 *             |                     of page?------------MIME=HTML-------------+-------MIME=RDF--------+
 *             |                            |                                                          |
 *             |              +-----NO------+-----YES-----+                                            |
 *             |              |                           |                                            |
 *             |              |                           |                                            |
 *             |              |                           |                                            |
 *         +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
 *             |              |                           |                                           |
 *             |   +----------v---------+       +---------v----------+                     +----------v----------+
 *             |   | /doc/id/{resource} |       |     {web page}     |                     | /data/id/{resource} |
 *             |   +---------+----------+       +---------+----------+                     +----------+----------+
 *                           |                             |                                          |
 *       Representation      |                             |                                    ¿ /id/{resource} ?   
 *           URLs            |                             |                                          |
 *             |    +--------v---------+         +---------v--------+                        +--------v--------+
 *             |    |                  |         |                  |                        |                 |
 *             |    |      ELDA        |         |        Web       |                        |   Triple-Store  |
 *             |    |                  |         |                  |                        |                 |
 *             v    +------------------+         +------------------+                        +--------^--------+
 *                          |                                                                        |
 *                          +--------------------------¿ /id/{resource} ?----------------------------+
 *       
 *      
 * </pre>
 * 
 * The URI is handled differently whether the requested MIME is HTML or RDF (or turtle, or whatever)
 * 	- If the requested MIME is HTML, the URI can belong to a [resource] that has an associated web page 
 * 	  ... to guess if the [resource] has an associates web page, query the [triple-store] to check the main-entity-of-page attribute
 * 			- if main-entity-of-page exists, a CLIENT REDIR to {mainEntityOfPage} is issued
 * 			- if main-entity-of-page DOES NOT exists, it's an entity that DOES NOT have an associated web page so the [triple-store] data 
 * 			  is "painted" in HTML format by ELDA: a CLIENT REDIR to /doc/{resource} is issued
 *
 * 	- If the requested MIME is RDF, the URI is for a [triple-store] data so a CLIENT REDIR to /data/{resource} is issued
 * 
 * All the above also means that:
 * 		- All /doc/{resource} calls MUST have a MIME=HTML (although ELDA supports other MIMES)
 * 		- All /data/{resource} calls MUST hava a MIME=RDF 
 */
abstract class R01HLODURIHandlerForHasWebRepresentationURIBase 
       extends R01HLODURIHandlerBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELD
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HLODMainEntityOfPageResolver _mainEntityOfPageResolver;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForHasWebRepresentationURIBase(final R01HLODURIHandlerConfig config) {
		super(config);
		_mainEntityOfPageResolver = new R01HLODMainEntityOfPageResolverByDefault(config);
	}
	public R01HLODURIHandlerForHasWebRepresentationURIBase(final R01HLODURIHandlerConfig config,
								  						   final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver) {
		super(config);
		_mainEntityOfPageResolver = isMainEntityOfPageResolver;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		// [1] The ACCEPT header is HTML:
		if (data.isRequestingHTMLFromMimes()) {
			// check if the resource has an associated web page
			Url webPageUrl = _mainEntityOfPageResolver.mainEntityOfPage(Url.from(data.getHost(),data.getPort(),
																		   		 data.getRequestedResourceUrlPath()));
			if (webPageUrl != null) {
				// Client redirect to page
				return new R01HLODHandledURIDataForClientRedirect(webPageUrl);
			} else {
				// Client redir to elda
				return new R01HLODHandledURIDataForClientRedirect(R01HLODURIType.DOC.getPathToken()
																			    .joinedWith(data.getRequestedResourceUrlPath()), 
																  data.getRequestQueryString(),
																  data.getRequestedUrlAnchor());
			}
		}
		// [2] The accept header is RDF
		else {
			// client redir to the [triple-store]
			return new R01HLODHandledURIDataForClientRedirect(R01HLODURIType.DATA.getPathToken()
																			.joinedWith(data.getRequestedResourceUrlPath()), 
															  data.getRequestQueryString(),
															  data.getRequestedUrlAnchor());
		}
	}	
}
