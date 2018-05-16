package r01hp.portal.appembed.config;

import r01f.debug.Debuggable;

/**
 * portal pages providen impl-dependent config
 */
public interface R01HPortalPageProviderConfig 
		 extends Debuggable {
	public R01HPortalPageProviderConfig cloneOverriddenWith(final R01HPortalPageProviderConfig other);
}
