/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.common;

import org.sensorhub.api.common.IdEncoder;
import org.sensorhub.api.common.IdEncoders;


/**
 * <p>
 * Helper class providing ID encoders for all resources available
 * on the sensor hub. This class provides the same {@link IdEncoder32} for
 * all resource types.
 * </p>
 *
 * @author Alex Robin
 * @since Oct 20, 2024
 */
public class IdEncodersBase32 implements IdEncoders
{
    final IdEncoder idEncoder;

    
    public IdEncodersBase32()
    {
        this.idEncoder = new IdEncoderBase32();
    }


    @Override
    public IdEncoder getFeatureIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getProcedureIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getSystemIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getDeploymentIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getFoiIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getDataStreamIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getObsIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getCommandStreamIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getCommandIdEncoder()
    {
        return idEncoder;
    }


    @Override
    public IdEncoder getPropertyIdEncoder()
    {
        return idEncoder;
    }
}
