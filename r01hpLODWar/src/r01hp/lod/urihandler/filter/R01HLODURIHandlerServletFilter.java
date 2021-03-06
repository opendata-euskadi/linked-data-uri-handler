package r01hp.lod.urihandler.filter;

import java.io.IOException;
import java.util.Map;

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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.Role;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlQueryString;
import r01f.types.url.UrlQueryStringParam;
import r01f.util.types.collections.CollectionUtils;
import r01hp.lod.config.R01HLODTripleStoreHostWithRole;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODHandledURIData;
import r01hp.lod.urihandler.R01HLODHandledURIDataForClientRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForServerRedirect;
import r01hp.lod.urihandler.R01HLODHandledURIDataForTripleStoreProxy;
import r01hp.lod.urihandler.R01HLODHandledURIDataForTripleStoreQuery;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolverByDefault;
import r01hp.lod.urihandler.R01HLODRequestedURIData;
import r01hp.lod.urihandler.R01HLODTripleStoreQuery;
import r01hp.lod.urihandler.R01HLODURIHandlerEngine;
import r01hp.lod.urihandler.R01HMIMEType;

/**
 * This is the setup:
 *          	+-----------------------+
 *          	|                       |
 *          	|          WEB          |http://web
 *          	+-----------+-----------+
 *          	          proxy
 *          	            |
 *          	            |
 *          	+-----------v-----------+
 *          	|APP SERVER             |
 *          	|   +----------------+  |
 *          	|   |    LOD WAR     |http://appServer/r01hpLODWar
 *          	+---+-------+-----------+
 *          	          proxy
 *                          |                                                                                                                     
 *          	            |
 *          	        +---v----+
 *          	        | SPARQL |http://triplestore/sparql
 *          	+-------+----------------+
 *          	|                        |
 *          	|       TripleStore      | 
 *              +------------------------+
 *
 */
@Slf4j
@Singleton
public class R01HLODURIHandlerServletFilter 
  implements Filter {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The uri handler config
	 */
	private final R01HLODURIHandlerConfig _uriHandlerConfig;
	/**
	 * The uri handler
	 */
	private final R01HLODURIHandlerEngine _reqHandler;
	/**
	 * Filter config
	 */
	private transient FilterConfig _filterConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01HLODURIHandlerServletFilter(final R01HLODURIHandlerConfig uriHandlerConfig) {
		_uriHandlerConfig = uriHandlerConfig;		
		
		// create the request handler
		boolean useMockMainEntityOfPageResolver = _uriHandlerConfig.isUseMockIsMainEntityOfPageResolver(); 
		R01HLODMainEntityOfPageResolver mainEntityOfPageResolver = useMockMainEntityOfPageResolver
																		? _createMockMainEntityOfPageResolver()
																	    : new R01HLODMainEntityOfPageResolverByDefault(_uriHandlerConfig);	// uses a tripe-store is-main-entity-of-page query
		_reqHandler = new R01HLODURIHandlerEngine(_uriHandlerConfig,
										   		  mainEntityOfPageResolver);
	}
	private R01HLODMainEntityOfPageResolver _createMockMainEntityOfPageResolver() {
		// mock main entity of page resolver that checks if the url query string
		// contains a param named isMainEntityOfPage with true value 
		return new R01HLODMainEntityOfPageResolver() {
						@Override
						public boolean isMainEntityOfPage(final Url url) {
							return url.getQueryStringParamValue("isMainEntityOfPage")
									  .asBoolean()
									  .orDefault(false);
						}
						@Override
						public Url mainEntityOfPage(final Url uri) {
							return Url.from(uri.getQueryStringParamValue("mainEntityOfPage").asString().orNull());
						}
			   };
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
		// [0] - Typed request & response
		HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        
        log.info("[LOD filter][init] uri={} (servlet path={})",
        		 req.getRequestURI(),req.getServletPath());
        
        if (req.getServletPath().startsWith("/elda-assets")) {
        	chain.doFilter(request,
        				   response);
        	return;
        }
        
        // [1] - Uri handler
        R01HMultiReadableHttpServletRequestWrapper multiReadableReq = new R01HMultiReadableHttpServletRequestWrapper(req);	// use a request wrapper that caches the POSTed BODY  
        	                                                                                                                // if it's read because if not, the BODY cannot be    
        																													// read again
		R01HLODRequestedURIData reqData = new R01HLODRequestedURIData(_uriHandlerConfig,                                            
																	  multiReadableReq);													
		R01HLODHandledURIData reqHandle = _reqHandler.handle(reqData);
		
		// [2] - do something with the handle data
		log.info("[LOD filter] > handle action: {}",
				 reqHandle.getAction());
		switch(reqHandle.getAction()) {
		case CLIENT_REDIRECT:
			R01HLODHandledURIDataForClientRedirect clientRedirHandleData = reqHandle.as(R01HLODHandledURIDataForClientRedirect.class);
			_clientRedirect(clientRedirHandleData.getTargetUrl(),
							res);
			break;
		case SERVER_REDIRECT:
			R01HLODHandledURIDataForServerRedirect serverRedirHandleData = reqHandle.as(R01HLODHandledURIDataForServerRedirect.class);
			_serverRedirect(serverRedirHandleData.getUrlPath(),serverRedirHandleData.getUrlQueryString(),
							serverRedirHandleData.getMimeType(),
							serverRedirHandleData.getRequestAttributes(),
							req,res,
							chain);
			break;
		case TRIPLE_STORE_QUERY:
			R01HLODHandledURIDataForTripleStoreQuery tripleStoreQueryHandleData = reqHandle.as(R01HLODHandledURIDataForTripleStoreQuery.class);
			_proxyTripleStoreQuery(tripleStoreQueryHandleData.getTripleStoreQuery(),
							  	   tripleStoreQueryHandleData.getMimeType(),
							  	   multiReadableReq,res,		// hand de MULTI-readable request
							  	   chain);
			break;
		case PROXY_TRIPLE_STORE:
			R01HLODHandledURIDataForTripleStoreProxy tripleStoreProxyData = reqHandle.as(R01HLODHandledURIDataForTripleStoreProxy.class);
			_proxyTripleStore(tripleStoreProxyData.getUrlPathToBeProxied(),
							  multiReadableReq,res,
							  chain);
			break;
		default:
			throw new UnsupportedOperationException(reqHandle.getAction() + " action is NOT supported!");
		}
        log.info("[LOD filter][end] uri={} (servlet path={})",
        		 req.getRequestURI(),req.getServletPath());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private void _clientRedirect(final Url url,
								 final HttpServletResponse res) throws ServletException, 
																	   IOException {
		log.warn("LOD URIHandler FILTER: client redirect to {}",
				 url);
		res.setStatus(HttpServletResponse.SC_SEE_OTHER);
		res.addHeader("Location",url.asString());
	}
	private void _serverRedirect(final UrlPath targetUrlPath,final UrlQueryString targetUrlQueryString,
								 final R01HMIMEType requestedMime,
								 final Map<String,Object> reqAttrs,
								 final HttpServletRequest req,final HttpServletResponse res,
								 final FilterChain chain) throws ServletException,
														 		 IOException {
		log.info("LOD URIHandler FILTER: server redir to {}",
				 targetUrlPath);
		if (targetUrlPath.startsWith(UrlPath.from("elda"))) {
			// it's an ELDA request: just chain
			UrlPath urlPath = UrlPath.from(req.getContextPath())
								     .joinedWith(targetUrlPath);
			log.warn("... chain to {} requesting mime type {}",
					urlPath,requestedMime);
			HttpServletRequest fakeHttpReq = new R01HFakeMimeTypeAndUrlPathHttpServletRequestWrapper(req,
																	    				  			 requestedMime,
																	    				  			 urlPath,targetUrlQueryString);
			if (CollectionUtils.hasData(reqAttrs)) {
				for (Map.Entry<String,Object> reqAttr : reqAttrs.entrySet()) {
					fakeHttpReq.setAttribute(reqAttr.getKey(),
											 reqAttr.getValue());
				}
			}
			chain.doFilter(fakeHttpReq,
						   res);
		} else {
			// it's another type of request: redir
			log.warn("...server redirect to {} requesting mime type {}",
					 targetUrlPath,requestedMime);
			_filterConfig.getServletContext().getRequestDispatcher(targetUrlPath.asAbsoluteString())
											 .forward(new R01HFakeMimeTypeRequestWrapper(req,
													 									 requestedMime),	// fake the request so it request the given mimetype
													  res);
		}
	}
	private void _proxyTripleStoreQuery(final R01HLODTripleStoreQuery tripleStoreQuery,
								   		final R01HMIMEType requestedMime,
								   		final HttpServletRequest req,final HttpServletResponse res,
								   		final FilterChain chain) throws ServletException,
																	   	IOException {
		UrlPath tripleStoreSPARQLUrlPah = _uriHandlerConfig.getTripleStoreConfig()
														   .getInternalSPARQLEndPointUrlPath();
		UrlPath urlPathToBeProxied  = UrlPath.from(req.getContextPath())
											 .joinedWith(tripleStoreSPARQLUrlPah);
		UrlQueryString urlQueryString = UrlQueryString.fromParams(UrlQueryStringParam.of("query",
																	    	  			 tripleStoreQuery.asString()));;
		log.warn("LOD URIHandler FILTER: proxy {} {} to {} >>> query {} requesting mime type {} ",
				 urlPathToBeProxied.asAbsoluteString(),
				 req.getMethod(),	// GET / POST
				 _uriHandlerConfig.getTripleStoreConfig().debugInfo(),					 
				 urlQueryString.asString(),
				 requestedMime);

		R01HFakeStatusCodeCapturingResponseWrapper fakeRes = new R01HFakeStatusCodeCapturingResponseWrapper(res);
		chain.doFilter(// request
					   new R01HFakeMimeTypeAndUrlPathHttpServletRequestWrapper(req,
																			   // mime
																			   requestedMime,
																			   // url path
																			   urlPathToBeProxied,
																			   // query string
																			   urlQueryString),
					   // response
					   fakeRes);
		log.warn("LOD URIHandler FILTER: triple-store query response code: {}",
				 fakeRes.getRealResponseCode());
	}
	private void _proxyTripleStore(final UrlPath urlPath,
								   final HttpServletRequest req,final HttpServletResponse res,
								   final FilterChain chain) throws ServletException,
																	   	IOException {
		UrlPath urlPathToBeProxied  = urlPath;
		final Role role = R01HLODTripleStoreHostWithRole.roleFrom(urlPathToBeProxied);
		
		log.warn("LOD URIHandler FILTER: triple-store proxy {} {} to {}/{}",
				 urlPathToBeProxied.asAbsoluteString(),
				 req.getMethod(),	// GET / POST
				 FluentIterable.from(_uriHandlerConfig.getTripleStoreConfig().getInternalTripleStoreServerHosts())
				 								.filter(new Predicate<R01HLODTripleStoreHostWithRole>() {
																public boolean apply(final R01HLODTripleStoreHostWithRole host) {
																	return host.getRole().is(role);
																}
				 								})
								  				.transform(new Function<R01HLODTripleStoreHostWithRole,String>() {
																	@Override
																	public String apply(final R01HLODTripleStoreHostWithRole host) {
																		return host.getHost().asUrl().toString();
																	}
								  						   })
								  				.toList(),
				urlPath);

		R01HFakeStatusCodeCapturingResponseWrapper fakeRes = new R01HFakeStatusCodeCapturingResponseWrapper(res);
		chain.doFilter(// request (wrap in order to return the rewritten path (/r01hpLODWar/read/blazegraph/) instead of the requested path (/r01hpLODWar/read/triplestore)
					   new R01HFakeMimeTypeAndUrlPathHttpServletRequestWrapper(req,
																			   // url path
																			   urlPathToBeProxied),
					   // response
					   fakeRes);
		log.warn("LOD URIHandler FILTER: triple-store proxy response code: {}",
				 fakeRes.getRealResponseCode());
	}	
}
