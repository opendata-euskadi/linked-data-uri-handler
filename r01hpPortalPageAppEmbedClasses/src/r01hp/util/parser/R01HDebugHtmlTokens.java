package r01hp.util.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.io.CharacterStreamSource;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import rx.Observable;
import rx.Observer;

/**
 * An utility type for debug html tokens
 * <pre class='brush:java'>
 * 		DebugHtmlTokens.fromUrl(new URL("http://www.euskadi.eus"));
 * </pre>
 */
@Slf4j
public class R01HDebugHtmlTokens {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void fromUrl(final URL url) throws IOException {
		CharacterStreamSource charReader = new CharacterStreamSource(HttpClient.forUrl(url)
																			   .GET()
																			   .loadAsStream().directNoAuthConnected(),
																	 Charset.defaultCharset());
		_debug(charReader);
	}
	public static void fromSerializedUrl(final Url url) throws IOException {
		R01HDebugHtmlTokens.fromUrl(url.asUrl());
	}
	public static void fromString(final String str) {
		CharacterStreamSource charReader = new CharacterStreamSource(new ByteArrayInputStream(str.getBytes()),
														 			 Charset.defaultCharset());
		_debug(charReader);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _debug(final CharacterStreamSource charReader) {
		Observable<R01HToken> obs = R01HTokenizerObservable.createFrom(charReader);
		obs.subscribe(new Observer<R01HToken>() {
							@Override
							public void onCompleted() {
								log.info("Completed!");
							}
							@Override
							public void onNext(final R01HToken t) {		
								String tokenText = t.asString().trim();
								if (Strings.isNOTNullOrEmpty(tokenText)) log.info("{}:\t\t[{}]",
																				  t.getType(),tokenText);
							}
							@Override
							public void onError(final Throwable th) {
								th.printStackTrace();
							}
					 });
	}
}
