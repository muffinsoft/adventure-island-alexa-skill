package com.muffinsoft.alexa.skills.adventureisland.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(ApiCommunicator.class);
    private static final String ISPS_URL = "/v1/users/~current/skills/~current/settings/voicePurchasing.enabled";

    public static boolean areInSkillPurchasesEnabled(HandlerInput input) {
        boolean result = true;
        try {
            String host = input.getRequestEnvelope().getContext().getSystem().getApiEndpoint();
            String token = input.getRequestEnvelope().getContext().getSystem().getApiAccessToken();
            URL url = new URL(host + ISPS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("content-type", "application/json");
            if (conn.getResponseCode() != 200) {
                logger.warn("Error getting response, server replied: {}", conn.getResponseMessage());
                return true;
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String output;
            while ((output = br.readLine()) != null) {
                logger.debug("Got line: {}", output);
                result = Boolean.parseBoolean(output.trim());
            }
        } catch (Exception e) {
            logger.warn("Caught exception while requesting the status of InSkill purchases", e);
        }
        logger.debug("Result is {}", result);
        return result;
    }

}
