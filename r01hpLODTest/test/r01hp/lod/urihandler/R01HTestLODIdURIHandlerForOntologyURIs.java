package r01hp.lod.urihandler;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODIdURIHandler;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;


@Slf4j
public class R01HTestLODIdURIHandlerForOntologyURIs 
	 extends R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	ONTOLOGY
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testOntologyURIs() {
		log.info("=====================================================================================");
		log.info("ONTOLOGY URIs");
		log.info("=====================================================================================");
		R01HLODURIHandlerConfig config = _loadLODURIHandlerConfig();
		
		String ontologyName = "my_ontology";
		Host host = Host.of("http://en.euskadi.eus");
		
		R01HLODURIHandler resHandler = new R01HLODIdURIHandler(config,
															   null);		// no main entity of page resolver needed
		
		// [1] - Ontology definition: /def/{ontology} with mime type=HTML
		log.info("[1] - Resource: /def/{ontology}.owl mime-type=HTML...........");
		R01HLODRequestedURIData reqData1 = new R01HLODRequestedURIData(config,
																	   Url.from(host,
																			    UrlPath.from("def")
																			   		   .joinedWith(ontologyName + ".owl")),
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData1.debugInfo());
		R01HLODHandledURIData handleData1 = resHandler.handle(reqData1);
		log.info("Request handle data: {}",
				 handleData1.debugInfo());
		Assert.assertTrue(handleData1.getAction() == R01HLODURIHandleAction.SERVER_REDIRECT
					   && handleData1.as(R01HLODHandledURIDataForServerRedirect.class)
					   				 .getUrlPath().equals(UrlPath.from("owl")
						       									 .joinedWith(ontologyName + ".owl")));
		
		// [2] - Ontology abstract: /def/{ontology}.html mime type=HTML
		log.info("[2] - Resource: /def/{ontology}.html mime-type=HTML...........");
		R01HLODRequestedURIData reqData2 = new R01HLODRequestedURIData(config,
																	   Url.from(host,
																			    UrlPath.from("def")
																			   		   .joinedWith(ontologyName + ".html")),
																	   null,		// no body
																	   R01HMIMEType.HTML);
		log.info("Request data: {}",
				 reqData2.debugInfo());
		R01HLODHandledURIData handleData2 = resHandler.handle(reqData2);
		log.info("Request handle data: {}",
				 handleData2.debugInfo());
		Assert.assertTrue(handleData2.getAction() == R01HLODURIHandleAction.CLIENT_REDIRECT
					   && handleData2.as(R01HLODHandledURIDataForClientRedirect.class)
					   				 .getTargetUrl().getUrlPath()
					   				 				.is(UrlPath.from("pages")
						       									 .joinedWith(ontologyName + ".html")));
	}	
}
