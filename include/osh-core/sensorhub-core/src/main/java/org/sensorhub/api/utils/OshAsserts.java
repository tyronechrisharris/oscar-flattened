/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.utils;

import java.net.URI;
import org.sensorhub.api.common.BigId;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.IProcedure;
import org.vast.util.Asserts;


public class OshAsserts
{
    static final int UID_MIN_CHARS = 8; 
    
    
    public static String checkValidUID(String uid)
    {
        return checkValidUID(uid, "UniqueID");
    }
    
    
    public static String checkValidUID(String uid, String idName)
    {
        Asserts.checkNotNull(uid, idName + " cannot be null");
        Asserts.checkArgument(uid.length() >= UID_MIN_CHARS, "{} must be at least {} characters long", idName, UID_MIN_CHARS);
        return uid;
    }
    
    
    public static long checkValidInternalID(long id)
    {
        return checkValidInternalID(id, "Internal");
    }
    
    
    public static long checkValidInternalID(long id, String idName)
    {
        Asserts.checkArgument(id > 0, "{} ID must be > 0", idName);
        return id;
    }
    
    
    public static BigId checkValidInternalID(BigId id)
    {
        return checkValidInternalID(id, "Internal");
    }
    
    
    public static BigId checkValidInternalID(BigId id, String idName)
    {
        Asserts.checkNotNull(id, BigId.class);
        //Asserts.checkArgument(id != BigId.NONE, "{} ID must not be NONE", idName);
        return id;
    } 
    
    
    public static String checkValidURI(String uri)
    {
        Asserts.checkNotNull(uri, "URI cannot be null");
        URI.create(uri);
        return uri;
    }
    
    
    public static String checkFeatureObject(IFeature f)
    {
        Asserts.checkNotNull(f, IFeature.class);
        return checkValidUID(f.getUniqueIdentifier(), "Feature UID");
    }
    
    
    public static String checkSystemObject(IProcedure f)
    {
        Asserts.checkNotNull(f, IProcedure.class);
        return checkValidUID(f.getUniqueIdentifier(), "System UID");
    }
}
