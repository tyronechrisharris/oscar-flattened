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


public class Vaisala
{
    public static final String PTB210_PROC_UID = "urn:x-vaisala:sensor:ptb210";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createPTB210Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(PTB210_PROC_UID)
            .name("Vaisala PTB210 Digital Barometer")
            .description("PTB210 is ideal for outdoor installations and harsh environments. "
                + "PTB210 is designed to operate in a wide temperature range, and the electronics "
                + "housing provides IP65 (NEMA 4) standardized protection against sprayed water.")
            
            .addIdentifier(sml.identifiers.shortName("Vaisala PTB210"))
            .addIdentifier(sml.identifiers.manufacturer("Vaisala"))
            .addIdentifier(sml.identifiers.modelNumber("PTB210"))
            .addClassifier(sml.classifiers.sensorType("Barometer"))
            
            .addInput("pressure", sml.createObservableProperty()
                .definition("https://mmisw.org/ont/ioos/parameter/air_pressure")
                .label("Atmospheric Pressure")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics (sensor only)")
                .add("weight", sml.characteristics.mass(110, "g"))
                .add("length", sml.characteristics.length(120, "mm"))
                .add("width", sml.characteristics.width(50, "mm"))
                .add("height", sml.characteristics.height(32, "mm"))
                .add("material", sml.characteristics.material("PC plastic")
                    .label("Housing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(5, 28, "V")
                    .label("Input Voltage (DC)"))
                .add("current", sml.characteristics.operatingCurrent(15, "mA")
                    .label("Max Current"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Type")
                    .value("RS-232"))
                .add("baud_rate", sml.createQuantityRange()
                    .label("Baud Rate")
                    .description("UART data transfer speed (fixed)")
                    .uom("Bd")
                    .value(1200, 19200))
            )
            
            .addCapabilityList("press_caps", sml.capabilities.systemCapabilities()
                .label("Pressure Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(50, 1100, "hPa"))
                .add("resolution", sml.capabilities.resolution(0.01, "hPa"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.5, "hPa"))
                .add("samp_freq", sml.capabilities.samplingFrequency(1))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-40, 60, "Cel"))
                .add("humidity", sml.conditions.humidityRange(0, 100, "%"))
            )
            
            .addContact(getVaisalaContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://www.vaisala.com/en/products/devices/instruments/ptb210")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://docs.vaisala.com/v/u/B210942EN-E/en-US")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://inemet.com/wp-content/uploads/2021/12/PTB210-copia.jpg")
                .mediaType("image/jpg")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getVaisalaContactInfo()
    {
        return sml.createContact()
            .organisationName("Vaisala Oyj")
            .website("https://www.vaisala.com")
            .deliveryPoint("Vanha Nurmijärventie 21")
            .city("Vantaa")
            .postalCode("01670")
            .country("Finland")
            .phone("+358 9 8949 3930")
            .email("helpdesk@vaisala.com");
    }

}
