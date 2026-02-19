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


public class VectorNav
{
    public static final String VN200_PROC_UID = "urn:x-vectornav:sensor:vn200";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createVN200Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(VN200_PROC_UID)
            .name("VectorNav VN-200 GNSS/INS (Rugged)")
            .description("The VN-200 is a miniature, high performance GNSS-Aided Inertial "
                + "Navigation System (GNSS/INS) that combines 3-axis gyros, accelerometers "
                + "and magnetometers, a high-sensitivity GNSS receiver, and advanced Kalman "
                + "filtering algorithms to provide optimal estimates of position, velocity, "
                + "and attitude.")
            
            .addIdentifier(sml.identifiers.shortName("VN-200 (Rugged Version)"))
            .addIdentifier(sml.identifiers.longName("VectorNav VN-200 GNSS/INS (Rugged Version)"))
            .addIdentifier(sml.identifiers.manufacturer("VectorNav"))
            .addIdentifier(sml.identifiers.modelNumber("VN-200"))
            .addClassifier(sml.classifiers.sensorType("Inertial Navigation System (INS)"))
            .addClassifier(sml.classifiers.sensorType("Global Navigation Satellite System (GNSS)"))
            
            .addInput("position", sml.createObservableProperty()
                .definition(GeoPosHelper.DEF_LOCATION)
                .label("Position")
                .build()
            )
            .addInput("attitude", sml.createObservableProperty()
                .definition(GeoPosHelper.DEF_ORIENTATION)
                .label("Attitude")
                .build()
            )
            .addInput("velocity", sml.createObservableProperty()
                .definition(GeoPosHelper.DEF_VELOCITY)
                .label("Velocity")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics")
                .add("weight", sml.characteristics.mass(16, "g"))
                .add("length", sml.characteristics.length(36, "mm")
                    .label("Length")
                    .description("Package length for 'rugged' version"))
                .add("width", sml.characteristics.width(33, "mm")
                    .label("Width")
                    .description("Package width for 'rugged' version"))
                .add("height", sml.characteristics.height(9.5, "mm")
                    .label("Height")
                    .description("Package height for 'rugged' version"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(3.3, 17, "V")
                    .label("Input Voltage"))
                .add("current", sml.characteristics.operatingCurrent(80, "mA")
                    .label("Current Draw")
                    .description("Current draw at 5.0 V"))
                .add("power", sml.characteristics.nominalPowerConsumption(500, "mW")
                    .description("Power consumed, not including active antenna power consumption"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Type")
                    .value("Serial TTL, TS-232"))
                .add("baud_rate", sml.createQuantityRange()
                    .label("Baud Rate")
                    .description("Comm interface data transfer speed")
                    .uom("Bd")
                    .value(9600, 921600))
            )
            
            .addCapabilityList("heading_caps", sml.capabilities.systemCapabilities()
                .label("Heading Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-180, 180, "deg"))
                .add("resolution", sml.capabilities.resolution(0.001, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.2, "deg")
                    .label("Absolute Accuracy (1σ)"))
            )
            
            .addCapabilityList("pitch_caps", sml.capabilities.systemCapabilities()
                .label("Pitch Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-90, 90, "deg"))
                .add("resolution", sml.capabilities.resolution(0.001, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.03, "deg")
                    .label("Absolute Accuracy (1σ)"))
            )
            
            .addCapabilityList("roll_caps", sml.capabilities.systemCapabilities()
                .label("Roll Measurement Capabilities")
                .add("resolution", sml.capabilities.resolution(0.001, "deg"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.03, "deg")
                    .label("Absolute Accuracy (1σ)"))
            )
            
            .addCapabilityList("hpos_caps", sml.capabilities.systemCapabilities()
                .label("Horizontal Position Measurement Capabilities")
                .add("resolution", sml.capabilities.resolution(0.01, "m"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(1.0, "m")
                    .label("Absolute Accuracy (RMS)"))
            )
            
            .addCapabilityList("vpos_caps", sml.capabilities.systemCapabilities()
                .label("Vertical Position Measurement Capabilities")
                .add("resolution", sml.capabilities.resolution(0.01, "m"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(1.5, "m")
                    .label("Absolute Accuracy (RMS)"))
            )
            
            .addCapabilityList("vel_caps", sml.capabilities.systemCapabilities()
                .label("Velocity Measurement Capabilities")
                .add("resolution", sml.capabilities.resolution(0.01, "m/s"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.05, "m/s")
                    .label("Absolute Accuracy (RMS)"))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-40, 85, "Cel"))
                .add("MTBF", sml.capabilities.mtbf(150000, "h"))
            )
            
            .addContact(getVectorNavContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://www.vectornav.com/products/detail/vn-200")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://www.vectornav.com/docs/default-source/datasheets/vn-200-datasheet-rev2.pdf?sfvrsn=e1a7b2a0_10")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.USER_MANUAL_DEF, sml.createDocument()
                .name("User Manual")
                .url("https://www.vectornav.com/resources/user-manuals/vn-200-user-manual")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.vectornav.com/images/default-source/products/vn-200-rugged_large.png")
                .mediaType("image/png")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getVectorNavContactInfo()
    {
        return sml.createContact()
            .organisationName("VectorNav")
            .website("https://www.vectornav.com")
            .deliveryPoint("10501 Markison Road")
            .city("Dallas")
            .postalCode("75238")
            .administrativeArea("TX")
            .country("USA")
            .phone("+1 512 772 3615")
            .email("support@vectornav.com");
    }

}
