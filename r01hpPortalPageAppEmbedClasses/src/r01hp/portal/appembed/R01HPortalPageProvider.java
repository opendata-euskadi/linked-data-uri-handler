package r01hp.portal.appembed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfig;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForFileSystemImpl;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForRESTServiceImpl;
import r01hp.portal.appembed.config.R01HPortalPageManagerConfig;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import r01hp.portal.common.R01HPortalPageCopy;

@Slf4j
@Accessors(prefix="_")
public class R01HPortalPageProvider {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HPortalPageManagerConfig _pageMgrConfig;
	private final R01HPortalPageLoader _loader;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageProvider(final R01HPortalPageManagerConfig pageMgrConfig,
								  final R01HPortalPageLoaderConfig pageLoaderConfig) throws IOException {
		_pageMgrConfig = pageMgrConfig;
		_loader = _createPortalPageLoaderFor(pageLoaderConfig);
	}
	private static R01HPortalPageLoader _createPortalPageLoaderFor(final R01HPortalPageLoaderConfig config) throws IOException {
		R01HPortalPageLoader outLoader = null;
		if (config.getImpl() == R01HPortalPageLoaderImpl.FILE_SYSTEM) {
			R01HPortalPageLoaderConfigForFileSystemImpl cfgForFileSystem = (R01HPortalPageLoaderConfigForFileSystemImpl)config;
			outLoader = new R01HPortalPageLoaderFileSystemImpl(cfgForFileSystem);
		}
		else if (config.getImpl() == R01HPortalPageLoaderImpl.REST_SERVICE) {
			R01HPortalPageLoaderConfigForRESTServiceImpl cfgForRESTService = (R01HPortalPageLoaderConfigForRESTServiceImpl)config;
			outLoader = new R01HPortalPageLoaderRESTServiceImpl(cfgForRESTService);
		}
		else {
			throw new IllegalArgumentException(config.getClass().getName() + " is NOT a supported loader!");
		}
		return outLoader;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the work copy of a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HPortalContainerPage provideWorkCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) {
		return this.provideFor(portalId,pageId,
							   R01HPortalPageCopy.WORK);
	}
	/**
	 * Returns the live copy of a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HPortalContainerPage provideLiveCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) {
		return this.provideFor(portalId,pageId,
							   R01HPortalPageCopy.LIVE);
	}
	/**
	 * Returns a portal page given the portal & page ids
	 * @param portalId
	 * @param pageId
	 * @param copy the page version (working copy / live copy)
	 * @return
	 */
	public R01HPortalContainerPage provideFor(final R01HPortalID portalId,final R01HPortalPageID pageId,
											  final R01HPortalPageCopy copy) {
        R01HPortalContainerPage outPage = null;
    	try {
    		// load the portal page
    		outPage = _loadFor(portalId,pageId,
    						   copy);
    		if (outPage == null) {
		        // the requested container page DOES NOT exists!
    			// ... try the last resource
    			InputStream is = null;
    			try {
    				is = _loadLastResourceAppContainerPage();
			        outPage = new R01HPortalContainerPage(portalId,pageId,
			        									  copy,
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
												   copy,
												   _wrapError(Strings.customized("Error loading the app container page {}-{}</h1>" +
																				 portalId,pageId),
														   	  th),
												  System.currentTimeMillis(),	// modified now
												  true);						// the last resource app container page HTML
    	} 
    	return outPage;
	}
	private R01HPortalContainerPage _loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId,
											 final R01HPortalPageCopy copy) {
        // load the page
        R01HPortalContainerPage outPage = null;
    	InputStream is = null;
    	try {
    		R01HLoadedContainerPortalPage loadedContainerPortalPage = null;
    		switch(copy) {
			case LIVE:
    			loadedContainerPortalPage = _loader.loadLiveCopyFor(portalId,pageId);
				break;
			case WORK:
				loadedContainerPortalPage = _loader.loadWorkCopyFor(portalId,pageId);
				break;
			default:
				loadedContainerPortalPage = _loader.loadLiveCopyFor(portalId,pageId);
				break;
    		
    		}
    		if (loadedContainerPortalPage == null
    		 || loadedContainerPortalPage.getHtml() == null) return null;		// the page does NOT exists!!!
    		
    		is = loadedContainerPortalPage.getHtml();
    		long lastModifiedTimeStamp = loadedContainerPortalPage.getLastModifiedTimeStamp();
	        outPage = new R01HPortalContainerPage(portalId,pageId,
	        									  copy,
	        								   	  is,
	        								   	  lastModifiedTimeStamp,
	        								   	  false);		// NOT the last resource app container page HTML
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{} file using loader {}: {}",
    				  portalId,pageId,
    				  _loader.getClass().getSimpleName(),
    				  th.getMessage(),
    				  th);
    	} finally {
    		try {
    			if (is != null) is.close();
    		} catch(Throwable th) { /* ignore */ }
    	}
    	return outPage;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static ResourcesLoader CLASSPATH_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01hClassPathResourcesLoader")
    																																	  .usingClassPathResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
    private InputStream _loadLastResourceAppContainerPage() {
    	InputStream is = null;
    	try {
    		log.warn("...loading last resource container page file to be used if requested one not found from {}",
    				 _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound());
    		is = CLASSPATH_RESOURCES_LOADER.getInputStream(_pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound());
			return is;
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading last resource app container page file at {}: {}",
    				  _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound(),th.getMessage(),
    				  th);
			return _wrapError(Strings.customized("Error loading the last resource app container page at {}</h1>",
												 _pageMgrConfig.getLastResourceContainerPageFileIfRequestedNotFound()),
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
