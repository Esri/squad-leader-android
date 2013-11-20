/*******************************************************************************
 * Copyright 2013 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.controller;

import com.esri.core.symbol.advanced.MessageHelper;

/**
 * A controller that sends messages to listening clients.
 * @see com.esri.militaryapps.controller.OutboundMessageController
 */
public class OutboundMessageController extends com.esri.militaryapps.controller.OutboundMessageController {

    /**
     * Creates an OutboundMessageController for the given UDP port.
     * @param messagingPort the UDP port through which messages will be sent.
     */
    public OutboundMessageController(int messagingPort) {
        super(messagingPort);
    }

    @Override
    public String getTypePropertyName() {
        return MessageHelper.MESSAGE_TYPE_PROPERTY_NAME;
    }

    @Override
    public String getIdPropertyName() {
        return MessageHelper.MESSAGE_ID_PROPERTY_NAME;
    }

    @Override
    public String getWkidPropertyName() {
        return MessageHelper.MESSAGE_WKID_PROPERTY_NAME;
    }

    @Override
    public String getControlPointsPropertyName() {
        return MessageHelper.MESSAGE_2525C_CONTROL_POINTS_PROPERTY_NAME;
    }

    @Override
    public String getActionPropertyName() {
        return MessageHelper.MESSAGE_ACTION_PROPERTY_NAME;
    }

}
