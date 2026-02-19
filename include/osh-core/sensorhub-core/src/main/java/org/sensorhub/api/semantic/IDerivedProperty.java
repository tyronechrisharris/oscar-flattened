/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.semantic;

import java.util.Collection;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Interface for derived property definitions. In OSH, such definitions are
 * used to describe:
 * <ul>
 * <li>Observed properties</li>
 * <li>Controllable properties</li>
 * <li>System properties</li>
 * <li>Feature properties</li>
 * </ul>
 * </p>
 *
 * @author Alex Robin
 * @since June 21, 2023
 */
public interface IDerivedProperty extends IConceptDef
{

    /**
     * @return The URI of the base property
     * (e.g. <i>https://qudt.org/vocab/quantitykind/Temperature</i>)
     */
    String getBaseProperty();
    
    
    /**
     * @return The URI of the type of entity the property applies to
     * (e.g. <i>http://dbpedia.org/resource/Central_processing_unit</i>)
     */
    String getObjectType();
    
    
    /**
     * @return The URI of the statistical operator applied to the property values
     * (e.g. <i>http://sensorml.com/ont/x-stats/HourlyMean</i>)
     */
    String getStatistic();
    
    
    /**
     * List of qualifiers to further define a more specific kind of the base property, such as:
     * <ul>
     * <li>Frequency band or range</li>
     * <li>Statistic flavor (e.g. <i>sampling period</i>, <i>integration period</i> for a custom temporal statistic)</li>
     * <li>Measurement conditions (e.g. <i>height above ground</i> or <i>under shelter</i>)</li>
     * <li>etc.</li>
     * </ul>
     * @return The list of qualifiers 
     */
    Collection<DataComponent> getQualifiers();
}
