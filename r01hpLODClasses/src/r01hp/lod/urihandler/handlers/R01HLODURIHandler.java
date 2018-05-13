package r01hp.lod.urihandler.handlers;

import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODRequestedURIData;

/**
 * Marker interface for URIHandler types
 */
public interface R01HLODURIHandler {
	/**
	 * Handles an url type
	 * @param data
	 * @return
	 */
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data);
}
