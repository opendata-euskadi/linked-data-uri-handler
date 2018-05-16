package r01hp.portal.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.annotations.Immutable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.OIDBaseMutable;
import r01f.objectstreamer.annotations.MarshallType;
import r01f.patterns.Memoized;
import r01f.util.types.Strings;

/**
 * Portal model object oids
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class R01HPortalOIDs {
/////////////////////////////////////////////////////////////////////////////////////////
//  PORTAL
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Portal code (ie web01)
	 */
	@MarshallType(as="portal")
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
	@MarshallType(as="portalPage")
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
/////////////////////////////////////////////////////////////////////////////////////////
//	PORTAL & PAGE
/////////////////////////////////////////////////////////////////////////////////////////
	@MarshallType(as="portalAndPage")
	@Immutable
	@Accessors(prefix="_")
    public static class R01HPortalAndPage 
                extends OIDBaseMutable<String> {
    	
		private static final long serialVersionUID = 2563597475930306545L;
		
		private Memoized<String[]> _components = new Memoized<String[]>() {
														@Override
														protected String[] supply() {
															return _componentsFrom(R01HPortalAndPage.this.getId());
														}
												 };
		private Memoized<R01HPortalID> _portalId = new Memoized<R01HPortalID>() {
															@Override
															protected R01HPortalID supply() {
																return R01HPortalID.forId(_components.get()[0]);
															}
												   };
		private Memoized<R01HPortalPageID> _pageId = new Memoized<R01HPortalPageID>() {
															@Override
															protected R01HPortalPageID supply() {
																return R01HPortalPageID.forId(_components.get()[1]);
															}
												    };
		public R01HPortalAndPage(final R01HPortalID portalId,final R01HPortalPageID pageId) {
			super(Strings.customized("{}-{}",
									 portalId,pageId));	
		}
        
		public R01HPortalID getPortalId() {
			return _portalId.get();
		}
		public R01HPortalPageID getPageId() {
			return _pageId.get();
		}
	
		private static final Pattern PORTAL_AND_PAGE_PATTERN = Pattern.compile("([^-]+)-([^-]+)");
		
		private static String[] _componentsFrom(final String str) {
			Preconditions.checkArgument(Strings.isNOTNullOrEmpty(str),
										"Portal and page string representation cannot be null");
			String[] outPortalAndPage = null;
			Matcher m = PORTAL_AND_PAGE_PATTERN.matcher(str);
			if (m.find()) {
				outPortalAndPage = new String[] {m.group(1),m.group(2)};
			}
			else {
				throw new IllegalArgumentException("Portal and page string " + str + " does NOT match the pattern " + PORTAL_AND_PAGE_PATTERN);
			}
			return outPortalAndPage;
		}
		public static R01HPortalAndPage from(final String str) {
			String[] components = _componentsFrom(str);
			R01HPortalAndPage outPortalAndPage = new R01HPortalAndPage(R01HPortalID.forId(components[0]),R01HPortalPageID.forId(components[1]));
			return outPortalAndPage;
		}
		public static R01HPortalAndPage valueOf(final String str) {
			return R01HPortalAndPage.from(str);
		}
    }
	
}
