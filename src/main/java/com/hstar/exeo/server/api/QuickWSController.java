package com.hstar.exeo.server.api;

import com.hstar.exeo.objects.ws.WSEvent;
import com.hstar.exeo.server.ExeoConstants;
import com.hstar.exeo.server.websockets.WSContext;
import com.hstar.exeo.server.websockets.annotations.RequestWSMapping;
import com.hstar.exeo.server.websockets.annotations.WSController;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.hstar.exeo.objects.ws.WSEName.*;
import static com.hstar.exeo.server.websockets.annotations.RequestMappingAuthState.*;

/**
 * Created by Saswat on 1/3/2017.
 */
@Service
@WSController
public class QuickWSController {

    @RequestWSMapping(value = QUICKMODE, state = NOT_LOGGED_IN)
    public void enableQuickMode(WSContext context) throws IOException {//todo handle exception
        context.setQuickMode(true);

        context.sendMessage(new WSEvent.Builder("init")
                .subObject(new WSEvent.Builder("server")
                        .entry("protocolVersion", ExeoConstants.PROTOCOL_VERSION))
                .build());

        //create and send pairing info
        context.createAndSendPairCodes();
    }

}
