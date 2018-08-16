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
 * Tests uris that has a representation like [resource] uris, [ELI] uris or [dataset] uris
 * <pre>
 *                     ^
 *                     |                                                  +--------------------------+
 *                     +                                                  | http://idsite/{resource} |
 *                 Resource                                               +-------------+------------+
 *                   URIs                                                               |
 *                     +                             Is main entity                     |
 *                     |                           +---+ of page? +-----+MIME=HTML+-----+-----+MIME=RDF+--------+
 *                     |                           |                                                            |
 *                     |              +------------+-------------+                                              |
 *                     |              |                          |                                              |
 *                     |              |                          |                                              |
 *                     |              |                          |                                              |
 *                 +---------------------------------------------------+[***** CLIENT REDIR ****]+----------------------------------+
 *                     |              |                          |                                              |
 *                                    |                          |                                              |
 *                     |   +----------v--------------+ +---------v---------------+                 +------------v-------------+
 *                     |   |http://docsite/{resource}| |http://website/{resource}|                 |http://datasite/{resource}|
 *                     +   +----------+--------------+ +---------+---------------+                 +------------+-------------+
 *                                    |                          |                                              |
 *               Representation       |                          |                                              |
 *                   URLs             |                          |                                              |
 *                     +       +------v-------+        +---------v--------+                            +--------v--------+
 *                     |       |              |        |                  |                            |                 |
 *                     |       |     ELDA     |        |        Web       |                            |   Triple+Store  |
 *                     |       |              |        |                  |                            |                 |
 *                     v       +--------------+        +------------------+                            +-----------------+
 * </pre> 
 */
@Slf4j
public class R01HTestLODIdURIHandlerForHasWebRepresentationResources
	 extends R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private final static Url MAIN_ENTITY_OF_PAGE_URL = Url.from("http://www.euskadi.eus");
/////////////////////////////////////////////////////////////////////////////////////////
//	HasRepresentation URIs
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testEuskadiResourceURI() {
		log.info("=====================================================================================");
		log.info("NTI URIs");
		log.info("=====================================================================================");
		final R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath resourceUrlPath = UrlPath.from("/sector/domain/class/theId");
		_testHasRepresentationURI(config,
								  resourceUrlPath);
	}
	@Test
	public void testELIResourceURI() {
		log.info("=====================================================================================");
		log.info("ELI URIs");
		log.info("=====================================================================================");
		final R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath eliResUrlPath = R01HLODResourceType.ELI.getPathToken()
											  .joinedWith("/es_pv/type/yyyy/MM/dd/disp/dof");
		UrlPath eliExprUrlPath = eliResUrlPath.joinedWith("spa");
		UrlPath eliFormatUrlPath = eliExprUrlPath.joinedWith("html");
		
		_testHasRepresentationURI(config,
								  eliFormatUrlPath);
	}
	@Test
	public void testDataSetResourceURI() {
		log.info("=====================================================================================");
		log.info("DataSet URIs");
		log.info("=====================================================================================");
		final R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		UrlPath dataSetResUrlPath = R01HLODResourceType.DATASET.getPathToken()
											  	  	   .joinedWith("my_named_graph");
		_testHasRepresentationURI(config,
								  dataSetResUrlPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private void _testHasRepresentationURI(final R01HLODURIHandlerConfig config,
										   final UrlPath resourceUrlPath) {
		Url uri = Url.from(config.getIdSite(),
						   resourceUrlPath);
		
		// [1] - Requesting HTML having the resource a web page (MAIN ENTITY OF PAGE = true)
		log.info("[1] - URI: {} (mime type=HTML / main entity of page = true)...........",
				 uri);	
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODURIHandler uriHandler1 = new R01HLODIdURIHandler(config,
																_createMainEntityOfPageResolver(true,MAIN_ENTITY_OF_PAGE_URL));
		R01HLODHandledURIData handleData1 = uriHandler1.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1.debugInfo());
		log.info("Expected CLIENT REDIR to {}",
				 MAIN_ENTITY_OF_PAGE_URL);		
		
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
				 	   && handleData1.as(R01HLODHandledURIDataForClientRedirect.class)
				 	   				 .getTargetUrl().is(MAIN_ENTITY_OF_PAGE_URL));
		
		// [2] - Requesting HTML NOT having the resource a web page (MAIN ENTITY OF PAGE = false)
		log.info("[2] - URI: {} (mime type=HTML / main entity of page = false)...........",
				 uri);
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODURIHandler resHandler2 = new R01HLODIdURIHandler(config,
																_createMainEntityOfPageResolver(false,null));
		R01HLODHandledURIData handleData2 = resHandler2.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());	
		log.info("Expected CLIENT REDIR to {}",
				 Url.from(config.getDocSite(),
				 	   	  resourceUrlPath));
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
				 	   && handleData2.as(R01HLODHandledURIDataForClientRedirect.class)
				 	   				 .getTargetUrl().is(Url.from(config.getDocSite(),
				 	   						 					 resourceUrlPath)));
		
		// [3] - Requesting RDF...........
		log.info("[3] - URI: {}  (mime type=RDF)...........",
				 uri);
		R01HLODRequestedURIData reqData3 = new R01HLODRequestedURIData(config,
																	   uri,
																	   null,	// no body 
																	   R01HMIMEType.RDFXML);
		log.info("Request data: {}",
				 reqData3.debugInfo());
		R01HLODURIHandler resHandler3 = new R01HLODIdURIHandler(config,
																null);	// main entity of page resolver is NOT used
		R01HLODHandledURIData handleData3 = resHandler3.handle(reqData3);
		log.info("Request handle data: {}",
				 handleData3.debugInfo());	
		log.info("Expected CLIENT REDIR to {}",
				 Url.from(config.getDataSite(),
				 	   	  resourceUrlPath));
		Assert.assertTrue(handleData3.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData3.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().is(Url.from(config.getDataSite(),
				 	   						 					 resourceUrlPath)));
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	MAIN ENTITY OF PAGE RESOLVER
/////////////////////////////////////////////////////////////////////////////////////////
	private R01HLODMainEntityOfPageResolver _createMainEntityOfPageResolver(final boolean whatToReturn,
																			final Url whatToReturnWebPage) {
		return new R01HLODMainEntityOfPageResolver() {
						@Override
						public boolean isMainEntityOfPage(final Url uri) {
							return whatToReturn;
						}

						@Override
						public Url mainEntityOfPage(final Url uri) {
							return whatToReturnWebPage;
						}
			   };
	}
}
