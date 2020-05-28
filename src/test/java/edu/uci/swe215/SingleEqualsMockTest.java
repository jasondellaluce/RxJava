package edu.uci.swe215;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.operators.single.SingleEquals;

@SuppressWarnings("unchecked")
public class SingleEqualsMockTest {

	private SingleSource<String> firstSource;
	private SingleSource<String> secondSource;
	private SingleObserver<Boolean> observer;
	
	@Before
	public void setup() {
		firstSource = mock(SingleSource.class);
		secondSource = mock(SingleSource.class);
		observer = mock(SingleObserver.class);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldInvokeSubscribeOnSource() {		
		SingleEquals<String> single = new SingleEquals<>(firstSource, secondSource);	
		verify(firstSource, times(0)).subscribe(any());
		
		single.subscribe(observer);
		verify(firstSource, times(1)).subscribe(any());
		verify(secondSource, times(1)).subscribe(any());

	}
	
	@Test
	public void shouldInvokeOnNextWithTrueOnObserver() {
		
		/* Mock subscription to first source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onSuccess("Test");
			return null;
		}).when(firstSource).subscribe(any());
		
		/* Mock subscription to second source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onSuccess("Test");
			return null;
		}).when(secondSource).subscribe(any());
		
		/* Capture parameter passed to observer */
		ArgumentCaptor<Boolean> boolCapture = ArgumentCaptor.forClass(Boolean.class);
		doNothing().when(observer).onSuccess(boolCapture.capture());
		
		SingleEquals<String> single = new SingleEquals<>(firstSource, secondSource);	
		single.subscribe(observer);
		
		verify(observer, times(1)).onSuccess(any());
		assertEquals(true, boolCapture.getValue());
	}
	
	@Test
	public void shouldInvokeOnNextWithFalseOnObserver() {
		
		/* Mock subscription to first source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onSuccess("Test1");
			return null;
		}).when(firstSource).subscribe(any());
		
		/* Mock subscription to second source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onSuccess("Test2");
			return null;
		}).when(secondSource).subscribe(any());
		
		/* Capture parameter passed to observer */
		ArgumentCaptor<Boolean> boolCapture = ArgumentCaptor.forClass(Boolean.class);
		doNothing().when(observer).onSuccess(boolCapture.capture());
		
		SingleEquals<String> single = new SingleEquals<>(firstSource, secondSource);	
		single.subscribe(observer);
		
		verify(observer, times(1)).onSuccess(any());
		assertEquals(false, boolCapture.getValue());
	}
	
	@Test
	public void shouldInvokeOnErrorOnObserver() {
		
		/* Mock subscription to first source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onError(new TestException());
			return null;
		}).when(firstSource).subscribe(any());
		
		/* Mock subscription to second source */
		doAnswer(invocation -> {
			SingleObserver<String> observer = (SingleObserver<String>) invocation.getArgument(0);
			observer.onSuccess("Test2");
			return null;
		}).when(secondSource).subscribe(any());
		
		/* Capture parameter passed to observer */
		ArgumentCaptor<Throwable> throwableCapture = ArgumentCaptor.forClass(Throwable.class);
		doNothing().when(observer).onError(throwableCapture.capture());
		
		SingleEquals<String> single = new SingleEquals<>(firstSource, secondSource);	
		single.subscribe(observer);
		
		verify(observer, times(1)).onError(any());
		verify(observer, times(0)).onSuccess(any());
		assertEquals(TestException.class, throwableCapture.getValue().getClass());
	}
	
}
