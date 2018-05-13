package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForClientRedirect;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODURIType;

/**
 * <pre>
 *        ^
 *        |
 *        |
 *    Resource                      +------------------------+
 *      URIs                        |      /graph/{graph}    |
 *        |                         +------------+-----------+
 *        |                                      |
 *        |                                      |
 *        |              +----------+MIME=HTML---+---MIME=RDF+--+
 *        |              |                                      |
 *        |              |                                      |
 *        |     +--------------+[*****-CLIENT REDIR ****]+------------+
 *        +              |                                      |
 *                       |                                      |
 * +--------------------------------------------------------------------------+
 *                       |                                      |
 *        +   +----------v------------+            +------------v-------------+
 *        |   |                       |            |                          |
 *        |   |   /doc/graph/{graph}  |            |  /data/graph/{resource}  |
 *        |   |                       |            |                          |
 *        |   +----------+------------+            +------------+-------------+
 * Representation        |                                      |
 *      URLs             |                              ¿ /graph/{graph} ?
 *        |              |                                       |
 *        |    +---------v----------+                  +---------v---------+
 *        |    |                    |                  |                   |
 *        |    |        ELDA        |                  |   TRIPLE-STORE    |
 *        |    |                    |                  |                   |
 *        v    +--------------------+                  +---------^---------+
 *                       |                                       |
 *                       +---------¿ /graph/{graph} ?------------+
 *         
 * </pre>
 */
abstract class R01HLODURIHandlerForHasRepresentationURIBase 
       extends R01HLODURIHandlerBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForHasRepresentationURIBase(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		// [1] The ACCEPT header is HTML:
		if (data.isRequestingHTMLFromMimes()) {
			// Client redir to elda
			return new R01HLODHandledURIDataForClientRedirect(R01HLODURIType.DOC.getPathToken()
																		    .joinedWith(data.getRequestedResourceUrlPath()), 
															  data.getRequestQueryString(),
															  data.getRequestedUrlAnchor());
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
