/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * <p>
 * Atomic reference that can be atomically initialized using a Supplier.
 * This can be used to compute a cached value lazily in a get method for
 * example.
 * </p>
 * 
 * @param <T> The type of object referred to by this reference
 *
 * @author Alex Robin
 * @since Apr 16, 2022
 */
public class AtomicInitializer<T> extends AtomicReference<T>
{
    private static final long serialVersionUID = 0L;
    
    
    public T get(Supplier<T> initializer)
    {
        T result = get();
        
        if (result == null) {
            result = initializer.get();
            if (!compareAndSet(null, result)) {
                // another thread has initialized the reference
                result = get();
            }
        }
        
        return result;
    }
}
