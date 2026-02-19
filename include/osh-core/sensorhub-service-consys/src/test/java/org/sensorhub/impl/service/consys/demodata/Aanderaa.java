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

import org.vast.sensorML.SMLHelper;
import org.vast.sensorML.SMLMetadataBuilders.CIResponsiblePartyBuilder;
import org.vast.sensorML.helper.CommonIdentifiers;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import net.opengis.sensorml.v20.AbstractProcess;


public class Aanderaa
{
    public static final String OX4831_PROC_UID = "urn:x-aanderaa:sensor:ox4831";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createOX4831Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(OX4831_PROC_UID)
            .name("Aanderaa Oxygen Optode 4831")
            .description("The Oxygen Optode 4831 is a compact fully integrated sensor "
                + "for measuring O2 concentration and temperature.")
            
            .addIdentifier(sml.identifiers.shortName("Aanderaa 4831"))
            .addIdentifier(sml.identifiers.manufacturer("Aanderaa"))
            .addIdentifier(sml.identifiers.modelNumber("4831"))
            .addClassifier(sml.classifiers.sensorType("Oxygen Optode"))
            .addClassifier(sml.classifiers.sensorType("https://dbpedia.org/resource/Oxygen_sensor"))
            .addClassifier(sml.classifiers.sensorType("https://dbpedia.org/resource/Optode"))
            
            .addInput("oxygen", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/dissolved_oxygen")
                .label("Dissolved Oxygen")
                .build()
            )
            
            .addInput("temp", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/water_temperature")
                .label("Water Temperature")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics (sensor only)")
                .add("weight", sml.characteristics.mass(217, "g"))
                .add("length", sml.characteristics.length(111.5, "mm"))
                .add("diameter", sml.characteristics.diameter(36, "mm"))
                .add("material", sml.characteristics.material("Epoxy coated titanium")
                    .label("Housing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(5, 14, "V")
                    .label("Input Voltage (DC)"))
                .add("avg_current", sml.characteristics.operatingCurrent(25, "mA")
                    .label("Average Current"))
                .add("max_current", sml.characteristics.operatingCurrent(100, "mA")
                    .label("Max Current"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Type")
                    .value("RS-232"))
                .add("baud_rate", sml.createQuantity()
                    .label("Baud Rate")
                    .description("UART data transfer speed (fixed)")
                    .uom("Bd")
                    .value(9600))
            )
            
            .addCapabilityList("oxy_caps", sml.capabilities.systemCapabilities()
                .label("Oxygen Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 1000, "umol/l"))
                .add("resolution", sml.capabilities.resolution(0.1, "umol/l"))
                .add("accuracy", sml.capabilities.relativeAccuracy(1.5))
                .add("resp_time", sml.capabilities.responseTime(25, "s"))
                .add("samp_freq", sml.capabilities.samplingFrequency(2.0))
            )
            
            .addCapabilityList("temp_caps", sml.capabilities.systemCapabilities()
                .label("Temperature Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-5, 40, "Cel"))
                .add("resolution", sml.capabilities.resolution(0.01, "Cel"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.03, "Cel"))
                .add("resp_time", sml.capabilities.responseTime(2, "s"))
                .add("samp_freq", sml.capabilities.samplingFrequency(0.5))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-5, 40, "Cel"))
                .add("max_depth", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("depth_below_geoid"))
                    .label("Max Depth")
                    .uom("m")
                    .value(12000))
            )
            
            .addContact(getAandereaaContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://www.aanderaa.com/media/pdfs/d403_aanderaa_oxygen_sensor_4831_4831f.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.USER_MANUAL_DEF, sml.createDocument()
                .name("User Manual")
                .url("https://www.aanderaa.com/media/pdfs/oxygen-optode-4330-4835-and-4831.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage presenting Aanderaa oxygen sensors")
                .url("https://www.aanderaa.com/oxygen-sensors")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.fondriest.com/media/catalog/product/cache/ae8a6bc677e17d25017855202e90e7e0/a/a/aanderaa_4831_5.jpg")
                .mediaType("image/jpg")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getAandereaaContactInfo()
    {
        return sml.createContact()
            .organisationName("Aanderaa Data Instruments AS")
            .website("https://www.aanderaa.com")
            .deliveryPoint("Sanddalsringen 5b, P.O. Box 103 Midtun")
            .city("Bergen")
            .postalCode("5843")
            .country("Norway")
            .phone("+47 55 60 48 00")
            .email("aanderaa.info@xyleminc.com");
    }

}
