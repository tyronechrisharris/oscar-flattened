/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.event;

import org.sensorhub.utils.ObjectUtils;

/**
 * <p>
 * Immutable base class for all sensor hub events.
 * All sub-classes should remain immutable.
 * </p>
 *
 * @author Alex Robin
 * @since Nov 5, 2010
 */
public abstract class Event
{
    protected long timeStamp;
    protected transient Object source;
        
    
    /**
     * @return Time stamp of event creation (Unix time in ms since 1970)
     */
    public long getTimeStamp()
    {
        return timeStamp;
    }
    
    
    /**
     * Gets the ID of the source of event.
     * @return ID of the event source
     */
    public abstract String getSourceID();
    
    
    /**
     * Gets the source of the event as an object reference.
     * <p><i>Note that this is not guaranteed to be available when using
     * events in a distributed system.</i></p>
     * @return Source object that generated this event or null
     */
    public Object getSource()
    {
        return source;
    }
    
    
    public String toString()
    {
        return ObjectUtils.toString(this, false);
    }
}
