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

import org.vast.util.IResource;


/**
 * <p>
 * Interface representing a triple in the RDF sense.
 * </p>
 *
 * @author Alex Robin
 * @since Apr 7, 2022
 */
public interface IRelationship extends IResource
{
    
    /**
     * @return the subject IRI
     */
    public String getSubjectURI();
    
    
    /**
     * @return the predicate IRI
     */
    public String getPredicateURI();
    
    
    /**
     * @return the object IRI
     */
    public String getObjectURI();
}
