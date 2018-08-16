package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.types.url.UrlQueryStringParam;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODApiURIHandler;


@Slf4j
public class R01HTestLODApiURIHandlerForSPARQLQuery 
	 extends R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	DATA
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testSPARQLURIs() {
		log.info("=====================================================================================");
		log.info("SPARQL URIs");
		log.info("=====================================================================================");
		R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath sparqlUrlPath = UrlPath.from("sparql");
		UrlQueryString sparqlQuery = UrlQueryString.fromParams(UrlQueryStringParam.of("query","an sparql query")); 
		
		R01HLODApiURIHandler resHandler = new R01HLODApiURIHandler(config);
		
		// [1] - Resource: 
		//		 1.a) /sparql?query={the query} with mime type=JSON
		log.info("[1.a] - Resource: /sparql?query=theQuery mime-type=JSON...........");
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   Url.from(config.getApiSite(),
																			    sparqlUrlPath,sparqlQuery),
																	   null,		// no body
																	   R01HMIMEType.JSON);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1a = resHandler.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1a.debugInfo());
		Assert.assertTrue(handleData1a.getAction() == R01HLODURIHandleAction.TRIPLE_STORE_QUERY);
		
		// 		1.b) POST to /sparql
		log.info("[1.b] - Resource: POST to /sparql with POST BODY=theQuery & mime-type=JSON...........");
		R01HLODRequestedURIData reqData1b = new R01HLODRequestedURIData(config,
																	    Url.from(config.getApiSite(),
																			     sparqlUrlPath),
																	    "query=an sparql query",
																	    R01HMIMEType.JSON);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1b = resHandler.handle(reqData1b);
		log.info("Request handle data: {}",
				 handleData1b.debugInfo());
		Assert.assertTrue(handleData1b.getAction() == R01HLODURIHandleAction.TRIPLE_STORE_QUERY);		
		
		
		// [2] - Resource: /sparql with mime type=HTML > yasgui server redir
		log.info("[2.a] - Resource: /sparql?query=theQuery mime-type=HTML > yasgui...........");
		R01HLODRequestedURIData reqData2a = new R01HLODRequestedURIData(config,
																	    Url.from(config.getApiSite(),
																			     sparqlUrlPath,sparqlQuery),
																	    null,		// no body
																	    R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData2a.debugInfo());
		R01HLODHandledURIData handleData2a = resHandler.handle(reqData2a);
		log.info("Request handle data: {} should be a SERVER REDIR to {}?query={}",
				 handleData2a.debugInfo(),
				 UrlPath.from("sparqlgui"));
		Assert.assertTrue(handleData2a.getAction() == R01HLODURIHandleAction.SERVER_REDIRECT
				       && handleData2a.as(R01HLODHandledURIDataForServerRedirect.class)
				       				 .getUrlPath().is(UrlPath.from("sparqlgui")));
		
		log.info("[2.b] - Resource: POST to /sparql with POST BODY=query & mime-type=HTML > yasgui...........");
		R01HLODRequestedURIData reqData2b = new R01HLODRequestedURIData(config,
																	    Url.from(config.getApiSite(),
																			     sparqlUrlPath),
																	    "an sparql query",	
																	    R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData2b.debugInfo());
		R01HLODHandledURIData handleData2b = resHandler.handle(reqData2a);
		log.info("Request handle data: {} should be a SERVER REDIR to {}?query={}",
				 handleData2b.debugInfo(),
				 UrlPath.from("sparqlgui"));
		Assert.assertTrue(handleData2b.getAction() == R01HLODURIHandleAction.SERVER_REDIRECT
				       && handleData2b.as(R01HLODHandledURIDataForServerRedirect.class)
				       				 .getUrlPath().is(UrlPath.from("sparqlgui")));
	}	
}
