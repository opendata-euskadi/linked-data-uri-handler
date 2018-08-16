package r01hp.portal.appembed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.regex.Matcher;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.io.CharacterStreamSource;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.locale.Languages;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import r01hp.portal.common.R01HPortalPageCopy;
import r01hp.util.parser.R01HToken;
import r01hp.util.parser.R01HTokenizerObservable;
import rx.Observable;
import rx.Observer;

/**
 * Models an app container page where an app html will be included
 * Usually this object will be cached at {@link R01HPortalPageManager}
 */
@Slf4j
@Accessors(prefix="_")
     class R01HPortalContainerPage
   extends R01HParsedPageBase
implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	protected final R01HPortalID _portalId;
	@Getter protected final R01HPortalPageID _pageId;
	@Getter protected final R01HPortalPageCopy _copy;
	
	// cache info
	@Getter 		protected final boolean _containsLastResourceContainerPageHtml;
    @Getter 		protected final long _lastModifiedTimeStamp; 			// last time the app container page file was modified
    @Getter @Setter protected long _lastCheckTimeStamp = -1;    		// last time the modify timestamp was checked
    @Getter @Setter protected int _hitCount = 0;		        		// Number of times the app container page has been accessed
	
	@Getter private String _preHeadHtml;
	@Getter private String _preAppContainerHtml;
	@Getter private String _postAppContainerHtml;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalContainerPage(final R01HPortalID portalId,final R01HPortalPageID pageId,
								   final R01HPortalPageCopy copy,
								   final InputStream is,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		this(portalId,pageId,
			 copy,
			 is,Charset.defaultCharset(),
			 lastModifiedTimeStamp,
			 containsLastResourceContainerPageHtml);
	}
	public R01HPortalContainerPage(final R01HPortalID portalId,final R01HPortalPageID pageId,
								   final R01HPortalPageCopy copy,
								   final InputStream is,final Charset isCharset,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		this(portalId,pageId,
			 copy,
			 new CharacterStreamSource(is,isCharset),
			 lastModifiedTimeStamp,
			 containsLastResourceContainerPageHtml);
	}
	public R01HPortalContainerPage(final R01HPortalID portalId,final R01HPortalPageID pageId,
								   final R01HPortalPageCopy copy,
								   final CharacterStreamSource appContainerCharReader,
								   final long lastModifiedTimeStamp,
								   final boolean containsLastResourceContainerPageHtml) {
		// Parse ALL the container page disassembling it into:
		//		pre-head html
		//		head
		//		pre-app-include html
		//		post-app-include html
		log.trace("Start parsing page {}-{}",portalId,pageId);
		
		final StringBuilder preHeadHtmlSb = new StringBuilder();
		final StringBuilder headTitle = new StringBuilder();
		final Collection<Meta> headMetas = Lists.newArrayList();
		final StringBuilder headOther = new StringBuilder();
		final StringBuilder bodyTag = new StringBuilder();
		final StringBuilder preAppContainerHtmlSb = new StringBuilder();
		final StringBuilder postAppContainerHtmlSb = new StringBuilder();
		
		Observable<R01HToken> obs = R01HTokenizerObservable.createFrom(appContainerCharReader);
		obs.subscribe(new Observer<R01HToken>() {						
							boolean insideHead = false;
							boolean insideHeadTitle = false;
							boolean preAppContainerHtml = false;
							boolean postAppContainerHtml = false;
							
							private void _appendTokenText(final String tokenText) {
								if (!insideHead && !preAppContainerHtml && !postAppContainerHtml) {
									preHeadHtmlSb.append(tokenText);
							    } else if (insideHead) {
							    	if (insideHeadTitle) {
							    		headTitle.append(tokenText);
							    	} else {
							    		headOther.append(tokenText);
							    	}
								} else if (preAppContainerHtml) {
									preAppContainerHtmlSb.append(tokenText);
								} else if (postAppContainerHtml) {
									postAppContainerHtmlSb.append(tokenText);
								}
							}

							@Override
							public void onNext(final R01HToken token) {		
								String tokenText = token.asString();
								
								// Change the parse status depending on the token type & content
								switch(token.getType()) {
								
								case EndTag:
									Matcher endTagMatcher = END_TAG_PATTERN.matcher(tokenText);
									if (!endTagMatcher.matches()) {
										log.error("Not a valid endTag token: {} it does NOT match {}",tokenText,END_TAG_PATTERN);
										throw new IllegalStateException("Not a valid endTag token: " + tokenText + " it does NOT match " + END_TAG_PATTERN);
									}
									
									String endTagName = endTagMatcher.group(1);
									if (endTagName.equalsIgnoreCase("head")) {
										insideHead = false;
									} else if (endTagName.equalsIgnoreCase("title")) {
										insideHeadTitle = false;
									} else if (endTagName.equalsIgnoreCase("meta")) {
										// ignore meta close tags
									} else if (endTagName.equalsIgnoreCase("body")) {
										// ignore body close tag
									} else if (endTagName.equalsIgnoreCase("html")) {
										// ignore html close tag
									} else {
										_appendTokenText(tokenText);
									}
									break;
									
								case StartTag:
									if (insideHead && insideHeadTitle) insideHeadTitle = false;	// Not closed head title
									
									Matcher startTagMatcher = START_TAG_PATTERN.matcher(tokenText);
									if (!startTagMatcher.matches()) {
										log.error("Not a valid startTag token: {} it does NOT match {}",tokenText,START_TAG_PATTERN);
										throw new IllegalStateException("Not a valid startTag token: " + tokenText + " it does NOT match " + START_TAG_PATTERN); 
									}
									
									String startTagName = startTagMatcher.group(1);
									if (startTagName.equalsIgnoreCase("head")) {
										insideHead = true;
									}
									else if (startTagName.equalsIgnoreCase("title")) {
										insideHeadTitle = true;	// the text is handled at the default case
									} 
									else if (startTagName.equalsIgnoreCase("meta")) {
										Meta meta = _parseMETA(tokenText);
										if (meta != null) headMetas.add(meta);
									}
									else if (startTagName.equalsIgnoreCase("body")) {
										bodyTag.append(tokenText);
										preAppContainerHtml = true;
									}
									else {
										_appendTokenText(tokenText);
									}
									break;
									
								case SSIInclude:
									if (tokenText.contains("$CONT")) {
										// app container
										preAppContainerHtml = false;
										postAppContainerHtml = true;
									} //legacy apps compatibility
									else if (tokenText.contains("/AVTemplates/r01gContainerVA/r01gContainerVA")) {
										// app container
										preAppContainerHtml = false;
										postAppContainerHtml = true;
										log.debug("The page has a container VA not configured." );
									} 
									else {
										// other ssi includes
										_appendTokenText(tokenText);	
									}
									break;
									
								default:
									_appendTokenText(tokenText);
									break;
								}
							}
							@Override
							public void onCompleted() {
								log.trace("Parse of page {}-{} completed",portalId,pageId);
							}
							@Override
							public void onError(final Throwable th) {
								th.printStackTrace();
							}
					 });
		// set parsed content
		_portalId = portalId;
		_pageId = pageId;
		_copy = copy;
		
		_containsLastResourceContainerPageHtml = containsLastResourceContainerPageHtml;
		_lastModifiedTimeStamp = lastModifiedTimeStamp;
		_lastCheckTimeStamp = lastModifiedTimeStamp;
		
		_preHeadHtml = preHeadHtmlSb.toString();
		_head = new Head(headTitle.toString(),
						 headMetas,
						 headOther.toString());
		_bodyTag = new BodyTag(bodyTag.toString());
		_preAppContainerHtml = preAppContainerHtmlSb.toString();
		
		_postAppContainerHtml = postAppContainerHtmlSb.toString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * @param ctx
	 * @param fakeServletResponse
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void includeApp(final R01HPortalPageAppEmbedContext ctx,
						   final R01HFakeServletResponseWrapper fakeServletResponse) throws IOException {
		// Get an included app wrapper: parse the target app server response
		R01HIncludedApp includedApp = new R01HIncludedApp(ctx.getRequestedUrlPath(),
														  fakeServletResponse.getProxiedAppResponseData());	
		
		// Get a writer to the real response
		Writer realOutputWriter = fakeServletResponse.getRealResponseWriter();
		
		// do the include
		_includeApp(ctx,
					includedApp,realOutputWriter);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TESTING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * (only for testing pourposes)
	 * @param appUrlPath
	 * @param appStream
	 * @param out
	 * @throws IOException
	 */
	public void includeApp(final UrlPath appUrlPath,
						   final InputStream appStream,final Writer out) throws IOException {
		// Get an included app wrapper
		R01HIncludedApp includedApp = new R01HIncludedApp(appUrlPath,appStream);
		R01HPortalPageAppEmbedContext ctx = new R01HPortalPageAppEmbedContext(appUrlPath);
		_includeApp(ctx,
					includedApp,out);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do the app html inclusion into an appcontainer portal page
	 * @param ctx
	 * @param includedApp
	 * @param realOutputWriter
	 * @throws IOException
	 */
	private void _includeApp(final R01HPortalPageAppEmbedContext ctx,
							 final R01HIncludedApp includedApp,final Writer realOutputWriter) throws IOException {
		// Mix the headers
		Head head = includedApp.newHeadMixingWith(this.getHead());
		
		// Start writing the container page before head content
		if (_preHeadHtml != null) realOutputWriter.write(_preHeadHtml);
		
		// write the head content
		if (head != null) {
			realOutputWriter.write("\n<head>\n");
			realOutputWriter.write(head.asString());
			realOutputWriter.write("\n</head>\n");
		}
		
		// write the bodyTag 
		BodyTag bodyTag = includedApp.newBodyTagMixingWith(this.getBodyTag());
		if (bodyTag != null) {
			realOutputWriter.write(bodyTag.asString());
		} else {
			realOutputWriter.write("<body>\n");
		}
		// inject a javascript structure with some data about the filter
		realOutputWriter.write("<script>\n");
		realOutputWriter.write(_composeFilterJSData(ctx));
		realOutputWriter.write("</script>\n");
		
		// write the html before the app include
		if (_preAppContainerHtml != null) realOutputWriter.write(_preAppContainerHtml);
		
		// write the app html
		includedApp.writeRestOfHtmlTo(realOutputWriter);
		
		// write the html after the app include
		if (_postAppContainerHtml != null) realOutputWriter.write(_postAppContainerHtml);
		
		// write the bodyTag and html end
		realOutputWriter.write("\n</body>");
		realOutputWriter.write("\n</html>");
		
		realOutputWriter.flush();
		realOutputWriter.close();
	}
	private static String _composeFilterJSData(final R01HPortalPageAppEmbedContext ctx) {
		StringBuilder outJSData = new StringBuilder();
		outJSData.append("var _r01 = {\n");
		if (ctx != null) {
			if (ctx.getRequestedUrlPath() != null) {
				outJSData.append("\trequestedUri : '").append(R01HPortalPageAppEmbedContext.removeProxyWarFromUrlPath(ctx.getRequestedUrlPath().asAbsoluteString())).append("',\n");
			} else {
				outJSData.append("\trequestedUri : null,\n");
			}
			if (Strings.isNOTNullOrEmpty(ctx.getClientIp())) {
				outJSData.append("\tclientIp : '").append(ctx.getClientIp()).append("',\n");
			} else {
				outJSData.append("\tclientIp : null,\n");
			}
			if (ctx.getPortalId() != null && ctx.getPageId() != null) {
				outJSData.append("\tportalInfo : {\n");
				outJSData.append("\t\tportalId : '").append(ctx.getPortalId()).append("',\n");
				outJSData.append("\t\tpageId : '").append(ctx.getPageId()).append("'\n");
				outJSData.append("\t},\n");
			} else {
				outJSData.append("\tportalInfo : {},\n");
			}
			if (ctx.getLang() != null) {
				outJSData.append("\tlang : {\n");
				outJSData.append("\t\tcode : '").append(Languages.countryLowerCase(ctx.getLang())).append("',\n");
				outJSData.append("\t\tname : '").append(ctx.getLang().name()).append("'\n");
				outJSData.append("\t},\n");
			} else {
				outJSData.append("\tlang : {},\n");
			}
			if (ctx.getUserAgentData() != null) {
				outJSData.append("\tuserAgent : ");
				outJSData.append(ctx.getUserAgentData().getUserAgentJSVar());
				outJSData.append("\n");
			}
		}
		outJSData.append("}\n");
		return outJSData.toString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	public String miniDebugInfo() {
		return Strings.customized("portal-page={}-{} ({}) lastResource={} lastModified={} lastCheck={} hitCount={}",
								  _portalId,_pageId,_copy,
								  _containsLastResourceContainerPageHtml,
								  _lastModifiedTimeStamp,_lastCheckTimeStamp,_hitCount);
	}
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("<html>\n" + 
								  "{}" +			// pre-head
								  "<head>\n" +
								  		"{}" +		// head
								  "</head>\n" +
								  "\t{}" +			// pre-container
								  "<!--#include virtual='$CONT'-->\n" +
								  "{}" + 				// post-container
								  "</html>",
								  _preHeadHtml,
								  _head.asString(),
								  _preAppContainerHtml,_postAppContainerHtml);
	}
}
