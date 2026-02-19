/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are copyright (C) 2018, Sensia Software LLC
 All Rights Reserved. This software is the property of Sensia Software LLC.
 It cannot be duplicated, used, or distributed without the express written
 consent of Sensia Software LLC.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.api.datastore;


/**
 * <p>
 * Interface for ID providers that generate internal IDs for stored objects 
 * </p>
 * 
 * @param <T> Type of object to generate IDs for
 *
 * @author Alex Robin
 * @date Oct 8, 2018
 */
public interface IdProvider<T>
{

    long newInternalID(T obj);
}
