package r01hp.portal.appembed;

import java.io.InputStream;

import r01hp.portal.appembed.config.R01HPortalPageProviderConfig;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

/**
 * Interface for types in charge of provide portal container pages
 */
public interface R01HPortalPageProvider {
	/**
	 * @return the page provider config
	 */
	public R01HPortalPageProviderConfig getConfig();
	/**
	 * Returns an {@link InputStream} to the app container page
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HPortalContainerPage loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId);
}
