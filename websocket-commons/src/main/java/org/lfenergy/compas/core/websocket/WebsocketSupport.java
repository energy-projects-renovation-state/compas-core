// SPDX-FileCopyrightText: 2022 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.core.websocket;

import org.lfenergy.compas.core.commons.exception.CompasException;
import org.lfenergy.compas.core.commons.model.ErrorResponse;

import javax.websocket.Session;
import javax.xml.bind.JAXBContext;
import java.io.StringReader;
import java.io.StringWriter;

import static org.lfenergy.compas.core.commons.exception.CompasErrorCode.*;

public final class WebsocketSupport {
    WebsocketSupport() {
        throw new UnsupportedOperationException("WebsocketSupport class");
    }

    public static <T> String encode(T jaxbObject, Class<T> jaxbClass) {
        try {
            var jaxbContext = JAXBContext.newInstance(jaxbClass);
            var marshaller = jaxbContext.createMarshaller();

            var st = new StringWriter();
            marshaller.marshal(jaxbObject, st);
            return st.toString();
        } catch (Exception exp) {
            throw new CompasException(WEBSOCKET_ENCODER_ERROR_CODE,
                    "Error marshalling to string from class '" + jaxbClass.getName() + "' for Websockets.",
                    exp);
        }
    }

    public static <T> T decode(String message, Class<T> jaxbClass) {
        try {
            var jaxbContext = JAXBContext.newInstance(jaxbClass);
            var unmarshaller = jaxbContext.createUnmarshaller();
            var reader = new StringReader(message);
            return jaxbClass.cast(unmarshaller.unmarshal(reader));
        } catch (Exception exp) {
            throw new CompasException(WEBSOCKET_DECODER_ERROR_CODE,
                    "Error unmarshalling to class '" + jaxbClass.getName() + "' from Websockets.",
                    exp);
        }
    }

    public static void handleException(Session session, RuntimeException re) {
        var response = new ErrorResponse();
        if (re instanceof CompasException) {
            response.addErrorMessage(((CompasException) re).getErrorCode(), re.getMessage());
        } else {
            response.addErrorMessage(WEBSOCKET_GENERAL_ERROR_CODE, re.getMessage());
        }
        session.getAsyncRemote().sendObject(response);
    }
}
