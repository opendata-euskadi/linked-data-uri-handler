package r01hp.portal.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.annotations.Immutable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.OIDBaseMutable;

/**
 * Portal model object oids
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class R01HPortalOIDs {
/////////////////////////////////////////////////////////////////////////////////////////
//  PORTAL
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Portal code (ie r33)
	 */
	@Immutable
	@NoArgsConstructor
	public static class R01HPortalID 
				extends OIDBaseMutable<String> {
		private static final long serialVersionUID = 3468415604309210166L;
		public R01HPortalID(final String oid) {
			super(oid);
		}
		public static R01HPortalID forId(final String id) {
			return new R01HPortalID(id);
		}
		public AppCode asAppCode() {
			return AppCode.forId(this.asString());
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PAGE
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Internal name for a portal page
	 */
	@Immutable
	@NoArgsConstructor
	public static class R01HPortalPageID 
			    extends OIDBaseMutable<String> {
		private static final long serialVersionUID = -4117464315684959622L;
		public R01HPortalPageID(final String oid) {
			super(oid);
		}
		public static R01HPortalPageID forId(final String id) {
			return new R01HPortalPageID(id);
		}
	}
}
