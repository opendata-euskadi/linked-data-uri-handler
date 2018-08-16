package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODIdURIHandler;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;

/**
 * <pre>
 *        ^
 *        |
 *        +
 *    Resource                    +------------------------+
 *      URIs                      |http://idsite/{resource}|
 *        +                       +-------------+----------+
 *        |                                     |
 *        |                                     |
 *        |                  +----+MIME=HTML+---+---+MIME=RDF+----+
 *        |                  |                                    |
 *        |                  |                                    |
 *        |     +--------------+[*****+CLIENT REDIR ****]+------------+
 *        +                  |                                    |
 *                           |                                    |
 * +--------------------------------------------------------------------------+
 *                           |                                    |
 *        +   +--------------v-------------+       +--------------v--------------+
 *        |   |                            |       |                             |
 *        |   |  http://docsite/{resource} |       |  http://datasite/{resource} |
 *        |   |                            |       |                             |
 *        +   +--------------+-------------+       +--------------+--------------+
 * Representation            |                                    |
 *      URLs                 |                             ¿ {resource} ?
 *        +                  |                                    |
 *        |         +--------v--------+                 +---------v---------+
 *        |         |                 |                 |                   |
 *        |         |       ELDA      |                 |   TRIPLE+STORE    |
 *        |         |                 |                 |                   |
 *        v         +---------+-------+                 +---------^---------+
 *                            |                                   |
 *                            +--------+¿ {resource ?+------------+
 * 
 * </pre>
 */
@Slf4j
public class R01HTestLODUIdRIHandlerForHasNOTWebRepreseentationResources 
	 extends R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	DATA
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testDataURIs() {
		log.info("=====================================================================================");
		log.info("DataSet Distribution URIs");
		log.info("=====================================================================================");
		R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath distributionUrlPath = R01HLODResourceType.DISTRIBUTION.getPathToken()
													.joinedWith("my_named_graph");
		Url uri = Url.from(config.getIdSite(),
						   distributionUrlPath);
		
		R01HLODURIHandler resHandler = new R01HLODIdURIHandler(config,
															   null);		// no main entity of page needed
		
		// URI: http://dataSite/{graph} with mime=RDF
		log.info("[1] - URI: {} mime-type=RDF...........",
				 uri);
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null, 	// no body
																	   R01HMIMEType.RDFXML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1 = resHandler.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1.debugInfo());
		log.info("Expected CLIENT REDIR to {}",
				  Url.from(config.getDataSite(),
					   	   distributionUrlPath));
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData1.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().is(Url.from(config.getDataSite(),
					   						 					 distributionUrlPath)));
		// Resource: /distribution/{resource} with mime=HTML
		log.info("[2] - URI: {} mime-type=HTML...........",
				 uri);
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null,		// no body
																	   R01HMIMEType.HTML);		// html
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODHandledURIData handleData2 = resHandler.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());
		log.info("Expected CLIENT REDIR to {}",
				 Url.from(config.getDocSite(),
					      distributionUrlPath));
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData2.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().is(Url.from(config.getDocSite(),
					   						 					 distributionUrlPath)));	
	}	
}
