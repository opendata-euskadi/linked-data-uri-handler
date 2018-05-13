package r01hp.bootstrap.lod;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.epimorphics.lda.support.LogRequestFilter;
import com.sun.jersey.api.container.filter.PostReplaceFilter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import lombok.RequiredArgsConstructor;
import r01hp.lod.config.R01HLODURIHandlerConfig;

@RequiredArgsConstructor
  class R01HLODELDAServletGuiceModule
extends JerseyServletModule {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private final R01HLODURIHandlerConfig _uriHandlerConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	protected void configureServlets() {
		this.serve("/doc/*").with(GuiceContainer.class,
								   _jerseyFilterParams());
									
		// elda Log filter
		this.bind(LogRequestFilter.class)
			.in(Singleton.class);	// filters MUST be singletons
		this.filter("/doc/*")
		    .through(LogRequestFilter.class,
		    		 _eldaRequestLoggerFilterParams());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	ELDA
/////////////////////////////////////////////////////////////////////////////////////////	
	private static Map<String,String> _jerseyFilterParams() {
		Map<String,String> jerseyFilterParams = new HashMap<String,String>();
		jerseyFilterParams.put("com.sun.jersey.config.property.packages",
							   "com.epimorphics.lda.restlets");
		jerseyFilterParams.put("com.sun.jersey.config.feature.FilterForwardOn404",
							   Boolean.toString(true));
		jerseyFilterParams.put("com.sun.jersey.spi.container.ContainerRequestFilters",
							   PostReplaceFilter.class.getName());
		return jerseyFilterParams;
	}
	private static Map<String,String> _eldaRequestLoggerFilterParams() {
		Map<String,String> loggerFilterParams = new HashMap<String,String>();
		loggerFilterParams.put("com.epimorphics.lda.logging.ignoreIfMatches",
							   ".*/lda-assets/.*");
		return loggerFilterParams;
	}
}
