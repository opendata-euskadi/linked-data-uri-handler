package r01hp.portal.appembed;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Strings;
import r01f.util.types.locale.Languages;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * A fixed-length little log of the last urls proxied through the container page include servlet filter
 * (see {@link R01HPortalPageAppEmbedServletFilter})
 */
public class R01HPortalEmbeddedAppUrlLog {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	// an event bus is used to put entries into the log entries array
	// in an async way
	private final ExecutorService _logExecutor;
	private final EventBus _eventBus;
	
	private final R01HIncludedAppUrlLogEntry[] _logEntries;
	private int _currLogEntryWritePos = 0;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalEmbeddedAppUrlLog(final int logSize) {
		// create the event bus backed by a single thread
		_logExecutor = Executors.newSingleThreadExecutor();
		_eventBus = new AsyncEventBus(_logExecutor);		// an event bus backed by a single thread executor
		_eventBus.register(new R01HIncludedAppUrlLogEntryAddEventListener());
		
		_logEntries = new R01HIncludedAppUrlLogEntry[logSize];		// stores the log entries
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public void shutdown() {
		try {
			_logExecutor.shutdown();
			_logExecutor.awaitTermination(1,TimeUnit.MINUTES);	// wait 1m for the executor to shutdown
		} catch (Throwable th) {
			th.printStackTrace(System.out);			
		}
	}
	/**
	 * Resets the request log
	 */
	public void reset() {
		_eventBus.post(new R01HIncludedAppUrlLogResetEvent());
	}
	/**
	 * Adds a new log entry
	 * @param ctx
	 */
	public void add(final R01HPortalPageAppEmbedContext ctx,
					final long elapsedMilis) {
		// create a log entry
    	R01HIncludedAppUrlLogEntry logEntry = new R01HIncludedAppUrlLogEntry(System.currentTimeMillis(),
    																		 ctx.getClientIp(),
    																		 ctx.getRequestedUrlPath().asAbsoluteString(),
    																		 !ctx.isIncludeInAppContainerPageDisabled(),
    																		 elapsedMilis,
    																		 ctx.getPortalId(),ctx.getPageId(),ctx.getLang());
    	// post to an event bus backed by a dedicated thread in order to increase throughput
    	_eventBus.post(new R01HIncludedAppUrlLogEntryAddEvent(logEntry));
	}
	/**
	 * Returns all log entries
	 * @return
	 */
	public Observable<R01HIncludedAppUrlLogEntry> getEntries() {
		return Observable.create(new OnSubscribe<R01HIncludedAppUrlLogEntry>() {
										@Override
										public void call(final Subscriber<? super R01HIncludedAppUrlLogEntry> observer) {
											observer.onStart();
											for(int i=_currLogEntryWritePos; i < _logEntries.length; i++) {
												if (_logEntries[i] != null) observer.onNext(_logEntries[i]);
											}
											for(int i=0; i < _currLogEntryWritePos; i++) {
												if (_logEntries[i] != null) observer.onNext(_logEntries[i]);
											}
											observer.onCompleted();
										}
						  		 });
	}
	/**
	 * Writes the log entries to the given writer
	 * @param w
	 */
	public void printEntriesTo(final Writer w) {
		this.getEntries().subscribe(new Action1<R01HIncludedAppUrlLogEntry>() {
												@Override
												public void call(final R01HIncludedAppUrlLogEntry entry) {
													try {
														w.write(entry.asString());
														w.write("\n");
													} catch(IOException ioEx) { /* ignored */ }
												}
									});
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOG ENTRY
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public class R01HIncludedAppUrlLogEntry 
	  implements CanBeRepresentedAsString {
		@Getter private final long _timeStamp;
		@Getter private final String _clientIp;
		@Getter private final String _url;
		@Getter private final boolean _includedInAppContainerPage;
		@Getter private final long _elapsedMilis;
		@Getter private final R01HPortalID _portalId;
		@Getter private final R01HPortalPageID _pageId;
		@Getter private final Language _lang;
		
		@Override
		public String asString() {
			return Strings.customized("{} | {} | {} | {} milis | {}-{}/{} | {}",
									  new Date(_timeStamp),
									  _clientIp,
									  _includedInAppContainerPage,
									  _elapsedMilis,
									  _portalId,_pageId,Languages.countryLowerCase(_lang),_url);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	LOG EVENT BUS
/////////////////////////////////////////////////////////////////////////////////////////
	private interface R01HIncludedAppUrlLogEvent {
		// just a marker interface
	}
	@NoArgsConstructor
	public class R01HIncludedAppUrlLogResetEvent
	  implements R01HIncludedAppUrlLogEvent {
		// just an event
	}
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public class R01HIncludedAppUrlLogEntryAddEvent 
	  implements R01HIncludedAppUrlLogEvent {
		@Getter private final R01HIncludedAppUrlLogEntry _logEntry;
	}
	public class R01HIncludedAppUrlLogEntryAddEventListener {
		@Subscribe
		public void onNewLogEntry(final R01HIncludedAppUrlLogEvent logEvent) {
			if (logEvent instanceof R01HIncludedAppUrlLogEntryAddEvent) {
				R01HIncludedAppUrlLogEntryAddEvent addEvent = (R01HIncludedAppUrlLogEntryAddEvent)logEvent;
				
				// just add the log entry to the entries array 
				// beware that this method is executed in a dedicated thread
				// to increase filter throughput
		    	synchronized(_logEntries) {
			    	_logEntries[_currLogEntryWritePos] = addEvent.getLogEntry();
			    	_currLogEntryWritePos++;
			    	if (_currLogEntryWritePos == _logEntries.length) _currLogEntryWritePos = 0;		// circular log
		    	}
			} 
			else if (logEvent instanceof R01HIncludedAppUrlLogResetEvent) {
				// Clear the buffer entries
				synchronized(_logEntries) {
					for (int i=0; i < _logEntries.length; i++) {
						_logEntries[i] = null;
					}
				}
			}
		}
	}
}
