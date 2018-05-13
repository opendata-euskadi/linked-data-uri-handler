package r01hp.portal.appembed;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

@Slf4j
@Accessors(prefix="_")
abstract class R01HParsedPageBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final Pattern START_TAG_PATTERN = Pattern.compile("<([a-zA-Z0-9]+)[^>]*>");	
	protected static final Pattern END_TAG_PATTERN = Pattern.compile("</([^\\s]+)(?:.|\\s)*>");
	protected static final Pattern META_ATTR_NAME_AND_VALUE_PATTERN = Pattern.compile("(name|http-equiv|content|charset)\\s*=\\s*[\"']([^\"']+)[\"']");
	protected static final Pattern TAG_ATTRS_PATTERN = Pattern.compile("(\\S+)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter	protected Path _pagePath;
	@Getter protected Head _head;
	@Getter protected BodyTag _bodyTag;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a meta tag from the parser token
	 * @param tokenText
	 * @return
	 */
	protected Meta _parseMETA(final String tokenText) {
		Meta outMeta = null;
		
		Map<String,String> attrs = Maps.newHashMap();
		Matcher attrMatcher = META_ATTR_NAME_AND_VALUE_PATTERN.matcher(tokenText);
		while(attrMatcher.find()) {
			String attrName = attrMatcher.group(1).toLowerCase();
			String attrValue = attrMatcher.group(2);
			attrs.put(attrName,attrValue);
		}
		if (CollectionUtils.hasData(attrs) && attrs.size() >= 0) {
			if (attrs.get("content") != null) {
				String content = Strings.removeNewlinesOrCarriageRetuns(attrs.get("content"));
				if (attrs.get("name") != null) {
					outMeta = new NamedMeta(attrs.get("name"),				// it's a named META
										   content);		
				} else if (attrs.get("http-equiv") != null) {
					outMeta = new HttpEquivMeta(attrs.get("http-equiv"),	// it's an http-equiv META
											    content);	
				}
			} else if (attrs.get("charset") != null) {
				outMeta = new CharsetMeta(attrs.get("charset"));			// it's a charset META
			} else {
				log.warn("Illegal META detected: {}",tokenText);
			}
		} else {
			log.warn("Illegal META detected: {}",tokenText);
		}
		return outMeta;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HEAD
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@AllArgsConstructor(access=AccessLevel.MODULE)
	     class Head 
	implements CanBeRepresentedAsString {
		@Getter private String _title;
		@Getter private Collection<Meta> _metas;
		@Getter private String _other;
		
		@Override
		public String asString() {
			StringBuilder outHeadStr = new StringBuilder();
			if (CollectionUtils.hasData(_metas)) {
				for (Meta meta : _metas) {
					String metaStr = meta.asString();
					if (Strings.isNOTNullOrEmpty(metaStr)) outHeadStr.append("\n")
																	 .append(meta.asString());
				}
			}
			if (Strings.isNOTNullOrEmpty(_other)) outHeadStr.append("\n")
															.append(_other);
			if (Strings.isNOTNullOrEmpty(_title)) outHeadStr.append(Strings.removeNewlinesOrCarriageRetuns(Strings.customized("\n<title>{}</title>",
																		   			   										  _title)));
			return outHeadStr.toString();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  META
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@AllArgsConstructor(access=AccessLevel.MODULE)
	abstract class Meta 
	    implements CanBeRepresentedAsString {
		@Getter protected final String _key;
		@Getter protected final String _content;
		
		@Override
		public String asString() {
			if (this instanceof CharsetMeta) return Strings.customized("<meta charset='{}' />",
										  							   _content);
			return Strings.customized("<meta {}='{}' content='{}' />",	// <meta name="x" content="y" /> or <meta http-equiv="x" content="y" />
									  (this instanceof HttpEquivMeta ? "http-equiv" : "name"),_key,
									  _content);
		}
	}
	@Accessors(prefix="_")
	  class NamedMeta 
	extends Meta {
		public NamedMeta(final String name,final String content) {
			super(name,content);
		}
	}
	@Accessors(prefix="_")
	  class HttpEquivMeta 
	extends Meta {
		public HttpEquivMeta(final String name,final String content) {
			super(name,content);
		}
	}
	  class CharsetMeta 
	extends Meta {
		public CharsetMeta(final String content) {
			super("charset",content);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BODY TAG
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	     class BodyTag 
	implements CanBeRepresentedAsString {
		@Getter private final Map<String,String> _attrs;
		
		public BodyTag() {
			_attrs = Maps.newHashMap();
		}
		public BodyTag( final String tagText) {
			this();
			Matcher m = TAG_ATTRS_PATTERN.matcher(tagText);
			while(m.find()) {
				_attrs.put(m.group(1),m.group(2));
			}
		}
		public void addAttributesFrom(final BodyTag otherBodyTag) {
			if (otherBodyTag == null || CollectionUtils.isNullOrEmpty(otherBodyTag.getAttrs())) return;
			for (Map.Entry<String,String> otherBodyTagAttr : otherBodyTag.getAttrs().entrySet()) {
				String thisVal = _attrs.get(otherBodyTagAttr.getKey());
				if (thisVal != null) {
					_attrs.put(otherBodyTagAttr.getKey(),
							   thisVal + " " + otherBodyTagAttr.getValue());
				} else {
					_attrs.put(otherBodyTagAttr.getKey(),
							   otherBodyTagAttr.getValue());
				}
			}
		}
		
		@Override
		public String asString() {
			StringBuilder outBodyTagStr = new StringBuilder();
			outBodyTagStr.append("<body");
			if (CollectionUtils.hasData(_attrs)) {
				for (Map.Entry<String,String> attr : _attrs.entrySet()) {
					outBodyTagStr.append(" ")
								 .append(attr.getKey())
								 .append("='")
								 .append(attr.getValue())
								 .append("'");
				}
			}
			outBodyTagStr.append(">\n");
			
			return outBodyTagStr.toString();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Mix this head with the given one
	 * IMPORTANT: The given head has precedence over this head
	 * @param otherHead
	 * @return
	 */
	Head newHeadMixingWith(final Head otherHead) {
		String thisTitle = _head != null ? _head.getTitle() : null;
		String otherTitle = otherHead != null ? otherHead.getTitle() : null;
		Collection<Meta> thisMetas = _head != null ? _head.getMetas() : null;
		Collection<Meta> otherMetas = otherHead != null ? otherHead.getMetas() : null;
		String thisRestOfHead = _head != null ? _head.getOther() : null;
		String otherRestOfHead = otherHead != null ? otherHead.getOther() : null;
		
		String title = _title(thisTitle,
							  otherTitle);
		Collection<Meta> metas = _metas(thisMetas, 
										otherMetas);
		String restOfHead = _restOfHead(thisRestOfHead,
										otherRestOfHead);
		Head finalHead = (title != null || CollectionUtils.hasData(metas) || restOfHead != null) ? new Head(title,
								  																			metas,
								  																			restOfHead)
																								 : null;	// no head
		return finalHead;
	}
	private static String _title(final String thisTitle,
						  		 final String otherTitle) {
		if (Strings.isNOTNullOrEmpty(thisTitle)) return thisTitle;
		if (Strings.isNOTNullOrEmpty(otherTitle)) return otherTitle;
		return null;
	}
	private static Collection<Meta> _metas(final Collection<Meta> thisMetas,
										   final Collection<Meta> otherMetas) {
		Collection<Meta> outMetas = null;
		if (CollectionUtils.hasData(thisMetas) && CollectionUtils.hasData(otherMetas)) {
			outMetas = Lists.newArrayList();
			
			// This metas has preference over the other ones
			Map<String,Meta> otherMetasIndexed = Maps.newHashMapWithExpectedSize(otherMetas.size());
			for (Meta otherMeta : otherMetas) otherMetasIndexed.put(otherMeta.getKey(),otherMeta);
			
			for (Meta thisMeta : thisMetas) {
				Meta otherMeta = otherMetasIndexed.get(thisMeta.getKey());			// exists in other?
				if (otherMeta != null) otherMetasIndexed.remove(thisMeta.getKey());	// if so, remove the meta in other (this meta has preference)		
			}
			outMetas.addAll(thisMetas);							// this metas has precedence over other metas
			outMetas.addAll(otherMetasIndexed.values());		// the remaining other metas
		} else if (CollectionUtils.hasData(thisMetas)) {
			outMetas = thisMetas;
		} else if (CollectionUtils.hasData(otherMetas)) {
			outMetas = otherMetas;
		}
		return outMetas;
	}
	private static String _restOfHead(final String thisRestOfHead,
							   	      final String otherRestOfHead) {
		int length = (Strings.isNOTNullOrEmpty(thisRestOfHead) ? thisRestOfHead.length() : 0) + 
					 (Strings.isNOTNullOrEmpty(otherRestOfHead) ? otherRestOfHead.length() : 0);
		StringBuilder sb = null;
		if (length > 0) {
			sb = new StringBuilder(length);
			if (Strings.isNOTNullOrEmpty(otherRestOfHead)) sb.append(otherRestOfHead);
			if (Strings.isNOTNullOrEmpty(thisRestOfHead)) sb.append(thisRestOfHead);
		}
		return sb != null ? sb.toString() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	BodyTag newBodyTagMixingWith(final BodyTag otherBodyTag) {
		BodyTag finalBodyTag = null;
		if (_bodyTag == null) {
			finalBodyTag = otherBodyTag;
		} else if (otherBodyTag == null) {
			finalBodyTag = _bodyTag;
		} else {
			finalBodyTag = new BodyTag();
			finalBodyTag.addAttributesFrom(_bodyTag);
			finalBodyTag.addAttributesFrom(otherBodyTag);
		}
		return finalBodyTag;
	}
}
