package com.mykyda.websocketdemo.http.controller;

import com.mykyda.websocketdemo.http.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController {

  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Message greeting(Message message) throws Exception {
    Thread.sleep(1000);
    System.out.println(message.getContent());
    return new Message("Hello, " + HtmlUtils.htmlEscape(message.getContent()) + "!");
  }
}