package r01hp.bootstrap.portal.appembed;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetrics;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetricsConfig;

/**
 * Codahale's dropwizard metrics
 */
@Slf4j
@RequiredArgsConstructor
public class R01HMetricsGuiceBindingsModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HPortalPageAppEmbedMetricsConfig _metricsConfig;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("resource")
	public void configure(final Binder binder) {
		log.warn("[START BINDING METRICS]");
		if (_metricsConfig.isEnabled()) {
			// config
			binder.bind(R01HPortalPageAppEmbedMetricsConfig.class)
			  	  .toInstance(_metricsConfig);
		
			// Bind a metrics registry instance
			final MetricRegistry metricsRegistryInstance = new MetricRegistry();
			binder.bind(MetricRegistry.class)
				  .toInstance(metricsRegistryInstance);
			
			// bind the metrics
			binder.bind(R01HPortalPageAppEmbedMetrics.class)
				  .in(Singleton.class);
			
			// bind a healthcheck registry instance
			binder.bind(HealthCheckRegistry.class)
				  .in(Singleton.class);
			
			// bind the reporters
			// ... bind the console reporter
			if (_metricsConfig.isConsoleReporterEnabled()) {
				ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricsRegistryInstance)
				          								  		 .convertRatesTo(TimeUnit.SECONDS)
				          								  		 .convertDurationsTo(TimeUnit.MILLISECONDS)
				          								  		 .build();
				binder.bind(ConsoleReporter.class)
					  .toInstance(consoleReporter);
				log.warn("\t-console reporter OK");
			} else {
				log.warn("\t-console reporter is NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
			}
			// ... bind the slf4j reporter
			if (_metricsConfig.isSlf4jReporterEnabled()) {
				Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricsRegistryInstance)
														   .outputTo(LoggerFactory.getLogger("r01h.portalPageAppEmbed"))
														   .convertRatesTo(TimeUnit.SECONDS)
														   .convertDurationsTo(TimeUnit.MILLISECONDS)
														   .build();
				binder.bind(Slf4jReporter.class)
					  .toInstance(slf4jReporter);
				log.warn("\t-slf4j reporter reporter OK");
			} else {
				log.warn("\t-slf4j reporter is NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
			}
			// ... bind the jmx reporter
			if (_metricsConfig.isJMXReporterEnabled()) {
				JmxReporter jmxReporter = JmxReporter.forRegistry(metricsRegistryInstance)
												     .build();
				binder.bind(JmxReporter.class)
					  .toInstance(jmxReporter);
				log.warn("\t-jmx reporter reporter OK");				
			} else {
				log.warn("\t-jmx reporter is NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
			}
		} else {
			log.warn("\tMetrics are NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
		}
		log.warn("[END BINDING METRICS]\n\n\n");
	}
//	@SuppressWarnings("resource")
//	private void _initReporters(final R01HMetricsConfig metricsCfg,
//								final MetricRegistry metricsRegistry) {
//		// Init the console reporter
//		if (metricsCfg.isConsoleReporterEnabled()) {
//			TimeLapse reportEvery = metricsCfg.getConsoleReporterConfig().getReportEveryOrDefault(TimeLapse.createFor("30s"));
//			ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricsRegistry)
//			          								  		 .convertRatesTo(TimeUnit.SECONDS)
//			          								  		 .convertDurationsTo(TimeUnit.MILLISECONDS)
//			          								  		 .build();
//			consoleReporter.start(reportEvery.asMilis(),TimeUnit.MILLISECONDS);	
//		} else {
//			log.warn("Metrics console reporter is NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
//		}
//		// Init the slf4j reporter
//		if (metricsCfg.isSlf4jReporterEnabled()) {
//			TimeLapse reportEvery = metricsCfg.getSlf4jReporterConfig().getReportEveryOrDefault(TimeLapse.createFor("30s"));
//			Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricsRegistry)
//													   .outputTo(LoggerFactory.getLogger("r01h.portalpageappembedfilter"))
//													   .convertRatesTo(TimeUnit.SECONDS)
//													   .convertDurationsTo(TimeUnit.MILLISECONDS)
//													   .build();
//			slf4jReporter.start(reportEvery.asMilis(),TimeUnit.MILLISECONDS);
//		} else {
//			log.warn("Metrics slf4j reporter is NOT available (disabled at r01h.portalpageappembedfilter.properties.xml config file)");
//		}
//		// Init the jmx reporter
//		if (metricsCfg.isJMXReporterEnabled()) {
//			JmxReporter reporter = JmxReporter.forRegistry(metricsRegistry)
//											  .build();
//			reporter.start();
//		}
//	}
}
