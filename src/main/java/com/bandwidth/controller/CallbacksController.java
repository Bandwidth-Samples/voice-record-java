package com.bandwidth.controller;

import com.bandwidth.sdk.ApiClient;
import com.bandwidth.sdk.api.RecordingsApi;
import com.bandwidth.sdk.model.InitiateCallback;
import com.bandwidth.sdk.model.RecordingAvailableCallback;
import com.bandwidth.sdk.model.bxml.*;
import com.bandwidth.sdk.model.bxml.Record;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {
    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    public final String accountId = System.getenv("BW_ACCOUNT_ID");
    public final String clientId = System.getenv("BW_CLIENT_ID");
    public final String clientSecret = System.getenv("BW_CLIENT_SECRET");

    public final RecordingsApi recordingsApi = new RecordingsApi(new ApiClient(clientId, clientSecret, null));

    @PostMapping(value = "/inbound/voice", produces = "application/xml")
    public ResponseEntity<String> inboundCall(@RequestBody InitiateCallback callback) throws JAXBException {
        if (!"initiate".equals(callback.getEventType())) {
            return ResponseEntity.badRequest().build();
        }

        Response response = new Response();
        JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);

        SpeakSentence ss1 = new SpeakSentence("You have reached Vandelay Industries, Kal Varnsen is unavailable at this time.");

        SpeakSentence ss2 = new SpeakSentence("At the tone, please record your message, when you have finished recording, you may hang up.");

        PlayAudio playAudio = new PlayAudio().builder()
                                   .audioUri("/tone.mp3")
                                   .build();

        Record record = new Record().builder()
                                   .recordingAvailableUrl("/callbacks/inbound/voice/recordingAvailable")
                                   .maxDuration(30)
                                   .build();

        response.withVerbs(ss1, ss2, playAudio, record);

        return ResponseEntity.ok(response.toBxml(jaxbContext));
    }

    @PostMapping("/inbound/voice/recordingAvailable")
    public ResponseEntity<Void> recordingAvailableCallback(@RequestBody RecordingAvailableCallback callback) {
        if (!"recordingAvailable".equals(callback.getEventType())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            File result = recordingsApi.downloadCallRecording(accountId, callback.getCallId(), callback.getRecordingId());
            logger.info(result.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to download recording: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }

}
