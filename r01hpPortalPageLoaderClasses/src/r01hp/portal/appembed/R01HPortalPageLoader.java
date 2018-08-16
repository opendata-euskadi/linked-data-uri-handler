package r01hp.portal.appembed;

import java.io.IOException;

import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

/**
 * Interface for types in charge of provide portal container pages
 */
public interface R01HPortalPageLoader {
	/**
	 * Returns the work version of a {@link R01HLoadedContainerPortalPage} to the app container page
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HLoadedContainerPortalPage loadWorkCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException;
	/**
	 * Returns the live version of a {@link R01HLoadedContainerPortalPage} to the app container page
	 * @param portalId
	 * @param pageId
	 * @return
	 */
	public R01HLoadedContainerPortalPage loadLiveCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException;
}
