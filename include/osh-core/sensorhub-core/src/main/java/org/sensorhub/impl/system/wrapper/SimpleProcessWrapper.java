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

import net.opengis.OgcProperty;
import net.opengis.sensorml.v20.ProcessMethod;
import net.opengis.sensorml.v20.SimpleProcess;


public class SimpleProcessWrapper extends ProcessWrapper<SimpleProcess> implements SimpleProcess
{
    private static final long serialVersionUID = -5941698322270472852L;


    SimpleProcessWrapper(SimpleProcess delegate)
    {
        super(delegate);
    }
    
    
    @Override
    public ProcessMethod getMethod()
    {
        return delegate.getMethod();
    }
    
    
    @Override
    public OgcProperty<ProcessMethod> getMethodProperty()
    {
        return delegate.getMethodProperty();
    }
    
    
    @Override
    public boolean isSetMethod()
    {
        return delegate.isSetMethod();
    }
    
    
    @Override
    public void setMethod(ProcessMethod method)
    {
        throw new UnsupportedOperationException(IMMUTABLE_ERROR);
    }
}
