package r01hp.lod.urihandler.handlers;

import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForClientRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * Handles uris like: <pre>http://site/def/{ontology}</pre>
 */
public class R01HLODURIHandlerForOntologyDefinition 
     extends R01HLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIHandlerForOntologyDefinition(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		// Get the ontology name from the url:
		//		http://site/def/{ontologyName}
		String ontologyName = data.getRequestedResourceUrlPath().getPathElementAt(0);
		

		if (ontologyName.endsWith(".owl")
		 || data.isAcceptingMime(R01HMIMEType.RDFXML)) {
			// server redirect to /owl/{ontology}
			return new R01HLODHandledURIDataForServerRedirect(UrlPath.from("/owl/" + ontologyName),
																	  data.getRequestQueryString(),
																	  R01HMIMEType.RDFXML);
		} else if (ontologyName.endsWith(".html")
		 || data.isRequestingHTMLFromMimes()) {
			// client redirect to /pages/{ontology}
			return new R01HLODHandledURIDataForClientRedirect(UrlPath.from("/pages/" + ontologyName),
																	  data.getRequestQueryString(),
																	  data.getRequestedUrlAnchor());
		} else {
			throw new IllegalArgumentException("NOT a valid ontology request!");
		}
	}
}
