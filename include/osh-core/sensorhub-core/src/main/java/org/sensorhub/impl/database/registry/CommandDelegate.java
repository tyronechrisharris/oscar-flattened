/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.database.registry;

import java.time.Instant;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.common.BigId;
import org.vast.util.Asserts;
import net.opengis.swe.v20.DataBlock;


/**
 * <p>
 * ICommandData delegate used to override behavior of an existing ICommandData
 * implementation. 
 * </p>
 *
 * @author Alex Robin
 * @date Mar 24, 2021
 */
public class CommandDelegate implements ICommandData
{
    ICommandData delegate;


    public CommandDelegate(ICommandData delegate)
    {
        this.delegate = Asserts.checkNotNull(delegate, "delegate");
    }


    @Override
    public BigId getID()
    {
        return delegate.getID();
    }
    
    
    @Override
    public BigId getCommandStreamID()
    {
        return delegate.getCommandStreamID();
    }


    @Override
    public BigId getFoiID()
    {
        return delegate.getFoiID();
    }


    @Override
    public String getSenderID()
    {
        return delegate.getSenderID();
    }


    @Override
    public Instant getIssueTime()
    {
        return delegate.getIssueTime();
    }


    @Override
    public DataBlock getParams()
    {
        return delegate.getParams();
    }


    @Override
    public void assignID(BigId id)
    {
        throw new UnsupportedOperationException();
        
    }
    
}
