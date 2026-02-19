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


public class Gill
{
    public static final String WINDMASTER_PROC_UID = "urn:x-gill:sensor:windmaster";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createWindmasterDatasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(WINDMASTER_PROC_UID)
            .name("Gill WindMaster 3-Axis Ultrasonic Anemometer")
            .description("The Gill WindMaster is a precision anemometer offering three-axis wind "
                + "measurement data. This instrument will monitor wind speeds of 0-50m/s and "
                + "provides sonic temperature, speed of sound and U, V & W vector outputs at 20Hz "
                + "(32Hz optional). This anemometer is of aluminium/carbon fibre construction "
                + "and is ideal for the understanding of turbulent flows, surface energy balance "
                + "and scalar fluxes. Each WindMaster can be calibrated with an optional Gill wind "
                + "tunnel test to provide optimum performance. "
                + "This 3D sonic anemometer is ideally suited to the measurement of air turbulence "
                + "around bridges, buildings, wind turbine sites, building ventilation control "
                + "systems, meteorological and flux measurement sites.")
            
            .addIdentifier(sml.identifiers.shortName("Gill Windmaster"))
            .addIdentifier(sml.identifiers.manufacturer("Gill Instruments"))
            .addIdentifier(sml.identifiers.modelNumber("Windmaster"))
            .addClassifier(sml.classifiers.sensorType("Anemometer"))
            
            .addInput("wind_speed", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("wind_speed"))
                .label("Wind Speed")
                .build()
            )
            .addInput("wind_dir", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("wind_from_direction"))
                .label("Wind Direction")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics (sensor only)")
                .add("weight", sml.characteristics.mass(1, "kg"))
                .add("length", sml.characteristics.length(750, "mm"))
                .add("diam", sml.characteristics.diameter(240, "mm"))
                .add("material", sml.characteristics.material("Aluminum / Carbon Fiber")
                    .label("Housing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(9, 30, "V")
                    .label("Input Voltage (DC)"))
                .add("current", sml.characteristics.operatingCurrent(55, "mA")
                    .label("Max Current")
                    .description("Current draw at 12 V DC"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Types")
                    .value("RS-232, RS-422, RS-485"))
                .add("baud_rate", sml.createQuantityRange()
                    .label("Baud Rate")
                    .description("UART data transfer speed (fixed)")
                    .uom("Bd")
                    .value(2400, 57600))
            )
            
            .addCapabilityList("wind_speed_caps", sml.capabilities.systemCapabilities()
                .label("Wind Speed Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 50, "m/s"))
                .add("resolution", sml.capabilities.resolution(0.01, "m/s"))
                .add("accuracy", sml.capabilities.relativeAccuracy(1.5))
                .add("samp_freq", sml.capabilities.samplingFrequency(20))
            )
            
            .addCapabilityList("wind_dir_caps", sml.capabilities.systemCapabilities()
                .label("Wind Direction Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 360, "deg"))
                .add("resolution", sml.capabilities.resolution(0.1, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(2, "deg"))
                .add("samp_freq", sml.capabilities.samplingFrequency(20))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-40, 70, "Cel"))
                .add("humidity", sml.conditions.humidityRange(5, 100, "%"))
            )
            
            .addContact(getGillContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://gillinstruments.com/compare-3-axis-anemometers/windmaster-3axis/")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://gillinstruments.com/wp-content/uploads/2022/08/WindMaster-iss7-Datasheet.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.alliance-technologies.net/wp-content/uploads/2019/04/WindMaster_head.png")
                .mediaType("image/png")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getGillContactInfo()
    {
        return sml.createContact()
            .organisationName("Gill Instruments Limited")
            .website("https://gillinstruments.com")
            .deliveryPoint("Saltmarsh Park, 67 Gosport Street")
            .city("Lymington")
            .postalCode("SO41 9EG")
            .country("United Kingdom")
            .phone("+44 (0) 1590 613500")
            .email("contact.gi@gill.group");
    }

}
