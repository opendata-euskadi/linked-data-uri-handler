package r01hp.portal.appembed;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import r01f.httpclient.HttpResponseCode;

  class R01HFakeStatusCodeCapturingResponseWrapper 
extends HttpServletResponseWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    // Stores the response status code set by the proxied app server
    protected HttpResponseCode _statusCode = HttpResponseCode.OK;
    protected int _realResponseCode;
    
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HFakeStatusCodeCapturingResponseWrapper(final HttpServletResponse realHttpResponse) {
		super(realHttpResponse);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void setStatus(final int sc) {
    	_realResponseCode = sc;
        _statusCode = HttpResponseCode.of(sc);
        super.setStatus(sc);
    }
    public int getRealResponseCode() {
    	return _realResponseCode;
    }
    public HttpResponseCode getHttpResponseCode() {
        return _statusCode;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  HEADER
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void setHeader(final String name,final String value) {
    	this.addHeader(name,value);	// fix the header if necessary
    }
    @Override
    public void addHeader(final String name,final String value) {
    	String headerValue = _fixHeader(name,value);	// fix the header if necessary
        super.addHeader(name,headerValue);
    }

	static final String LOCATION_HEADER = "Location";	
    
    // Apache Map                    After apache rewrite                  After apache WL proxy
    // ==========================    =================================     ================================
    // xxx   r01hProxy_xxx			/r01hpProxyWar/r01hProxy_xxx          /r01hpProxyWar/r01hProxy_xxx
    //																			apache wl proxy definition
    //																				<LocationMatch "^/r01hpProxyWar">
	//																					SetHandler weblogic-handler
	//																					WebLogicCluster 
    //																				</LocationMatch>
	// zzz   zzz/r01hProxy_zzz		/r01hpProxyWar/zzz/r01hProxy_zzz	  /r01hpProxyWar/zzz/r01hProxy_zzz	
    //																			apache wl proxy definition
    //																			BEWARE of the PathTrim / PathPrepend!!!!!
    //																				<LocationMatch "^/r01hpProxyWar">
	//																					SetHandler weblogic-handler
	//																					WebLogicCluster 
    //																					PathTrim "/r01hpProxyWar/zzz"
	//																					PathPrepend "/r01hpProxyWar"
    //																				</LocationMatch>
    protected String _fixHeader(final String headerName,final String headerValue) {
    	String outFixedHeaderValue = headerValue;	// by default the header value is NOT fixed
    	
    	// LOCATION Header fix: when a client-redir (300x) response code is returned by the app
    	//			If the app returns a client-redir (300x) return code, the weblogic proxy
    	//			incorrectly injects the proxy context (r01hpProxyWar)
        if (// Is it a client redir response?
        	_statusCode.isIn(HttpResponseCode.MOVED_PERMANENTLY,	// HttpServletResponse.SC_MULTIPLE_CHOICES,		//300
        					 HttpResponseCode.MULTIPLE_CHOICES,		// HttpServletResponse.SC_MOVED_PERMANENTLY,	//301
        					 HttpResponseCode.MOVED_TEMPORARILY,	// HttpServletResponse.SC_MOVED_TEMPORARILY,	//302
        					 HttpResponseCode.SEE_OTHER,			// HttpServletResponse.SC_SEE_OTHER,			//303
        					 HttpResponseCode.NOT_MODIFIED,			// HttpServletResponse.SC_NOT_MODIFIED,			//304
        					 HttpResponseCode.USE_PROXY,			// HttpServletResponse.SC_USE_PROXY,			//305
        					 HttpResponseCode.TEMPORARTY_REDIRECT)	// HttpServletResponse.SC_TEMPORARY_REDIRECT)	//307
            &&
            // the header name is Location
            headerName.equals(LOCATION_HEADER)
            &&
            // the location header value the proxy is trying to set includesr01htProxyWAR
            (headerValue.indexOf("r01hpProxyWar") >= 0)) {

		    		outFixedHeaderValue = R01HPortalPageAppEmbedContext.removeProxyWarFromUrlPath(headerValue);
        }
        return outFixedHeaderValue;
    }
}