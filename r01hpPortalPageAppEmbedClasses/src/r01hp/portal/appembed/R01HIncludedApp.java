package r01hp.portal.appembed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Matcher;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.io.CharacterStreamSource;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01hp.util.parser.R01HToken;
import r01hp.util.parser.R01HTokenizerObservable;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;

/**
 * Models an app html that will be included into an app container page
 * This type contains some hacks to cope with some bad habits like include <link> tags inside the body 
 * of the app response
 */
@Slf4j
@Accessors(prefix="_")
  class R01HIncludedApp 
extends R01HParsedPageBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final UrlPath _appUrlPath;
	
	@Getter private final String _preHeadHtml;
			private final String _erroneouslyDetectedAtPreHead;
			private final CharacterStreamSource _restOfHtmlCharReader;
			
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HIncludedApp(final UrlPath includedAppUrlPath,
						   final InputStream is) {
		this(includedAppUrlPath,
			 is,Charset.defaultCharset());
	}
	public R01HIncludedApp(final UrlPath includedAppUrlPath,
						   final InputStream is,final Charset isCharset) {
		this(includedAppUrlPath,
			 new CharacterStreamSource(is,isCharset));
	}
	public R01HIncludedApp(final UrlPath includedAppUrlPath,
						   final CharBuffer charBuffer) {
		this(includedAppUrlPath,
			 new CharacterStreamSource(charBuffer));
	}
	public R01HIncludedApp(final UrlPath includedAppUrlPath,
						   final CharacterStreamSource includedAppCharReader) {
		// Parses the included app but ONLY until the head and body tag has been parsed 
		// (parses all the head content BUT ONLY the body tag, NOT the whole body content: it'll be a waste of resources
		//  to parse all the body content since nothing is done with it)
		// ... so it disassembles the app html as:
		//		pre-head html
		//		head
		//		bodyTag
		// ... and the rest of the html is CONSUMMED (not parsed) at method writeRestOfHtmlTo(out)
		//
		// The parse is done in two phases due to the need of having the app HEAD and BODY tag
		// and mix it with the container page's HEAD and BODY tag
		// (the body content is NOT parsed since there's no need to do anything with it, just include it)
		
		log.trace("Start parsing included app {}",includedAppUrlPath);
		
		final StringBuilder preHeadHtmlSb = new StringBuilder();	// the preHeadHtml is usually ignored since it usually contains the <DOCTYPE> or <html> tags
		final StringBuilder headTitle = new StringBuilder();
		final Collection<Meta> headMetas = Lists.newArrayList();
		final StringBuilder headOther = new StringBuilder();
		final StringBuilder scriptBody = new StringBuilder();
		final StringBuilder styleBody = new StringBuilder();
		final StringBuilder erroneouslyDetectedAsPreHead = new StringBuilder();
		final StringBuilder bodyTag = new StringBuilder();
		
		Observable<R01HToken> obs = R01HTokenizerObservable.createFrom(includedAppCharReader);
		obs.subscribe(new Subscriber<R01HToken>() {
							boolean insideHead = false;
							boolean insideHeadTitle = false;
							boolean insideScript = false;
							boolean insideStyle = false;
							
							private void _appendTokenText(final String tokenText) {
								if (insideScript) {
									// script body
									scriptBody.append(tokenText);
							    } 
								else if (insideStyle) {
									// style body
									styleBody.append(tokenText);
								}
								else if (insideHeadTitle) {
									// head title
									headTitle.append(tokenText);
								}
								else if (insideHead) {
									// other head content
							    	headOther.append(tokenText);
							    } 
								else {
									// pre-head
									preHeadHtmlSb.append(tokenText);
							    }
							}
							
							@Override
							public void onNext(final R01HToken token) {								
								String tokenText = token.asString();
								
								// Change the parse status depending on the token type & content
								switch (token.getType()) {
								
								case StartTag:
									if (insideHeadTitle) insideHeadTitle = false;	// Not closed head title
									
									Matcher startTagMatcher = START_TAG_PATTERN.matcher(tokenText);
									if (!startTagMatcher.matches()) {
										log.error("R01HIncludedApp>> {} Not a valid startTag token: {} it does NOT match {}",tokenText,START_TAG_PATTERN);
										throw new IllegalStateException("Not a valid startTag token: " + tokenText + " it does NOT match " + START_TAG_PATTERN); 
									}
																		
									String startTagName = startTagMatcher.group(1);
									
									if (startTagName.equals("html")) {
										// ignore
									}
									else if (startTagName.equalsIgnoreCase("head")) {
										insideHead = true;
									}
									else if (startTagName.equalsIgnoreCase("title")) {
										insideHeadTitle = true;								// the text is handled at the default case
									} 
									else if (startTagName.equalsIgnoreCase("meta")) {
										Meta meta = _parseMETA(tokenText);										
										if (meta != null) headMetas.add(meta);
									}
									else if (startTagName.equalsIgnoreCase("link")) {
										// <link> tags NOT in <head> section
										// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html)
										headOther.append(tokenText);
									}
									else if (startTagName.equalsIgnoreCase("style")) {
										// <style> tags NOT in <style> section
										// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html)
										insideStyle = true;			// style with body
										_appendTokenText(tokenText);										
									}
									else if (Strings.isContainedWrapper(startTagName)
										   			 		.inIgnoringCase("script","noscript")) {
										insideScript = true;			// script with body
										_appendTokenText(tokenText);
									}
									// If the body tag is detected, parse it and stop parsing
									// (the body content will be flushed as is later)
									else if (startTagName.equalsIgnoreCase("body")) {
										bodyTag.append(tokenText);
										insideHead = false;
										
										// anything above the body tag should be considered as HEAD
										if (Strings.isNOTNullOrEmpty(scriptBody)) {
											headOther.append("\n").append(scriptBody).append("\n");
											scriptBody.delete(0,scriptBody.length());
										}
										if (Strings.isNOTNullOrEmpty(styleBody)) {
											headOther.append("\n").append(styleBody).append("\n");
											styleBody.delete(0,styleBody.length());
										}
										
										this.unsubscribe();			// stop observing... no need to parse the rest of the html
									}
									// if the included app does NOT contain <head> section everything might be consumed as pre-head
									// ... in order to avoid this and have all the html in memory, if a non-head tag is detected (div, section, etc), 
									//	   the html observing is stopped
									// ... BUT beware the inlined tags inside scripts as:
									//		<script>
									//			document.write("<div>.... <-- this will be detected as a start tag
									//		</script>
									else if ( !insideScript
										   && Strings.isContainedWrapper(startTagName)
										   			 .inIgnoringCase("div","section","article","header","a","p","h1","h2","h3","span") ) {
										
										if (insideHead) {
											// NOT closed head section
											if (Strings.isNOTNullOrEmpty(scriptBody)) {
												headOther.append("\n").append(scriptBody).append("\n");
												scriptBody.delete(0,scriptBody.length());
											}
											if (Strings.isNOTNullOrEmpty(scriptBody)) {
												headOther.append("\n").append(styleBody).append("\n");
												styleBody.delete(0,styleBody.length());
											}
										} else {
											// no head section...
											// scripts or sytles 
											if (Strings.isNOTNullOrEmpty(scriptBody)) {
												preHeadHtmlSb.append("\n").append(scriptBody).append("\n");
												scriptBody.delete(0,scriptBody.length());
											}
											if (Strings.isNOTNullOrEmpty(styleBody)) {
												headOther.append("\n").append(styleBody).append("\n");		// style ALLWAYS go to header
												styleBody.delete(0,styleBody.length());
											}
											// the text detected as preHead is NOT actually before the head since there's no head
											// ... so put it at the erroneouslyDetectedAsPreHead and clear the preHeadHtmlSb buffer
											erroneouslyDetectedAsPreHead.append(preHeadHtmlSb.toString());
											preHeadHtmlSb.delete(0,preHeadHtmlSb.length());	
											
											// also put the start tag in the badly detected as head buffer
											erroneouslyDetectedAsPreHead.append(tokenText);
										}
										insideHead = false;
										
										this.unsubscribe();			// stop observing: NOT at head section
									}
									else {
										// anything else
										_appendTokenText(tokenText);
									}
									break;
									
								case EndTag:
									Matcher endTagMatcher = END_TAG_PATTERN.matcher(tokenText);
									if (!endTagMatcher.matches()) {
										log.error("R01HIncludedApp()>> {} Not a valid endTag token {} it does NOT match {}", tokenText,END_TAG_PATTERN);
										throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + END_TAG_PATTERN);
									}
									
									String endTagName = endTagMatcher.group(1);
									
									if (endTagName.equalsIgnoreCase("head")) {
										insideHead = false;
										if (Strings.isNOTNullOrEmpty(scriptBody)) {
											headOther.append("\n").append(scriptBody).append("\n");
											scriptBody.delete(0,scriptBody.length());
										}
										if (Strings.isNOTNullOrEmpty(styleBody)) {
											headOther.append("\n").append(styleBody).append("\n");
											styleBody.delete(0,styleBody.length());
										}
									} 
									else if (endTagName.equalsIgnoreCase("title")) {
										insideHeadTitle = false;
									} 
									else if (endTagName.equalsIgnoreCase("meta")) {
										// ignore meta close tags
									}
									else if (endTagName.equalsIgnoreCase("link")) {
										// <link> tags NOT in <head> section
										// (included apps that do NOT have <head> section and put <link> tags at the beginning of their html) 
										headOther.append(tokenText);
									} 
									else  if (endTagName.equalsIgnoreCase("style")) {
										// <style> tags NOT in <head> section
										// (included apps that do NOT have <head> section and put <style> tags at the beginning of their html)
										_appendTokenText(tokenText);										
										if (insideStyle) insideStyle = false;
									}
									else if (Strings.isContainedWrapper(endTagName)
										   			 		.inIgnoringCase("script","noscript")) {
										_appendTokenText(tokenText);
										insideScript = false;
									}
									else {
										_appendTokenText(tokenText);
									}
									break;
									
								case SSIInclude:
									// other ssi includes
									_appendTokenText(tokenText);	
									break;
								case DocType:
									// ignore app doctypes
									break;
								default:
									_appendTokenText(tokenText);
									break;
								}
							}
							@Override
							public void onCompleted() {
								log.trace("Parse of included app {} HEAD completed",includedAppUrlPath);
							}
							@Override
							public void onError(final Throwable th) {
								th.printStackTrace();
							}
					 });
		
		// beware the if the included app DOES NOT return a head section:
		// 	ie: 		<link rel='apple-touch-icon' href='/img/i/apple-icon-57x57.png'/>
		//				<script>
		//					console.log("hello!");
		//				</script>
		// then the intended head section is flushed when the first div / section / hx / etc tag is detected
		// BUT when the app returns just an script NOT followed by any div / section / hx, it MUST be flused
		// here
		if (Strings.isNOTNullOrEmpty(scriptBody)) {
			preHeadHtmlSb.append("\n").append(scriptBody).append("\n");
			scriptBody.delete(0,scriptBody.length());
		}
		if (Strings.isNOTNullOrEmpty(styleBody)) {
			headOther.append("\n").append(styleBody).append("\n");		// style ALLWAYS go to header
			styleBody.delete(0,styleBody.length());
		}
		
		// set parsed content
		_appUrlPath = includedAppUrlPath;
		_preHeadHtml = preHeadHtmlSb.toString();
		_head = (Strings.isNOTNullOrEmpty(headTitle) 
			  || CollectionUtils.hasData(headMetas) 
			  || Strings.isNOTNullOrEmpty(headOther)) ? new Head(headTitle.toString(),
								   								 headMetas,
								   								 headOther.toString())
													  : null;
		_erroneouslyDetectedAtPreHead = erroneouslyDetectedAsPreHead.toString();
		_bodyTag = Strings.isNOTNullOrEmpty(bodyTag) ? new BodyTag(bodyTag.toString()) 
													 : null;
		_restOfHtmlCharReader = includedAppCharReader;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Writes the html returned by the included app starting just after the body tag
	 * (remember that the head and body tag are parsed in the constructor BUT the body content is NOT parsed,
	 *  it's just consumed -written out without being parsed-)
	 * @param writer
	 */
	public void writeRestOfHtmlTo(final Writer writer) throws IOException {
		// The preHeadHtml is usually ignored since it usually contains the <DOCTYPE> or <html> tags,
		// but sometimes when the included app returns only text (not html content), it should be returned
		if (Strings.isNOTNullOrEmpty(_preHeadHtml)) {
			writer.write(_preHeadHtml);
		}
		
		// [1] - write the erroneously detected as pre-head html
		if (Strings.isNOTNullOrEmpty(_erroneouslyDetectedAtPreHead)) writer.write(_erroneouslyDetectedAtPreHead);
		
		// [2] - write the not previously read html from the reader (previously, only the head was read)
		Observable<R01HToken> obs = R01HTokenizerObservable.createFrom(_restOfHtmlCharReader);
		obs.subscribe(new Subscriber<R01HToken>() {
							@Override
							public void onNext(final R01HToken token) {
								String tokenText = token.asString();
								try {
									switch(token.getType()) {
									case StartTag:
										Matcher startTagMatcher = START_TAG_PATTERN.matcher(tokenText);
										if (!startTagMatcher.matches()) {
											writer.write(tokenText);
											break;
//											log.error("R01HIncludedApp.writeRestOfHtmlTo() >>Not a valid startTag token: {} it does NOT match {}",tokenText,START_TAG_PATTERN);
//											throw new IllegalStateException("Not a valid startTag token: " + tokenText + " it does NOT match " + START_TAG_PATTERN); 
										}
										
										// remove the html and body tags returned by the app
										String startTagName = startTagMatcher.group(1);
										if (!startTagName.equalsIgnoreCase("body") && !startTagName.equalsIgnoreCase("html")) writer.write(tokenText);	// ignore <body> tag	
										break;
									case EndTag:
										Matcher endTagMatcher = END_TAG_PATTERN.matcher(tokenText);
										if (!endTagMatcher.matches()) {
											writer.write(tokenText);
											break;
//											log.error("R01HIncludedApp.writeRestOfHtmlTo() Not a valid endTag token: {} it does NOT match {}",tokenText,END_TAG_PATTERN);
//											throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + END_TAG_PATTERN);
										}
										
										String endTagName = endTagMatcher.group(1);
										if (!endTagName.equalsIgnoreCase("body") && !endTagName.equalsIgnoreCase("html")) writer.write(tokenText);		// ignore </body> and </html> tags
										break;
									case DocType:
										writer.write(tokenText);
										break;							// write by default
									
									default:
										writer.write(tokenText);		// write by default
									}
								} catch(IOException ioEx) {
									OnErrorThrowable.from(ioEx);
								}
							}
							@Override
							public void onCompleted() {
								log.trace("Parse of included app {} completed",
										  _appUrlPath);
							}
							@Override
							public void onError(final Throwable th) {
								th.printStackTrace();
							}
					  });
	}
	

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	
}
