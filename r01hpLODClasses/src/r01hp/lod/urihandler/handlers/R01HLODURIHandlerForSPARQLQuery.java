package r01hp.lod.urihandler.handlers;

import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.types.url.UrlQueryStringParam;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForTripleStoreQuery;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODTripleStoreQuery;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * Handles uris like: <pre>/sparql?query={the_query}</pre>
 * <pre>
 *                   |
 *          /sparql?query={the_query}
 *                   |
 *       +-----------v-----------+
 *       |          WEB          |
 *       +-----------+-----------+
 *                 proxy 
 *               	 |
 *    http://appServer/r01hpLODWar/sparql?query={the_query} 
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
 *                   proxy
 *                     |
 *                     |
 *    http://triplestore/sparql?query={the query}
 *                     |
 *            +--------v--------+
 *            |                 |
 *            |   Triple-Store  |
 *            |                 |
 *            +-----------------+
 * </pre>
 */
public class R01HLODURIHandlerForSPARQLQuery 
	 extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIHandlerForSPARQLQuery(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
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
			return new R01HLODHandledURIDataForServerRedirect(UrlPath.from("sparqlgui"),qryString,
															  R01HMIMEType.HTML);
		}
		// if MIME=RDF go directly to the SPARQL endpoint
		else {
			if (queryStr == null) throw new IllegalStateException("NO sparql query!");
			R01HLODTripleStoreQuery tripleStoreQuery = new R01HLODTripleStoreQuery(queryStr);
			
			return new R01HLODHandledURIDataForTripleStoreQuery(data.getLanguage(),
															    tripleStoreQuery,
															    data.getBestAcceptedMimeTypeOrNull());
		}
	}
}
