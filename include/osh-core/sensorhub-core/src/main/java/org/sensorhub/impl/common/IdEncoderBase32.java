/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.common;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoder;


/**
 * <p>
 * Helper class to manage resource identifiers. We use this class to convert
 * from public facing resource IDs exposed via the API as Strings to internal
 * OSH IDs (BigId instances).
 * </p><p>
 * This implementation simply converts IDs to/from their base 32 string
 * representations.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 20, 2024
 */
public class IdEncoderBase32 implements IdEncoder
{
    
    /**
     * Encodes an ID to its default base 32 string representation
     * @param internalID
     * @return The string representation of the ID
     */
    public String encodeID(BigId internalID)
    {
        return BigId.toString32(internalID);
    }
    
    
    /**
     * Decodes an ID from its default base 32 string representation
     * @param encodedID
     * @return A BigId instance
     * @throws IllegalArgumentException if the String cannot be decoded to a valid ID
     */
    public BigId decodeID(String encodedID)
    {
        return BigId.fromString32(encodedID);
    }
}
