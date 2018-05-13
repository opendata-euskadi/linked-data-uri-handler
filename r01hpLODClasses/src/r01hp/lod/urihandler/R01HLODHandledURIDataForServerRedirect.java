package r01hp.lod.urihandler;

import java.util.Map;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.util.types.Strings;

@Accessors(prefix="_")
public class R01HLODHandledURIDataForServerRedirect
	 extends R01HLODHandledURIDataBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final UrlPath _urlPath;
	@Getter private final UrlQueryString _urlQueryString;
	@Getter private final R01HMIMEType _mimeType;
	@Getter private final Map<String,Object> _requestAttributes;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODHandledURIDataForServerRedirect(final UrlPath urlPath,final UrlQueryString urlQueryString,
												  final R01HMIMEType mimeType,
												  final Map<String,Object> reqAttrs) {
		super(R01HLODURIHandleAction.SERVER_REDIRECT);
		_urlPath = urlPath;
		_urlQueryString = urlQueryString;
		_mimeType = mimeType;
		_requestAttributes = reqAttrs;
	}
	public R01HLODHandledURIDataForServerRedirect(final UrlPath urlPath,
												  final R01HMIMEType mimeType,
												  final Map<String,Object> reqAttrs) {
		this(urlPath,null,
			 mimeType,
			 reqAttrs);
	}
	public R01HLODHandledURIDataForServerRedirect(final UrlPath urlPath,
												  final R01HMIMEType mimeType) {
		this(urlPath,
			 mimeType,
			 null);		// no req attrs
	}
	public R01HLODHandledURIDataForServerRedirect(final UrlPath urlPath,final UrlQueryString urlQueryString,
												  final R01HMIMEType mimeType) {
		this(urlPath,urlQueryString,
			 mimeType,
			 null);		// no req attrs
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("Server redirect to {}{} (mime type={})",
								  _urlPath,
								  _urlQueryString != null ? "?" + _urlQueryString.asString() : "",
								  _mimeType);
	}
}
