package r01hp.portal.appembed.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.Environment;
import r01f.types.Path;
import r01f.util.OSUtils;
import r01f.util.OSUtils.OSType;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.appembed.R01HPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01HPortalPageLoaderConfigForFileSystemImpl 
     extends R01HPortalPageLoaderConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _appContainerPageFilesWorkingCopyRootPath;
	@Getter private final Path _appContainerPageFilesLiveCopyRootPath;
	@Getter private final Path _appContainerPageFilesRelPath;
	@Getter private final R01HPortalPageLoaderImpl _impl = R01HPortalPageLoaderImpl.FILE_SYSTEM;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageLoaderConfigForFileSystemImpl() {
		this(Path.from("/datos/r01hp/file/aplic/"),Path.from("/datos/r01hp/file/aplic/"),
			 Path.from("/html/pages/portal"));
	}
	public R01HPortalPageLoaderConfigForFileSystemImpl(final Path appContainerPageFilesWorkingCopyRootPath,
													   final Path appContainerPageFilesLiveCopyRootPath,
													   final Path appContainerPageFilesRelPath) {
		_appContainerPageFilesWorkingCopyRootPath = appContainerPageFilesWorkingCopyRootPath;
		_appContainerPageFilesLiveCopyRootPath = appContainerPageFilesLiveCopyRootPath;
		_appContainerPageFilesRelPath = appContainerPageFilesRelPath;
	}
	public R01HPortalPageLoaderConfigForFileSystemImpl(final XMLPropertiesForAppComponent props) {
		this(// files root
			 props.propertyAt("portalpageappembedfilter/@environment").asEnvironment("loc")
				  .is(Environment.LOCAL) ? props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/fileSystemLoader/workingCopyRoot")
										   		.asPath(OSUtils.getOS() == OSType.WINDOWS ? "d:/temp_dev/r01hp/"
										   												  : "/datos/r01hp/file/aplic/")
										 : props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/fileSystemLoader/workingCopyRoot")
									   	   		.asPath("/datos/r01hp/file/aplic/"),
			 props.propertyAt("portalpageappembedfilter/@environment").asEnvironment("loc")
				  .is(Environment.LOCAL) ? props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/fileSystemLoader/liveCopyRoot")
										   		.asPath(OSUtils.getOS() == OSType.WINDOWS ? "d:/temp_dev/r01hp/"
										   												  : "/datos/r01hp/file/aplic/")
										 : props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/fileSystemLoader/liveCopyRoot")
									   	   		.asPath("/datos/r01hp/file/aplic/"),
			 // page files rel path
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/fileSystemLoader/pages")
				  .asPath("/html/pages/portal"));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01HPortalPageLoaderConfig cloneOverriddenWith(final R01HPortalPageLoaderConfig other) {
		R01HPortalPageLoaderConfigForFileSystemImpl otherFS = (R01HPortalPageLoaderConfigForFileSystemImpl)other;
		Path appContainerPageFilesWorkingCopyRootPath = otherFS.getAppContainerPageFilesWorkingCopyRootPath() != null ? otherFS.getAppContainerPageFilesWorkingCopyRootPath()
																										   			  : this.getAppContainerPageFilesWorkingCopyRootPath();
		Path appContainerPageFilesLiveCopyRootPath = otherFS.getAppContainerPageFilesLiveCopyRootPath() != null ? otherFS.getAppContainerPageFilesLiveCopyRootPath()
																										   		: this.getAppContainerPageFilesLiveCopyRootPath();
		Path appContainerPageFilesRelPath = otherFS.getAppContainerPageFilesRelPath() != null ? otherFS.getAppContainerPageFilesRelPath()
																								: this.getAppContainerPageFilesRelPath();		
		return new R01HPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
															   appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tFile System page loader using pattern: {}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for WORKING copy and " +
								    									   "{}/%PORTAL%/{}/%PORTAL%-%PAGE%.shtml for LIVE copy",
								  // working
								  _appContainerPageFilesWorkingCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString(),
								  // live
								  _appContainerPageFilesLiveCopyRootPath.asAbsoluteString(),
								  _appContainerPageFilesRelPath.asRelativeString());
	}
}
