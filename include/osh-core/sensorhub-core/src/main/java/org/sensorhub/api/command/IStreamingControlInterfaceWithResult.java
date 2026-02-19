/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.command;

import org.vast.data.TextEncodingImpl;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;


public interface IStreamingControlInterfaceWithResult extends IStreamingControlInterface
{
    
    /**
     * Retrieves description of command result data
     * @return Data component containing result structure
     */
    public DataComponent getResultDescription();
    
    
    /**
     * Retrieves encoding of command result data
     * @return Data Recommended encoding of result structure
     */
    public default DataEncoding getResultEncoding()
    {
        return new TextEncodingImpl();
    }
    
}
