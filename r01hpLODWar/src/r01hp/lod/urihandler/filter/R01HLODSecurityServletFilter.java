package r01hp.lod.urihandler.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Checks security
 */
@Slf4j
@Singleton
public class R01HLODSecurityServletFilter 
  implements Filter {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Filter config
	 */
	private transient FilterConfig _filterConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01HLODSecurityServletFilter() {
		// nothing
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	INIT & DESTROY
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		_filterConfig = filterConfig;
	}
	@Override
	public void destroy() {
		_filterConfig = null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FILTER
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void doFilter(final ServletRequest request,final ServletResponse response,
						 final FilterChain chain) throws IOException, 
														 ServletException {
		HttpServletRequest httpReq = (HttpServletRequest)request;
        HttpServletResponse httpRes = (HttpServletResponse)response;
        
        // check if there's xlnets session
        boolean internalIp = _isInternalIp(httpReq);
        boolean hasXLNetsSession = _hasXLNetsSession(httpReq);
        
        log.info("[LOD Security][init]: internal={}, xlNetsSession={} uri={}",
        		 internalIp,hasXLNetsSession,
        		 httpReq.getRequestURI());
        
        if (internalIp && hasXLNetsSession) {
        	chain.doFilter(request,
        				   response);
        }
        else {
        	httpRes.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
	}
/////////////////////////////////////////////////////////////////////////////////////////
// Security checks
// BEWARE!!!	This method reads the request body and this can be read only once.
// 				If you the body is readed in a filter, the target servlet will NOT be
// 				able to re-read it and this will also cause IllegalStateException
// 				... the only solution is to use a ServletRequestWrapper
// 					(see http://natch3z.blogspot.com.es/2009/01/read-request-body-in-filter.html
// 					 and ContentCachingRequestWrapper from Spring framework)
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean _isInternalIp(final HttpServletRequest httpReq) {
		return true;
//        // real client ip
//        String ip = HttpServletRequestUtils.requestingClientIp(httpReq);
//        if (ip == null) ip = httpReq.getRemoteAddr();
//        
//        // check if it's an internal IP
//        return HttpServletRequestUtils.isInternalIP(ip);
	}
	private boolean _hasXLNetsSession(final HttpServletRequest httpReq) {
		return true;
//        boolean outHasXLNetsSession = false;   	// false by default
//
//		// Auth config
//		XLNetsAppCfg authCfg = new XLNetsAppCfg();
//		authCfg.loadConfig("r01mt");
//
//        // Instanciar el provider de autenticacion especificado en la configuracion y pasarle la request (parametros de n38) de la peticion actual
//        R01FBaseAuthProvider authProvider = null;
//		try {
//            authProvider = R01FAuthFilterUtils.obtainAuthProviderInstance(authCfg,(HttpServletRequest)request,
//            															  "userProvider");
//        } catch (ReflectionException refEx) {
//            throw new ServletException(refEx);
//        }
//
//		// En base a los parametros de sesion de n38 recibidos en la request se comprueba el contexto de autorizacion
//		R01FAuthCtx authCtx = authProvider.getContext(authCfg);
//	    if (authCtx != null) {
//	    	outHasXLNetsSession = true; 
//        }
//	    return outHasXLNetsSession;
	}
}
