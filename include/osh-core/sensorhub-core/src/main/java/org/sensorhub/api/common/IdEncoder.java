/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.common;


/**
 * <p>
 * Interface for all ID encoders/decoders that can encode/decode BigId
 * instances to/from String representations.
 * </p>
 *
 * @author Alex Robin
 * @since Jun 25, 2022
 */
public interface IdEncoder
{

    public String encodeID(BigId internalID);
    
    
    public BigId decodeID(String encodedID);
}
