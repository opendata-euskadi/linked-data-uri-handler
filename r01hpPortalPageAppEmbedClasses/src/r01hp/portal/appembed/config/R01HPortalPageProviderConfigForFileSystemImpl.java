package r01hp.portal.appembed.config;

import javax.servlet.FilterConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.Environment;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HPortalPageProviderConfigForFileSystemImpl 
  implements R01HPortalPageProviderConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _appContainerPageFilesRootPath;
	@Getter private final Path _appContainerPageFilesRelPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageProviderConfigForFileSystemImpl() {
		this(Path.from("/datos/r01hp/file/aplic/"),
			 Path.from("/html/pages/portal"));
	}
	public R01HPortalPageProviderConfigForFileSystemImpl(final FilterConfig filterConfig) {
		String newAppContainerPageFilesRootPath = filterConfig.getInitParameter("r01hp.appembed.appContainerPageFilesRootPath");
		String newAppContainerPageFilesRelPathStr = filterConfig.getInitParameter("r01hp.appembed.appContainerPageFilesRelPath");
		if (newAppContainerPageFilesRootPath != null && newAppContainerPageFilesRelPathStr != null) {
			 log.warn("Location where to look after container page files overriden al web.xml (servlet filter init params): appContainerPageFilesRootPath={}, appContainerPageFilesRelPath={}",
					  newAppContainerPageFilesRootPath,newAppContainerPageFilesRelPathStr);
			
			 _appContainerPageFilesRootPath = Path.from(newAppContainerPageFilesRootPath);
			 _appContainerPageFilesRelPath = Path.from(newAppContainerPageFilesRelPathStr);
		} else {
			_appContainerPageFilesRootPath = null;
			_appContainerPageFilesRelPath = null;
		}
	}
	public R01HPortalPageProviderConfigForFileSystemImpl(final XMLPropertiesForAppComponent props) {
		this(// files root
			 props.propertyAt("portalpageappembedfilter/@environment").asEnvironment("loc")
				  .is(Environment.LOCAL) ? props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/root")
										   		.asPath("d:/temp_dev/r01hp/")
										 : props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/root")
									   	   		.asPath("/datos/r01hp/file/aplic/"),
			 // page files rel path
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/pages")
				  .asPath("/html/pages/portal"));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01HPortalPageProviderConfig cloneOverriddenWith(final R01HPortalPageProviderConfig other) {
		R01HPortalPageProviderConfigForFileSystemImpl otherFS = (R01HPortalPageProviderConfigForFileSystemImpl)other;
		Path appContainerPageFilesRootPath = otherFS.getAppContainerPageFilesRootPath() != null ? otherFS.getAppContainerPageFilesRootPath()
																								: this.getAppContainerPageFilesRootPath();
		Path appContainerPageFilesRelPath = otherFS.getAppContainerPageFilesRelPath() != null ? otherFS.getAppContainerPageFilesRelPath()
																								: this.getAppContainerPageFilesRelPath();		
		return new R01HPortalPageProviderConfigForFileSystemImpl(appContainerPageFilesRootPath,
																 appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tFile System page loader using pattern: {}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml",
								  _appContainerPageFilesRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString());
	}
}
