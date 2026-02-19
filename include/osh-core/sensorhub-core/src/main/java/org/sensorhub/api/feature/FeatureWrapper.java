/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.feature;

import java.util.Map;
import javax.xml.namespace.QName;
import org.vast.ogc.gml.IFeature;
import org.vast.util.TimeExtent;
import net.opengis.gml.v32.AbstractGeometry;


/**
 * <p>
 * Utility class for wrapping a feature and overriding some of its behavior
 * </p>
 *
 * @author Alex Robin
 * @since Oct 2, 2021
 */
public class FeatureWrapper implements IFeature
{
    protected IFeature f;


    public FeatureWrapper(IFeature delegate)
    {
        this.f = delegate;
    }


    @Override
    public String getId()
    {
        return f.getId();
    }


    public String getUniqueIdentifier()
    {
        return f.getUniqueIdentifier();
    }


    public String getName()
    {
        return f.getName();
    }


    public String getDescription()
    {
        return f.getDescription();
    }


    public Map<QName, Object> getProperties()
    {
        return f.getProperties();
    }


    public AbstractGeometry getGeometry()
    {
        return f.getGeometry();
    }


    @Override
    public TimeExtent getValidTime()
    {
        return f.getValidTime();
    }


    @Override
    public String getType()
    {
        return f.getType();
    }


    @Override
    public boolean hasCustomGeomProperty()
    {
        return f.hasCustomGeomProperty();
    }


    @Override
    public boolean hasCustomTimeProperty()
    {
        return f.hasCustomTimeProperty();
    }
}
