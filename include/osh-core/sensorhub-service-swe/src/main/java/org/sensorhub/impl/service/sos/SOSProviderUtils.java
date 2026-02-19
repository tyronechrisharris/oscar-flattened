/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.time.Instant;
import java.util.Set;
import javax.xml.namespace.QName;
import net.opengis.fes.v20.BBOX;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.SimpleComponent;
import net.opengis.swe.v20.Vector;
import org.vast.ogc.def.DefinitionRef;
import org.vast.ogc.gml.FeatureRef;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.ObservationImpl;
import org.vast.ogc.om.ProcedureRef;
import org.vast.ows.OWSRequest;
import org.vast.ows.fes.FESRequestUtils;
import org.vast.ows.sos.GetResultRequest;
import org.vast.swe.SWEConstants;
import org.vast.util.Bbox;
import org.vast.util.TimeExtent;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;


public class SOSProviderUtils
{
    static final QName EXT_REPLAY = new QName("replayspeed"); // kvp params are always lower case
    static final QName EXT_WS = new QName("websocket");
    
    static final String OUTPUT_DEF_URI_PREFIX = "urn:osh:datastream:";
    static final String OUTPUT_DEF_URI_SUFFIX = ":all_properties";
    
    
    private SOSProviderUtils() {}
    
    
    public static boolean isWebSocketRequest(OWSRequest request)
    {
        return request.getExtensions().containsKey(EXT_WS);
    }
    
    
    public static boolean isStreamingRequest(GetResultRequest request)
    {
        return isReplayRequest(request) || isFutureTimePeriod(request.getTime());
    }
    
    
    public static boolean isReplayRequest(GetResultRequest request)
    {
        return request.getExtensions().containsKey(EXT_REPLAY);
    }
    
    
    public static double getReplaySpeed(GetResultRequest request)
    {
        if (isReplayRequest(request))
        {
            String replaySpeed = (String)request.getExtensions().get(EXT_REPLAY);
            return Double.parseDouble(replaySpeed);
        }
        
        return Double.NaN;
    }
    
    
    public static boolean isFutureTimePeriod(TimeExtent timeFilter)
    {
        return timeFilter != null &&
               timeFilter.beginsNow() &&
               !timeFilter.isNow();
    }
    
    
    public static Geometry toRoiGeometry(BBOX spatialFilter)
    {
        Bbox bbox = FESRequestUtils.filterToBbox(spatialFilter);
        return new GeometryFactory().createPolygon(new Coordinate[] {
           new Coordinate(bbox.getMinX(), bbox.getMinY()),
           new Coordinate(bbox.getMinX(), bbox.getMaxY()),
           new Coordinate(bbox.getMaxX(), bbox.getMaxY()),
           new Coordinate(bbox.getMaxX(), bbox.getMinY()),
           new Coordinate(bbox.getMinX(), bbox.getMinY())
        });
    }
    
    
    public static String getObsType(DataComponent result)
    {
        String obsType;
        if (result instanceof SimpleComponent)
            obsType = IObservation.OBS_TYPE_SCALAR;
        else if (result instanceof DataRecord || result instanceof Vector)
            obsType = IObservation.OBS_TYPE_RECORD;
        else if (result instanceof DataArray)
            obsType = IObservation.OBS_TYPE_ARRAY;
        else
            throw new IllegalStateException("Unsupported obs type");
        return obsType;
    }
    
    
    public static IObservation buildObservation(String procURI, String foiURI, DataComponent result)
    {        
        // get phenomenon time from record 'SamplingTime' if present
        // otherwise use current time
        double samplingTime = System.currentTimeMillis() / 1000.;
        for (int i = 0; i < result.getComponentCount(); i++)
        {
            DataComponent comp = result.getComponent(i);
            if (comp.isSetDefinition())
            {
                String def = comp.getDefinition();
                if (def.equals(SWEConstants.DEF_SAMPLING_TIME))
                {
                    samplingTime = comp.getData().getDoubleValue();
                }
            }
        }

        long samplingTimeMillis = (long)(samplingTime*1000.);
        TimeExtent phenTime = TimeExtent.instant(Instant.ofEpochMilli(samplingTimeMillis));

        // use same value for resultTime for now
        Instant resultTime = phenTime.begin();

        // observation property URI
        String obsPropDef = result.getDefinition();
        if (obsPropDef == null)
            obsPropDef = SWEConstants.NIL_UNKNOWN;
        
        // foi
        if (foiURI == null)
            foiURI = SWEConstants.NIL_UNKNOWN;

        // create observation object
        ObservationImpl obs = new ObservationImpl();
        obs.setType(getObsType(result));
        obs.setFeatureOfInterest(new FeatureRef<IFeature>(foiURI));
        obs.setObservedProperty(new DefinitionRef(obsPropDef));
        obs.setProcedure(new ProcedureRef(procURI));
        obs.setPhenomenonTime(phenTime);
        obs.setResultTime(resultTime);
        obs.setResult(result);

        return obs;
    }
    
    
    public static boolean hasCatchAllObservedProperty(DataComponent dataStruct)
    {
        // ok if we have a def on the root component
        if (dataStruct.getDefinition() != null)
            return true;
        
        // not good if root is an array w/o def
        if (dataStruct instanceof DataArray)
            return false;
        
        // if root has no def, ok if there is a single component other than time with a def
        return dataStruct.getComponentCount() == 2 &&
            dataStruct.getComponent(1).getDefinition() != null;
    }
    
    
    public static String getOutputURI(String outputName)
    {
        return OUTPUT_DEF_URI_PREFIX + outputName + OUTPUT_DEF_URI_SUFFIX;
    }
    
    
    public static String getOutputName(String outputURI)
    {
        return outputURI
            .replace(OUTPUT_DEF_URI_PREFIX, "")
            .replace(OUTPUT_DEF_URI_SUFFIX, "");
    }
    
    
    public static String getOutputNameFromObservableURIs(Set<String> observedProperties)
    {
        for (var prop: observedProperties)
        {
            if (prop.startsWith(OUTPUT_DEF_URI_PREFIX))
                return getOutputName(prop);
        }
        
        return null;
    }
}
