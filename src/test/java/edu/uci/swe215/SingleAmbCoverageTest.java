package edu.uci.swe215;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.Test;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.operators.single.SingleAmb;

@SuppressWarnings("unchecked")
public class SingleAmbCoverageTest {

	@Test
	public void shouldErrorOnNullValueInList() {
		List<Single<Integer>> sourceList = Arrays.asList(Single.just(1), null);
		SingleAmb<Integer> singleAmb = new SingleAmb<>(null, sourceList);
		singleAmb.test().assertError(NullPointerException.class);	
	}
	
	@Test
	public void shouldErrorOnNullValueInArray() {
		SingleSource<Integer>[] sourceArray = new SingleSource[2];
		sourceArray[0] = null;
		sourceArray[1] = null;
		SingleAmb<Integer> singleAmb = new SingleAmb<Integer>(sourceArray, null);
		singleAmb.test().assertError(NullPointerException.class);	
	}
	
	@Test
	public void shouldPropagateOnError() {
		List<Single<Integer>> sourceList = Arrays.asList(Single.error(new TestException()));
		SingleAmb<Integer> singleAmb = new SingleAmb<>(null, sourceList);
		singleAmb.test().assertError(TestException.class);
	}
	
	@Test
	public void shouldExpandArrayOnBiggerSize() {
		List<Single<Integer>> sourceList = Stream
				.generate(() -> Single.just(1))
				.limit(10)
				.collect(Collectors.toList());
		SingleAmb<Integer> singleAmb = new SingleAmb<>(null, sourceList);
		singleAmb.test().assertComplete();
	}
	
}
