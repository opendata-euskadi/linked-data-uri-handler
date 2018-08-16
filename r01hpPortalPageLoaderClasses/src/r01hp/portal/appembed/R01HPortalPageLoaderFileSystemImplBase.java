package r01hp.portal.appembed;

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import r01f.file.FileProperties;
import r01f.filestore.api.FileStoreAPI;
import r01f.types.Path;
import r01f.util.types.Paths;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForFileSystemImpl;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import r01hp.portal.common.R01HPortalPageCopy;

@Slf4j
abstract class R01HPortalPageLoaderFileSystemImplBase 
       extends R01HPortalPageLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    private final R01HPortalPageLoaderConfigForFileSystemImpl _config;
    private final FileStoreAPI _fileStoreApi;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageLoaderFileSystemImplBase(final R01HPortalPageLoaderConfigForFileSystemImpl cfg,
    											  final FileStoreAPI api) {
    	_config = cfg;
    	_fileStoreApi = api;
    }
	public R01HPortalPageLoaderFileSystemImplBase(final Path appContainerPageFilesWorkingCopyRootPath,final Path appContainerPageFilesLiveCopyRootPath,
											  	  final Path appContainerPageFilesRelPath,
											  	  final FileStoreAPI api) {
		_config = new R01HPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
																  appContainerPageFilesRelPath);
		_fileStoreApi = api;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	R01HPortalPageLoader
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLoadedContainerPortalPage loadWorkCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01HPortalPageCopy.WORK);
	}
	@Override
	public R01HLoadedContainerPortalPage loadLiveCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01HPortalPageCopy.LIVE);		
	}
	private R01HLoadedContainerPortalPage _loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId,
												   final R01HPortalPageCopy copy) {
        // Container page file path
		Path rootPath = copy.is(R01HPortalPageCopy.WORK) ? _config.getAppContainerPageFilesWorkingCopyRootPath()
														 : _config.getAppContainerPageFilesLiveCopyRootPath();
        Path portalPageFilePath = Paths.forPaths().join(rootPath,
        												portalId,
        												_config.getAppContainerPageFilesRelPath(),
        												Strings.customized("{}-{}.shtml",
									    						  		   portalId,pageId));
		// load
    	InputStream is = null;
    	long lastModifiedTimeStamp = 0;
    	try {
    		FileProperties props = null;
    		try {
    			props = _fileStoreApi.getFileProperties(portalPageFilePath);
    		} catch(Throwable th) {
    			// error loading the page!
    			if (!_fileStoreApi.existsFile(portalPageFilePath)) {
    				log.error("Portal page {}-{} at {} does NOT exists!!",
    						  portalId,pageId,
    						  portalPageFilePath);
    				return null;	// the page does NOT exists: cannot be loaded
    			} else {
    				throw th;		// throw the original exception
    			}
    		}
    		// the page exists
	        lastModifiedTimeStamp = props.getModificationTimeStamp();
	        is = _fileStoreApi.readFromFile(portalPageFilePath);
	        log.info("... loaded portal page {}-{} from {}",
	        		 portalId,pageId,
	        		 portalPageFilePath);
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading a portal page {}-{} file at {}: {}",
    				  portalId,pageId,portalPageFilePath,th.getMessage(),
    				  th);
    	} 
    	// return
    	return new R01HLoadedContainerPortalPage(portalId,pageId,
    											 copy,
    											 lastModifiedTimeStamp,
    											 portalPageFilePath,
    											 is); 
	}

}
