/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.obs;

import java.io.IOException;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.datastore.obs.DataStreamKey;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBinding;


public interface CustomObsFormat
{
    boolean isCompatible(IDataStreamInfo dsInfo);
    
    
    ResourceBinding<DataStreamKey, IDataStreamInfo> getSchemaBinding(RequestContext ctx, IdEncoders idEncoders, IDataStreamInfo dsInfo) throws IOException;
    
    
    ResourceBinding<BigId, IObsData> getObsBinding(RequestContext ctx, IdEncoders idEncoders, IDataStreamInfo dsInfo) throws IOException;
}
