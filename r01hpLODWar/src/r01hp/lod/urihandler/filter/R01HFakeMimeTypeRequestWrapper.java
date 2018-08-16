package r01hp.lod.urihandler.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.common.collect.Lists;

import r01hp.lod.urihandler.R01HMIMEType;

/**
 * A request wrapper that:
 * 		- Fakes the url path
 */
  class R01HFakeMimeTypeRequestWrapper
extends HttpServletRequestWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	  private final R01HMIMEType _mime;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HFakeMimeTypeRequestWrapper(final HttpServletRequest request,
										  final R01HMIMEType mime) {
		super(request);
		_mime = mime;
	}
	public R01HFakeMimeTypeRequestWrapper(final HttpServletRequest request) {
		super(request);
		_mime = null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDE
/////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String getHeader(final String name) {
		// TODO review 
		if (name.equalsIgnoreCase("accept")
		 && _mime != null) {
			return _mime.getMime().asString();
		}
		// cookies can harm the [triple-store]
		if (name.toUpperCase().startsWith("COOKIE")) {
			return "";
		}
		return super.getHeader(name);
	}
	@Override
	public Enumeration getHeaders(final String name) {
		if (name.equalsIgnoreCase("accept")
		 && _mime != null) {
			List<String> list = new ArrayList<String>();
			list.add(_mime.getMime().asString());			
			return Collections.enumeration(list);
		}
		// cookies can harm the [triple-store]
		if (name.toUpperCase().startsWith("COOKIE")) {
			return Collections.enumeration(Lists.newArrayList());
		}
		return super.getHeaders(name);
	}
}
