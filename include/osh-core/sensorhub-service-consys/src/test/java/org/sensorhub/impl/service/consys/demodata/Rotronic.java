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


public class Rotronic
{
    public static final String HC2_PROC_UID = "urn:x-rotronic:sensor:hc2a-s3";
    
    static SMLHelper sml = new SMLHelper();
    static GeoPosHelper swe = new GeoPosHelper();
    
    
    static AbstractProcess createHC2Datasheet()
    {
        return sml.createPhysicalSystem()
            .definition(SWEConstants.DEF_SENSOR)
            .uniqueID(HC2_PROC_UID)
            .name("Rotronic HC2-S3 Temp/Humidity Probe")
            .description("The HC2A-S / HC2A-S3 is the most versatile probe from ROTRONIC. "
                + "The HC2A-S3 version has a filter with larger pore size suitable for "
                + "meteorological applications. It measures humidity and temperature, and "
                + "calculates the dew/frost point.")
            
            .addIdentifier(sml.identifiers.shortName("Rotronic HC2-S3"))
            .addIdentifier(sml.identifiers.manufacturer("Rotronic"))
            .addIdentifier(sml.identifiers.modelNumber("HC2A-S3"))
            .addClassifier(sml.classifiers.sensorType("Thermometer"))
            .addClassifier(sml.classifiers.sensorType("Hygrometer"))
            
            .addInput("temp", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("air_temperature"))
                .label("Air Temperature")
                .build()
            )
            .addInput("humidity", sml.createObservableProperty()
                .definition(SWEHelper.getCfUri("relative_humidity"))
                .label("Relative Humidity")
                .build()
            )
            
            .addCharacteristicList("mech_specs", sml.createCharacteristicList()
                .label("Mechanical Characteristics")
                .add("weight", sml.characteristics.mass(17, "g"))
                .add("length", sml.characteristics.length(108, "mm"))
                .add("diameter", sml.characteristics.diameter(15, "mm"))
                .add("material", sml.characteristics.material("Polycarbonate")
                    .label("Casing Material"))
            )
            
            .addCharacteristicList("elec_specs", sml.createCharacteristicList()
                .label("Electrical Characteristics")
                .add("voltage", sml.characteristics.operatingVoltageRange(3.3, 5, "V")
                    .label("Input Voltage"))
                .add("current", sml.characteristics.operatingCurrent(4.5, "mA")
                    .label("Current Draw")
                    .description("Current draw at 3.3 VDC"))
                .add("if_type", sml.createText()
                    .definition(SWEHelper.getDBpediaUri("Interface_(computing)"))
                    .label("Interface Type")
                    .value("UART"))
                .add("baud_rate", sml.createQuantity()
                    .label("Baud Rate")
                    .description("UART data transfer speed (fixed)")
                    .uom("Bd")
                    .value(19200))
            )
            
            .addCapabilityList("temp_caps", sml.capabilities.systemCapabilities()
                .label("Temperature Measurement Capabilities")
                .add("range", sml.capabilities.measurementRange(-100, 200, "Cel"))
                .add("resolution", sml.capabilities.resolution(0.1, "Cel"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.1, "Cel"))
            )
            
            .addCapabilityList("hum_caps", sml.capabilities.systemCapabilities()
                .label("Humidity Measurement Capabilities")
                .addCondition("temp", sml.conditions.temperatureRange(10, 30, "Cel"))
                .add("range", sml.capabilities.measurementRange(0, 100, "%"))
                .add("resolution", sml.capabilities.resolution(0.1, "%"))
                .add("accuracy", sml.capabilities.absoluteAccuracy(0.8, "%"))
                .add("resp_time", sml.capabilities.responseTime(15, "s"))
            )
            
            .addCapabilityList("op_range", sml.capabilities.operatingProperties()
                .add("temperature", sml.conditions.temperatureRange(-50, 80, "Cel"))
                .add("humidity", sml.conditions.humidityRange(0, 100, "%"))
            )
            
            .addContact(getRotronicContactInfo()
                .role(CommonIdentifiers.MANUFACTURER_DEF)
            )
            
            .addDocument(CommonIdentifiers.WEBPAGE_DEF, sml.createDocument()
                .name("Product Web Site")
                .description("Webpage with specs an other resources")
                .url("https://www.rotronic.com/en/hc2a-s3")
                .mediaType("text/html")
            )
            .addDocument(CommonIdentifiers.SPECSHEET_DEF, sml.createDocument()
                .name("Spec Sheet")
                .url("https://www.rotronic.com/pub/media/productattachments/files/59055E_HC2A_1.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.USER_MANUAL_DEF, sml.createDocument()
                .name("Instruction Manual")
                .url("https://www.rotronic.com/pub/media/productattachments/files/12.1085.0103_HygroClip2_Advanced_web.pdf")
                .mediaType("application/pdf")
            )
            .addDocument(CommonIdentifiers.PHOTO_DEF, sml.createDocument()
                .name("Photo")
                .url("https://www.rotronic.com/pub/media/catalog/product/cache/63d1467875b1593edea42d54c3efc37c/h/c/hc2_s3-meteo.jpg")
                .mediaType("image/jpg")
            )
            
            .build();
    }
    
    
    static CIResponsiblePartyBuilder getRotronicContactInfo()
    {
        return sml.createContact()
            .organisationName("ROTRONIC AG")
            .website("https://www.rotronic.com")
            .deliveryPoint("Grindelstrasse 6")
            .city("Bassersdorf")
            .postalCode("CH-8303")
            .country("Switzerland")
            .phone("+41 44 838 11 11")
            .email("measure@rotronic.ch");
    }

}
