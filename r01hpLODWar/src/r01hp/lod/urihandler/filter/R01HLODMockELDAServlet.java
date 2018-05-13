package r01hp.lod.urihandler.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import r01hp.lod.config.R01HLODURIHandlerConfig;

@Slf4j
@Singleton
public class R01HLODMockELDAServlet 
	 extends HttpServlet {
	
	private static final long serialVersionUID = 2864142213469364675L;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS	
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HLODURIHandlerConfig _uriHandlerConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01HLODMockELDAServlet(final R01HLODURIHandlerConfig uriHandlerConfig) {
		_uriHandlerConfig = uriHandlerConfig;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doGet(final HttpServletRequest req,final HttpServletResponse res) throws ServletException, 
																							 IOException {
		System.out.println("\n\n\n\n[ELDA MOCK] " + req.getRequestURI());
		res.getWriter().write("This is ELDA!!!");
	}
	@Override
	protected void doPost(final HttpServletRequest req,final HttpServletResponse res) throws ServletException, 
																							 IOException {
		System.out.println("\n\n\n\n[ELDA MOCK] " + req.getRequestURI());
		res.getWriter().write("This is ELDA!!!");
	}
	
}
