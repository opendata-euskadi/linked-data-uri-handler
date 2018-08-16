package r01hp.portal.appembed.config;

import r01f.debug.Debuggable;
import r01hp.portal.appembed.R01HPortalPageLoaderImpl;

/**
 * portal pages providen impl-dependent config
 */
public interface R01HPortalPageLoaderConfig 
		 extends Debuggable {
	public R01HPortalPageLoaderConfig cloneOverriddenWith(final R01HPortalPageLoaderConfig other);
	/**
	 * @return the impl
	 */
	public R01HPortalPageLoaderImpl getImpl();
	/**
	 * @return true if the loader loads the pages from the file system
	 */
	public boolean loadsPagesFromFileSystem();
	/**
	 * @return true if the loader loads the pages from the file system
	 */
	public boolean loadsPagesFromRESTService();
}
