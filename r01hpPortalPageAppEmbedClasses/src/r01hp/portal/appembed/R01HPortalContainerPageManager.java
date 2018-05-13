package r01hp.portal.appembed;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.types.Path;
import r01f.util.types.Strings;

/**
 * Last recently used templates manager
 * Stores last recently used templates in memory to avoid disk access
 */
@Slf4j
@Singleton
public class R01HPortalContainerPageManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HPortalContainerPagesManagerConfig _config;
    private final LoadingCache<Path,CacheElement> _cache;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
    @Inject
    public R01HPortalContainerPageManager(final R01HPortalContainerPagesManagerConfig config) {
       _config = config;

       _cache = CacheBuilder.newBuilder()
        					.initialCapacity(_config.getInitialCapacity())
        					.maximumSize(_config.getMaxSize())
        					.removalListener(new RemovalListener<Path,CacheElement>() {
													@Override
													public void onRemoval(final RemovalNotification<Path,CacheElement> notification) {
														log.debug("{} was evited due to {}; it was used {} times since it was cached",
																  notification.getKey(),notification.getCause(),
																  notification.getValue().hitCount);
													}
        									 })
        					.build(new CacheLoader<Path,CacheElement>() {
											@Override
											public CacheElement load(final Path path) throws Exception {
												return _loadAppContainerPageCacheElement(path);
											}
        					 		});
    }
///////////////////////////////////////////////////////////////////////////////////////////
//
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a app container page reader
     * @param appContainerPagePath template path
     * @return page file reader
     */
    public R01HPortalContainerPage getAppContainerPage(final Path appContainerPagePath) {
        log.debug("Portal container page cache manager > load path with path={}",appContainerPagePath);

        // try to get the page from the cache:
        //		- If the page is NOT cached it's loaded from the fileSystem and cached
        //		- If the page DOES NOT exists at the fileSytem, it returns the DEFAULT PAGE
        //		  (beware that if in a later moment the page is present in the fileSystem, the cache entry MUST be invalidated
        //		   because it's associated with the DEFAULT PAGE)
        CacheElement cachedElement = _cache.getUnchecked(appContainerPagePath);		// getUnchecked = the cache loader does NOT throw any exception (instead use _cache.get())
        log.debug("Cache loaded page: {}",
        		  cachedElement.debugInfo());

        // if it's the default page nothing else have to be done
        if (cachedElement.isDefaultAppContainerPage) {
        	// maybe the page is now present and the cache entry must be invalidated
        	if (cachedElement.lastCheckTimeStamp + _config.getAppContainerPageModifiedCheckIntervalMilis() < System.currentTimeMillis()) {
	        	File appContainerPageFile = new File(appContainerPagePath.asAbsoluteString());
	        	if (appContainerPageFile.exists()) {
	        		// the page is now present... invalidate the cache that points to the DEFAULT_PAGE
	        		_cache.invalidate(appContainerPagePath);
	        		// force the page caching (now we're sure that the page exists)
	        		return this.getAppContainerPage(appContainerPagePath);
	        	}
	        	// update the timestamp of the last page update checking
	        	cachedElement.lastCheckTimeStamp = System.currentTimeMillis();
        	}
	        // increase the number of times the page has been used
	        cachedElement.hitCount++;

        	// the page is still not present; return the default container page
        	return cachedElement.html;
        }

        if (cachedElement.lastCheckTimeStamp + _config.getAppContainerPageModifiedCheckIntervalMilis() < System.currentTimeMillis()) {
        	// need to check if the app container page at the filesystem has been changed
        	File appContainerPageFile = new File(appContainerPagePath.asAbsoluteString());
        	if (!appContainerPageFile.exists()) {
        		log.error("The app container page file at {} previously existed but somehow it has been deleted: invalidate in cache",
        				  appContainerPagePath.asAbsoluteString());
        		// the page was DELETED from the fileSystem, so the cache MUST be invalidated
        		_cache.invalidate(appContainerPagePath);
        		// force the page caching (now we're sure that the page exists)
        		return this.getAppContainerPage(appContainerPagePath);		// recursive call... the cache entry will be now the DEFAULT_PAGE
        	} else {
        		long fileLastModifyTimeStamp = appContainerPageFile.lastModified();
        		if (fileLastModifyTimeStamp > cachedElement.lastModifiedTimeStamp) {
        			log.warn("The app container page file at {} has changed, it'll be reloaded",
        					 appContainerPagePath);
        			// the page is now present... invalidate the cache that points to the DEFAULT_PAGE
        			_cache.invalidate(appContainerPagePath);
        			// force the page caching (now we're sure that the page exists)
        			return this.getAppContainerPage(appContainerPagePath);		// recursive call... the cache entry will be reloaded
        		}
        	}
        	// update the timestamp of the last page update checking
        	cachedElement.lastCheckTimeStamp = System.currentTimeMillis();
        }
        // increase the number of times the page has been used
        cachedElement.hitCount++;

        // Return a the page
        return cachedElement.html;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CACHE ELEMENT
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Cache element
     */
    private class CacheElement
       implements Debuggable {

    	public final boolean isDefaultAppContainerPage;
        public final Path filePath;      			// app container page path
        public final R01HPortalContainerPage html;  // The app container page html content
        public final long lastModifiedTimeStamp; 	// last time the app container page file was modified
        public long lastCheckTimeStamp = -1;    	// last time the modify timestamp was checked
        public int hitCount = 0;		        	// Number of times the app container page has been accessed

        public CacheElement(final Path filePath,
        					final InputStream html,
        					final long lastModifiedTimeStamp) {
        	this.isDefaultAppContainerPage = filePath.is(_config.getDefaultContainerPageFileIfRequestedNotFound()) ? true : false;
        	this.filePath = filePath;
        	this.html = new R01HPortalContainerPage(filePath,
        										    html);
        	this.lastModifiedTimeStamp = lastModifiedTimeStamp;
        }
		@Override
		public CharSequence debugInfo() {
			return Strings.customized("path={} default={} lastModified={} lastCheck={} hitCount={}",
									  filePath,isDefaultAppContainerPage,lastModifiedTimeStamp,lastCheckTimeStamp,hitCount);
		}
    }
    private static ResourcesLoader FILESYSTEM_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01hFileSystemResourcesLoader")
    																																	  .usingFileSystemResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
    private static ResourcesLoader CLASSPATH_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01hClassPathResourcesLoader")
    																																	  .usingClassPathResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
    private CacheElement _loadAppContainerPageCacheElement(final Path appContainerPagePath) {
    	InputStream is = null;
    	try {
	    	// If loading the default page use the classpath loader
	    	if (appContainerPagePath == _config.getDefaultContainerPageFileIfRequestedNotFound()) {
	    		return _defaultAppContainerPageCacheElement();
	    	}
	        // ... otherwise, use the file loader
	        File appContainerPageFile = new File(appContainerPagePath.asAbsoluteString());

	        // If the app container page does NOT exists, return the default app container page
	        if (!appContainerPageFile.exists()) {
	            log.error("App container path was not found at path={} using the default app container page instead; {}",
	            		  appContainerPagePath.asAbsoluteString(),
	            		  _defaultAppContainerPageCacheElement());
	            return _defaultAppContainerPageCacheElement();
	        }
	        is = FILESYSTEM_RESOURCES_LOADER.getInputStream(appContainerPagePath);
	        CacheElement outEl = new CacheElement(appContainerPagePath,
												  is,
												  System.currentTimeMillis());
	        return outEl;
    	} catch(Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page file at {}",appContainerPagePath,th);
    		return _defaultAppContainerPageCacheElement();
    	} finally {
    		try {
    			if (is != null) is.close();
    		} catch(Throwable th) { /* ignore */ }
    	}
    }
    private CacheElement _defaultAppContainerPageCacheElement() {
    	InputStream is = null;
    	try {
    		log.warn("...loading default container page file if requested one not found from {}",
    				_config.getDefaultContainerPageFileIfRequestedNotFound());
    		is = CLASSPATH_RESOURCES_LOADER.getInputStream(_config.getDefaultContainerPageFileIfRequestedNotFound());
			return new CacheElement(_config.getDefaultContainerPageFileIfRequestedNotFound(),
									is,
									System.currentTimeMillis());
    	} catch(Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page file at {}",_config.getDefaultContainerPageFileIfRequestedNotFound(),th);
			return new CacheElement(_config.getDefaultContainerPageFileIfRequestedNotFound(),
									new ByteArrayInputStream(Strings.customized("<html>" +
																					"<body>" +
																						"<h1>Error loading the default app container page at {}</h1>" +
																						"<pre>{}</pre>" +
																					"</body>" +
																			     "</html>",
																			     _config.getDefaultContainerPageFileIfRequestedNotFound(),Throwables.getStackTraceAsString(th))
																	.getBytes()),
									System.currentTimeMillis());
    	} finally {
    		try {
    			if (is != null) is.close();
    		} catch(Throwable th) { /* ignore */ }
    	}
    }
}
