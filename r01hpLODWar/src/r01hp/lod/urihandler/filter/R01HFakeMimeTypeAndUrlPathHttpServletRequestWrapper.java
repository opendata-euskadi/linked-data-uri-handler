package r01hp.lod.urihandler.filter;

import javax.servlet.http.HttpServletRequest;

import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * A request wrapper that:
 * 		- Fakes the MimeType
 * 		- Fakes the url path
 */
  class R01HFakeMimeTypeAndUrlPathHttpServletRequestWrapper
extends R01HFakeMimeTypeRequestWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final UrlPath _urlPath;
	private final UrlQueryString _queryString;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HFakeMimeTypeAndUrlPathHttpServletRequestWrapper(final HttpServletRequest request,
															   final R01HMIMEType mime,
															   final UrlPath urlPath,final UrlQueryString qryString) {
		super(request,
			  mime);
		_urlPath = urlPath;
		_queryString = _sanitizeQueryString(qryString);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDE
//  (see https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl)
//
//	Servlet is mapped as /test%3F/* and the application is deployed under /app.
//
//	http://30thh.loc:8480/app/test%3F/a%3F+b;jsessionid=S%3F+ID?p+1=c+d&p+2=e+f#a
//
//	Method              URL-Decoded Result           
//	----------------------------------------------------
//	getContextPath()        no      /app
//	getLocalAddr()                  127.0.0.1
//	getLocalName()                  30thh.loc
//	getLocalPort()                  8480
//	getMethod()                     GET
//	getPathInfo()           yes     /a?+b
//	getProtocol()                   HTTP/1.1
//	getQueryString()        no      p+1=c+d&p+2=e+f
//	getRequestedSessionId() no      S%3F+ID
//	getRequestURI()         no      /app/test%3F/a%3F+b;jsessionid=S+ID
//	getRequestURL()         no      http://30thh.loc:8480/app/test%3F/a%3F+b;jsessionid=S+ID
//	getScheme()                     http
//	getServerName()                 30thh.loc
//	getServerPort()                 8480
//	getServletPath()        yes     /test?
//	getParameterNames()     yes     [p 2, p 1]
//	getParameter("p 1")     yes     c d
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getQueryString() {
		return _queryString != null ? _queryString.asStringEncodingParamValues()
									: "";
	}
	@Override
	public String getRequestURI() {
//		String outURI =  Url.from(_urlPath,
//								  _queryString)
//				  			.asStringUrlEncodingQueryStringParamsValues();
		String outURI =  Url.from(_urlPath)
							.asString();
		return outURI;
	}
	@Override
	public StringBuffer getRequestURL() {
//		Url outUrl = Url.from(super.getRequest().getRemoteHost(),super.getRequest().getRemotePort(),
//				  	    	  _urlPath,
//				  	    	  _queryString);
		String outUrl =  Url.from(_urlPath)
							.asString();
		return new StringBuffer(outUrl);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sanitizes the url query string
	 *		When the app is embeded into a portal page, the apache rewrite rules
	 *		"inject" some url parameters that can be harmful to the target app 
	 *		(this is the case of ELDA)
	 * 		... so these params MUST be stripped off
	 * @param queryString
	 * @return
	 */
	private static UrlQueryString _sanitizeQueryString(final UrlQueryString queryString) {
		if (queryString == null) return null;
		
		// ?R01HPortal=$1&R01HPage=$2&R01HLang=$3
		UrlQueryString outQryString = queryString.withoutParamsMatching(R01HLODRequestedURIData.IGNORED_QUERY_STRING_PARAMS_NAME_PATTERN);
		return outQryString;
	}
}
