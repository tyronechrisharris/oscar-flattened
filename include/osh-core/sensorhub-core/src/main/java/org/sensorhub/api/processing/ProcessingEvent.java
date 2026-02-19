/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.processing;

import org.sensorhub.api.system.SystemEvent;


/**
 * <p>
 * Simple base data structure for all events linked to processing modules
 * </p>
 *
 * @author Alex Robin
 * @since Feb 20, 2015
 */
public class ProcessingEvent extends SystemEvent
{
	/**
	 * Possible event types for a ProcessingEvent
	 */
    public enum Type
	{
		PROCESSING_STARTED,
		PROCESSING_PROGRESS,
		PROCESSING_ENDED,
		PARAMS_CHANGED
	};


	/**
	 * Type of process event
	 */
	protected Type type;


	/**
	 * Sole constructor
	 * @param timeStamp unix time of event generation
     * @param processUID Unique ID of originating process
     * @param type type of event
	 */
	public ProcessingEvent(long timeStamp, String processUID, Type type)
	{
	    super(timeStamp, processUID);
	    this.type = type;
	}


    public Type getType()
    {
        return type;
    }
}
