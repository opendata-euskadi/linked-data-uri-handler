package r01hp.lod.urihandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestHeader;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlQueryString;
import r01f.types.url.UrlQueryStringParam;
import r01f.util.types.Strings;
import r01hp.lod.config.R01HLODURIHandlerConfig;

/**
 * Uses a SPARQL query to guess if the requested resource is the main entity of page
 */
@Slf4j
public class R01HLODMainEntityOfPageResolverByDefault 
  implements R01HLODMainEntityOfPageResolver {
	
	// BEWARE!!! do NOT make a direct call to a triplestore instance,
	//			 use the proxy in order to have load balance and redundancy
    //
    //                   +----------------+
    //                   |     LODWAR     |
    //                   |            |   +-------------+
    //                   +------------|---+             |
    //                    |    proxy  v  |        DO NOT DO THIS > direct call to a triplestore instance (DO NOT DO THIS!!!)
    //                    +--------------+              |
    //                        |     |                   |
    //           +------------+     +--------------+    |
    //           |                                 |    |
    //   +-----------------+              +-----------------+
    //   |  TripleStore    |              |  TripleStore    |
    //   +-----------------+              +-----------------+
	//
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////		
	private final R01HLODURIHandlerConfig _uriHandlerConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODMainEntityOfPageResolverByDefault(final R01HLODURIHandlerConfig uriHandlerConfig) {
		_uriHandlerConfig = uriHandlerConfig;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isMainEntityOfPage(final Url uri) {
		try {
			// Create a triplestore query like
			//		http://tripleStoreSPARQLEndPoint?query=PREFIX schema:<http://schema.org/> 
			//			    							   ASK { <URI> schema:mainEntityOfPage ?page }
			//			Eg:{
			//			 "head": {},
			//			 "boolean": true
			//			}
			R01HLODTripleStoreQuery tripleStoreQuery = R01HLODTripleStoreQuery.isMainEntityOfPage(uri);	
			
			// Exec the query and read the content
			InputStream is = _execQuery(tripleStoreQuery);
			
	        JSONParser parser = new JSONParser();
	        JSONObject obj = (JSONObject)parser.parse(new InputStreamReader(is));
	        boolean outJson = obj.containsKey("boolean") ? ((Boolean)obj.get("boolean")).booleanValue()
		        							  			 : false;
	        return outJson;	        		
        } catch(Throwable th) {
        	log.error("Error quering the triple-store to check if {} is main entity of page: {}",
        			  uri,th.getMessage(),
        			  th);
        }
		return false;	// false if error
	}
	@Override
	public Url mainEntityOfPage(final Url uri) {
		try {	
			// Create a triplestore query like
			//		http://tripleStoreSPARQLEndPoint?query=PREFIX schema:<http://schema.org/> 
			//			    							  SELECT ?page WHERE { <{}> schema:mainEntityOfPage ?page . }
			//			Eg: "results": { 
			//					"bindings": [ 
			//				 	{ 
			//				 		"page": { 
			//				 			"type": "uri", 
			//				 			"value": "http://opendata.euskadi.eus/catalogo/contenidos/fundacion/b06/es_def/index.shtml\" 
			//				 				}
			//				 	}, 
			//				 	{ 
			//					"page": { 
			//				 			"type": "uri", 
			//				 			"value": "http://opendata.euskadi.eus/catalogo/contenidos/fundacion/b06/es_def/index.shtml\" 
			//				 				}
			//				 	}
			//				 	]
			//				}
			R01HLODTripleStoreQuery tripleStoreQuery = R01HLODTripleStoreQuery.mainEntityOfPage(uri);	
			InputStream is = _execQuery(tripleStoreQuery);
				 
			JSONParser parser = new JSONParser();
	        JSONObject obj = (JSONObject)parser.parse(new InputStreamReader(is));
	        JSONArray pages = (JSONArray)((JSONObject) obj.get("results")).get("bindings");
	        // TODO Get language webPage. 
	        String webPage =  pages.size() > 0 ? ((JSONObject)((JSONObject) pages.get(0)).get("page")).get("value").toString() : null;
			Url outWebPage = Strings.isNOTNullOrEmpty(webPage) ? Url.from(webPage)
															   : null;
			if (outWebPage == null) log.error("Resolved a NULL main entity of page for {}",uri);
			return outWebPage;
		} catch(Throwable th) {
        	log.error("Error quering the triple-store to check if {} is main entity of page: {}",
        			  uri,th.getMessage(),
        			  th);
        }
		return null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private InputStream _execQuery(final R01HLODTripleStoreQuery tripleStoreQuery) throws IOException {
		// The query is proxied throught the WAR's proxy to the triple-store:
	    //                   +----------------+
	    //                   |     LODWAR     |
	    //                   |            |   +-------------+
	    //                   +------------|---+             |
	    //                    |    proxy  v  |        DO NOT DO THIS
	    //                    +--------------+              |
	    //                        |     |                   |
	    //           +------------+     +--------------+    |
	    //           |                                 |    |
	    //   +-----------------+              +-----------------+
	    //   |  TripleStore    |              |  TripleStore    |
	    //   +-----------------+              +-----------------+
		
		// Pick any app server host 
		// TODO use netflix's ribbon or any other client-side balance library
		Host appServerHost = Iterables.getFirst(_uriHandlerConfig.getAppServerConfig().getAppServerHosts(),
												Host.from("localhost:8080"));
		Url qryUrl = Url.from(// host
							  appServerHost,
							  // path: /r01hpLODWar/read/blazegraph/sparql/execute
							  _uriHandlerConfig.getAppServerConfig().getLodWarUrlPath()																			// r01hpLODWar/
							  										.joinedWith(_uriHandlerConfig.getTripleStoreConfig().getInternalSPARQLEndPointUrlPath())	// /read/blazegraph/namespace/euskadi_db/sparql/
								  								    .joinedWith("execute"), 																	// /execute
							  // ?query= (replace id.localhost with id.euskadi.eus)
							  UrlQueryString.fromParams(UrlQueryStringParam.of("query",
																			    tripleStoreQuery.asString())));
		log.info("IS MAIN ENTITY OF PAGE URL={}",
				 qryUrl);
		// Exec the query and read the content
		InputStream is = HttpClient.forUrl(qryUrl)
								   .withHeaders(HttpRequestHeader.create("Accept",
										   								 R01HMIMEType.JSON.asString()))
								   .GET()
								   .loadAsStream()
								   		.directNoAuthConnected();
		return is;
	}

}
