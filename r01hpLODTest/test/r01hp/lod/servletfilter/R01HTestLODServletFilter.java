package r01hp.lod.servletfilter;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpResponseCode;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.util.types.collections.CollectionUtils;
import r01hp.lod.urihandler.R01HLODURIType;
import r01hp.lod.urihandler.R01HMIMEType;

@Slf4j
public class R01HTestLODServletFilter {
/////////////////////////////////////////////////////////////////////////////////////////
//	RESOURCE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testResourceURIs() throws MalformedURLException,
										  IOException {
		log.info("=====================================================================================");
		log.info("RESOURCE URIs");
		log.info("=====================================================================================");

		Host host = Host.of("http://localhost:8080");		// local tomcat
		UrlPath warContext = UrlPath.from("r01hpLODWar");	// lod war
//		UrlPath resourceUrlPath = UrlPath.from("/sector/domain/class/theId");
		UrlPath resourceUrlPath = UrlPath.from("/sector_publico/legislacion-justicia/fundacion/fundacion-kalitatea-fundazioa");
		
		
		log.info("[1] - Resource: /id/{resource} with mime type=RDF...........");
		Url url1 = Url.from(host,
					        warContext.joinedWith(R01HLODURIType.ID.getPathToken())
					        		  .joinedWith(resourceUrlPath));
		HttpResponse response1 = _doHttpRequest(url1,
										     	R01HMIMEType.RDFXML);
		HttpResponseCode respCode1 = HttpResponseCode.of(response1.getStatusLine().getStatusCode());
		UrlPath redirUrlPath1 = CollectionUtils.hasData(response1.getHeaders("Location")) 
										? UrlPath.from(response1.getHeaders("Location")[0].getValue())
										: null;
		log.info("\tresponse code={} / redirect location={}",
				 respCode1,redirUrlPath1);
		Assert.assertTrue(respCode1.is300());
		Assert.assertTrue(redirUrlPath1.is(R01HLODURIType.DATA.getPathToken()
												  		 .joinedWith(resourceUrlPath)));
		
		
		log.info("[2] - Resource: /id/{resource} with mime type=HTML...........");
		Url url2 = Url.from(host,
					        warContext.joinedWith(R01HLODURIType.ID.getPathToken())
					        		  .joinedWith(resourceUrlPath));
		HttpResponse response2 = _doHttpRequest(url2,
										     	R01HMIMEType.HTML);
		HttpResponseCode respCode2 = HttpResponseCode.of(response2.getStatusLine().getStatusCode());
		UrlPath redirUrlPath2 = CollectionUtils.hasData(response2.getHeaders("Location")) 
										? UrlPath.from(response2.getHeaders("Location")[0].getValue())
										: null;
		log.info("\tresponse code={} / redirect location={}",
				 respCode2,redirUrlPath2);
		Assert.assertTrue(respCode2.is300());
		Assert.assertTrue(redirUrlPath2.is(R01HLODURIType.DOC.getPathToken()
												  		 .joinedWith(resourceUrlPath)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private static HttpResponse _doHttpRequest(final Url url,
									   		   final R01HMIMEType mime) throws ClientProtocolException, 
																			   IOException {
		HttpGet getReq = new HttpGet(url.asString());
		getReq.addHeader("Accept",mime.getMime().asString());
		
		CloseableHttpClient httpClient = HttpClientBuilder.create()
												 .disableRedirectHandling()		// BEWARE!!!
												 .build();
		return httpClient.execute(getReq);
	}
}
