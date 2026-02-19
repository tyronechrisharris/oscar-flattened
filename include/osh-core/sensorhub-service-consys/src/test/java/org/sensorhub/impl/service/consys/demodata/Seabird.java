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


public class Seabird
{
    public static final String SBE37_PROC_UID = "urn:x-seabird:sensor:sbe37-smp-odo";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createSBE37Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(SBE37_PROC_UID)
            .name("Seabird SBE37 CTD/Oxygen Sensor")
            .description("The SBE 37-SM/SMP/SMP-ODO MicroCAT is a high-accuracy conductivity "
                + "and temperature (pressure optional) recorder with RS-232 or RS-485 interface, "
                + "internal batteries, data storage, optional pump, and optional optical dissolved "
                + "oxygen sensor. The MicroCAT is designed for moorings or other long-duration, "
                + "fixed-site deployments for fully autonomous applications.")
            
            .addIdentifier(sml.identifiers.shortName("Seabird SBE37"))
            .addIdentifier(sml.identifiers.manufacturer("Seabird"))
            .addIdentifier(sml.identifiers.modelNumber("SBE 37 SML-ODO"))
            .addClassifier(sml.classifiers.sensorType("CTD"))
            .addClassifier(sml.classifiers.sensorType("Oxygen Sensor"))
            
            .addInput("temp", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/water_temperature")
                .label("Water Temperature")
                .build()
            )
            .addInput("conductivity", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/conductivity")
                .label("Conductivity")
                .build()
            )
            .addInput("depth", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/depth")
                .label("Depth")
                .build()
            )
            .addInput("oxygen", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/dissolved_oxygen")
                .label("Dissolved Oxygen")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics")
                .add("weight", sml.characteristics.mass(3.4, "kg"))
                .add("length", sml.characteristics.length(627.6, "mm"))
                .add("diameter", sml.characteristics.diameter(62, "mm"))
                .add("material", sml.characteristics.material("Plastic")
                    .label("Casing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(9, 24, "V")
                    .label("Input Voltage"))
                .add("current", sml.characteristics.operatingCurrent(250, "mA")
                    .label("Current Draw")
                    .description("Current draw at 9 VDC"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Types")
                    .value("RS-232, RS-485"))
                .add("baud_rate", sml.createQuantity()
                    .label("Baud Rate")
                    .description("UART data transfer speed (fixed)")
                    .uom("Bd")
                    .value(19200))
            )
            
            .addCapabilityList("temp_caps", sml.capabilities.systemCapabilities()
                .label("Temperature Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-5, 45, "Cel"))
                .add("resolution", sml.capabilities.resolution(0.0001, "Cel"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.002, "Cel"))
            )
            
            .addCapabilityList("cond_caps", sml.capabilities.systemCapabilities()
                .label("Conductivity Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 7, "S/m"))
                .add("resolution", sml.capabilities.resolution(0.00001, "S/m"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.0003, "S/m"))
            )
            
            .addCapabilityList("press_caps", sml.capabilities.systemCapabilities()
                .label("Depth Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 350, "m"))
                .add("resolution", sml.capabilities.resolution(0.007, "m"))
                .add("accuracy", sml.capabilities.relativeAccuracy(0.1))
            )
            
            .addCapabilityList("oxy_caps", sml.capabilities.systemCapabilities()
                .label("Oxygen Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(0, 1000, "umol/l"))
                .add("resolution", sml.capabilities.resolution(0.2, "umol/l"))
                .add("accuracy", sml.capabilities.relativeAccuracy(2.0))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-5, 45, "Cel"))
                .add("max_depth", sml.createQuantity()
                    .definition(SWEHelper.getCfUri("depth_below_geoid"))
                    .label("Max Depth")
                    .uom("m")
                    .value(350))
            )
            
            .addContact(getSeaBirdContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://www.seabird.com/moored/sbe-37-sm-smp-smp-odo-microcat/family?productCategoryId=54627473786")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://www.seabird.com/asset-get.download.jsa?id=54627861945")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.USER_MANUAL_DEF, sml.createDocument()
                .name("User Manual")
                .url("https://www.seabird.com/asset-get.download.jsa?id=54627862348")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.seabird.com/asset-get.class.image.jsa?code=263967&type=P&size=L")
                .mediaType("image/jpg")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getSeaBirdContactInfo()
    {
        return sml.createContact()
            .organisationName("Sea-Bird Scientific")
            .website("https://www.seabird.com")
            .deliveryPoint("13431 NE 20th St")
            .city("Bellevue")
            .administrativeArea("WA")
            .postalCode("98005")
            .country("USA")
            .phone("+1 425-643-9866")
            .email("techsupport@seabird.com");
    }

}
