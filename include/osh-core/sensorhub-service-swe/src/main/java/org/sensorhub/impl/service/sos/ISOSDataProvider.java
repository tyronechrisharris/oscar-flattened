/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Initial Developer of the Original Code is SENSIA SOFTWARE LLC.
 Portions created by the Initial Developer are Copyright (C) 2012
 the Initial Developer. All Rights Reserved.

 Please Contact Alexandre Robin for more
 information.
 
 Contributor(s): 
    Alexandre Robin
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import java.io.IOException;
import org.vast.ogc.om.IObservation;


/**
 * <p>
 * Interface to be implemented for providing data to the SOS engine.
 * One data provider is mapped for each SOS offering.
 * </p>
 *
 * @author Alex Robin
 * @date Nov 18, 2012
 * */
public interface ISOSDataProvider
{  
    
    /**
     * Requests provider to generate the next observation from the
     * underlying data, given the current config and filter
     * @return observation instance
     * @throws IOException
     */
    public IObservation getNextObservation() throws IOException;
    
    
    /**
     * Requests provider to generate the next CDM record from the
     * underlying data, given the current config and filter
     * @return data block
     * @throws IOException
     */
    public DataBlock getNextResultRecord() throws IOException;
    
    
    /**
     * Requests provider to provide the result structure corresponding
     * to the current config and filter
     * @return data component
     * @throws IOException 
     */
    public DataComponent getResultStructure() throws IOException;
    
    
    /**
     * Requests provider to specify the preferred encoding for the
     * underlying data, given the current config and filter
     * @return encoding instance
     * @throws IOException
     */
    public DataEncoding getDefaultResultEncoding() throws IOException;
    
    
    /**
     * @return true if this provider aggregates data from several producers
     */
    public boolean hasMultipleProducers();
    
    
    /**
     * @return Prefix to use for generating full FOI URIs
     */
    public String getProducerIDPrefix();
    
    
    /**
     * @return ID of the producer associated to the result
     * record returned by the last call to getNextResultRecord()
     */
    public String getNextProducerID();
    
    
    /**
     * Properly releases all resources accessed by provider
     * (for instance, when connection is ended by client)
     */
    public void close();
}
