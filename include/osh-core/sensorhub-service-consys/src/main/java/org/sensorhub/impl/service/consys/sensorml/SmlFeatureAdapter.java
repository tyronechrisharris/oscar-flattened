/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.sensorml;

import java.util.Map;
import javax.xml.namespace.QName;
import org.sensorhub.api.feature.ISmlFeature;
import org.vast.ogc.gml.IFeature;
import org.vast.util.TimeExtent;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.sensorml.v20.DescribedObject;


/**
 * <p>
 * Helper class to adapt a regular IFeature to the ISystemWithDesc interface
 * </p>
 * 
 * @param <T> Type of SensorML object
 *
 * @author Alex Robin
 * @since Jan 7, 2021
 */
public abstract class SmlFeatureAdapter<T extends DescribedObject> implements ISmlFeature<T>
{
    IFeature delegate;
    

    public SmlFeatureAdapter(IFeature f)
    {
        this.delegate = f;
    }
    
    
    public String getId()
    {
        return delegate.getId();
    }
    

    public String getUniqueIdentifier()
    {
        return delegate.getUniqueIdentifier();
    }
    
    
    @Override
    public String getType()
    {
        var sml = getFullDescription();
        return sml != null ? sml.getType() : delegate.getType();
    }
    

    public String getName()
    {
        return delegate.getName();
    }
    

    public Map<QName, Object> getProperties()
    {
        return delegate.getProperties();
    }
    

    public String getDescription()
    {
        return delegate.getDescription();
    }
    

    @Override
    public TimeExtent getValidTime()
    {
        return delegate.getValidTime();
    }


    @Override
    public AbstractGeometry getGeometry()
    {
        var sml = getFullDescription();
        return sml != null ? sml.getGeometry() : delegate.getGeometry();
    }
    

    @Override
    public abstract T getFullDescription();
    
}
