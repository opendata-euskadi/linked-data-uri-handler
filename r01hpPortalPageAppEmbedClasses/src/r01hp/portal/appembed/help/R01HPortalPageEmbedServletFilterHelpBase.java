package r01hp.portal.appembed.help;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import lombok.extern.slf4j.Slf4j;
import r01f.io.Streams;
import r01f.io.util.StringPersistenceUtils;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.servlet.HttpRequestQueryStringParamsWrapper;
import r01f.types.Path;
import r01hp.portal.appembed.R01HPortalEmbeddedAppUrlLog;
import r01hp.portal.appembed.R01HPortalPageAppEmbedContext;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;
import r01hp.portal.appembed.config.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetrics;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetricsHelp;

/**
 * Renders help about {@link R01HPortalPageAppEmbedServletFilter}
 */
@Slf4j
public abstract class R01HPortalPageEmbedServletFilterHelpBase 
		   implements R01HPortalPageEmbedServletFilterHelp {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * A log of request for debugging pourposes
     */
    protected final R01HPortalEmbeddedAppUrlLog _reqLog = new R01HPortalEmbeddedAppUrlLog(100);	// a log with a fixed size of 100
	/**
	 * Config
	 */
	protected final R01HPortalPageAppEmbedServletFilterConfig _config;
	/**
	 * Metrics help render
	 */
	private final R01HPortalPageAppEmbedMetricsHelp _metricsHelp;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageEmbedServletFilterHelpBase(final R01HPortalPageAppEmbedServletFilterConfig config,
													final R01HPortalPageAppEmbedMetrics metrics) {
		_config = config;
		_metricsHelp = new R01HPortalPageAppEmbedMetricsHelp(metrics);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void addRequestLogEntry(final R01HPortalPageAppEmbedContext ctx,
								   final long elapsedMilis) {
		_reqLog.add(ctx,
					elapsedMilis);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void renderHelp(final HttpServletRequest realHttpReq,
                           final HttpServletResponse realHttpResp) throws IOException {

		HttpRequestQueryStringParamsWrapper qryStrParams = new HttpRequestQueryStringParamsWrapper(realHttpReq);
		
		// download doc
		if (qryStrParams.containsParamWithName("doc")){
			log.debug("downloading help document");
			InputStream docStream = ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER.getInputStream(Path.from("r01hp/doc/R01hpHelp.docx"));
			realHttpResp.setContentType("application/octet-stream");
			realHttpResp.setHeader("Content-Disposition", "attachment;filename=\"R01hpHelp.zip\"");
//			realHttpResp.setContentLength(docStream.available());
			
			ServletOutputStream responseOutput = realHttpResp.getOutputStream();
			IOUtils.copyLarge(docStream,responseOutput);
		    responseOutput.flush();
		    responseOutput.close();
		    return;
		}
		
		
		Writer w = realHttpResp.getWriter();
		
		// refresh metrics
		if (qryStrParams.containsParamWithName("r01hCmd")
		     && qryStrParams.paramWithName("r01hCmd").asString().orDefault("")
		     				.equals("refreshMetrics")) {
			if (qryStrParams.paramWithName("r01hCompactMetricsReport").asString().orDefault("off").equals("on")) {
				_metricsHelp.refreshMetrics(w,true);	// compact
			} else {
				_metricsHelp.refreshMetrics(w,false);	// not compact
			}
		}
		// refresh log
		else if (qryStrParams.containsParamWithName("r01hCmd")
			  && qryStrParams.paramWithName("r01hCmd").asString().orDefault("")
			  				 .equals("refreshReqLog")) {
			if (qryStrParams.paramWithName("r01hClearReqLog").asString().orDefault("off").equals("on")) {
				_reqLog.reset();	// reset log
			}
			_reqLog.printEntriesTo(w);
		}			
		// paint help
		else {
			w.write("<!DOCTYPE html>\n");
			w.write("<html lang='en'>\n");
			w.write("<head>\n");
			_renderHEADResources(w);
			w.write("</head>\n");
			w.write("<body>\n");
			w.write(	"<div class='container-fluid' data-role='page' id='r01hpHelp'>\n");
			_renderTabs(w);
			_renderTabContent(w,
							  realHttpReq.getRequestURI());
			w.write(	"</div>\n");
			w.write("</body>\n");
			w.write("</html>\n");
		}
		w.flush();
		w.close();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HEAD
/////////////////////////////////////////////////////////////////////////////////////////
	private void _renderHEADResources(final Writer w) throws IOException {
		w.write(	"\t<title>::: " + R01HPortalPageAppEmbedServletFilter.class.getSimpleName() + " HELP page :::</title>\n");
		w.write(	"\t<meta name='viewport' content='width=device-width, initial-scale=1'>\n");
		w.write(	"\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n");

		// jquery & bootstrap
		w.write(	"\t<script src='https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js'></script>\n");
		w.write(	"\t<link rel='stylesheet' type='text/css' href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css'/>\n");
		w.write(	"\t<script src='http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js'></script>\n");

		// custom js
		w.write("\t<script type='text/javascript'>\n");
		w.write("//<![CDATA[\n");	// html5 parses script tags... escape
		w.write(StringPersistenceUtils.load(ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER
																  .getInputStream(Path.from("r01hp/portal/appembed/help/r01hPortalPageEmbedServletFilterHelp.js"))));
		w.write("//]]>\n");
		w.write("\t</script>\n");

		// custom css
		w.write(	"\t<style>\n");
		w.write(StringPersistenceUtils.load(ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER
																  .getInputStream(Path.from("r01hp/portal/appembed/help/r01hPortalPageEmbedServletFilterHelp.css"))));
		w.write(	"\t</style>\n");
		
		_renderMyHEADResources(w);
	}
	protected abstract void _renderMyHEADResources(final Writer w) throws IOException;
/////////////////////////////////////////////////////////////////////////////////////////
//  TABS
/////////////////////////////////////////////////////////////////////////////////////////
	private void _renderTabs(final Writer w) throws IOException {
//		w.write("<nav class='navbar navbar-default'>\n");
		w.write(	"<ul class='nav nav-tabs'>\n");
		w.write(		"<li role='presentation' class='active'><a data-toggle='tab' href='#main'>Overview</a></li>\n");
		w.write(		"<li role='presentation'><a data-toggle='tab' href='#metrics'>Metrics</a></li>\n");
		_renderMyTabs(w);
		w.write(	"</ul>\n");
//		w.write("</nav>\n");
	}
	protected abstract void _renderMyTabs(final Writer w) throws IOException;
	
	private void _renderTabContent(final Writer w,
								   final String url) throws IOException {
		w.write("<div class='tab-content'>\n");

		// ----- Main / Help
		// Link to the doc as word document
		w.write(	"<div id='main' class='tab-pane fade in active'>\n");
		w.write(		"<p>Download the documentation <a href='" + url + "?doc=true&r01hpHelp=true'>here</a></p>\n");
		w.write(	"</div>\n");
		
		// Doc as html (created with https://html-cleaner.com/)
		w.write(    "<div class='r01hpHelpDoc'>\n");
		InputStream docStream = ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER.getInputStream(Path.from("r01hp/doc/R01hpHelp.html"));
		Streams.copy(new InputStreamReader(docStream),
					 w,
					 1024 * 4);		// 4k buffer
		w.write(    "</div>\n");


		// ----- Metrics & Log
		w.write(	"<div id='metrics' class='tab-pane fade'>\n");
						_metricsHelp.render(w);	// metrics
						_renderRequestLog(w);	// the last requests through the filter
		w.write(	"</div>\n");
		
		// ----- MyTabs
		_renderMyTabsContent(w);
	}
	protected abstract void _renderMyTabsContent(final Writer w) throws IOException;
/////////////////////////////////////////////////////////////////////////////////////////
//  METRICS & Log
/////////////////////////////////////////////////////////////////////////////////////////
	private void _renderRequestLog(final Writer w) throws IOException {
		w.write("<div>\n");
					_renderReqLogHelp(w);
		w.write(	"<section id='sectionLog'>\n");
					_renderRequestLogEntries(w);
		w.write(	"</section>\n");
		w.write("</div>\n");
	}
	private void _renderRequestLogEntries(final Writer w) throws IOException {
		// The form
		w.write(	"<form class='form-inline' id='frmRefreshReqLog'>\n");
		w.write(		"<input type='hidden' id='r01hpHelp' name='r01hpHelp' value='true'/>\n");
		w.write(		"<input type='hidden' id='r01hCmd' name='r01hCmd' value='refreshReqLog'/>\n");
		w.write(		"<input type='hidden' id='r01hReqLogClear' name='r01hReqLogClear' value='false'/>\n");
		w.write(		"<div class='form-group'>\n");
		w.write(			"<button type='button' class='btn btn-primary' id='btnRefreshReqLog'>Refresh Log</button>\n");
		w.write(			"<label><input type='checkbox' name='r01hClearReqLog' id='r01hClearReqLog'/>Reset Log</label>\n");
		w.write(		"</div>\n");
		w.write(	"</form>\n");
		// The lines
		w.write(	"<pre id='reqLogDetail'>\n");
						_reqLog.printEntriesTo(w);
		w.write(	"</pre>\n");
	}
	@SuppressWarnings("static-method")
	private void _renderReqLogHelp(final Writer w) throws IOException {
		w.write("<div class='alert alert-info' role='alert'>\n");
		w.write(	"<p>The request log only stores a bunch of the latest requests passing through the portal page application embed filter</p>");
		w.write("</div>\n");
	}
}
