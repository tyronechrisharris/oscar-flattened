/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system.wrapper;

import org.sensorhub.api.system.IDeploymentWithDesc;
import org.vast.util.Asserts;
import org.vast.util.TimeExtent;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.sensorml.v20.Deployment;


/**
 * <p>
 * Wrapper class for deployment instances.
 * </p>
 *
 * @author Alex Robin
 * @date Mar 24, 2024
 */
public class DeploymentWrapper implements IDeploymentWithDesc
{
    Deployment fullDesc;


    public DeploymentWrapper(Deployment fullDesc)
    {
        this.fullDesc = Asserts.checkNotNull(fullDesc, Deployment.class);
    }


    @Override
    public String getId()
    {
        return fullDesc.getId();
    }
    
    
    @Override
    public String getUniqueIdentifier()
    {
        return fullDesc.getUniqueIdentifier();
    }


    @Override
    public String getName()
    {
        return fullDesc.getName();
    }


    @Override
    public String getDescription()
    {
        return fullDesc.getDescription();
    }
    

    @Override
    public String getType()
    {
        return fullDesc.getType();
    }


    @Override
    public TimeExtent getValidTime()
    {
        return fullDesc.getValidTime();
    }


    @Override
    public AbstractGeometry getGeometry()
    {
        return fullDesc.getGeometry();
    }


    @Override
    public Deployment getFullDescription()
    {
        return fullDesc;
    }
}
