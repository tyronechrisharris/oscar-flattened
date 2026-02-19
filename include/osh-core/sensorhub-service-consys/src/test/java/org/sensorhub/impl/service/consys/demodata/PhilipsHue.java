/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.demodata;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.ogc.gml.IFeature;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.sensorML.sampling.ViewingSector;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.impl.GMLFactory;
import net.opengis.sensorml.v20.AbstractProcess;


public class PhilipsHue
{
    public static final String HUEMOTION_PROC_UID = "urn:x-philips:sensor:8718696769881";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    static GMLFactory gml = new GMLFactory();
    
    
    static void addResources() throws IOException
    {
        Api.addOrUpdateProcedure(createMotionSensorDatasheet(), true);
        var sensor = createSensorInstance("0BCC970C",
            Instant.parse("2020-03-14T00:00:00Z"),
            gml.newPoint(-0.14237482178976277, 51.5016709109362));
        Api.addOrUpdateSystem(sensor, true);
        Api.addOrUpdateSF(sensor.getUniqueIdentifier(), createMotionSf(sensor), true);
        Api.addOrUpdateDataStream(createDataStream(sensor), true);
    }
    
    
    static AbstractProcess createMotionSensorDatasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(HUEMOTION_PROC_UID)
            .name("Philips Hue Motion Sensor")
            .description("The battery-powered Hue motion sensor can be easily installed anywhere in your home "
                + "and used to trigger your smart lights with movement. ")
            
            .addIdentifier(sml.identifiers.shortName("Philips Hue Motion Sensor"))
            .addIdentifier(sml.identifiers.manufacturer("Philips Hue"))
            .addIdentifier(sml.identifiers.modelNumber("8718696769881"))
            .addClassifier(sml.classifiers.sensorType("Motion Detector"))
            .addClassifier(sml.classifiers.sensorType("PIR Sensor"))
            
            .addInput("motion", sml.createObservableProperty()
                .definition(SWEHelper.getDBpediaUri("Motion"))
                .label("Object Motion")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics")
                .add("weight", sml.characteristics.mass(65, "g"))
                .add("width", sml.characteristics.width(32, "mm"))
                .add("length", sml.characteristics.length(28, "mm"))
                .add("height", sml.characteristics.height(32, "mm"))
                .add("material", sml.characteristics.material("Plastic")
                    .label("Housing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(2, 5, "V")
                    .label("Input Voltage (DC)"))
                .add("bat_cap", sml.characteristics.batteryCapacityRange(850, 1200, "mA.h", false)
                    .label("Battery Capacity"))
                .add("bat_type", sml.createCategory()
                    .definition(SWEHelper.getDBpediaUri("Battery_types"))
                    .label("Battery Type")
                    .value("AAA"))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(0, 40, "Cel"))
            )
            
            .addContact(getPhilipsContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage presenting Philips Hue sensors")
                .url("https://www.philips-hue.com/en-gb/p/hue-hue-motion-sensor/8719514342125")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://mcbcdn.com/images/w/PKq30995WrLPSpjalTBCsQ/s/11870/138/Philips_Hue_Motion_Sensor___lampemesteren.jpg")
                .mediaType("image/png")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getPhilipsContactInfo()
    {
        return sml.createContact()
            .organisationName("Philips Hue UK")
            .website("https://www.philips-hue.com/en-gb")
            .country("United Kingdom")
            .phone("00800 7445 4775");
    }
    
    
    static AbstractProcess createSensorInstance(String serialNum, Instant startTime, Point location)
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID("urn:x-osh:sensor:philips:" + serialNum)
            .name("Philips Motion Sensor " + serialNum)
            .typeOf(HUEMOTION_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("Philips Hue Motion Sensor"))
            .validFrom(startTime.atOffset(ZoneOffset.UTC))
            .location(location)
            .build();
    }
    
    
    static IFeature createMotionSf(AbstractProcess sys)
    {
        var sysUid = sys.getUniqueIdentifier();
        var sensorId = sysUid.substring(sysUid.lastIndexOf(':')+1);
        
        var sf = new ViewingSector();
        sf.setUniqueIdentifier(sysUid + ":sf");
        sf.setName("Motion Sensor " + sensorId + " Coverage Area");
        sf.setSampledFeature("Dining Room", SWEConstants.NIL_UNKNOWN);
        sf.setShape((Point)sys.getLocation());
        sf.setRadius(15.0);
        sf.setMinElevation(-90.0);
        sf.setMaxElevation(0.0);
        sf.setMinAzimuth(50.0);
        sf.setMaxAzimuth(250.0);
        
        return sf;
    }
    
    
    static IDataStreamInfo createDataStream(AbstractProcess sensor)
    {
        return new DataStreamInfo.Builder()
            .withSystem(new FeatureId(BigId.NONE, sensor.getUniqueIdentifier()))
            .withName(sensor.getName() + " - Motion Detections")
            .withRecordEncoding(sml.newTextEncoding())
            .withRecordDescription(sml.createRecord()
                .name("out1")
                .addField("time", sml.createTime()
                    .asPhenomenonTimeIsoUTC()
                )
                .addField("motion", sml.createBoolean()
                    .definition(SWEHelper.getDBpediaUri("Motion"))
                    .label("Motion Detected")
                    .description("Flag set to true when motion has been detected, false otherwise")
                )
                .build()
            )
            .build();
    }

}
