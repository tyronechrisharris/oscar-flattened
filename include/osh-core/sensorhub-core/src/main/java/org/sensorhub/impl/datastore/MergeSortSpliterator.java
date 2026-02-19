/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are copyright (C) 2018, Sensia Software LLC
 All Rights Reserved. This software is the property of Sensia Software LLC.
 It cannot be duplicated, used, or distributed without the express written
 consent of Sensia Software LLC.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * <p>
 * Implementation of spliterator used to merge and sort elements obtained
 * from several pre-ordered streams. The source streams must
 * already be ordered according to the specified Comparator.
 * </p>
 * @param <T> 
 * 
 * @author Alex Robin
 * @date Apr 6, 2018
 */
public class MergeSortSpliterator<T> implements Spliterator<T>
{
    Collection<Stream<T>> streams;
    PriorityQueue<StreamSource> sources;
    Comparator<T> comparator;
    boolean needInit;
    
    
    private class StreamSource implements Comparable<StreamSource>
    {
        Spliterator<T> iterator;
        T nextRecord;
        
        StreamSource(Stream<T> stream)
        {
            this.iterator = stream.spliterator();
        }
        
        @Override
        public int compareTo(StreamSource other)
        {
            return comparator.compare(nextRecord, other.nextRecord);
        }       
        
        boolean tryAdvance()
        {
            return iterator.tryAdvance(elt -> nextRecord = elt);
        }
    }


    public MergeSortSpliterator(Collection<Stream<T>> streams, Comparator<T> comparator)
    {
        this.streams = streams;
        this.comparator = comparator;
        this.needInit = true;
    }


    @Override
    public int characteristics()
    {
        return Spliterator.SORTED;
    }


    @Override
    public Comparator<? super T> getComparator()
    {
        return comparator;
    }


    @Override
    public long estimateSize()
    {
        return 0;
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        if (needInit)
            loadFirstElements();
        
        // get next element from queue
        StreamSource nextSource = sources.poll();
        if (nextSource == null)
            return false;
        
        // apply action
        action.accept(nextSource.nextRecord);
        
        // if an element was found, prepare for next iteration by fetching
        // the next element on the corresponding iterator.
        if (nextSource.tryAdvance())
            sources.add(nextSource);
                
        return true;
    }
    
    
    private void loadFirstElements()
    {
        this.sources = new PriorityQueue<>(streams.size());
        
        for (Stream<T> s: streams)
        {
            StreamSource src = new StreamSource(s);
            if (src.tryAdvance())
                sources.add(src);
        }
        
        needInit = false;
    }
    

    @Override
    public Spliterator<T> trySplit()
    {
        return null;
    }
    
    
    public void close()
    {
        for (Stream<T> s: streams)
            s.close();
        streams.clear();
        streams = null;
    }
}