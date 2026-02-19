/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo.compat.v4;

import org.sensorhub.api.data.DataStreamInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;


/**
 * <p>
 * Custom serializer for older version of DataStreamInfo class.
 * </p>
 *
 * @author Alex Robin
 * @since Jul 8, 2024
 */
public class DataStreamInfoSerializerV4 extends FieldSerializer<DataStreamInfo>
{
    
    public DataStreamInfoSerializerV4(Kryo kryo)
    {
        super(kryo, DataStreamInfo.class);
    }
    
    
    @Override
    protected void initializeCachedFields()
    {
        // remove class attributes missing from older version of the class
        this.removeField("phenomenonTimeInterval");
        this.removeField("resultTimeInterval");
        this.removeField("procedureID");
        this.removeField("deploymentID");
        this.removeField("featureOfInterestID");
        this.removeField("samplingFeatureID");
    }

}
