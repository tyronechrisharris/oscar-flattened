/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2023 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.consys.sensorml;

import org.sensorhub.api.procedure.IProcedureWithDesc;
import org.vast.ogc.gml.IFeature;
import net.opengis.sensorml.v20.AbstractProcess;


public class ProcedureAdapter extends SmlFeatureAdapter<AbstractProcess> implements IProcedureWithDesc
{
    public ProcedureAdapter(AbstractProcess f)
    {
        super(f);
    }
    
    
    public ProcedureAdapter(IFeature f)
    {
        super(f);
    }
    
    
    @Override
    public AbstractProcess getFullDescription()
    {
        if (delegate instanceof AbstractProcess)
            return (AbstractProcess)delegate;
        else
            return null;
    }
}
