package r01hp.portal.appembed;

import java.io.IOException;

import r01f.filestore.api.local.LocalFileStoreAPI;
import r01f.types.Path;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForFileSystemImpl;

public class R01HPortalPageLoaderFileSystemImpl 
     extends R01HPortalPageLoaderFileSystemImplBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageLoaderFileSystemImpl(final R01HPortalPageLoaderConfigForFileSystemImpl cfg) throws IOException {
    	super(cfg,
    		  new LocalFileStoreAPI());		// local file system
    }
	public R01HPortalPageLoaderFileSystemImpl(final Path appContainerPageFilesWorkingCopyRootPath,final Path appContainerPageFilesLiveCopyRootPath,
											  final Path appContainerPageFilesRelPath) throws IOException {
		this(new R01HPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesWorkingCopyRootPath,appContainerPageFilesLiveCopyRootPath,
															 appContainerPageFilesRelPath));
	}
}
