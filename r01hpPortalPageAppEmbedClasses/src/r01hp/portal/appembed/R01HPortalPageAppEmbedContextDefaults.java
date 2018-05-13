package r01hp.portal.appembed;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Environment;
import r01f.locale.Language;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

@Accessors(prefix="_")
public class R01HPortalPageAppEmbedContextDefaults
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _appContainerPageFilesRootPath;
	@Getter private final Path _appContainerPageFilesRelPath;
    @Getter private final R01HPortalID _defaultPortalId;
    @Getter private final R01HPortalPageID _defaultAppContainerPageId;
    @Getter private final Language _defaultLanguage;
    @Getter private final String _portalCookieName;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageAppEmbedContextDefaults(final Path appContainerPageFilesRootPath,final Path appContainerPageFilesRelPath,
    											 final R01HPortalID defPortalId,final R01HPortalPageID defPortalPageId,final Language defLang,
    											 final String portalCookieName) {
    	_appContainerPageFilesRootPath = appContainerPageFilesRootPath;
    	_appContainerPageFilesRelPath = appContainerPageFilesRelPath;
    	_defaultPortalId = defPortalId;
    	_defaultAppContainerPageId = defPortalPageId;
    	_defaultLanguage = defLang;
    	_portalCookieName = portalCookieName;
    }
    public static R01HPortalPageAppEmbedContextDefaults from(final Environment env,
    												  		 final XMLPropertiesForAppComponent props) {
		if (props == null) {
			return new R01HPortalPageAppEmbedContextDefaults(Path.from("d:/temp_dev/r01hp/"),Path.from("/html/pages/portal"),
																  R01HPortalID.forId("web01"),R01HPortalPageID.forId("ejeduki"),Language.DEFAULT,
																  "r01hpPortalCookie");
		} 
		else {
			// init pseudo-constans (they're NOT constants to use the injected properties)	
	    	Path appContainerPageFilesRootPath = null;
	    	if (env.is("loc")) {
				appContainerPageFilesRootPath = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/root")
													 .asPath("d:/temp_dev/r01hp/");
			} else {
				appContainerPageFilesRootPath = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/root")
													 .asPath("/datos/r01hp/file/aplic/");
			}
			Path appContainerPagesRelPath = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/pages")
											 	 .asPath("/html/pages/portal");
			R01HPortalID defaultPortalId = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPortal")
									  			.asObjectFromString(R01HPortalID.class,"web01");
			R01HPortalPageID defaultAppContainerPageId = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPage")
															  .asObjectFromString(R01HPortalPageID.class,"ejeduki");
			Language defaultLanguage = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultLang")
								    		.asLanguageFromCode(Language.DEFAULT);
			String portalCookieName = props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/portalCookieName")
									 	   .asString("r01hpPortalCookie");
			
			return new R01HPortalPageAppEmbedContextDefaults(appContainerPageFilesRootPath,appContainerPagesRelPath,
															 defaultPortalId,defaultAppContainerPageId,defaultLanguage,
															 portalCookieName);
		}
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-App container page files path: {}/{}\n" + 
								  "\t- Default portal / page / lang: {}-{}/{}\n" + 
								  "\t-           Portal cookie name: {}",
								  _appContainerPageFilesRootPath,_appContainerPageFilesRelPath.asRelativeString(),
								  _defaultPortalId,_defaultAppContainerPageId,_defaultLanguage,
								  _portalCookieName);
	}    
}
