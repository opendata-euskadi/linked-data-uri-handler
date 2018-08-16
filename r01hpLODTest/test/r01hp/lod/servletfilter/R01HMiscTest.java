package r01hp.lod.servletfilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import r01f.httpclient.HttpClient;
import r01f.io.util.StringPersistenceUtils;
import r01f.types.url.Url;
import r01f.util.types.Strings;

public class R01HMiscTest {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testProxy() throws MalformedURLException, 
								   IOException {
//		SELECT DISTINCT  ?item
//		WHERE
//		  { ?item ?p ?o
//		    FILTER ( ?item = iri("http://id.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof") )
//		  }
//		OFFSET  0
//		LIMIT   10
		String query = "SELECT DISTINCT  ?item WHERE { ?item ?p ?o FILTER ( ?item = iri(\"http://id.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof\") ) } OFFSET  0 LIMIT 10";
//		String query = "DESCRIBE <http://id.euskadi.eus/eli/es-pv/l/1979/03/20/(0)/dof>";
		Url url = Url.from("http://localhost:8080/r01hpLODWar/read/blazegraph/namespace/euskadi_db/sparq??query=" + query);
		
		
		// Using r01f http client
		InputStream is = HttpClient.forUrl(url)
								.withAcceptHeader("application/sparql-results+xml")
								.GET()
								.loadAsStream()
								.notUsingProxy()
								.withoutTimeOut()
								.noAuth();
		String respStr1 = StringPersistenceUtils.load(is);
		System.out.println(respStr1);		
		
		
		System.out.println("\n\n\n\n\n\n==============================================");
		HttpURLConnection conx = HttpClient.forUrl(url)
									.withAcceptHeader("application/sparql-results+xml")
									.GET()
									.getConnection()
									.notUsingProxy()
									.withoutTimeOut()
									.noAuth();
		String respStr2 = conx.getResponseMessage();		
		System.out.println(respStr2);
		
		// using the apache http client
		System.out.println("\n\n\n\n\n\n==============================================");
		HttpGet requestToBeProxied = new HttpGet(url.asStringUrlEncodingQueryStringParamsValues());
		requestToBeProxied.setHeader("accept","application/sparql-results+xml");
		
    	HttpClientBuilder clientBuilder = HttpClientBuilder.create();	// HttpParams httpClientParams = new BasicHttpParams();
    	clientBuilder.disableRedirectHandling();						// httpClientParams.setParameter(ClientPNames.HANDLE_REDIRECTS,false);
    																	// httpClientParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,false);
    																	// HttpClientParams.setRedirecting(httpClientParams,false);
        CloseableHttpClient httpClient = clientBuilder.build(); 					// HttpClient httpClient = new SystemDefaultHttpClient(httpClientParams);
        HttpResponse endPointResponse = httpClient.execute(requestToBeProxied);
        InputStream  endPointResponseIS = endPointResponse.getEntity()
        												  .getContent();
        String respStr3 = StringPersistenceUtils.load(endPointResponseIS);
        System.out.println(respStr3);	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testRegEx() {
		Pattern p = Pattern.compile(
								// any character NOT preceded by /read/
								"(?:" +					// start capturing
									"(?<!/(?:read|write)/blazegraph/?)." +	// any character NOT preceded by /read/blazegraph or /write/blazegraph
								")" +					// end of gruping
								// multiple characters matching the previous
								"*"					
							);	
		String m1Str = "/read/blazegraph/foo";
		String m2Str = "/read/blazegraph/foo";
		String m3Str = "/foo";
		Matcher m1 = p.matcher(m1Str);
		Matcher m2 = p.matcher(m2Str);
		Matcher m3 = p.matcher(m3Str);
		m1.find();
		m2.find();
		m3.find();
		System.out.println(Strings.customized("Applying {} to {} > matches={}",
											  p.toString(),m1Str,
											  m1.matches()));
		System.out.println(Strings.customized("Applying {} to {} > matches={}",
											  p.toString(),m2Str,
											  m2.matches()));
		System.out.println(Strings.customized("Applying {} to {} > matches={}",
											  p.toString(),m3Str,
											  m3.matches()));
		Assert.assertFalse(m1.matches());
		Assert.assertFalse(m2.matches());
		Assert.assertTrue(m3.matches());
	}
}
