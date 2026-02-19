/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import java.io.IOException;
import java.util.Set;
import javax.servlet.AsyncContext;
import javax.xml.stream.XMLStreamException;
import org.vast.ogc.OGCRegistry;
import org.vast.ogc.def.DefinitionRef;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.OMUtils;
import org.vast.ogc.om.ObservationImpl;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.swe.SWEHelper;
import org.vast.xml.DOMHelper;
import org.vast.xml.IXMLWriterDOM;
import org.vast.xml.XMLWriterException;
import org.w3c.dom.Element;
import net.opengis.swe.v20.DataComponent;


/**
 * <p>
 * Abstract base class for all observation serializers using a StAX writer
 * </p>
 *
 * @author Alex Robin
 * @date Apr 5, 2020
 */
public abstract class AbstractObsSerializerStax extends AbstractAsyncSerializerStax<GetObservationRequest, IObservation> implements ISOSAsyncObsSerializer
{
    protected static final String DEFAULT_VERSION = "2.0.0";
    
    IXMLWriterDOM<IObservation> obsWriter;
    Set<String> selectedObservables;
    boolean allObservables;
    boolean firstObs;
    
    
    protected abstract void serializeObsElement(DOMHelper dom, Element obsElt) throws XMLStreamException;
    
    
    @Override
    @SuppressWarnings("unchecked")
    public void init(SOSServlet servlet, AsyncContext asyncCtx, GetObservationRequest request) throws IOException
    {
        super.init(servlet, asyncCtx, request);
        
        obsWriter = (IXMLWriterDOM<IObservation>)OGCRegistry.createWriter(OMUtils.OM, OMUtils.OBSERVATION, DEFAULT_VERSION);
        selectedObservables = request.getObservables();
        allObservables = request.getObservables().isEmpty();
    }


    @Override
    protected void writeRecord(IObservation obs) throws IOException
    {
        try
        {
            // if entire result requested
            if (allObservables)
            {
                writeSingleObs(obs);
            }
            
            // else write a different obs for each requested observable
            else
            {
                DataComponent fullObsResult = obs.getResult();
                
                for (String observable: selectedObservables)
                {                    
                    // filter obs result to only selected observable
                    if (!observable.equals(fullObsResult.getDefinition()))
                    {
                        DataComponent comp = SWEHelper.findComponentByDefinition(fullObsResult, observable);
                        if (isParentSelected(comp))
                            continue;
                        
                        // create new obs containing only the selected observable
                        ObservationImpl simpleObs = new ObservationImpl();
                        simpleObs.setType(SOSProviderUtils.getObsType(comp));
                        simpleObs.setFeatureOfInterest(obs.getFeatureOfInterest());
                        simpleObs.setProcedure(obs.getProcedure());
                        simpleObs.setObservedProperty(new DefinitionRef(observable));
                        simpleObs.setPhenomenonTime(obs.getPhenomenonTime());
                        simpleObs.setResultTime(obs.getResultTime());
                        simpleObs.setResult(comp);
                        
                        writeSingleObs(simpleObs);
                    }
                    else                
                        writeSingleObs(obs);
                }
            }
        }
        catch (XMLStreamException e)
        {
            throw new IOException("Error writing observation", e);
        }
    }
    
    
    private boolean isParentSelected(DataComponent comp)
    {
        while ((comp = comp.getParent()) != null)
        {
            String defUri = comp.getDefinition();
            if (defUri != null && selectedObservables.contains(defUri))
                return true;
        }
        
        return false;
    }
    
    
    protected void writeSingleObs(IObservation obs) throws XMLStreamException
    {
        try
        {
            // first write obs as DOM
            DOMHelper dom = new DOMHelper();
            Element obsElt = obsWriter.write(dom, obs);
            
            // serialize observation DOM tree into stream writer
            serializeObsElement(dom, obsElt);
        }
        catch (XMLWriterException e)
        {
            throw new XMLStreamException(e);
        }
    }

}
