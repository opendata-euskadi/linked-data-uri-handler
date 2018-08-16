package r01hp.lod.urihandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.servlet.HttpServletRequestUtils;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01hp.lod.config.R01HLODURIHandlerConfig;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODRequestedURIData 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final int MAX_POST_DATA_CHARS = 1024;	// 1K
	public static final Pattern IGNORED_QUERY_STRING_PARAMS_NAME_PATTERN = Pattern.compile("R01H.*");
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Host _host;
	@Getter private final int _port;
	@Getter private final R01HLODURIType _uriType;
	@Getter private final R01HLODResourceType _resourceType;
	@Getter private final UrlPath _requestedResourceUrlPath;
	@Getter private final UrlQueryString _requestQueryString;
	@Getter private final String _requestedUrlAnchor;
	@Getter private Collection<R01HMIMEType> _acceptedMimes;	// see https://wiki.blazegraph.com/wiki/index.php/REST_API#MIME_Types for blazegraph accepted mime-types
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODRequestedURIData(final R01HLODURIHandlerConfig cfg,
								   final Url url,
								   final String formEncodedQueryString,
								   final R01HMIMEType... acceptedMimeTypes) {
		// host & port
		_host = url.getHost();
		_port = url.getPort();
		
		// path
		_requestedResourceUrlPath = url.getUrlPath();
		
		// query string or port body > SPARQL queries
		UrlQueryString urlEncodedQueryString = url.getQueryString();
		UrlQueryString formEncodedQryStr = _urlEncodedQueryString(formEncodedQueryString);
		_requestQueryString = _urlQueryStringOf(urlEncodedQueryString,
												formEncodedQryStr);
		// anchor
		_requestedUrlAnchor = url.getAnchor();
		// accept
		_acceptedMimes = Lists.newArrayList(acceptedMimeTypes);
		
		// uri type
		_uriType = R01HLODURIType.of(_host);
		_resourceType = R01HLODResourceType.of(_requestedResourceUrlPath);
	}
	public R01HLODRequestedURIData(final R01HLODURIHandlerConfig cfg,
								   final HttpServletRequest req) throws IOException {
		// the request could be proxied so the REAL host might NOT be the one returned by req.getServerName();
		// BEWARE!!!	This method reads the request body and this can be read only once.
		// 				If you the body is read in a filter, the target servlet will NOT be
		// 				able to re-read it and this will also cause IllegalStateException
		// 				... the only solution is to use a ServletRequestWrapper
		// 					(see http://natch3z.blogspot.com.es/2009/01/read-request-body-in-filter.html
		// 					 and ContentCachingRequestWrapper from Spring framework)
		Host requestedHost = Host.of(HttpServletRequestUtils.clienteRequestedHost(req));
		_host = Host.of(requestedHost.getId());		// strictly the host (ignore protocol)
		_port = requestedHost.asUrl().getPort();
		
		// path
		_requestedResourceUrlPath = UrlPath.preservingTrailingSlash()		// BEWARE!!!
										   .from(req.getRequestURI())
								           .urlPathAfter(UrlPath.from(req.getContextPath()));	
		// uri type
		_uriType = R01HLODURIType.of(_host);
		_resourceType = R01HLODResourceType.of(_requestedResourceUrlPath);

		// query string or post body > SPARQL queries
		UrlQueryString urlEncodedQueryString = _urlEncodedQueryString(req);
		UrlQueryString formEncodedQryStr = _formEncodedQueryString(req);
		_requestQueryString = _urlQueryStringOf(urlEncodedQueryString,
												formEncodedQryStr);
		// anchor
		int anchorIndex = req.getRequestURI().indexOf('#');
		_requestedUrlAnchor = anchorIndex > req.getRequestURI().length() 
									? req.getRequestURI().substring(anchorIndex+1)
									: null;
		// accept header
		_acceptedMimes = R01HMIMEType.fromRequest(req);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasAcceptedMimeTypes() {
		return CollectionUtils.hasData(_acceptedMimes);
	}
	public boolean isRequestingHTMLFromMimes() {
		return this.hasAcceptedMimeTypes()
		    && _acceptedMimes.size() == 1
		    && CollectionUtils.pickOneAndOnlyElement(_acceptedMimes) ==  R01HMIMEType.HTML;
	}
	public boolean isAcceptingMime(final R01HMIMEType mime) {
		return this.hasAcceptedMimeTypes() ? _acceptedMimes.contains(mime) : false;
	}
	public R01HMIMEType getBestAcceptedMimeTypeOrNull() {
		return this.hasAcceptedMimeTypes() ? CollectionUtils.pickElementAt(_acceptedMimes,0)	// first element
										   : null;
	}
	public R01HMIMEType getBestAcceptedMimeTypeOrDefault(final R01HMIMEType def) {
		return this.hasAcceptedMimeTypes() ? CollectionUtils.pickElementAt(_acceptedMimes,0)	// first element
										   : def;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUGS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("type={} resourceType={} host={} resource={}{}{} mimeTypes={}",
				 				  _uriType,_resourceType,
			     				  _host,
			     				  _requestedResourceUrlPath,
			     				  _requestQueryString != null ? "?" +_requestQueryString : "",
			     				  _requestedUrlAnchor != null ? "#" + _requestedUrlAnchor : "",
			     				  _acceptedMimes);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	QUERY STRING
/////////////////////////////////////////////////////////////////////////////////////////
	private UrlQueryString _urlQueryStringOf(final UrlQueryString urlEncodedQueryString,
											 final UrlQueryString formEncodedQryString) {
		UrlQueryString outUrlQueryString = null;
		if (urlEncodedQueryString != null && urlEncodedQueryString.hasParams()
		 && formEncodedQryString != null && formEncodedQryString.hasParams()) {
			outUrlQueryString = urlEncodedQueryString.joinWith(formEncodedQryString);
		} else if (urlEncodedQueryString != null && urlEncodedQueryString.hasParams()) {
			outUrlQueryString = urlEncodedQueryString;
		} else if (formEncodedQryString != null && formEncodedQryString.hasParams()) {
			outUrlQueryString = formEncodedQryString;
		}
		return outUrlQueryString;
	}
	private UrlQueryString _urlEncodedQueryString(final HttpServletRequest req) {
		return _urlEncodedQueryString(req.getQueryString());
	}
	private UrlQueryString _urlEncodedQueryString(final String str) {
		UrlQueryString urlQryString = Strings.isNOTNullOrEmpty(str) ? UrlQueryString.fromParamsString(str)
																					.withoutParamsMatching(IGNORED_QUERY_STRING_PARAMS_NAME_PATTERN)	// ignore params whose name starts with R01H
																	: null;
		return urlQryString;
	}
	private UrlQueryString _formEncodedQueryString(final HttpServletRequest req) {
		UrlQueryString formEncodedQryString = null;
		
		String contentTypeHeader = req.getContentType();		
		
		// BEWARE! req.getReader() can try to read the body
		// ... and the body cannot be read twice
		// ... so the request MUST be wrapped
		if (req.getMethod().equalsIgnoreCase("POST")
		 && contentTypeHeader != null && contentTypeHeader.contains("application/x-www-form-urlencoded")) {
			@SuppressWarnings("unchecked")
			Map<String,String> params = Maps.transformValues(req.getParameterMap(),
															 new Function<String[],String>() {
																		@Override
																		public String apply(final String[] paramValues) {
																			if (CollectionUtils.isNullOrEmpty(paramValues)) return "";
																			if (paramValues.length > 1) throw new IllegalArgumentException("param with multiple values are NOT supported: received: " + Lists.newArrayList(paramValues));
																			return paramValues[0];
																		}
															 });
			formEncodedQryString = CollectionUtils.hasData(params) ? new UrlQueryString(params)
																				.withoutParamsMatching(IGNORED_QUERY_STRING_PARAMS_NAME_PATTERN)	// ignore params whose name starts with R01H
																   : null;
	//			_requestBody = StringPersistenceUtils.loadNotExceding(req.getReader(),
	//																  MAX_POST_DATA_CHARS);		// BEWARE! long post bodies
		}
		return formEncodedQryString;
	}
}
