package r01hp.portal.appembed;

import java.io.InputStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import r01hp.portal.common.R01HPortalPageCopy;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLoadedContainerPortalPage 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final R01HPortalID _portalId;
	@Getter private final R01HPortalPageID _pageId;
	@Getter private final R01HPortalPageCopy _copy;
	@Getter private final long _lastModifiedTimeStamp;
	@Getter private final Path _filePath;
	@Getter private final InputStream _html;
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("portal={} page={} ({}) last modified timestamp={} > path={}",
								  _portalId,_pageId,
								  _copy,
								  _lastModifiedTimeStamp,
								  _filePath);
	}
}
