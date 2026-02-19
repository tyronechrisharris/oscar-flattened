/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys;

import static org.junit.Assert.assertEquals;
import java.io.StringWriter;
import java.util.Map;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLJsonBindings;
import org.vast.util.TimeExtent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;


public class TestProcedures extends AbstractTestAllSmlFeatures
{
    public static final String UID_FORMAT = "urn:osh:procedure:test%03d";
    static SMLHelper sml = new SMLHelper();

    
    public TestProcedures()
    {
        super(PROCEDURE_COLLECTION, UID_FORMAT);
    }
    
    
    // Non-Test helper methods
    
    @Override
    protected JsonObject createFeatureGeoJson(int procNum, TimeExtent validTime, Map<String, Object> props) throws Exception
    {
        var json = JsonBuilderFactory.buildObject()
            .add("type", "Feature")
            .addNull("geometry")
            .addObject("properties")
              .add("uid", String.format(UID_FORMAT, procNum))
              .add("name", "Test Procedure #" + procNum);
        
        if (validTime != null)
        {
            json.addArray("validTime")
                .add(validTime.begin().toString())
                .add(validTime.endsNow() ? "now" : validTime.end().toString())
            .end();
        }
        
        // add all other properties
        for (var prop: props.entrySet())
        {
            var val = prop.getValue();
            if (val instanceof String)
                json.add(prop.getKey(), (String)val);
            else if (val instanceof Number)
                json.add(prop.getKey(), (Number)val);
            else
                throw new IllegalArgumentException();
        }
        
        return json.end().getJson();
    }
    
    
    @Override
    protected JsonObject createFeatureSmlJson(int procNum) throws Exception
    {
        var numId = String.format("%03d", procNum);
        var builder = sml.createPhysicalSystem()
            .uniqueID(String.format(UID_FORMAT, procNum))
            .description("Sensor registered using CONSYS API")
            .name("Test Sensor")
            .addIdentifier(sml.identifiers.shortName("Test Procedure #" + numId))
            .addIdentifier(sml.identifiers.longName("Test Datasheet " + numId + " for temperature sensor"))
            .addIdentifier(sml.identifiers.manufacturer("SensorMakers Inc."))
            .addIdentifier(sml.identifiers.modelNumber("0123456879"))
            .addClassifier(sml.classifiers.sensorType("thermometer")
                .label("Instrument Type")
                .codeSpace("http://gcmdservices.gsfc.nasa.gov/static/kms/instruments/instruments.xml"))
            .location(1.2311, 43.5678, 0);
        
        var strWriter = new StringWriter();
        try (var writer = new JsonWriter(strWriter))
        {
            new SMLJsonBindings().writeDescribedObject(writer, builder.build());
        }

        return (JsonObject)JsonParser.parseString(strWriter.toString());
    }
    
    
    @Override
    protected void assertFeatureEquals(JsonObject expected, JsonObject actual)
    {
        actual.remove("id");
        actual.remove("links");
        assertEquals(expected, actual);
    }
    
}
