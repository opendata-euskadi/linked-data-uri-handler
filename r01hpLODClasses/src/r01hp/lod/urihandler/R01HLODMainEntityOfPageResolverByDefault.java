package r01hp.lod.urihandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestHeader;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
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
	
	// BEWARE!!! do NOT make a direct call to a blazegraph instance,
	//			 use the proxy in order to have load balance and redundancy
    //
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
	//
	// direct call to a blazegraph instance (DO NOT DO THIS!!!)
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("^/id/property/.*");
	
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
		// resources that do NOT have a web page for sure:
		if (uri == null) return false;
		UrlPath urlPath = uri.getUrlPath();
		if (urlPath == null
		 || urlPath.matches(PROPERTY_PATTERN)) {
			return false;
		}
		
		try {
			// Create a triplestore query like
			//		http://tripleStoreSPARQLEndPoint?query=PREFIX schema:<http://schema.org/> 
			//			    							   ASK { <URI> schema:mainEntityOfPage ?page } 
			// [1] - Get the language from the requested host
			R01HLODTripleStoreQuery tripleStoreQuery = R01HLODTripleStoreQuery.isMainEntityOfPage(uri);	
			Url isMainEntityOfPageSPARQLUrl = Url.from(// host
													   _uriHandlerConfig.getLodWarHost(),
													   // path: /r01hpLODWar/sparql/execute
													   UrlPath.from("r01hpLODWar",R01HLODURIType.SPARQL.getPathToken(),	// /sparql
														  			"execute"), 							// /execute
													   // ?query=
													   UrlQueryString.fromParams(UrlQueryStringParam.of("query",
																										tripleStoreQuery.asString()))); 
			log.info("IS MAIN ENTITY OF PAGE URL={}",
					 isMainEntityOfPageSPARQLUrl);
			// Exec the query and read the content
			InputStream is = HttpClient.forUrl(isMainEntityOfPageSPARQLUrl)
									   .withHeaders(HttpRequestHeader.create("Accept",
											   								 R01HMIMEType.JSON.asString()))
									   .GET()
									   .loadAsStream()
									   		.directNoAuthConnected();
			
			//			Eg:{
			//			 "head": {},
			//			 "boolean": true
			//			}
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
		// resources that do NOT have a web page for sure:
		if (uri == null) return null;
		UrlPath urlPath = uri.getUrlPath();
		if (urlPath == null
		 || urlPath.matches(PROPERTY_PATTERN)) {
			return null;
		}
		
		try {	
			// Create a triplestore query like
			//		http://tripleStoreSPARQLEndPoint?query=PREFIX schema:<http://schema.org/> 
			//			    							  SELECT ?page WHERE { <{}> schema:mainEntityOfPage ?page . }
			// [1] - Get the language from the requested host
			R01HLODTripleStoreQuery tripleStoreQuery = R01HLODTripleStoreQuery.mainEntityOfPage(uri);	
			Url mainEntityOfPageSPARQLUrl = Url.from(// host
													   _uriHandlerConfig.getLodWarHost(),
													   // path: /r01hpLODWar/sparql/execute
													   UrlPath.from("r01hpLODWar",R01HLODURIType.SPARQL.getPathToken(),	// /sparql
														  			"execute"), 							// /execute
													   // ?query= (replace localhost with data.euskadi.eus)
													   UrlQueryString.fromParams(UrlQueryStringParam.of("query",
																										tripleStoreQuery.asString()
																											.replaceAll("localhost",	// beware local testing
																														_uriHandlerConfig.getDataSite()
																																		 .getHost().getId())))); 
			
			log.info("MAIN ENTITY OF PAGE URL={}",
					 mainEntityOfPageSPARQLUrl);
			// Exec the query and read the content
			InputStream is = HttpClient.forUrl(mainEntityOfPageSPARQLUrl)
									   .withHeaders(HttpRequestHeader.create("Accept",
											   								 R01HMIMEType.JSON.asString()))
									   .GET()
									   .loadAsStream()
									   		.directNoAuthConnected();
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
				 	
				 
			JSONParser parser = new JSONParser();
	        JSONObject obj = (JSONObject)parser.parse(new InputStreamReader(is));
	        JSONArray pages = (JSONArray)((JSONObject) obj.get("results")).get("bindings");
	        //TODO Get language webPage. 
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
	

}
