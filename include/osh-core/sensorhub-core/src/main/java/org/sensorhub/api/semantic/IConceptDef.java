/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.semantic;

import java.util.Collection;
import org.vast.util.IResource;
import net.opengis.gml.v32.Reference;


/**
 * <p>
 * Interface for concept definitions. In OSH, such definitions are used
 * mainly to describe:
 * <ul>
 * <li>Observed properties</li>
 * <li>Controllable properties</li>
 * <li>System properties</li>
 * <li>Feature properties</li>
 * <li>System and component types</li>
 * </ul>
 * </p>
 *
 * @author Alex Robin
 * @since Apr 7, 2022
 */
public interface IConceptDef extends IResource
{
    
    /**
     * @return the concept IRI
     */
    public String getURI();
    
    
    /**
     * @return List of URLs to reference documents
     */
    public Collection<Reference> getReferences();
}
