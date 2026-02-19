/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.feature;

import java.time.Instant;
import org.sensorhub.api.data.IDataProducer;
import org.sensorhub.api.system.SystemEvent;
import org.sensorhub.api.utils.OshAsserts;
import org.vast.ogc.gml.IFeature;
import org.vast.util.Asserts;


/**
 * <p>
 * Event sent when a new FOI is being targeted by an observing system.
 * It is immutable and carries feature data by reference.
 * </p>
 *
 * @author Alex Robin
 * @since Apr 23, 2015
 */
public class FoiAddedEvent extends SystemEvent
{
    protected IFeature foi;
	protected String foiUID;
	protected Instant startTime;
	//protected Instant stopTime;


    /**
     * Creates a new event with only the feature UID
     * @param timeStamp Time of event generation (unix time in milliseconds, base 1970)
     * @param sysUID Unique ID of producer that generated the event
     * @param foiUID Unique ID of feature of interest
     * @param startTime Time at which observation of the FoI started
     */
	public FoiAddedEvent(long timeStamp, String sysUID, String foiUID, Instant startTime)
    {
        super(timeStamp, sysUID);
        this.foiUID = OshAsserts.checkValidUID(foiUID);
        this.startTime = Asserts.checkNotNull(startTime, "startTime");
    }


	/**
	 * Creates a new event with only the feature UID
	 * @param timeStamp Time of event generation (unix time in milliseconds, base 1970)
     * @param producer Producer that generated the event
	 * @param foiUID Unique ID of feature of interest
     * @param startTime Time at which observation of the FoI started
	 */
	public FoiAddedEvent(long timeStamp, IDataProducer producer, String foiUID, Instant startTime)
	{
	    this(timeStamp,
	        producer.getUniqueIdentifier(),
	        foiUID,
	        startTime);
        this.source = Asserts.checkNotNull(producer, IDataProducer.class);
	}


	/**
     * Creates a new event with an attached feature object
     * @param timeStamp time of event generation (unix time in milliseconds, base 1970)
     * @param producer producer that generated the event
     * @param foi feature object
	 * @param startTime time at which observation of the FoI started
     */
	public FoiAddedEvent(long timeStamp, IDataProducer producer, IFeature foi, Instant startTime)
    {
	    this(timeStamp,
	        producer.getUniqueIdentifier(),
	        foi.getUniqueIdentifier(),
	        startTime);
        this.source = Asserts.checkNotNull(producer, IDataProducer.class);
        this.foi = foi;
    }


    /**
     * @return The unique ID of the feature of interest related to this event
     */
    public String getFoiUID()
    {
        return foiUID;
    }
    

	/**
     * @return The description of the feature of interest related to this event
     * or null if the description was already registered by some other means.
     * (in this case, only the UID is provided in the event)
     */
	public IFeature getFoi()
    {
        return foi;
    }


	/**
     * @return The time at which the feature of interest started being observed.
     */
    public Instant getStartTime()
    {
        return startTime;
    }


    /*
     * @return The time at which the feature of interest stopped being observed.
    public Instant getStopTime()
    {
        return stopTime;
    }*/

}
