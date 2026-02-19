/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;


/**
 * <p>
 * Interface for asynchronous streams.
 * </p>
 * <p>
 * This adds asynchronous methods for all terminal operations returning 
 * completable futures. When these methods are called the stream pipeline
 * is run asynchronously as data is pushed to the stream.
 * <p>
 * <i>Default implementations of these new methods simply execute their
 * synchronous counterparts and return the result wrapped in a completed
 * future.</i>
 * </p>
 * 
 * The implementation works by separating the terminal operation from
 * the rest of the stream. The non-terminal part of the stream is then
 * surrounded by 2 (spl)iterators that are controlled by the thread supplying
 * the new data. The terminal operation is also executed by the supplier
 * thread everytime an element comes out of the non-terminal part of the
 * stream.
 *
 * @author Alex Robin
 * @param <T> the type of the stream elements
 * @date Sep 10, 2019
 */
public interface AsyncStream<T> extends Stream<T>
{
    
    public default CompletableFuture<Boolean> allMatchAsync(Predicate<? super T> predicate)
    {
        return CompletableFuture.completedFuture(allMatch(predicate));
    }
    
    
    public default CompletableFuture<Boolean> anyMatchAsync(Predicate<? super T> predicate)
    {
        return CompletableFuture.completedFuture(anyMatch(predicate));
    }
    
    
    public default <R,A> CompletableFuture<R> collectAsync(Collector<? super T, A, R> collector)
    {
        return CompletableFuture.completedFuture(collect(collector));
    }
    
    
    public default <R> CompletableFuture<R> collectAsync(Supplier<R> supplier, BiConsumer<R,? super T> accumulator, BiConsumer<R,R> combiner)
    {
        return CompletableFuture.completedFuture(collect(supplier, accumulator, combiner));
    }
    
    
    public default CompletableFuture<Long> countAsync()
    {
        return CompletableFuture.completedFuture(count());
    }
    
    
    public default CompletableFuture<Optional<T>> findAnyAsync()
    {
        return CompletableFuture.completedFuture(findAny());
    }
    
    
    public default CompletableFuture<Optional<T>> findFirstAsync()
    {
        return CompletableFuture.completedFuture(findFirst());
    }
    
    
    public default CompletableFuture<Void> forEachAsync(Consumer<? super T> action)
    {
        forEach(action);
        return CompletableFuture.completedFuture(null);
    }
    
    
    public default CompletableFuture<Void> forEachOrderedAsync(Consumer<? super T> action)
    {
        forEachOrdered(action);
        return CompletableFuture.completedFuture(null);
    }
    
    
    public default CompletableFuture<Optional<T>> maxAsync(Comparator<? super T> comparator)
    {
        return CompletableFuture.completedFuture(max(comparator));
    }
    
    
    public default CompletableFuture<Optional<T>> minAsync(Comparator<? super T> comparator)
    {
        return CompletableFuture.completedFuture(min(comparator));
    }
    
    
    public default CompletableFuture<Optional<T>> reduceAsync(BinaryOperator<T> accumulator)
    {
        return CompletableFuture.completedFuture(reduce(accumulator));
    }
    
    
    public default CompletableFuture<T> reduceAsync(T identity, BinaryOperator<T> accumulator)
    {
        return CompletableFuture.completedFuture(reduce(identity, accumulator));
    }
    
    
    public default <U> CompletableFuture<U> reduceAsync(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner)
    {
        return CompletableFuture.completedFuture(reduce(identity, accumulator, combiner));
    }


    @Override
    public AsyncStream<T> parallel();


    @Override
    public AsyncStream<T> sequential();


    @Override
    public AsyncStream<T> unordered();


    @Override
    public AsyncStream<T> distinct();


    @Override
    public AsyncStream<T> filter(Predicate<? super T> predicate);


    @Override
    public <R> AsyncStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);


    @Override
    public AsyncStream<T> limit(long maxSize);


    @Override
    public <R> AsyncStream<R> map(Function<? super T, ? extends R> mapper);


    @Override
    public AsyncStream<T> peek(Consumer<? super T> action);


    @Override
    public AsyncStream<T> skip(long n);


    @Override
    public AsyncStream<T> sorted();


    @Override
    public AsyncStream<T> sorted(Comparator<? super T> comparator);
    
}
