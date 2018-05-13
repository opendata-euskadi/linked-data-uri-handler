package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForDataSet;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForELI;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForID;

/**
 * Tests uris that has a representation like [resource] uris, [ELI] uris or [dataset] uris
 * <pre>
 *             ^
 *             |                                                      +------------------+
 *             |                                                      |  /id/{resource}  |
 *         Resource                                                   +--------+---------+
 *           URIs                                                              |
 *             |                     Is main entity                            |
 *             |                     of page?------------MIME=HTML-------------+-------MIME=RDF--------+
 *             |                            |                                                          |
 *             |              +-----NO------+-----YES-----+                                            |
 *             |              |                           |                                            |
 *             |              |                           |                                            |
 *             |              |                           |                                            |
 *         +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
 *             |              |                           |                                           |
 *             |   +----------v---------+       +---------v----------+                     +----------v----------+
 *             |   | /doc/id/{resource} |       |     {web page}     |                     | /data/id/{resource} |
 *             |   +---------+----------+       +---------+----------+                     +----------+----------+
 *                          |                             |                                           |
 *       Representation     |                             |                                     ¿ /id/{resource} ?   
 *           URLs           |                             |                                           |
 *             |   +--------v---------+         +---------v--------+                         +--------v--------+
 *             |   |                  |         |                  |                         |                 |
 *             |   |      ELDA        |         |        Web       |                         |   Triple-Store  |
 *             |   |                  |         |                  |                         |                 |
 *             v   +------------------+         +------------------+                         +--------^--------+
 *                          |                                                                         |
 *                          +--------------------------¿ /id/{resource} ?-----------------------------+
 *       
 *       
 * </pre>
 */
@Slf4j
public class R01HTestLODURIHandlerForHasRepresentationURIs
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
		final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver = _createMainEntityOfPageResolver(true,MAIN_ENTITY_OF_PAGE_URL);
		final R01HLODMainEntityOfPageResolver isNOTMainEntityOfPageResolver = _createMainEntityOfPageResolver(false,null);
		
		UrlPath resourceUrlPath = R01HLODURIType.ID.getPathToken()
												.joinedWith("/sector/domain/class/theId");
		_testHasRepresentationURI(config,
								  // creates a generic uri handler
								  new R01HLODURIHandlerFactory() {
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsMainEntityOfPage() {
											return(H)new R01HLODURIHandlerForID(config,
																			  	isMainEntityOfPageResolver);
										}
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsNOTMainEntityOfPage() {
											return (H)new R01HLODURIHandlerForID(config,
																			  	 isNOTMainEntityOfPageResolver);
										}	
								  },
								  resourceUrlPath);
	}
	@Test
	public void testELIResourceURI() {
		log.info("=====================================================================================");
		log.info("ELI URIs");
		log.info("=====================================================================================");
		final R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver = _createMainEntityOfPageResolver(true,MAIN_ENTITY_OF_PAGE_URL);
		final R01HLODMainEntityOfPageResolver isNOTMainEntityOfPageResolver = _createMainEntityOfPageResolver(false,null);
		
		UrlPath eliResUrlPath = R01HLODURIType.ELI.getPathToken()
											  .joinedWith("/es_pv/type/yyyy/MM/dd/disp/dof");
		UrlPath eliExprUrlPath = eliResUrlPath.joinedWith("spa");
		UrlPath eliFormatUrlPath = eliExprUrlPath.joinedWith("html");
		
		_testHasRepresentationURI(config,
								  // creates an ELI uri handler
								  new R01HLODURIHandlerFactory() {
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsMainEntityOfPage() {
											return(H)new R01HLODURIHandlerForELI(config,
																			  	 isMainEntityOfPageResolver);
										}
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsNOTMainEntityOfPage() {
											return (H)new R01HLODURIHandlerForELI(config,
																			  	  isNOTMainEntityOfPageResolver);
										}
								  },
								  eliFormatUrlPath);
	}
	@Test
	public void testDataSetResourceURI() {
		log.info("=====================================================================================");
		log.info("DataSet URIs");
		log.info("=====================================================================================");
		final R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver = _createMainEntityOfPageResolver(true,MAIN_ENTITY_OF_PAGE_URL);
		final R01HLODMainEntityOfPageResolver isNOTMainEntityOfPageResolver = _createMainEntityOfPageResolver(false,null);
		
		UrlPath dataSetResUrlPath = R01HLODURIType.DATASET.getPathToken()
											  	  .joinedWith("my_named_graph");
		_testHasRepresentationURI(config,
								  // creates an ELI uri handler
								  new R01HLODURIHandlerFactory() {
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsMainEntityOfPage() {
											return(H)new R01HLODURIHandlerForDataSet(config,
																			  	 	 isMainEntityOfPageResolver);
										}
										@Override @SuppressWarnings("unchecked")
										public <H extends R01HLODURIHandler> H createHandlerForIsNOTMainEntityOfPage() {
											return (H)new R01HLODURIHandlerForDataSet(config,
																			  	      isNOTMainEntityOfPageResolver);
										}
								  },
								  dataSetResUrlPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private void _testHasRepresentationURI(R01HLODURIHandlerConfig config,
										   final R01HLODURIHandlerFactory handlerFactory,
										   final UrlPath resourceUrlPath) {
		// [1] - Requesting HTML having the resource a web page (MAIN ENTITY OF PAGE = true)
		log.info("[1] - Resource: {}  (mime type=HTML / main entity of page = true)...........",
				resourceUrlPath);
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   Url.from(HOST,
																			   	resourceUrlPath),
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODURIHandler uriHandler1 = handlerFactory.createHandlerForIsMainEntityOfPage();
		R01HLODHandledURIData handleData1 = uriHandler1.handle(reqData1);
		log.info("Request handle data: {} should match: {}",
				 handleData1.debugInfo(),
				 MAIN_ENTITY_OF_PAGE_URL);		
		
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
				 	   && handleData1.as(R01HLODHandledURIDataForClientRedirect.class)
				 	   				 .getTargetUrl().is(MAIN_ENTITY_OF_PAGE_URL));
		
		// [2] - Requesting HTML NOT having the resource a web page (MAIN ENTITY OF PAGE = false)
		log.info("[2] - Resource: {} (mime type=HTML / main entity of page = false)...........",
				 resourceUrlPath);
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   Url.from(HOST,
																			   	resourceUrlPath),
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODURIHandler resHandler2 = handlerFactory.createHandlerForIsNOTMainEntityOfPage();
		R01HLODHandledURIData handleData2 = resHandler2.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());		
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
				 	   && handleData2.as(R01HLODHandledURIDataForClientRedirect.class)
				 	   				.getTargetUrl().getUrlPath()
				 	   							   .is(UrlPath.from(R01HLODURIType.DOC.getPathToken(),
				 	   						 					     resourceUrlPath)));
		
		// [3] - Requesting RDF...........
		log.info("[3] - Resource: {}  (mime type=RDF)...........",
				 resourceUrlPath);
		R01HLODRequestedURIData reqData3 = new R01HLODRequestedURIData(config,
																	   Url.from(HOST,
																			   	resourceUrlPath),
																	   null,	// no body 
																	   R01HMIMEType.RDFXML);
		log.info("Request data: {}",
				 reqData3.debugInfo());
		R01HLODURIHandlerForID resHandler3 = new R01HLODURIHandlerForID(config,
																		null);	// main entity of page resolver is NOT used
		R01HLODHandledURIData handleData3 = resHandler3.handle(reqData3);
		log.info("Request handle data: {}",
				 handleData3.debugInfo());		
		Assert.assertTrue(handleData3.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData3.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().getUrlPath()
					   				 				.is(UrlPath.from(R01HLODURIType.DATA.getPathToken(),
				 	   						 					     resourceUrlPath)));
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	MAIN ENTITY OF PAGE RESOLVER
/////////////////////////////////////////////////////////////////////////////////////////
	private interface R01HLODURIHandlerFactory {
		public abstract <H extends R01HLODURIHandler> H createHandlerForIsMainEntityOfPage();
		public abstract <H extends R01HLODURIHandler> H createHandlerForIsNOTMainEntityOfPage();
	}
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
