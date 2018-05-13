package r01hp.util.parser;

import java.io.InputStream;
import java.nio.charset.Charset;

import lombok.experimental.Accessors;
import r01f.io.CharacterStreamSource;
import rx.Observable;
import rx.Subscriber;

/**
 * Creates an {@link Observable} of {@link R01HToken}s
 * Usage: 
 * <pre class='brush:java'>
 *		String src = HttpClient.forUrl("http://www.euskadi.eus")
 *							   .GET()
 *							   .loadAsString();
 *		CharacterReader charReader = new CharacterReader(new ByteArrayInputStream(src.getBytes()),Charset.defaultCharset());		
 *		Observable<R01HToken> obs = R01HTokenizerObservable.createFrom(charReader);
 *		obs.subscribe(new Observer<R01HToken>() {
 *							@Override
 *							public void onCompleted() {
 *								System.out.println("Completed!");
 *							}
 *							@Override
 *							public void onNext(final R01HToken t) {		
 *								String tokenText = t.asString().trim();
 *								System.out.println(t.getType() + " token \t\t:{" + tokenText + "}");
 *							}
 *							@Override
 *							public void onError(final Throwable th) {
 *								th.printStackTrace();
 *							}
 *					 });
 * </pre>
 */
@Accessors(prefix="_")
public class R01HTokenizerObservable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private CharacterStreamSource _charReader;
		
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The underlying CharacterReader as a {@link Readable} thing
	 * @return
	 */
	public Readable getSource() {
		return _charReader.asReadable();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	private R01HTokenizerObservable(final CharacterStreamSource charReader) {
		_charReader = charReader;
	}
	public static Observable<R01HToken> createFrom(final Readable readable) {
		CharacterStreamSource charReader = new CharacterStreamSource(readable);
		return R01HTokenizerObservable.createFrom(charReader);
	}
	public static Observable<R01HToken> createFrom(final InputStream source,final String charsetName) {
		CharacterStreamSource charReader = new CharacterStreamSource(source,charsetName);
		return R01HTokenizerObservable.createFrom(charReader);
	}
	public static Observable<R01HToken> createFrom(final InputStream source,final Charset charset) {
		CharacterStreamSource charReader = new CharacterStreamSource(source,charset);
		return R01HTokenizerObservable.createFrom(charReader);
	}
	public static Observable<R01HToken> createFrom(final CharacterStreamSource charReader) {
		R01HTokenizerObservable outObs = new R01HTokenizerObservable(charReader);
		return outObs._createObservable();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private Observable<R01HToken> _createObservable() {
		return Observable.create(
					new Observable.OnSubscribe<R01HToken>() {
							@Override
							public void call(final Subscriber<? super R01HToken> observer) {
								if (observer.isUnsubscribed()) return;

								// start!!
								observer.onStart();
								
								// start with a TextToken
								R01HTokenizer tokenizer = new R01HTokenizer(_charReader);														
								R01HToken prevTextToken = null;
								while(tokenizer.getCurrentState() != R01HTokenizerState.EOF 	// not the EOF token
								   && !observer.isUnsubscribed()) {								// observer still there
									
									boolean tokenComplete = false;
									
									// read a complete token
									while(!tokenComplete) {
										// force the token to read data
										try {
											tokenComplete = tokenizer.read();
										} catch (R01HParseError parseErr) {
											observer.onError(parseErr);
										}
										
										// ... if the tokenizer has a complete token, emit it
										// note that if there are two consecutive text tokens they should
										// be emited as a single one
										if (tokenComplete) {
											R01HToken currentToken = tokenizer.getCurrentToken();
											
											if (currentToken != null) {
												if (currentToken.getType() == R01HTokenType.Text) {
													if (prevTextToken != null) {
														// Merge the current token with the previous token
														prevTextToken = new R01HToken(R01HTokenType.Text,
																				  	  prevTextToken.getText() + currentToken.getText());	
													} else {
														// store the current text token; maybe it has to be merged
														// with the next token if it's also a text token
														prevTextToken = currentToken;
													}
												} else {
													// if the current token is NOT a text token and there's a text token
													// pending to be emited, emit it
													if (prevTextToken != null) observer.onNext(prevTextToken);
													prevTextToken = null;
													
													// emit the current token
													observer.onNext(currentToken);
												}
											}
										}
										
										// tokenizer state may change from one state to another on result of
										// reading the stream (ie: if the current state is a Text and a 
										// 						   '<' char is readed, transition to a StartTag state)
										if (tokenizer.hasToChangeState()) tokenizer.changeState();
									}
								}
								// maybe a last text token is available
								if (prevTextToken != null) observer.onNext(prevTextToken);
								
								// all done!
								observer.onCompleted();
							}
					}
				);
	}
}
