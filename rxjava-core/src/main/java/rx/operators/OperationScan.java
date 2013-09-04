/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.operators;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func2;

/**
 * Returns an Observable that applies a function to the first item emitted by a source Observable,
 * then feeds the result of that function along with the second item emitted by an Observable into
 * the same function, and so on until all items have been emitted by the source Observable,
 * emitting the result of each of these iterations.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/scan.png">
 * <p>
 * This sort of function is sometimes called an accumulator.
 * <p>
 * Note that when you pass a seed to <code>scan()</code> the resulting Observable will emit that
 * seed as its first emitted item.
 */
public final class OperationScan {
    /**
     * Applies an accumulator function over an observable sequence and returns each intermediate result with the specified source and accumulator.
     * 
     * @param sequence
     *            An observable sequence of elements to project.
     * @param initialValue
     *            The initial (seed) accumulator value.
     * @param accumulator
     *            An accumulator function to be invoked on each element from the sequence.
     * 
     * @return An observable sequence whose elements are the result of accumulating the output from the list of Observables.
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212007%28v=vs.103%29.aspx">Observable.Scan(TSource, TAccumulate) Method (IObservable(TSource), TAccumulate, Func(TAccumulate, TSource, TAccumulate))</a>
     */
    public static <T, R> OnSubscribeFunc<R> scan(Observable<? extends T> sequence, R initialValue, Func2<? super R, ? super T, ? extends R> accumulator) {
        return new Accumulator<T, R>(sequence, initialValue, accumulator);
    }

    /**
     * Applies an accumulator function over an observable sequence and returns each intermediate result with the specified source and accumulator.
     * 
     * @param sequence
     *            An observable sequence of elements to project.
     * @param accumulator
     *            An accumulator function to be invoked on each element from the sequence.
     * 
     * @return An observable sequence whose elements are the result of accumulating the output from the list of Observables.
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211665(v=vs.103).aspx">Observable.Scan(TSource) Method (IObservable(TSource), Func(TSource, TSource, TSource))</a>
     */
    public static <T> OnSubscribeFunc<T> scan(Observable<? extends T> sequence, Func2<? super T, ? super T, ? extends T> accumulator) {
        return new AccuWithoutInitialValue<T>(sequence, accumulator);
    }

    private static class AccuWithoutInitialValue<T> implements OnSubscribeFunc<T> {
        private final Observable<? extends T> sequence;
        private final Func2<? super T, ? super T, ? extends T> accumulatorFunction;
        
        private AccumulatingObserver<T, T> accumulatingObserver;
        
        private AccuWithoutInitialValue(Observable<? extends T> sequence, Func2<? super T, ? super T, ? extends T> accumulator) {
            this.sequence = sequence;
            this.accumulatorFunction = accumulator;
        }
        
        @Override
        public Subscription call(final Observer<? super T> observer) {
            return sequence.subscribe(new Observer<T>() {
                
                // has to be synchronized so that the initial value is always sent only once.
                @Override
                public synchronized void onNext(T value) {
                    if (accumulatingObserver == null) {
                        observer.onNext(value);
                        accumulatingObserver = new AccumulatingObserver<T, T>(observer, value, accumulatorFunction);
                    } else {
                        accumulatingObserver.onNext(value);
                    }
                }
                
                @Override
                public void onError(Throwable e) {
                    observer.onError(e);
                }

                @Override
                public void onCompleted() {
                    observer.onCompleted();
                }
            });
        }
    }
    
    private static class Accumulator<T, R> implements OnSubscribeFunc<R> {
        private final Observable<? extends T> sequence;
        private final R initialValue;
        private final Func2<? super R, ? super T, ? extends R> accumulatorFunction;

        private Accumulator(Observable<? extends T> sequence, R initialValue, Func2<? super R, ? super T, ? extends R> accumulator) {
            this.sequence = sequence;
            this.initialValue = initialValue;
            this.accumulatorFunction = accumulator;
        }

        @Override
        public Subscription call(final Observer<? super R> observer) {
            observer.onNext(initialValue);
            return sequence.subscribe(new AccumulatingObserver<T, R>(observer, initialValue, accumulatorFunction));
        }
    }

    private static class AccumulatingObserver<T, R> implements Observer<T> {
        private final Observer<? super R> observer;
        private final Func2<? super R, ? super T, ? extends R> accumulatorFunction;

        private R acc;

        private AccumulatingObserver(Observer<? super R> observer, R initialValue, Func2<? super R, ? super T, ? extends R> accumulator) {
            this.observer = observer;
            this.accumulatorFunction = accumulator;
            
            this.acc = initialValue;
        }

        /**
         * We must synchronize this because we can't allow
         * multiple threads to execute the 'accumulatorFunction' at the same time because
         * the accumulator code very often will be doing mutation of the 'acc' object such as a non-threadsafe HashMap
         * 
         * Because it's synchronized it's using non-atomic variables since everything in this method is single-threaded
         */
        @Override
        public synchronized void onNext(T value) {
            try {
                acc = accumulatorFunction.call(acc, value);
                observer.onNext(acc);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }
        
        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }
        
        @Override
        public void onCompleted() {
            observer.onCompleted();
        }
    }
    
    public static class UnitTest {

        @Before
        public void before() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void testScanIntegersWithInitialValue() {
            @SuppressWarnings("unchecked")
            Observer<String> observer = mock(Observer.class);

            Observable<Integer> observable = Observable.from(1, 2, 3);

            Observable<String> m = Observable.create(scan(observable, "", new Func2<String, Integer, String>() {

                @Override
                public String call(String s, Integer n) {
                    return s + n.toString();
                }

            }));
            m.subscribe(observer);

            verify(observer, never()).onError(any(Throwable.class));
            verify(observer, times(1)).onNext("");
            verify(observer, times(1)).onNext("1");
            verify(observer, times(1)).onNext("12");
            verify(observer, times(1)).onNext("123");
            verify(observer, times(4)).onNext(anyString());
            verify(observer, times(1)).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
        }

        @Test
        public void testScanIntegersWithoutInitialValue() {
            @SuppressWarnings("unchecked")
            Observer<Integer> Observer = mock(Observer.class);

            Observable<Integer> observable = Observable.from(1, 2, 3);

            Observable<Integer> m = Observable.create(scan(observable, new Func2<Integer, Integer, Integer>() {

                @Override
                public Integer call(Integer t1, Integer t2) {
                    return t1 + t2;
                }

            }));
            m.subscribe(Observer);

            verify(Observer, never()).onError(any(Throwable.class));
            verify(Observer, never()).onNext(0);
            verify(Observer, times(1)).onNext(1);
            verify(Observer, times(1)).onNext(3);
            verify(Observer, times(1)).onNext(6);
            verify(Observer, times(3)).onNext(anyInt());
            verify(Observer, times(1)).onCompleted();
            verify(Observer, never()).onError(any(Throwable.class));
        }

        @Test
        public void testScanIntegersWithoutInitialValueAndOnlyOneValue() {
            @SuppressWarnings("unchecked")
            Observer<Integer> Observer = mock(Observer.class);

            Observable<Integer> observable = Observable.from(1);

            Observable<Integer> m = Observable.create(scan(observable, new Func2<Integer, Integer, Integer>() {

                @Override
                public Integer call(Integer t1, Integer t2) {
                    return t1 + t2;
                }

            }));
            m.subscribe(Observer);

            verify(Observer, never()).onError(any(Throwable.class));
            verify(Observer, never()).onNext(0);
            verify(Observer, times(1)).onNext(1);
            verify(Observer, times(1)).onNext(anyInt());
            verify(Observer, times(1)).onCompleted();
            verify(Observer, never()).onError(any(Throwable.class));
        }
    }

}
