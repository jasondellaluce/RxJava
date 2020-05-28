package edu.uci.swe215;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.reactivex.rxjava3.internal.operators.single.SingleCache;
import io.reactivex.rxjava3.internal.operators.single.SingleCache.CacheDisposable;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SingleCacheTestableDesignTest {

	@Test
	public void shouldModifyArrayOnAdd() {
		SingleCache<?> singleCache = new SingleCache<>(null);
		assertEquals(singleCache.getObservers().length, 0);
		
		singleCache.add(new CacheDisposable(null, null));
		assertEquals(singleCache.getObservers().length, 1);
	}
	
	@Test
	public void shouldModifyArrayOnRemove() {
		SingleCache<?> singleCache = new SingleCache<>(null);
		assertEquals(singleCache.getObservers().length, 0);
		
		CacheDisposable o = new CacheDisposable(null, null);
		singleCache.add(o);
		assertEquals(singleCache.getObservers().length, 1);
		singleCache.remove(o);
		assertEquals(singleCache.getObservers().length, 0);
		
	}
	
	@Test
	public void shouldBeConsistentOnConcurrentAdd() throws InterruptedException {
		int numberOfThreads = 10;
		SingleCache<?> singleCache = new SingleCache<>(null);
		Thread[] threads = new Thread[10];
		for(int i = 0; i < numberOfThreads; i++) {
			threads[i] = new Thread(() -> {
				singleCache.add(new CacheDisposable(null, null));
			});
			threads[i].start();
		}
		for(int i = 0; i < numberOfThreads; i++) {
			threads[i].join();
		}
		
		assertEquals(singleCache.getObservers().length, numberOfThreads);
	}
	
}
