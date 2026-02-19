/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.system.wrapper;

import java.util.List;
import net.opengis.OgcPropertyList;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.AggregateProcess;
import net.opengis.sensorml.v20.Link;


public class AggregateProcessWrapper extends ProcessWrapper<AggregateProcess> implements AggregateProcess
{
    private static final long serialVersionUID = 443409548936838249L;


    AggregateProcessWrapper(AggregateProcess delegate)
    {
        super(delegate);
    }


    @Override
    public AbstractProcess getComponent(String name)
    {
        return delegate.getComponent(name);
    }


    @Override
    public OgcPropertyList<AbstractProcess> getComponentList()
    {
        return delegate.getComponentList();
    }


    @Override
    public List<Link> getConnectionList()
    {
        return delegate.getConnectionList();            
    }


    @Override
    public int getNumComponents()
    {
        return delegate.getNumComponents();
    }


    @Override
    public int getNumConnections()
    {
        return delegate.getNumConnections();
    }


    @Override
    public void addComponent(String name, AbstractProcess component)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }


    @Override
    public void addConnection(Link connection)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
}
