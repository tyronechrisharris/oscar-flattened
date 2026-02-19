/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.ui;

import org.sensorhub.ui.api.UIConstants;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;


@SuppressWarnings({ "serial", "deprecation" })
public class SearchBox extends TextField
{
    
    SearchBox(String caption, String description)
    {
        super(caption);
        addStyleName(UIConstants.STYLE_SMALL);
        setDescription(description);
        this.setColumns(20);
    }
    
    
    FormLayout wrap()
    {
        var searchLayout = new FormLayout();
        searchLayout.setMargin(false);
        searchLayout.setSpacing(false);
        searchLayout.addComponent(this);
        return searchLayout;
    }
    
    
    void addToParent(AbstractOrderedLayout parent)
    {
        parent.addComponent(wrap());
    }
    
    
    void addToParent(AbstractOrderedLayout parent, int index)
    {
        parent.addComponent(wrap(), index);
    }
}
