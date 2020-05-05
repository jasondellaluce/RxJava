package edu.uci.swe215;

import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;

@SuppressWarnings("unchecked")
public class ObservableCacheTest {

	@Test
	public void shouldStayToEmptyOnSubscription() {
		Observable<Integer> emptyObservable = Observable.empty();
		Observable<Integer> observable = emptyObservable.cache();
		observable.subscribe();
		observable.test().assertNoValues();
	}
	
	@Test
	public void shouldChangeFromEmptyToHasErrorOnErrorEmission() {
		Observable<Integer> emptyObservable = Observable.empty();
		Observable<Integer> observable = emptyObservable.cache();
		Observer<Integer> observer = (Observer<Integer>) observable;
		observable.test().assertNoValues();
		observer.onError(new TestException());
		observable.test().assertError(TestException.class);
	}
	
	@Test
	public void shouldChangeFromEmptyToHasValuesOnValueEmission() {
		Observable<Integer> emptyObservable = Observable.empty();
		Observable<Integer> observable = emptyObservable.cache();
		Observer<Integer> observer = (Observer<Integer>) observable;
		observable.test().assertNoValues();
		observer.onNext(5);
		observable.test().assertValueCount(1);
	}
	
	@Test
	public void shouldStayToHasValuesOnSubscription() {
		Observable<Integer> observable = Observable.fromArray(5).cache();
		observable.test().assertValueCount(1);
		observable.subscribe();
		observable.test().assertValueCount(1);
	}
	
	@Test
	public void shouldStayToHasValuesOnValueEmission() {
		Observable<Integer> observable = Observable.fromArray(5).cache();
		Observer<Integer> observer = (Observer<Integer>) observable;
		observable.test().assertValueCount(1);
		observer.onNext(6);
		observable.test().assertValueCount(2);
	}
	
	@Test
	public void shouldChangeFromHasValuesToHasErrorOnErrorEmission() {
		Observable<Object> observable = Observable.empty().cache();
		Observer<Integer> observer = (Observer<Integer>) observable;
		observer.onNext(10);
		observable.test().assertValueCount(1);
		observer.onError(new TestException());
		observable.test().assertError(TestException.class);
	}
	
	@Test
	public void shouldChangeFromHasValuesToCompletedOnCompletion() {
		Observable<Object> observable = Observable.empty().cache();
		Observer<Integer> observer = (Observer<Integer>) observable;
		observer.onNext(10);
		observer.onNext(1);
		observable.test().assertValueCount(2);
		observer.onComplete();
		observable.test().assertComplete();
	}
	
	@Test
	public void shouldStayToHasErrorOnSubscription() {
		Observable<Object> observable = Observable.error(new TestException()).cache();
		observable.test().assertError(TestException.class);
		observable.subscribe((v) -> {}, (v) -> {});
		observable.test().assertError(TestException.class);
	}
	
	@Test
	public void shouldStayToCompletedOnSubscription() {
		Observable<Integer> observable = Observable.just(1).cache();
		observable.test().assertComplete();
		observable.test().assertValueCount(1);
		observable.subscribe((v) -> {}, (v) -> {});
		observable.test().assertComplete();
		observable.test().assertValueCount(1);
	}
	
	
}
