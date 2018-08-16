package r01hp.portal.common;

import lombok.RequiredArgsConstructor;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;

/**
 * A managed object (portal, content, etc) copy: working copy or staging copy
 */
@RequiredArgsConstructor
public enum R01HPortalPageCopy 
 implements EnumExtended<R01HPortalPageCopy> {
	WORK,
	LIVE;
	
	private static final EnumExtendedWrapper<R01HPortalPageCopy> DELEGATE = EnumExtendedWrapper.wrapEnumExtended(R01HPortalPageCopy.class);
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isIn(final R01HPortalPageCopy... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01HPortalPageCopy el) {
		return DELEGATE.is(this,el);
	}
}
