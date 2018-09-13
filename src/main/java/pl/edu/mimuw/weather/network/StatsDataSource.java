package pl.edu.mimuw.weather.network;

/**
 * Created by marian on 6/23/17.
 */

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import pl.edu.mimuw.weather.event.*;
import rx.Observable;
import rx.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import static pl.edu.mimuw.weather.event.EventStream.eventStream;


public abstract class StatsDataSource {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.
            getLogger(StatsDataSource.class);
    private static final int POLL_INTERVAL = 60;
    private static final int INITIAL_DELAY = 3;
    private static final int TIMEOUT = 20;
    protected static final double NAN = -101;

    public Observable<? extends WeatherEvent> dataSourceStream() {
        /*
		 * This creates a stream of data events. Each event emitted corresponds
		 * to a piece of data fetched from a remote (i.e. Internet) data source.
		 * This class is capable of grabbing data in one of two ways. Firstly,
		 * it can poll the data source every POLL_INTERVAL seconds. Secondly, it
		 * can fetch data on request (e.g. when a user hits the refresh button
		 * which causes a RefreshRequestEvent to be triggered; the event is
		 * handled here). The code below essentially merges events that arrive
		 * via one of the two routes into a single stream of events.
		 */
        return fixedIntervalStream().compose(this::wrapRequest)
                .mergeWith(eventStream().eventsInIO().ofType(RefreshRequestEvent.class).compose(this::wrapRequest));
    }

    protected Observable<Long> fixedIntervalStream() {
        return Observable.interval(INITIAL_DELAY, POLL_INTERVAL, TimeUnit.SECONDS, Schedulers.io());
    }

    protected abstract <T> Observable<? extends WeatherEvent> makeRequest();

    protected HttpClientRequest<ByteBuf> prepareHttpGETRequest(String url) {
		/*
		 * As the name says, this creates an HTTP GET request (but does not send
		 * it, sending is done elsewhere).
		 */
        return HttpClientRequest.createGet(url);
    }

    protected <T> Observable<String> unpackResponse(Observable<HttpClientResponse<ByteBuf>> responseObservable) {
		/*
		 * Extracts HTTP response's body to a plain Java string
		 */
        return responseObservable.flatMap(HttpClientResponse::getContent)
                .map(buffer -> buffer.toString(CharsetUtil.UTF_8));
    }

    private <T> Observable<WeatherEvent> wrapRequest(Observable<T> observable) {
		/*
		 * Issues an HTTP query but emits an appropriate even before the query
		 * is made and another event when the query is completed. This allows us
		 * to give visual feedback (spinning icon) to the user during the
		 * request.
		 */
        return observable.flatMap(ignore -> Observable.
                        concat(Observable.just(new NetworkRequestIssuedEvent()),
                makeRequest().timeout(TIMEOUT, TimeUnit.SECONDS).doOnError(log::error)
                        /* if a request takes more than 30 seconds abort it */
                        .cast(WeatherEvent.class).onErrorReturn(ErrorEvent::new),
                                /* emit an error event */
                Observable.just(new NetworkRequestFinishedEvent()))
                /* emit NetworkRequestFinishedEvent after */
        );
    }
}