package r01hp.portal.appembed;

import javax.inject.Inject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Singleton;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01hp.portal.appembed.config.R01HPortalPageManagerConfig;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalAndPage;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

/**
 * Last recently used templates manager
 * Stores last recently used templates in memory to avoid disk access
 */
@Slf4j
@Accessors(prefix="_")
@Singleton
public class R01HPortalPageManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01HPortalPageManagerConfig _config;
	@Getter private final R01HPortalPageProviderWrapper _portalContainerPagesProvider;
	
    		private final LoadingCache<R01HPortalAndPage,R01HPortalContainerPage> _cache;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
    @Inject
    public R01HPortalPageManager(final R01HPortalPageManagerConfig config,
    							 final R01HPortalPageProvider pageProvider) {
       _config = config;
       _portalContainerPagesProvider = new R01HPortalPageProviderWrapper(pageProvider,
    		   															 config.getLastResourceContainerPageFileIfRequestedNotFound());
       log.warn("Creating a portal page cache with initial size={} and max size={}. The elements at the cache will be checked every {}",
    		   	config.getInitialCapacity(),config.getMaxSize(),
    		   	config.getAppContainterPageModifiedCheckInterval());
       _cache = CacheBuilder.newBuilder()
        					.initialCapacity(_config.getInitialCapacity())
        					.maximumSize(_config.getMaxSize())
        					.removalListener(new RemovalListener<R01HPortalAndPage,R01HPortalContainerPage>() {
													@Override
													public void onRemoval(final RemovalNotification<R01HPortalAndPage,R01HPortalContainerPage> notification) {
														log.debug("{} was evited due to {}; it was used {} times since it was cached",
																  notification.getKey(),notification.getCause(),
																  notification.getValue().getHitCount());
													}
        									 })
        					.build(new CacheLoader<R01HPortalAndPage,R01HPortalContainerPage>() {
											@Override
											public R01HPortalContainerPage load(final R01HPortalAndPage portalAndPage) throws Exception {
												return _portalContainerPagesProvider.loadFor(portalAndPage.getPortalId(),
																						 	 portalAndPage.getPageId());
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
    public R01HPortalContainerPage getAppContainerPage(final R01HPortalID portalId,final R01HPortalPageID pageId) {
    	R01HPortalAndPage portalAndPage = new R01HPortalAndPage(portalId,pageId);
        log.debug("Portal container page cache manager > load portal-page={}",
        		  portalAndPage);

        // try to get the page from the cache:
        //		- If the page is NOT cached it's loaded from the fileSystem and cached
        //		- If the page DOES NOT exists at the fileSytem, it returns the DEFAULT PAGE
        //		  (beware that if in a later moment the page is present in the fileSystem, the cache entry MUST be invalidated
        //		   because it's associated with the DEFAULT PAGE)
        R01HPortalContainerPage cachedElement = _cache.getUnchecked(portalAndPage);	// getUnchecked = the cache loader does NOT throw any exception (instead use _cache.get())
        if (log.isTraceEnabled()) log.trace("Cache loaded page:\n{}",
        		  							cachedElement.debugInfo());

        // need to check if the app container page at the file system has been changed
        // the cache should be updated if:
        //		- the page has been modified
        //		- the cached page is the last resource and the loaded one is not
        if (cachedElement.getLastCheckTimeStamp() + _config.getAppContainerPageModifiedCheckIntervalMilis() < System.currentTimeMillis()) {
        	log.info("Portal page={}-{} was last checked {} milis ago. The check period of {} milis is over: check again",
        			cachedElement.getPortalId(),cachedElement.getPageId(),
        			 System.currentTimeMillis() - cachedElement.getLastCheckTimeStamp(),
        			 _config.getAppContainerPageModifiedCheckIntervalMilis());
        	// try to load the app container page again...
        	R01HPortalContainerPage loadedPage = _portalContainerPagesProvider.loadFor(portalId,pageId);
        	
        	// maybe the page was deleted
        	if (loadedPage.isContainsLastResourceContainerPageHtml() && !cachedElement.isContainsLastResourceContainerPageHtml()) {
        		log.warn("It seems that page {} was DELETED... keep the loaded one (it's NOT refreshed)",
        				 portalAndPage);
        	}
        	// if it was loaded or now it does NOT contains the last resource app container page
        	else if ( (loadedPage.getLastModifiedTimeStamp() > cachedElement.getLastModifiedTimeStamp())
        	      ||  (cachedElement.isContainsLastResourceContainerPageHtml() && !loadedPage.isContainsLastResourceContainerPageHtml()) ) {
        			log.warn("The app container page {} has changed, it'll be reloaded",
        					 portalAndPage);
        			// the page is now present... invalidate the cache that points to the DEFAULT_PAGE
        			_cache.invalidate(portalAndPage);
        			
        			// force the page caching (now we're sure that the page exists)
        			_cache.put(portalAndPage,loadedPage);
        			return this.getAppContainerPage(portalId,pageId);		// recursive call... the cache entry will be reloaded
        	}
        	// update the timestamp of the last page update checking
        	cachedElement.setLastCheckTimeStamp(System.currentTimeMillis());
        }
        // increase the number of times the page has been used
        cachedElement.setHitCount(cachedElement.getHitCount() + 1);

        // Return a the page
        return cachedElement;
    }
}