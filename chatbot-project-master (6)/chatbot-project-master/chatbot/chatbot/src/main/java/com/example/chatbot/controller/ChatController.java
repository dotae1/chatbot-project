package com.example.chatbot.controller;

import com.example.chatbot.domain.ChatMessage;
import com.example.chatbot.domain.ChatRoom;
import com.example.chatbot.service.ChatRoomService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Controller
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL = "https://8gkzxq7qhe.apigw.ntruss.com/custom/v1/14758/b69c5ebb2364bb61a7ea436a8e362f5804dc6d15a085a87412ea7f18bdd0c788";
    private static final String SECRET_KEY = "WXBFcWphWG1SZFZoS2RBdG9OS1B3S2RCUld2R2JFalU=";

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public String sendMessage(@Payload ChatMessage chatMessage) throws Exception {
        System.out.println("Received message: " + chatMessage.getMessage());
        String clovaResponse = callClovaChatbotApi(chatMessage.getMessage());

        ChatRoom.ContextUser contextUser = new ChatRoom.ContextUser();
        contextUser.setQuestion(chatMessage.getMessage());
        contextUser.setAnswer(clovaResponse);
        contextUser.setClova(true);
        contextUser.setModifyRequest(false);
        contextUser.setModifyText("");

        chatRoomService.addMessageToChatRoom(chatMessage.getChatRoomId(), contextUser);

        System.out.println("Sending response: " + clovaResponse);
        return clovaResponse;
    }

    private String callClovaChatbotApi(String message) throws Exception {
        String reqMessage = getReqMessage(message);
        String signature = makeSignature(reqMessage, SECRET_KEY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-CHATBOT_SIGNATURE", signature);

        HttpEntity<String> entity = new HttpEntity<>(reqMessage, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String jsonString = response.getBody();
            System.out.println("CLOVA API response: " + jsonString);

            // 받은 JSON 응답을 파싱하여 이미지 URL과 일반 URL, 텍스트를 추출
            JSONParser jsonparser = new JSONParser();
            JSONObject json = (JSONObject) jsonparser.parse(jsonString);
            JSONArray bubblesArray = (JSONArray) json.get("bubbles");
            JSONObject bubbles = (JSONObject) bubblesArray.get(0);
            JSONObject data = (JSONObject) bubbles.get("data");

            // 텍스트 설명 추출
            String description = (String) data.get("description");

            // 이미지 URL 추출
            String imageUrl = data.containsKey("imageUrl") ? (String) data.get("imageUrl") : "";
            // 일반 URL 추출
            String url = data.containsKey("url") ? (String) data.get("url") : "";

            // 이미지와 URL이 모두 비어 있으면 텍스트만 반환
            if (imageUrl.isEmpty() && url.isEmpty()) {
                return description;
            }

            // 클라이언트로 반환할 응답을 JSON 형태로 구성
            JSONObject responseObj = new JSONObject();
            responseObj.put("description", description);
            if (!imageUrl.isEmpty()) {
                responseObj.put("imageUrl", imageUrl);
            }
            if (!url.isEmpty()) {
                // URL에서 백슬래시 제거
                url = url.replace("\\", "");
                responseObj.put("url", url);
            }

            return responseObj.toString();
        } else {
            System.out.println("Error from CLOVA Chatbot API: " + response.getStatusCode());
            return "Error from CLOVA Chatbot API";
        }
    }

    public static String makeSignature(String message, String secretKey) throws Exception {
        byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(rawHmac);
    }

    public static String getReqMessage(String voiceMessage) {
        JSONObject obj = new JSONObject();
        long timestamp = new Date().getTime();
        obj.put("version", "v2");
        obj.put("userId", "U47b00b58c90f8e47428af8b7bddc1231heo2");
        obj.put("timestamp", timestamp);

        JSONObject bubblesObj = new JSONObject();
        bubblesObj.put("type", "text");

        JSONObject dataObj = new JSONObject();
        dataObj.put("description", voiceMessage);

        bubblesObj.put("data", dataObj);

        JSONArray bubblesArray = new JSONArray();
        bubblesArray.add(bubblesObj);

        obj.put("bubbles", bubblesArray);
        obj.put("event", "send");

        return obj.toString();
    }
}
