package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForDataSetDistribution;

/**
 * <pre>
 *        ^
 *        |
 *        |
 *    Resource                      +------------------------+
 *      URIs                        |  /distribution/{graph} |
 *        |                         +------------+-----------+
 *        |                                      |
 *        |                                      |
 *        |              +----------+MIME=HTML---+---MIME=RDF+---+
 *        |              |                                       |
 *        |              |                                       |
 *        |     +----------------+[*****-CLIENT REDIR ****]+---------------+
 *        +              |                                       |
 *                       |                                       |
 * +--------------------------------------------------------------------------+
 *                       |                                       |
 *        +   +----------v---------------+           +-----------v------------+
 *        |   |                          |           |                        |
 *        |   | /doc/distribution/{graph}|           |  /data/eli/{resource}  |
 *        |   |                          |           |                        |
 *        |   +----------+---------------+           +-----------+------------+
 * Representation        |                                       |
 *      URLs             |                              ¿ /distribution/{graph} ?
 *        |              |                                       |
 *        |    +---------v----------+                  +---------v---------+
 *        |    |                    |                  |                   |
 *        |    |        ELDA        |                  |   TRIPLE-STORE    |
 *        |    |                    |                  |                   |
 *        v    +--------------------+                  +---------^---------+
 *                       |                                       |
 *                       +---------¿ /distribution/{graph} ?-----+
 *         
 * </pre>
 */
@Slf4j
public class R01HTestLODURIHandlerForDataSetDistributionURIs 
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
		
		UrlPath distributionUrlPath = R01HLODURIType.DISTRIBUTION.getPathToken()
													.joinedWith("my_named_graph");
		
		R01HLODURIHandlerForDataSetDistribution resHandler = new R01HLODURIHandlerForDataSetDistribution(config);
		
		// Resource: /data/{graph} with mime=RDF
		log.info("[1] - Resource: /distribution/{graph} mime-type=RDF...........");
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   Url.from(HOST,
																			    distributionUrlPath),
																	   null, 	// no body
																	   R01HMIMEType.RDFXML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1 = resHandler.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1.debugInfo());
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData1.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().getUrlPath().is(UrlPath.from(R01HLODURIType.DATA.getPathToken())
																  		    .joinedWith(distributionUrlPath)));
		// Resource: /distribution/{resource} with mime=HTML
		log.info("[2] - Resource: /data/{graph} mime-type=HTML...........");
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   Url.from(HOST,
																			    distributionUrlPath),
																	   null,		// no body
																	   R01HMIMEType.HTML);		// html
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODHandledURIData handleData2 = resHandler.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData2.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().getUrlPath().is(UrlPath.from(R01HLODURIType.DOC.getPathToken())
																  		    .joinedWith(distributionUrlPath)));	
	}	
}
