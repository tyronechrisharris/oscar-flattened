/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.processing;

import java.util.Collection;
import org.vast.sensorML.IProcessFactory;


/**
 * <p>
 * Management interface for process implementations
 * </p>
 *
 * @author Alex Robin
 * @since Nov 5, 2010
 */
public interface IProcessingManager extends IProcessFactory
{
	/**
	 * @return the list of all process code configured on the system
	 */
	public Collection<IProcessProvider> getAllProcessingPackages();
}
