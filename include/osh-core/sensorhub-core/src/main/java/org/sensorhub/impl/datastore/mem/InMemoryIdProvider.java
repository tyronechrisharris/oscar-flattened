/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are copyright (C) 2018, Sensia Software LLC
 All Rights Reserved. This software is the property of Sensia Software LLC.
 It cannot be duplicated, used, or distributed without the express written
 consent of Sensia Software LLC.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.mem;

import java.util.concurrent.atomic.AtomicLong;
import org.sensorhub.api.datastore.IdProvider;


/**
 * <p>
 * A simple ID provider that just increments a local counter. This is only
 * suited for in-memory store and there is no guarantee that IDs will
 * be regenerated identically after a restart
 * </p>
 * 
 * @param <T> Type of object to generate IDs for
 *
 * @author Alex Robin
 * @date Oct 8, 2018
 */
public class InMemoryIdProvider<T> implements IdProvider<T>
{
    AtomicLong nextId;
    
    
    public InMemoryIdProvider(long startFrom)
    {
        this.nextId = new AtomicLong(startFrom);
    }
    
    
    @Override
    public long newInternalID(T obj)
    {
        return nextId.getAndIncrement();
    }

}
