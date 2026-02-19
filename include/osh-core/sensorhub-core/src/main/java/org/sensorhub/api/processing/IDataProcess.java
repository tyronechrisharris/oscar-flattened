/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2017 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.processing;

import java.util.Map;
import org.sensorhub.api.command.ICommandReceiver;
import org.sensorhub.api.data.IDataProducer;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Interface for data processing components run by OSH.<br/>
 * Depending on the type of data sources, the process can be a streaming
 * process (i.e. always running to process incoming data streams) or an
 * on-demand process that is triggered externally.<br/>
 * The {@link #getInputDescriptors()}, {@link #getOutputDescriptors()} and
 * {@link #getParameterDescriptors()} methods only provide descriptors, not
 * streaming interfaces. For on-demand processes, outputs may have
 * descriptors but no associated streaming interfaces (in this case
 * output data is provided inline in the command result, and not evented
 * into a datastream).
 * </p>
 *
 * @author Alex Robin
 * @since May 8, 2017
 */
public interface IDataProcess extends IDataProducer, ICommandReceiver
{
    
    /**
     * Gets the list of all input descriptors.
     * <br/><br/>
     * Note that only inputs that are not connected to data sources will be
     * available for external trigger via the command interface
     * @return Read-only map of input name to input descriptor
     */
    public Map<String, DataComponent> getInputDescriptors();
    
    
    /**
     * Gets the list of all input descriptors.
     * <br/><br/>
     * Note that only inputs that are not connected to data sources will be
     * available for external trigger via the command interface
     * @return Read-only map of input name to input descriptor
     */
    public Map<String, DataComponent> getOutputDescriptors();
    
    
    /**
     * Gets the list of all parameter descriptors.
     * <br/><br/>
     * The list contains both fixed and taskable parameters. Parameters that
     * are taskable (i.e. that can be changed after the process is started)
     * must be marked as 'updatable'. Such parameters can be changed using
     * the command interface (see {@link org.sensorhub.api.command.IStreamingControlInterface IStreamingControlInterface}).
     * <br/><br/>
     * Note that changing the value of components in the parameter descriptors
     * (even when they are marked as 'updatable') has no effect.
     * @return Read-only map of parameter name to parameter descriptor
     */
    public Map<String, DataComponent> getParameterDescriptors();
    
}
