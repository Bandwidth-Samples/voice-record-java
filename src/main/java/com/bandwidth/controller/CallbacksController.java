package com.bandwidth.controller;

import com.bandwidth.Main;
import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.ApiResponse;
import com.bandwidth.sdk.ApiException;
import com.bandwidth.sdk.auth.HttpBasicAuth;
import com.bandwidth.sdk.Configuration;

import com.bandwidth.sdk.model.InitiateCallback;
import com.bandwidth.sdk.model.RecordingAvailableCallback;
import com.bandwidth.sdk.api.RecordingsApi;
import com.bandwidth.sdk.model.bxml.*;
import com.bandwidth.sdk.model.bxml.Record;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {
    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    public final String username = System.getenv("BW_USERNAME");
    public final String password = System.getenv("BW_PASSWORD");
    public final String accountId = System.getenv("BW_ACCOUNT_ID");
    public final String baseUrl = System.getenv("BASE_CALLBACK_URL");

    public ApiClient defaultClient = Configuration.getDefaultApiClient();
    public HttpBasicAuth Basic = (HttpBasicAuth) defaultClient.getAuthentication("Basic");

    public final RecordingsApi recordingsApi = new RecordingsApi(defaultClient);

    @PostMapping("/inbound")
    public String inboundCall(@RequestBody InitiateCallback callback) throws IOException, JAXBException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        logger.info(callback.getCallId());

        SpeakSentence ss1 = new SpeakSentence("You have reached Vandelay Industries, Kal Varnsen is unavailable at this time.");

        SpeakSentence ss2 = new SpeakSentence("At the tone, please record your message, when you have finished recording, you may hang up.");

        PlayAudio playAudio = new PlayAudio().builder()
                                   .audioUri("/files/tone")
                                   .build();    

	Record record =  new Record().builder()
	                           .recordingAvailableUrl("/callbacks/recordingAvailableCallback")
	                           .build();

        response.withVerbs(ss1, ss2, playAudio, record);

	logger.info(response.toBxml(jaxbContext));

        return response.toBxml(jaxbContext);
    }

    @RequestMapping("/recordingAvailableCallback")
    public String gatherCallback(@RequestBody RecordingAvailableCallback callback) throws JAXBException {

        Response response = new Response();
	JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        Basic.setUsername(username);
        Basic.setPassword(password);

	String callId = callback.getCallId();
        String recordingId = callback.getRecordingId();

        logger.info(callback.getEventType());
        logger.info(callId);

        if("recordingAvailable".equalsIgnoreCase(callback.getEventType())) {

            try {
                File result = recordingsApi.downloadCallRecording(accountId, callId, recordingId);
                logger.info(result.getAbsolutePath());
            } catch (ApiException e) {
                System.err.println("Exception when calling RecordingsApi#downloadCallRecording");
                System.err.println("Status code: " + e.getCode());

		System.err.println("Reason: " + e.getResponseBody());
                System.err.println("Response headers: " + e.getResponseHeaders());
                e.printStackTrace();
	   }
        }

        return response.toBxml(jaxbContext);
    }

}
