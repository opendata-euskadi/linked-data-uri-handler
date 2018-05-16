package r01hp.portal.appembed;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.locale.Language;
import r01f.util.types.Strings;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

@Accessors(prefix="_")
public class R01HPortalPageAppEmbedContextDefaults
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final R01HPortalID _defaultPortalId;
    @Getter private final R01HPortalPageID _defaultAppContainerPageId;
    @Getter private final Language _defaultLanguage;
    @Getter private final String _portalCookieName;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageAppEmbedContextDefaults(final R01HPortalID defPortalId,final R01HPortalPageID defPortalPageId,final Language defLang) {
    	this(defPortalId,defPortalPageId,defLang,
    		 "r01hpPortalCookie");
    }
    public R01HPortalPageAppEmbedContextDefaults(final R01HPortalID defPortalId,final R01HPortalPageID defPortalPageId,final Language defLang,
    											 final String portalCookieName) {
    	_defaultPortalId = defPortalId;
    	_defaultAppContainerPageId = defPortalPageId;
    	_defaultLanguage = defLang;
    	_portalCookieName = portalCookieName;
    }
    
/////////////////////////////////////////////////////////////////////////////////////////
//	CLONE
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageAppEmbedContextDefaults cloneOverriddenWith(final R01HPortalPageAppEmbedContextDefaults other) {
    	R01HPortalID portalId = other.getDefaultPortalId() != null ? other.getDefaultPortalId() : this.getDefaultPortalId();
    	R01HPortalPageID pageId = other.getDefaultAppContainerPageId() != null ? other.getDefaultAppContainerPageId() : this.getDefaultAppContainerPageId();
    	Language lang = other.getDefaultLanguage() != null ? other.getDefaultLanguage() : this.getDefaultLanguage();
    	String cokieName = Strings.isNOTNullOrEmpty(other.getPortalCookieName()) ? other.getPortalCookieName() : this.getPortalCookieName();
    	return new R01HPortalPageAppEmbedContextDefaults(portalId,pageId,lang,
    													 cokieName);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t- Default portal / page / lang: {}-{}/{}\n" + 
								  "\t-           Portal cookie name: {}",
								  _defaultPortalId,_defaultAppContainerPageId,_defaultLanguage,
								  _portalCookieName);
	}    
}
