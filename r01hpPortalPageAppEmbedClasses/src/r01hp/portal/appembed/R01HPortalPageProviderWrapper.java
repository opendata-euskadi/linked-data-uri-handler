package r01hp.portal.appembed;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.base.Throwables;

import lombok.extern.slf4j.Slf4j;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageProviderConfig;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

@Slf4j
class R01HPortalPageProviderWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static ResourcesLoader CLASSPATH_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01hClassPathResourcesLoader")
    																																	  .usingClassPathResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    private final R01HPortalPageProvider _appContainerPageProvider;
    private final Path _lastResourceContainerPageFileIfRequestedNotFound;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    R01HPortalPageProviderWrapper(final R01HPortalPageProvider appContainerPageProvider,
    							  final Path lastResourceContainerPageFileIfRequestedNotFound) {
    	_appContainerPageProvider = appContainerPageProvider;
    	_lastResourceContainerPageFileIfRequestedNotFound = lastResourceContainerPageFileIfRequestedNotFound;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return the page provider config
     */
    public R01HPortalPageProviderConfig getConfig() {
    	return _appContainerPageProvider.getConfig();
    }
	/**
	 * Returns a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HPortalContainerPage loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId) {
        R01HPortalContainerPage outPage = null;
    	try {
    		// load the portal page
    		outPage = _appContainerPageProvider.loadFor(portalId,pageId);
    		if (outPage == null) {
		        // the requested container page DOES NOT exists!
    			// ... try the last resource
    			InputStream is = null;
    			try {
    				is = _loadLastResourceAppContainerPage();
			        outPage = new R01HPortalContainerPage(portalId,pageId,
			        								   	  is,
			        								   	  System.currentTimeMillis(),	// modified now
			        								   	  true);						// the last resource app container page HTML
    			} finally {
		    		try {
		    			if (is != null) is.close();
		    		} catch(Throwable th) { /* ignore */ }
		    	}
	        }
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{}: {}",
    				  portalId,pageId,th.getMessage(),
    				  th);
			outPage =  new R01HPortalContainerPage(portalId,pageId,
												   _wrapError(Strings.customized("Error loading the app container page {}-{}</h1>" +
																				 portalId,pageId),
														   	  th),
												  System.currentTimeMillis(),	// modified now
												  true);						// the last resource app container page HTML
    	} 
    	return outPage;
	}
    private InputStream _loadLastResourceAppContainerPage() {
    	InputStream is = null;
    	try {
    		log.warn("...loading last resource container page file to be used if requested one not found from {}",
    				 _lastResourceContainerPageFileIfRequestedNotFound);
    		is = CLASSPATH_RESOURCES_LOADER.getInputStream(_lastResourceContainerPageFileIfRequestedNotFound);
			return is;
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading last resource app container page file at {}: {}",
    				  _lastResourceContainerPageFileIfRequestedNotFound,th.getMessage(),
    				  th);
			return _wrapError(Strings.customized("Error loading the last resource app container page at {}</h1>",
												 _lastResourceContainerPageFileIfRequestedNotFound),
							  th);
    	} 
    }	
    private InputStream _wrapError(final String msg,final Throwable th) {
    	String errHtml = Strings.customized("<html>" +
										    "<body>" +
												"<h1>{}</h1>" +
												"<pre>{}</pre>" +
										    "</body>" +
										    "</html>",
										    msg,Throwables.getStackTraceAsString(th));
    	return new ByteArrayInputStream(errHtml.getBytes());
    }
}
