package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODDataURIHandler;

/**
 * <pre>
 *		   	   +--------------------------+
 *		       |http://dataSite/{resource}|
 *		       +------------+-------------+
 *	 	                    |
 *	                     	|
 *	                     	|
 *	                     	|
 *	       	  MIME=RDF      |         MIME=HTML
 *			+-----------------------------------+
 *	                        |
 *	           		    MIME=RDF
 *	                     	|
 *	                     	v
 *		           +---------+--------+
 *	    	       |                  |
 *	           	   |   Triple-Store   |
 *	           	   |                  |
 *	           	   +------------------+
 * </pre>
 */
@Slf4j
public class R01HTestLODDataURIHandler 
	 extends R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	DATA
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testDataURIs() {
		log.info("=====================================================================================");
		log.info("DATA URIs");
		log.info("=====================================================================================");
		R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath resourceUrlPath = UrlPath.from("/sector/domain/class/theId");
		Url uri = Url.from(config.getDataSite(),
						   resourceUrlPath);
		
		R01HLODDataURIHandler resHandler = new R01HLODDataURIHandler(config);
		
		// URI: http://dataSite/{resource} with mime=RDF
		log.info("[1] - URI: {} mime-type=RDF...........");
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null, 	// no body
																	   R01HMIMEType.RDFXML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1 = resHandler.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1.debugInfo());
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.TRIPLE_STORE_QUERY
					   && handleData1.as(R01HLODHandledURIDataForTripleStoreQuery.class)
					   				 .getTripleStoreQuery().equals(R01HLODTripleStoreQuery.describe(Url.from(config.getIdSite(),
						       																				 resourceUrlPath))));
		// URI: http://dataSite/{resource}.rdf with mime=HTML
		UrlPath rdfFileUrlPath = UrlPath.from(resourceUrlPath.asString() + ".rdf");
		Url uri2 = Url.from(config.getDataSite(),
						    rdfFileUrlPath);
		log.info("[2] - URI: {} mime-type=HTML...........",
				 uri2);
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   uri2,
																	   null,		// no body
																	   R01HMIMEType.HTML);		// html
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODHandledURIData handleData2 = resHandler.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.TRIPLE_STORE_QUERY
					   && handleData2.as(R01HLODHandledURIDataForTripleStoreQuery.class)
					   				 .getTripleStoreQuery().equals(R01HLODTripleStoreQuery.describe(Url.from(config.getIdSite(),
						       																				 resourceUrlPath))));	// BEWARE without .rdf extension!!!
	}	
}
