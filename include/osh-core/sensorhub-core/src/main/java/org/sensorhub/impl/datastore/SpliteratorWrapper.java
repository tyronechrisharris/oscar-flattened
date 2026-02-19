/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.vast.util.Asserts;


/**
 * <p>
 * Base class for implementing filtering spliterators
 * </p>
 * 
 * @param <T> Element type 
 *
 * @author Alex Robin
 * @date Nov 9, 2020
 */
public abstract class SpliteratorWrapper<T> implements Spliterator<T>
{
    Spliterator<T> delegate;
    

    public SpliteratorWrapper(Spliterator<T> in)
    {
        this.delegate = Asserts.checkNotNull(in, Spliterator.class);
    }
    
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        return delegate.tryAdvance(action);
    }


    @Override
    public Spliterator<T> trySplit()
    {
        return delegate.trySplit();
    }


    @Override
    public long estimateSize()
    {
        return delegate.estimateSize();
    }


    @Override
    public long getExactSizeIfKnown()
    {
        return delegate.getExactSizeIfKnown();
    }


    @Override
    public int characteristics()
    {
        return delegate.characteristics();
    }


    @Override
    public boolean hasCharacteristics(int characteristics)
    {
        return delegate.hasCharacteristics(characteristics);
    }


    @Override
    public Comparator<? super T> getComparator()
    {
        return delegate.getComparator();
    }
}
