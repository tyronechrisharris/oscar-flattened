/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2.kryo;

import org.h2.mvstore.MVMap;
import org.sensorhub.api.ISensorHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;
import com.esotericsoftware.kryo.ClassResolver;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.CuckooObjectMap;
import com.esotericsoftware.kryo.util.IntMap;
import com.esotericsoftware.kryo.util.Util;


/**
 * <p>
 * Extension of Kryo class resolver to persist class <-> id mappings in the
 * database itself.<br/>
 * When this persistent class map is used, the MVStore needs to be committed
 * twice on close to make sure the mappings are also serialized. This is
 * needed because the class mappings are updated during serialization of
 * the main pages using KryoDataType and thus are not always committed to
 * disk at the same time.<br/>
 * This class is NOT threadsafe although the underlying persistent map is. 
 * So a different instance must be attached to each Kryo instance.
 * </p>
 *
 * @author Alex Robin
 * @since Sep 20, 2021
 */
@SuppressWarnings("rawtypes")
public class PersistentClassResolver implements ClassResolver
{
    static Logger log = LoggerFactory.getLogger(PersistentClassResolver.class);
    
    final IntMap<Registration> idToRegistration = new IntMap<>();
    final CuckooObjectMap<Class, Registration> classToRegistration = new CuckooObjectMap<>();
    final MVMap<String, Integer> classNameToIdMap;
    final ClassLoader classLoader;
    Kryo kryo;
    boolean mappingsLoaded;
    
    
    public PersistentClassResolver(MVMap<String, Integer> classNameToIdMap)
    {
        // use sensorhub-core classloader by default
        // this is needed so core classes can be loaded even when booted with OSGi
        this(classNameToIdMap, ISensorHub.class.getClassLoader());
    }
    
    
    public PersistentClassResolver(MVMap<String, Integer> classNameToIdMap, ClassLoader classLoader)
    {
        this.classNameToIdMap = Asserts.checkNotNull(classNameToIdMap, MVMap.class);
        this.classLoader = Asserts.checkNotNull(classLoader, ClassLoader.class);
    }
    
    
    public void loadMappings()
    {
        //if (mappingsLoaded)
        //    return;
        
        // preload mappings on startup
        var it = classNameToIdMap.entrySet().iterator();
        while (it.hasNext())
        {
            var entry = it.next();
            var className = entry.getKey();
            var classId = entry.getValue();
            
            try
            {
                if (!idToRegistration.containsKey(classId))
                {
                    className = mapClassName(className);
                    register(Class.forName(className, true, classLoader), classId);
                    log.trace("Loading class mapping: {} -> {}", className, classId);
                }
            }
            catch (ClassNotFoundException e)
            {
                log.error("Error loading class mapping for " + className, e);
            }
        }
        
        //mappingsLoaded = true;
    }
    
    
    /*
     * Handle classes that have been renamed or moved to a different package
     */
    protected String mapClassName(String className)
    {
        if (className.startsWith("com.vividsolutions"))
            return className.replace("com.vividsolutions", "org.locationtech");
        
        else if (className.equals("org.sensorhub.api.system.SystemId"))
            return "org.sensorhub.api.feature.FeatureId";
        
        return className;
    }
    
    
    public Registration writeClass(Output output, Class type)
    {
        if (type == null)
        {
            output.writeByte(Kryo.NULL);
            return null;
        }
        
        var reg = kryo.getRegistration(type);
        
        // if first time class is written
        // register mapping and add to persistent map
        int classId;
        if (reg == null || reg.getId() < 0)
        {
            // synchronize since multiple Kryo instance can share this class resolver
            synchronized (classNameToIdMap.getStore())
            {
                var clazz = type;
                classId = classNameToIdMap.computeIfAbsent(type.getName(), name -> {
                    // keep 10 slots for primitive types registered by Kryo
                    var nextId = classNameToIdMap.size()+10;
                    log.trace("Adding class mapping: {} -> {}", clazz, nextId);
                    return nextId;
                });
            }
            
            reg = register(type, classId);
        }
        else
            classId = reg.getId();
        
        // always write integer class ID
        output.writeVarInt(classId+1, true);
        
        return reg;
    }
    
    
    public Registration readClass(Input input)
    {
        // read integer class ID
        int classId = input.readVarInt(true);
        
        if (classId == Kryo.NULL)
            return null;
        
        var reg = idToRegistration.get(classId-1);
        if (reg == null)
        {
            // if class id is unknown, it's probably because mappings
            // were updated so reload them
            // this rescans the entire map but it's ok since it shouldn't happen too often
            loadMappings();
            reg = idToRegistration.get(classId-1);
        }
        
        return Asserts.checkNotNull(reg, Registration.class);
    }
    
    
    protected Registration register(Class type, int classId)
    {
        return register(new Registration(type, kryo.getDefaultSerializer(type), classId));
    }


    @Override
    public Registration register(Registration registration)
    {
        idToRegistration.put(registration.getId(), registration);
        
        classToRegistration.put(registration.getType(), registration);
        Class wrapperClass = Util.getWrapperClass(registration.getType());
        if (wrapperClass != registration.getType()) classToRegistration.put(wrapperClass, registration);
        
        return registration;
    }


    @Override
    public Registration unregister(int classID)
    {
        return null;
    }


    @Override
    public Registration registerImplicit(Class type)
    {
        return register(type, -1);
    }


    @Override
    public Registration getRegistration(Class type)
    {
        return classToRegistration.get(type);
    }


    @Override
    public Registration getRegistration(int classID)
    {
        return idToRegistration.get(classID);
    }


    @Override
    public void setKryo(Kryo kryo)
    {
        this.kryo = kryo;
    }


    @Override
    public synchronized void reset()
    {
    }
}
