package r01hp.lod.urihandler.handlers;

import lombok.extern.slf4j.Slf4j;
import r01f.rewrite.RewriteRule;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.types.url.UrlQueryStringParam;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForTripleStoreProxy;
import r01hp.lod.urihandler.R01HLODHandledURIDataForTripleStoreQuery;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODResourceType;
import r01hp.lod.urihandler.R01HLODTripleStoreQuery;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * Handles uris like: <pre>http://apiSite/sparql?query={the_query}</pre>
 * <pre>
 *                   |
 *    http://apiSite/sparql?query={the_query}  (GET or POST)
 *                   |
 *       +-----------v-----------+
 *       |          WEB          |
 *       +-----------+-----------+
 *                 proxy 
 *               	 |
 *  http://appServer/r01hpLODWar/sparql?query={the_query} 
 *                   |
 *        +----------v-----------+
 *        |       LOD WAR        |
 *        |          |--MIME=HTML|
 *        |          |      |    |
 *        |          |      v    |
 *        |          |   [YASGUI]|
 *        |          |           |
 *        |        MIME=RDF      |
 *        +----------------------+ 
 *                 proxy
 *                   |
 *                   |
 *  http://triplestore/sparql?query={the query}
 *                   |
 *          +--------v--------+
 *          |                 |
 *          |   Triple-Store  |
 *          |                 |
 *          +-----------------+
 * </pre>
 */
@Slf4j
public class R01HLODApiURIHandler 
	 extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODApiURIHandler(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		R01HLODHandledURIData outData = null;
		if (data.getResourceType().is(R01HLODResourceType.SPARQL)) {
			// the sparql query can be:
			//		- a query string query=[the sparql query]
			//		- the POST body
			String queryStr = data.getRequestQueryString() != null 
									? data.getRequestQueryString().getParamValue("query")
									: null;
			
			// If MIME=HTML do a server redir to the YASGUI page
			if (data.isRequestingHTMLFromMimes()) {
				// sparqlgui is mapping the /_pages/yasgui.jsp (see web.xml)
				UrlQueryString qryString = queryStr != null ? UrlQueryString.fromParams(new UrlQueryStringParam("query",queryStr))
															: null;
				outData = new R01HLODHandledURIDataForServerRedirect(UrlPath.from("sparqlgui"),
																  	 qryString,
																  	 R01HMIMEType.HTML);
			}
			// if MIME=RDF go directly to the SPARQL endpoint
			else {
				if (queryStr == null) throw new IllegalStateException("NO sparql query!");
				R01HLODTripleStoreQuery tripleStoreQuery = new R01HLODTripleStoreQuery(queryStr);
				
				outData = new R01HLODHandledURIDataForTripleStoreQuery(tripleStoreQuery,
																       data.getBestAcceptedMimeTypeOrNull());
			}
		}
		else if (data.getResourceType().isIn(R01HLODResourceType.READ_TRIPLE_STORE,
											 R01HLODResourceType.WRITE_TRIPLE_STORE)) {
			// rewrite from /read/triplestore/$1 to /read/blazegraph/$1
			String urlPathRewritten = RewriteRule.matching("/(read|write)/triplestore/?(.*)")
			 									 .rewriteTo("/" + R01HLODURIHandlerConfig.LOD_WAR_NAME + "/$1/blazegraph/$2")
			 									 .applyTo(data.getRequestedResourceUrlPath().asAbsoluteString());			
			outData = new R01HLODHandledURIDataForTripleStoreProxy(UrlPath.preservingTrailingSlash()	// BEWARE!!
																		  .from(urlPathRewritten));
		}
		else {
			log.error("{} is NOT a supported resource type at API site",
					  data.getResourceType());
			return null;
		}
		return outData;
	}
}
