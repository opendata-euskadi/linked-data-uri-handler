package r01hp.lod.urihandler.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

/**
 * A request wrapper that:
 * 		- Stores the POSTed BODY so it can be read multiple times 
 * 		  (if not cached, if the request body is read, it cannot be read again and an IllegalStateException is thrown)
 * 		- Fakes the MimeType
 * 		- Fakes the url path
 * (see http://natch3z.blogspot.com.es/2009/01/read-request-body-in-filter.html
 *  and ContentCachingRequestWrapper from Spring framework)
 */
  class R01HMultiReadableHttpServletRequestWrapper 
extends HttpServletRequestWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
	private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String METHOD_POST = "POST";
	
	private static final int MAX_POST_SIZE = 1024 * 4;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final ByteArrayOutputStream _cachedContent;
	private ServletInputStream _inputStream;
	private BufferedReader _reader;

/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HMultiReadableHttpServletRequestWrapper(final HttpServletRequest req) {
		super(req);
		int contentLength = req.getContentLength();
		_cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : MAX_POST_SIZE);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (_inputStream == null) _inputStream = new ContentCachingInputStream(super.getRequest()
																					.getInputStream());
		return _inputStream;
	}
	@Override
	public BufferedReader getReader() throws IOException {
		if (_reader == null) _reader = new BufferedReader(new InputStreamReader(this.getInputStream(), 
														  this.getCharacterEncoding()));
		return _reader;
	}
	/**
	 * Return the cached request content as a byte array.
	 */
	public byte[] getContentAsByteArray() {
		return _cachedContent.toByteArray();
	}
	@Override
	public String getCharacterEncoding() {
		String enc = super.getCharacterEncoding();
		return (enc != null ? enc : DEFAULT_CHAR_ENCODING);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public String getParameter(final String paramName) {
		if (_cachedContent.size() == 0 
		 && this.isFormPost()) {
			_writeRequestParametersToCachedContent();
		}
		return super.getParameter(paramName);
	}
	@Override @SuppressWarnings("unchecked")
	public Map<String,String[]> getParameterMap() {
		if (_cachedContent.size() == 0 
		 && this.isFormPost()) {
			_writeRequestParametersToCachedContent();
		}
		return super.getParameterMap();
	}
	@Override @SuppressWarnings("unchecked")
	public Enumeration<String> getParameterNames() {
		if (_cachedContent.size() == 0 
		 && this.isFormPost()) {
			_writeRequestParametersToCachedContent();
		}
		return super.getParameterNames();
	}
	@Override
	public String[] getParameterValues(final String paramName) {
		if (_cachedContent.size() == 0 
		 && this.isFormPost()) {
			_writeRequestParametersToCachedContent();
		}
		return super.getParameterValues(paramName);
	}
	@SuppressWarnings("unchecked")
	private void _writeRequestParametersToCachedContent() {
		try {
			if (_cachedContent.size() == 0) {
				String encoding = this.getCharacterEncoding();
				Map<String,String[]> form = super.getParameterMap();
				for (Iterator<String> paramNameIt = form.keySet().iterator(); paramNameIt.hasNext(); ) {
					String paramName = paramNameIt.next();
					List<String> paramValues = Arrays.asList(form.get(paramName));
					
					// write paramName=paramValue1&paramName=paramValue2...
					for (Iterator<String> paramValueIt = paramValues.iterator(); paramValueIt.hasNext(); ) {
						String paramValue = paramValueIt.next();
						
						_cachedContent.write(URLEncoder.encode(paramName,encoding)
													   .getBytes());
						if (paramValue != null) {
							_cachedContent.write('=');
							_cachedContent.write(URLEncoder.encode(paramValue,encoding)
														   .getBytes());
							if (paramValueIt.hasNext()) _cachedContent.write('&');
						}
					}
					if (paramNameIt.hasNext()) _cachedContent.write('&');
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to write request parameters to cached content", ex);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean isFormPost() {
		String contentType = this.getContentType();
		return (contentType != null 
			 && contentType.contains(FORM_CONTENT_TYPE) 			// application/x-www-form-urlencoded
			 && METHOD_POST.equalsIgnoreCase(this.getMethod()));	// POST
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private class ContentCachingInputStream 
		  extends ServletInputStream {

		private final InputStream _src;
		
		public ContentCachingInputStream(final ServletInputStream is) {
			// the _writeRequestParametersToCachedContent() method also reads the 
			// body content if it's a form encoded POST
			// ... if a getParameter method is called, _writeRequestParametersToCachedContent()
			// is called before getInputStream()
			if (_cachedContent.size() == 0) {
				try {
					IOUtils.copyLarge(is,_cachedContent,
									  0,						// initial offset
									  MAX_POST_SIZE);			// max number of chars to be readed
				} catch(IOException ioEx) {
					ioEx.printStackTrace(System.out);
				}
			}
			_src = new ByteArrayInputStream(R01HMultiReadableHttpServletRequestWrapper.this.getContentAsByteArray());
		}
		@Override
		public int read() throws IOException {
			int ch = _src.read();
			return ch;
		}
	}

}
