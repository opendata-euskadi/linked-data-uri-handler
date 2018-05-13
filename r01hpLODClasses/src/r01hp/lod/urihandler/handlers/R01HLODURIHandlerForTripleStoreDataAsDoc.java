package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODURIHandlerEngine;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * Handles uris like: <pre>/doc/id/{resource}</pre>
 * These URIs usually are CLIENT-REDIRs after a /id/{resource} with MIME=HTML and where the {resource} DOES NOT have
 * an associated web page (see {@link R01HLODURIHandlerEngine})
 * <pre>
 *                 |
 *       http://site/doc/{resource}    (MIME=HTML)
 *                 |
 *     +-----------------------+
 *     |          WEB          |
 *     +-----------+-----------+
 *               proxy 
 *                 |
 * http://appServer/r01hpLODWar/doc/{resource}	
 *                 |
 *     +-----------v-----------+
 *     |APP SERVER             |
 *     |   +----------------+  |
 *     |   |    LOD WAR     |  |
 *     |   |       |        |  |
 *     |   | chain.doFilter |  |
 *     |   |   +---------+  |  |
 *     |   |   |  ELDA   |  |  |
 *     |   |   +---------+  |  |
 *     |   +----------------+  |
 *     +-----------------------+
 * </pre>
 */
public class R01HLODURIHandlerForTripleStoreDataAsDoc 
	 extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIHandlerForTripleStoreDataAsDoc(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		// just redir to ELDA
		return new R01HLODHandledURIDataForServerRedirect(data.getRequestedResourceUrlPath(),
														  data.getRequestQueryString(),
														  data.getBestAcceptedMimeTypeOrDefault(R01HMIMEType.HTML));
	}
}
