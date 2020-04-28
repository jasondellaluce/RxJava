package edu.uci.swe215;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

public class SingleAmbTest extends RxJavaTest {

	@Test(expected = NullPointerException.class)
	public void shouldThrowOnNullIterable() {
		Single.amb(null).subscribe();
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldThrowOnNullArray() {
		Single.ambArray((SingleSource<? extends Object>[]) null)
			.subscribe();
	}
	
	/* Because empty observables, that emits no values, are legal
	 * by specification */
	@Test
	public void shouldAcceptEmptyIterable() {
		Single.amb(Arrays.asList()).subscribe();
	}
	
	@Test
	public void shouldAcceptOneSingleElement() {
		Single.ambArray(Single.just(1))
			.subscribe(e -> assertEquals(e.intValue(), 1));
	}
	
	@Test
	public void shouldErrorOnNullEmittedValue() {
		Single.ambArray(Single.fromSupplier(() -> null))
			.test().assertError(NullPointerException.class);
		
	}
	
	/* Because the Single is defined as "cold" by default as specifciation
	 * , and should not iterate on the iterator before a subscription. */
	@Test
	public void shouldAcceptFaultyIterable() {
		Single.amb(new MaliciousIterable());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void shouldErrorOnFaultyIterableIteration() {
		Single.amb(new MaliciousIterable()).blockingGet();
	}
	
	@Test
	public void shouldRespectOrder() {
		Single<Integer> first = Single.just(1).delay(3, TimeUnit.SECONDS);
		Single<Integer> second = Single.just(2);
		int result = Single.ambArray(first, second).blockingGet(); 
		assertEquals(result, 2);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldRespectOrderOnLargeInput() {
		Single<Integer> first = Single.just(1).delay(50, TimeUnit.MILLISECONDS);
		Single<Integer> second = Single.just(2);
		
		Single<Integer>[] testArray = new Single[10000000];
		Arrays.fill(testArray, Single.never());
		testArray[0] = first;	
		testArray[testArray.length - 1] = second;
		
		int result = Single.ambArray(testArray).blockingGet(); 
		assertEquals(result, 2);
	}
	
	/* 
	 * This class simulates an Iterable that states to have an element,
	 * but for some reason is not able to retrieve it, throwing an exception.
	 */
	private class MaliciousIterable 
			implements Iterable<SingleSource<? extends Object>> {

		@Override
		public Iterator<SingleSource<? extends Object>> iterator() {
			return new Iterator<SingleSource<? extends Object>> () {
				
				/* The iterator believes that a next element exists. */
				@Override
				public boolean hasNext() {
					return true;
				}

				/* The iterator is not able to fetch the next element */
				@Override
				public SingleSource<? extends Object> next() {
					throw new NoSuchElementException();
				}
			};	
		}
	}
	
}
