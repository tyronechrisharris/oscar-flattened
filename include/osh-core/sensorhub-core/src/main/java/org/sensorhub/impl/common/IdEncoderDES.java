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

import java.io.StringReader;
import java.io.StringWriter;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoder;
import org.sensorhub.utils.VarInt;
import com.google.common.io.BaseEncoding;


/**
 * <p>
 * Helper class to manage resource identifiers. We use this class to convert
 * from public facing resource IDs exposed via the API as Strings to internal
 * OSH IDs (BigId instances).
 * </p><p>
 * In addition to converting ids to/from String representations, this class
 * can optionally encrypt IDs for added security.
 * We do this because internal IDs are often generated in sequence for
 * performance reason but we don't want to expose them to the outside for
 * security reasons (i.e. if IDs are assigned sequentially, one ID can be used
 * to infer the presence of another resource even if the user doesn't have
 * access to it, which facilitate certain kinds of cyber attacks).
 * </p>
 *
 * @author Alex Robin
 * @date Nov 5, 2018
 */
public class IdEncoderDES implements IdEncoder
{
    static final BaseEncoding BASE32_ENCODING = BaseEncoding.base32Hex().lowerCase().omitPadding();
    final ThreadLocal<Cipher> ecipher;
    final ThreadLocal<Cipher> dcipher;
    
    
    public IdEncoderDES()
    {
        this.ecipher = null;
        this.dcipher = null;
    }
    
    
    public IdEncoderDES(SecretKey key)
    {
        ecipher = ThreadLocal.withInitial(() -> {
            try
            {
                var cipher = Cipher.getInstance("DES");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return cipher;
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Error initializing ID encoder", e);
            }
        });
        
        dcipher = ThreadLocal.withInitial(() -> {
            try
            {
                var cipher = Cipher.getInstance("DES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                return cipher;
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Error initializing ID decoder", e);
            }
        });
    }
    
    
    /**
     * Encodes an ID to its string representation
     * @param internalID
     * @return The string representation of the ID
     */
    public String encodeID(BigId internalID)
    {
        if (ecipher != null)
        {
            try
            {
                // convert BigId to bytes
                var len = VarInt.varIntSize(internalID.getScope()) + internalID.size();
                var buf = new byte[len];
                int off = VarInt.putVarInt(internalID.getScope(), buf, 0);
                System.arraycopy(internalID.getIdAsBytes(), 0, buf, off, internalID.size());
                
                // encrypt
                var enc = ecipher.get().doFinal(buf);
                
                // write as base32
                var writer = new StringWriter();
                var os = BASE32_ENCODING.encodingStream(writer);
                os.write(enc);
                os.close();
                
                return writer.toString();
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Error encoding resource ID", e);
            }
        }
        else
            return BigId.toString32(internalID);
    }
    
    
    /**
     * Decodes an ID from its String representation
     * @param encodedID
     * @return A BigId instance
     * @throws IllegalArgumentException if the String cannot be decoded to a valid ID
     */
    public BigId decodeID(String encodedID)
    {
        if (dcipher != null)
        {
            try
            {
                // decode base32 string
                var reader = new StringReader(encodedID);
                var is = BASE32_ENCODING.decodingStream(reader);
                var enc = is.readAllBytes();
                
                // decypher
                var buf = dcipher.get().doFinal(enc);
                
                // read BigId from bytes
                var scope = VarInt.getVarInt(buf, 0);
                var off = VarInt.varIntSize(scope);
                return BigId.fromBytes(scope, buf, off, buf.length-off);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Error decoding resource ID: " + encodedID, e);
            }
        }
        else
            return BigId.fromString32(encodedID);
    }
}
