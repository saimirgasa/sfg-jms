package guru.springframework.jms.sender;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import guru.springframework.jms.config.JmsConfig;
import guru.springframework.jms.model.HelloWorldMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {

        System.out.println("I'm Sending a message");

        HelloWorldMessage message = HelloWorldMessage
            .builder()
            .id(UUID.randomUUID())
            .message("Hello World!")
            .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);

        System.out.println("Message Sent!");
    }

    @Scheduled(fixedRate = 2000)
    public void sendAndReceiveMessage() throws JMSException {

        HelloWorldMessage message = HelloWorldMessage
            .builder()
            .id(UUID.randomUUID())
            .message("Hello")
            .build();

        Message receivedMessage = jmsTemplate.sendAndReceive(JmsConfig.MY_SEND_RCVQUEUE, session -> {
            Message helloMessage = null;
            try {
                helloMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
                helloMessage.setStringProperty("_type", "guru.springframework.jms.model.HelloWorldMessage");

                System.out.println("Sending Hello!");

                return helloMessage;
            } catch (JsonProcessingException e) {
                throw new JMSException("boom");
            }
        });

        System.out.println(receivedMessage.getBody(String.class));
    }
}
