package r01hp.lod.urihandler.handlers;

import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODURIHandlerEngine;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * Handles uris like: <pre>http:///docSite/{resource}</pre>
 * These URIs usually are CLIENT-REDIRs after a http://idSite/{resource} with MIME=HTML and where the {resource} DOES NOT have
 * an associated web page (see {@link R01HLODURIHandlerEngine})
 * <pre>
 *                 |
 *     http://docSite/{resource}    (MIME=HTML)
 *                 |
 *     +-----------------------+
 *     |          WEB          |
 *     +-----------+-----------+
 *               proxy 
 *                 |
 * http://appServer/r01hpLODWar/{resource}	
 *                 |
 *     +-----------v-----------+
 *     |APP SERVER             |
 *     |   +----------------+  |
 *     |   |    LOD WAR     |  |
 *     |   |       |  /r01hpLODWar/elda/{resource}
 *     |   |       |        |  |
 *     |   | chain.doFilter |  |
 *     |   |   +---------+  |  |
 *     |   |   |  ELDA   |  |  |
 *     |   |   +---------+  |  |
 *     |   +----------------+  |
 *     +-----------------------+
 * </pre>
 */
public class R01HLODDocURIHandler 
	 extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODDocURIHandler(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		// just redir to ELDA
		return new R01HLODHandledURIDataForServerRedirect(UrlPath.from("elda").joinedWith(data.getRequestedResourceUrlPath()),	// elda/{resource}
														  data.getRequestQueryString(),
														  data.getBestAcceptedMimeTypeOrDefault(R01HMIMEType.HTML));
	}
}
