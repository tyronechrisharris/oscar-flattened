/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import org.sensorhub.impl.service.swe.RecordTemplate;
import org.vast.ows.sos.GetResultTemplateRequest;


/**
 * <p>
 * Interface for all SOS result template serializers
 * </p>
 *
 * @author Alex Robin
 * @date Nov 30, 2020
 */
public interface ISOSAsyncResultTemplateSerializer extends IAsyncResponseSerializer<GetResultTemplateRequest, RecordTemplate>
{
        
}
