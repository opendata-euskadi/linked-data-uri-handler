package r01hp.portal.appembed.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.appembed.R01HPortalPageLoaderImpl;

@Accessors(prefix="_")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class R01HPortalPageLoaderConfigBase 
    	   implements R01HPortalPageLoaderConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean loadsPagesFromFileSystem() {
		return this.getImpl().is(R01HPortalPageLoaderImpl.FILE_SYSTEM);
	}
	@Override
	public boolean loadsPagesFromRESTService() {
		return this.getImpl().is(R01HPortalPageLoaderImpl.REST_SERVICE);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HPortalPageLoaderConfig createFrom(final XMLPropertiesForAppComponent props) {
		R01HPortalPageLoaderConfig outCfg = null;
		R01HPortalPageLoaderImpl pageLoaderImpl = R01HPortalPageLoaderImpl.configuredAt(props);
		switch(pageLoaderImpl) {
		case FILE_SYSTEM:
			outCfg = new R01HPortalPageLoaderConfigForFileSystemImpl(props);
			break;
		case REST_SERVICE:
			outCfg = new R01HPortalPageLoaderConfigForRESTServiceImpl(props);
			break;
		default:
			outCfg = new R01HPortalPageLoaderConfigForFileSystemImpl(props);
			break;
		}
		return outCfg;
	}
}
