package r01hp.lod.urihandler;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.util.types.Strings;

@Accessors(prefix="_")
public class R01HLODHandledURIDataForClientRedirect
	 extends R01HLODHandledURIDataBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Url _targetUrl;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODHandledURIDataForClientRedirect(final UrlPath urlPath) {
		this(urlPath,
			 null,		// query string
			 null);		// anchor
	}
	public R01HLODHandledURIDataForClientRedirect(final UrlPath urlPath,final UrlQueryString urlQueryString) {
		this(urlPath,
			 urlQueryString,
			 null);		// anchor
	}
	public R01HLODHandledURIDataForClientRedirect(final UrlPath urlPath,final UrlQueryString urlQueryString,final String urlAnchor) {
		this(Url.from(urlPath,urlQueryString,urlAnchor));
	}
	public R01HLODHandledURIDataForClientRedirect(final Host host,final UrlPath urlPath,final UrlQueryString urlQueryString,final String urlAnchor) {
		this(Url.from(host,
					   urlPath,urlQueryString,urlAnchor));
	}
	public R01HLODHandledURIDataForClientRedirect(final Url url) {
		super(R01HLODURIHandleAction.CLIENT_REDIRECT);
		_targetUrl = url;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("Client redirect to {}",
								  _targetUrl);
	}
}
