/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.swe;

import org.vast.ows.OWSException;
import org.vast.ows.OWSExceptionReport;
import net.opengis.sensorml.v20.AbstractProcess;


public class TransactionUtils
{
    private static final String INVALID_SML_MSG = "Invalid SensorML description: ";
    
    
    /*
     * Checks that SensorML description contains proper info
     */
    public static void checkSensorML(AbstractProcess smlProcess, OWSExceptionReport report) throws OWSException
    {
        String sensorUID = smlProcess.getUniqueIdentifier();
        
        if (sensorUID == null || sensorUID.length() < 10)
            report.add(new OWSException(OWSException.invalid_param_code, "procedureDescription", sensorUID, INVALID_SML_MSG + "Procedure unique ID is missing or too short"));
        
        if (smlProcess.getName() == null || smlProcess.getName().length() < 4)
            report.add(new OWSException(OWSException.invalid_param_code, "procedureDescription", sensorUID, INVALID_SML_MSG + "Procedure name is missing or too short"));
    }
}
