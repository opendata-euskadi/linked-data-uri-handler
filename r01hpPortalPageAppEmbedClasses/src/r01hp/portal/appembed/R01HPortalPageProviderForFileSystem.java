package r01hp.portal.appembed;

import java.io.File;
import java.io.InputStream;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderDefBuilder;
import r01f.types.Path;
import r01f.util.types.Paths;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageProviderConfig;
import r01hp.portal.appembed.config.R01HPortalPageProviderConfigForFileSystemImpl;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

@Slf4j
@Accessors(prefix="_")
public class R01HPortalPageProviderForFileSystem
  implements R01HPortalPageProvider {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static ResourcesLoader FILESYSTEM_RESOURCES_LOADER = ResourcesLoaderBuilder.createResourcesLoaderFor(ResourcesLoaderDefBuilder.create("r01hFileSystemResourcesLoader")
    																																	  .usingFileSystemResourcesLoader()
    																																	  .notReloading()
    																																	  .defaultCharset()
    																																	  .build());
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01HPortalPageProviderConfig _config;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageProviderForFileSystem(final R01HPortalPageProviderConfig cfg) {
		if (!(cfg instanceof R01HPortalPageProviderConfigForFileSystemImpl)) throw new IllegalArgumentException("Must be an instance of " + R01HPortalPageProviderConfigForFileSystemImpl.class.getName());
		_config = cfg;
	}
	public R01HPortalPageProviderForFileSystem(final Path appContainerPageFilesRootPath,final Path appContainerPageFilesRelPath) {
		_config = new R01HPortalPageProviderConfigForFileSystemImpl(appContainerPageFilesRootPath,appContainerPageFilesRelPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HPortalContainerPage loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId) {
		R01HPortalPageProviderConfigForFileSystemImpl fsCfg = (R01HPortalPageProviderConfigForFileSystemImpl)_config;
        // Container page file path
        Path appContainerPageFilePath = Paths.forPaths().join(fsCfg.getAppContainerPageFilesRootPath(),
        													  portalId,
        													  fsCfg.getAppContainerPageFilesRelPath(),
        													  Strings.customized("{}-{}.shtml",
									    						  		   		 portalId,pageId));
        // load the page
        R01HPortalContainerPage outPage = null;
    	InputStream is = null;
    	try {
	        File appContainerPageFile = new File(appContainerPageFilePath.asAbsoluteString());
	        if (appContainerPageFile.exists()) {
		        is = FILESYSTEM_RESOURCES_LOADER.getInputStream(appContainerPageFilePath);
		        outPage = new R01HPortalContainerPage(portalId,pageId,
		        								   	  is,
		        								   	  appContainerPageFile.lastModified(),
		        								   	  false);		// NOT the last resource app container page HTML
		        log.info("... loaded app container page {}-{} from {}",
		        		 portalId,pageId,
		        		 appContainerPageFilePath);
	        } 
	        else {
		        // the requested container page DOES NOT exists!
		        // ... return null
		        log.warn("... the requested app container page {}-{} does NOT exist at {}",
		        		 portalId,pageId,
		        		 appContainerPageFilePath);
	        }
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{} file at {}: {}",
    				  portalId,pageId,appContainerPageFilePath,th.getMessage(),
    				  th);
    	} finally {
    		try {
    			if (is != null) is.close();
    		} catch(Throwable th) { /* ignore */ }
    	}
    	return outPage;
	}
}
