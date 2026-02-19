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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import org.isotc211.v2005.gmd.CIResponsibleParty;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.ogc.gml.IFeature;
import org.vast.ogc.om.SamplingPoint;
import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.impl.GMLFactory;
import net.opengis.sensorml.v20.AbstractProcess;


public class Davis
{
    public static final String VPRO2_PROC_UID = "urn:x-davis:station:vantagepro2";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static void addResources() throws IOException
    {
        // add weather station datasheet
        Api.addOrUpdateProcedure(Davis.createVantagePro2Datasheet(), true);
        
        // add station instances and datastreams
        for (var sys: Davis.getAllStations())
        {
            Api.addOrUpdateSystem(sys, true);
            Api.addOrUpdateSF(sys.getUniqueIdentifier(), createStationSf(sys), true);
            Api.addOrUpdateDataStream(Davis.createWeatherDataStream(sys), true);
        }
    }
    
    
    static AbstractProcess createVantagePro2Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(VPRO2_PROC_UID)
            .name("Davis Vantage Pro2 Weather Station")
            .description("An industrial-grade weather station engineered to handle the harshest "
                + "environments and deliver data with scientific precision, year after year. "
                + "The Vantage Pro2 and Vantage Pro2 Plus offer the professional weather observer "
                + "or serious weather enthusiast robust performance with a wide range of options "
                + "and sensors.")
            
            .addIdentifier(sml.identifiers.shortName("Davis Vantage Pro2"))
            .addIdentifier(sml.identifiers.longName("Davis Vantage Pro2 Weather Station"))
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addIdentifier(sml.identifiers.modelNumber("Vantage Pro2"))
            .addClassifier(sml.classifiers.sensorType("Weather Station"))
            
            .addCharacteristicList("mech_specs1", sml.createCharacteristicList()
                .label("Mechanical Characteristics (Console)")
                .add("weight", sml.characteristics.mass(850, "g"))
                .add("length", sml.characteristics.length(245, "mm"))
                .add("width", sml.characteristics.width(156, "mm"))
                .add("height", sml.characteristics.height(41, "mm"))
                .add("material", sml.characteristics.material("UV-resistant ABS plastic")
                    .label("Housing Material"))
            )
            
            .addCharacteristicList("mech_specs2", sml.createCharacteristicList()
                .label("Mechanical Characteristics (Sensor Suite)")
                .add("weight", sml.characteristics.mass(850, "g"))
                .add("length", sml.characteristics.length(356, "mm"))
                .add("width", sml.characteristics.width(239, "mm"))
                .add("height", sml.characteristics.height(368, "mm"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltage(5, "V")
                    .label("Input Voltage (DC)"))
                .add("current", sml.characteristics.operatingCurrent(10, "mA")
                    .label("Max Current"))
                .add("if_type", sml.createText()
                    .label("Interface Type")
                    .value("RS-232"))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-40, 65, "Cel"))
                .add("humidity", sml.conditions.humidityRange(0, 100, "%"))
            )
            
            .addContact(getDavisContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://www.davisinstruments.com/pages/vantage-pro2")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://cdn.shopify.com/s/files/1/0515/5992/3873/files/6152c_6162c_ss.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://m.media-amazon.com/images/I/71rycLk7sFL.jpg")
                .mediaType("image/jpg")
            )
            
            .addComponent("temp_sensor", createTempSensorDatasheet())
            .addComponent("press_sensor", createPressureSensorDatasheet())
            .addComponent("hum_sensor", createHumiditySensorDatasheet())
            .addComponent("wind_sensor", createWindSensorDatasheet())
            .addComponent("rain_sensor", createRainfallSensorDatasheet())
            
            .build();
    }
    
    
    static AbstractProcess createTempSensorDatasheet()
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .name("Temperature Sensor")
            
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addClassifier(sml.classifiers.sensorType("Thermometer"))
            
            .addInput("temp", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("air_temperature"))
                .label("Air Temperature")
                .build()
            )
            
            .addCapabilityList("meas_caps", sml.capabilities.systemCapabilities()
                .label("Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-40, 60, "Cel"))
                .add("resolution", sml.capabilities.resolution(0.1, "Cel"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.3, "Cel"))
                .add("samp_freq", sml.capabilities.samplingFrequency(1./10))
            )
            
            .build();
    }
    
    
    static AbstractProcess createPressureSensorDatasheet()
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .name("Pressure Sensor")
            
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addClassifier(sml.classifiers.sensorType("Barometer"))
            
            .addInput("press", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("air_pressure"))
                .label("Air Pressure")
                .build()
            )
            
            .addCapabilityList("meas_caps", sml.capabilities.systemCapabilities()
                .label("Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(540, 1100, "hPa"))
                .add("resolution", sml.capabilities.resolution(0.1, "hPa"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(1.0, "hPa"))
                .add("samp_freq", sml.capabilities.samplingFrequency(0.016))
            )
            
            .build();
    }
    
    
    static AbstractProcess createHumiditySensorDatasheet()
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .name("Humidity Sensor")
            
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addClassifier(sml.classifiers.sensorType("Hygrometer"))
            
            .addInput("hum", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("relative_humidity"))
                .label("Relative Humidity")
                .build()
            )
            
            .addCapabilityList("meas_caps", sml.capabilities.systemCapabilities()
                .label("Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(1, 100, "%"))
                .add("resolution", sml.capabilities.resolution(1, "%"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(2, "%"))
                .add("samp_freq", sml.capabilities.samplingFrequency(0.016))
            )
            
            .build();
    }
    
    
    static AbstractProcess createWindSensorDatasheet()
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .name("Wind Sensor")
            
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addClassifier(sml.classifiers.sensorType("Anemometer"))
            
            .addInput("hum", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("wind_speed"))
                .label("Wind Speed")
                .build()
            )
            
            .addCapabilityList("speed_meas_caps", sml.capabilities.systemCapabilities()
                .label("Speed Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(1, 322, "km/h"))
                .add("resolution", sml.capabilities.resolution(1, "km/h"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(3.2, "km/h"))
                .add("rel_accuracy", sml.capabilities.relativeAccuracy(0.5))
                .add("samp_freq", sml.capabilities.samplingFrequency(1/2.5))
            )
            
            .addCapabilityList("dir_meas_caps", sml.capabilities.systemCapabilities()
                .label("Direction Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 360, "deg"))
                .add("resolution", sml.capabilities.resolution(1, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(3, "deg"))
                .add("samp_freq", sml.capabilities.samplingFrequency(1/2.5))
            )
            
            .build();
    }
    
    
    static AbstractProcess createRainfallSensorDatasheet()
    {
        return sml.createPhysicalComponent()
            .definition(SWEConstants.DEF_SENSOR)
            .name("Rainfall Sensor")
            
            .addIdentifier(sml.identifiers.manufacturer("Davis Instruments"))
            .addClassifier(sml.classifiers.sensorType("Rain Gauge"))
            
            .addInput("rain", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("rainfall_amount"))
                .label("Rainfall Amount")
                .build()
            )
            
            .addCapabilityList("meas_caps", sml.capabilities.systemCapabilities()
                .label("Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 999.8, "mm"))
                .add("resolution", sml.capabilities.resolution(0.2, "mm"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.2, "mm"))
                .add("rel_accuracy", sml.capabilities.relativeAccuracy(3))
                .add("samp_freq", sml.capabilities.samplingFrequency(1./20))
            )
            
            .addCapabilityList("rate_meas_caps", sml.capabilities.systemCapabilities()
                .label("Rate Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 762, "mm/h"))
                .add("resolution", sml.capabilities.resolution(0.1, "mm/h"))
                .add("rel_accuracy", sml.capabilities.relativeAccuracy(5))
                .add("samp_freq", sml.capabilities.samplingFrequency(1./20))
            )
            
            .build();
    }
    
    
    static AbstractProcess createStationInstance(String id, Point location, double heading, Instant startTime)
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID("urn:x-meteofrance:stations:davis:" + id)
            .name("Meteo France Weather Station " + id)
            .typeOf(VPRO2_PROC_UID)
            .addIdentifier(sml.identifiers.shortName("Weather Station " + id))
            .addContact(getOperatorContactInfo())
            .validFrom(startTime.atOffset(ZoneOffset.UTC))
            .location(location)
            .build();
    }
    
    
    static Collection<AbstractProcess> getAllStations()
    {
        var list = new ArrayList<AbstractProcess>(100);
        
        var fac = new GMLFactory();
        var locations = new Point[] {
            fac.newPoint(1.359970, 43.637788),
            fac.newPoint(3.972312, 43.582533),
            fac.newPoint(4.885052, 44.930460),
            fac.newPoint(0.701791, 47.412094),
            fac.newPoint(-4.510511, 48.394632),
            fac.newPoint(2.329744, 48.862854)
        };
        
        for (int i = 0; i < locations.length; i++)
        {
            list.add(createStationInstance(
                String.format("WS%05d", (i+10)),
                locations[i],
                0.0,
                Instant.parse("2020-04-28T08:00:00Z").plus((i+1)*(i-1), ChronoUnit.DAYS)
            ));
        }
        
        return list;
    }
    
    
    static CIResponsiblePartyBuilder getDavisContactInfo()
    {
        return sml.createContact()
            .organisationName("Davis Instruments Corp.")
            .website("https://www.davisinstruments.com")
            .deliveryPoint("3465 Diablo Avenue")
            .city("Hayward")
            .postalCode("94545")
            .administrativeArea("CA")
            .country("USA")
            .phone("+1 (510) 732-7814")
            .email("support@davisinstruments.com");
    }
    
    
    static CIResponsibleParty getOperatorContactInfo()
    {
        return sml.createContact()
            .role(CommonIdentifiers.OPERATOR_DEF)
            .organisationName("Meteo France")
            .website("https://www.meteo.fr")
            .deliveryPoint("42 avenue Gaspard-Coriolis")
            .city("TOULOUSE")
            .postalCode("31057 Cedex 1")
            .country("France")
            .phone("+33 5 61 07 80 80")
            .build();
    }
    
    
    static IFeature createStationSf(AbstractProcess sys)
    {
        var sysUid = sys.getUniqueIdentifier();
        
        var sf = new SamplingPoint();
        sf.setUniqueIdentifier(sysUid + ":sf");
        sf.setName(sys.getName());
        sf.setShape((Point)sys.getLocation());
        sf.setSampledFeature("Earth Atmosphere", SWEHelper.getDBpediaUri("Atmosphere_of_Earth"));
        return sf;
    }
    
    
    static IDataStreamInfo createWeatherDataStream(AbstractProcess sys)
    {
        return new DataStreamInfo.Builder()
            .withSystem(new FeatureId(BigId.NONE, sys.getUniqueIdentifier()))
            .withName(sys.getName() + " - Weather Measurements")
            .withDescription("Weather measurements aggregated from all station sensors")
            .withRecordEncoding(sml.newTextEncoding())
            .withRecordDescription(sml.createRecord()
                .name("weather_data")
                .label("Weather Measurements")
                .addField("time", sml.createTime()
                    .asPhenomenonTimeIsoUTC()
                )
                .addField("temp", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("air_temperature"))
                    .label("Air Temperature")
                    .uomCode("Cel")
                )
                .addField("press", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("air_pressure"))
                    .label("Air Pressure")
                    .uomCode("hPa")
                )
                .addField("hum", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("relative_humidity"))
                    .label("Relative Humidity")
                    .uomCode("%")
                )
                .addField("wind_speed", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("wind_speed"))
                    .label("Wind Speed")
                    .uomCode("km/h")
                )
                .addField("wind_dir", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("wind_from_direction"))
                    .label("Wind Direction")
                    .description("Direction the wind is coming from, measured clockwise from north")
                    .refFrame(SWEConstants.REF_FRAME_NED)
                    .axisId("Z")
                    .uomCode("deg")
                )
                .addField("rain", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("rainfall_amount"))
                    .label("Rainfall Amount")
                    .uomCode("mm")
                )
                .build()
            )
            .build();
    }

}
