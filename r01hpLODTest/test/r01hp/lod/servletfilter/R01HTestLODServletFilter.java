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
import r01hp.lod.config.R01HLODURIHandlerConfig;
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

		Host idSite = Host.of("http://id.localhost");		// local apache server
		Host dataSite = Host.of("http://data.localhost");		
		Host docSite = Host.of("http://doc.localhost");
		UrlPath warContext = UrlPath.from(R01HLODURIHandlerConfig.LOD_WAR_NAME);	// lod war
		UrlPath resourceUrlPath = UrlPath.from("/sector/domain/class/theId");
		Url uri = Url.from(idSite,
						   resourceUrlPath);
		
		
		log.info("[1] - URI: {} with mime type=RDF...........",
				 uri);
		HttpResponse response1 = _doHttpRequest(uri,
										     	R01HMIMEType.RDFXML);
		HttpResponseCode respCode1 = HttpResponseCode.of(response1.getStatusLine().getStatusCode());
		Url redirUrl1 = CollectionUtils.hasData(response1.getHeaders("Location")) 
										? Url.from(response1.getHeaders("Location")[0].getValue())
										: null;
		log.info("\tresponse code={} / redirect location={}",
				 respCode1,redirUrl1);
		Assert.assertTrue(redirUrl1 != null);
		Assert.assertTrue(respCode1.is300());
		Assert.assertTrue(redirUrl1.is(Url.from(dataSite,
												resourceUrlPath)));
		
		
		log.info("[2] - URI: {} with mime type=HTML...........",
				 uri);
		HttpResponse response2 = _doHttpRequest(uri,
										     	R01HMIMEType.HTML);
		HttpResponseCode respCode2 = HttpResponseCode.of(response2.getStatusLine().getStatusCode());
		Url redirUrl2 = CollectionUtils.hasData(response2.getHeaders("Location")) 
										? Url.from(response2.getHeaders("Location")[0].getValue())
										: null;
		log.info("\tresponse code={} / redirect location={}",
				 respCode2,redirUrl2);
		Assert.assertTrue(redirUrl2 != null);
		Assert.assertTrue(respCode2.is300());
		Assert.assertTrue(redirUrl2.is(Url.from(docSite,
												resourceUrlPath)));
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
