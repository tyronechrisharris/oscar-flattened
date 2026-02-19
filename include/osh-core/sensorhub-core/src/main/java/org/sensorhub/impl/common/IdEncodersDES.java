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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.common.IdEncoder;
import org.sensorhub.api.common.IdEncoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Helper class providing ID encoders for all resources available
 * on the sensor hub.
 * </p>
 *
 * @author Alex Robin
 * @since Jun 25, 2022
 */
public class IdEncodersDES implements IdEncoders
{
    static final Logger log = LoggerFactory.getLogger(IdEncodersDES.class);
    static final String ID_KEYS_FILE = "idKeys.txt";
    
    static final String FEATURE_ID_KEY = "featIdKey";
    static final String PROC_ID_KEY = "procIdKey";
    static final String SYS_ID_KEY = "sysIdKey";
    static final String DEPL_ID_KEY = "deplIdKey";
    static final String FOI_ID_KEY = "foiIdKey";
    static final String DS_ID_KEY = "dsIdKey";
    static final String OBS_ID_KEY = "obsIdKey";
    static final String CS_ID_KEY = "csIdKey";
    static final String CMD_ID_KEY = "cmdIdKey";
    static final String PROP_ID_KEY = "propIdKey";
    
    final IdEncoder featureIdEncoder;
    final IdEncoder procIdEncoder;
    final IdEncoder sysIdEncoder;
    final IdEncoder deplIdEncoder;
    final IdEncoder foiIdEncoder;
    final IdEncoder dsIdEncoder;
    final IdEncoder obsIdEncoder;
    final IdEncoder csIdEncoder;
    final IdEncoder cmdIdEncoder;
    final IdEncoder propIdEncoder;
    
    
    public IdEncodersDES(ISensorHub hub)
    {
        var sm = hub.getModuleRegistry().getCoreStateManager();
        var idKeysFile = sm != null ? sm.getDataFile(ID_KEYS_FILE) : null;
        var idKeysProps = new Properties();
        
        // load existing keys if any
        if (idKeysFile != null && idKeysFile.exists())
        {
            try (var is = new FileInputStream(idKeysFile))
            {
                idKeysProps.load(is);
            }
            catch (IOException e)
            {
                log.error("Could not read ID keys file", e);
            }
        }
        
        // create ID encoders
        int numSavedKeys = idKeysProps.size();
        try
        {
            var prng = new SecureRandom();
            this.featureIdEncoder = createEncoder(FEATURE_ID_KEY, idKeysProps, prng);
            this.procIdEncoder = createEncoder(PROC_ID_KEY, idKeysProps, prng);
            this.sysIdEncoder = createEncoder(SYS_ID_KEY, idKeysProps, prng);
            this.deplIdEncoder = createEncoder(DEPL_ID_KEY, idKeysProps, prng);
            this.foiIdEncoder = createEncoder(FOI_ID_KEY, idKeysProps, prng);
            this.dsIdEncoder = createEncoder(DS_ID_KEY, idKeysProps, prng);
            this.obsIdEncoder = createEncoder(OBS_ID_KEY, idKeysProps, prng);
            this.csIdEncoder = createEncoder(CS_ID_KEY, idKeysProps, prng);
            this.cmdIdEncoder = createEncoder(CMD_ID_KEY, idKeysProps, prng);
            this.propIdEncoder = createEncoder(PROP_ID_KEY, idKeysProps, prng);
        }
        catch (GeneralSecurityException e)
        {
            throw new IllegalStateException("Error generating ID encoder keys", e);
        }
        
        // save generated keys (if any)
        var needSave = idKeysProps.size() != numSavedKeys;
        if (idKeysFile != null && needSave)
        {
            try (var os = new FileOutputStream(idKeysFile))
            {
                idKeysProps.store(os, null);
            }
            catch (IOException e)
            {
                log.error("Could not save ID keys file", e);
            }
        }
    }
    
    
    IdEncoderDES createEncoder(String keyName, Properties idKeysProps, Random prng) throws GeneralSecurityException
    {
        // get key or generate new one
        var key = (String)idKeysProps.computeIfAbsent(keyName, k -> {
            // generate random 56 bytes key
            var keyBytes = new byte[56];
            prng.nextBytes(keyBytes);
            return Base64.getEncoder().encodeToString(keyBytes);
        });
        
        // create DESKey and ID encoder from key bytes
        var keyBytes = Base64.getDecoder().decode(key);
        DESKeySpec dks = new DESKeySpec(keyBytes);
        var skf = SecretKeyFactory.getInstance("DES");
        return new IdEncoderDES(skf.generateSecret(dks));
    }
    
    
    public IdEncoder getFeatureIdEncoder()
    {
        return featureIdEncoder;
    }
    
    
    public IdEncoder getProcedureIdEncoder()
    {
        return procIdEncoder;
    }
    
    
    public IdEncoder getSystemIdEncoder()
    {
        return sysIdEncoder;
    }


    @Override
    public IdEncoder getDeploymentIdEncoder()
    {
        return deplIdEncoder;
    }
    
    
    public IdEncoder getFoiIdEncoder()
    {
        return foiIdEncoder;
    }
    
    
    public IdEncoder getDataStreamIdEncoder()
    {
        return dsIdEncoder;
    }
    
    
    public IdEncoder getObsIdEncoder()
    {
        return foiIdEncoder;
    }
    
    
    public IdEncoder getCommandStreamIdEncoder()
    {
        return csIdEncoder;
    }
    
    
    public IdEncoder getCommandIdEncoder()
    {
        return cmdIdEncoder;
    }


    @Override
    public IdEncoder getPropertyIdEncoder()
    {
        return propIdEncoder;
    }
}
