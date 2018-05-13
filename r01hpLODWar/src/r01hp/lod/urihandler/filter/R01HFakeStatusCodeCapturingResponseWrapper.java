package r01hp.lod.urihandler.filter;

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
    @Override
	public void setStatus(int sc, String sm) {
    	_realResponseCode = sc;
        _statusCode = HttpResponseCode.of(sc);
		super.setStatus(sc,sm);
	}
	public int getRealResponseCode() {
    	return _realResponseCode;
    }
    public HttpResponseCode getHttpResponseCode() {
        return _statusCode;
    }
}