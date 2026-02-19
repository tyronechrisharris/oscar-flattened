/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2021 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.serialization.kryo;


import java.awt.geom.Point2D;


/*
 * Reordering of fields has no impact if FieldSerializer was used since they
 * are serialized in alphabetical order
 */
public class TestObjectV2
{
    String att1_new_name = "test2";
    double att3 = 2.22;
    int att2 = 2;
    Point2D att4_new_type = new Point2D.Float(2.2f, 2.2f);
    String att5_new = "test5";
}
