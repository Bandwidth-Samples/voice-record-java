package com.bandwidth.controller;

import com.bandwidth.BandwidthClient;
import com.bandwidth.Environment;
import com.bandwidth.Model.VoiceCallback;
import com.bandwidth.exceptions.ApiException;
import com.bandwidth.http.response.ApiResponse;
import com.bandwidth.voice.bxml.verbs.*;
import com.bandwidth.voice.controllers.APIController;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    private String username = System.getenv("BW_USERNAME");
    private String password = System.getenv("BW_PASSWORD");
    private String accountId = System.getenv("BW_ACCOUNT_ID");
    private String applicationId = System.getenv("BW_VOICE_APPLICATION_ID");

    private BandwidthClient client = new BandwidthClient.Builder()
            .voiceBasicAuthCredentials(username, password)
            .environment(Environment.PRODUCTION)
            .build();

    private APIController controller = client.getVoiceClient().getAPIController();

    @PostMapping("/callInitiatedCallback")
    public String voiceCallback(@RequestBody VoiceCallback callback, HttpServletRequest request) throws IOException, MalformedURLException {

        logger.info(request.getServletPath() + " requested");

        Response response = new Response();

        SpeakSentence ss1 = SpeakSentence.builder().text("You have reached Vandelay Industries, Kal Varnsen is unavailable at this time.").build();

        SpeakSentence ss2 = SpeakSentence.builder().text("At the tone, please record your message, when you have finished recording, you may hang up.").build();

        PlayAudio playAudio = PlayAudio.builder().audioUri("/files/tone").build();

        Record record = Record.builder().recordingAvailableUrl("/callbacks/recordingAvailableCallback").build();

        String bxml = response.addAll(ss1, ss2, playAudio, record).toBXML();

        logger.info(bxml);

        return bxml;

    }

    @PostMapping("/recordingAvailableCallback")
    public String gatherCallback(@RequestBody VoiceCallback callback, HttpServletRequest request) throws IOException, ApiException {

        logger.info(request.getServletPath() + " requested");

        Response response = new Response();

        System.out.println(callback.getEventType());
        System.out.println(callback.getCallId());
        switch( callback.getEventType()) {
            case "recordingAvailable":

                ApiResponse<InputStream> recording = controller.getStreamRecordingMedia(accountId, callback.getCallId(), callback.getRecordingId());

                InputStream stream = recording.getResult();

                File file = new File("./recording." + callback.getFileFormat());

                FileUtils.copyInputStreamToFile(stream, file);

            default:
                break;
        }
        return response.toBXML();
    }


}
