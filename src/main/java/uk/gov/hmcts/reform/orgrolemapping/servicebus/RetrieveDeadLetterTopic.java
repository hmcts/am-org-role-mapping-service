/*
package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class RetrieveDeadLetterTopic {

    static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        deleteMessagesInEnvelopesDlq();
        log.info("clients registered.....");
    }

    //@Scheduled(cron = "${scheduling.task.delete-envelopes-dlq-messages.cron}")
    public static void deleteMessagesInEnvelopesDlq() throws ServiceBusException, InterruptedException {
        log.info("Started {} job", "deleteMessagesInEnvelopesDlq");
        IMessageReceiver messageReceiver = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            messageReceiver = getMessageReceiver();
            log.info("MessageReceiver is: " + messageReceiver.toString());
            int completedCount = 0;
            IMessage message = messageReceiver.receive();
            log.info("received first message from DLQ: " + message.getMessageBody().toString());
            while (message != null) {
                */
/*if (canBeCompleted(message)) {
                    logMessage(message);*//*

                //DO not log user ID's
                log.info("Message is : " + mapper.readValue(message.getMessageBody().getBinaryData().get(0),
                        String.class));
                log.info("Message logging complete");
                messageReceiver.complete(message.getLockToken());
                completedCount++;
                log.info(
                        "Completed message from envelopes dlq. messageId: {} Current time: {}",
                        message.getMessageId(),
                        Instant.now()
                );
               */
/* } else {
                    // just continue, lock on the current msg will expire automatically
                    log.info("Leaving message on dlq, ttl has not passed yet. Message id: {}", message.getMessageId());
                }*//*

                message = messageReceiver.receive();
                //temporary break
                break;
            }

            log.info("Finished processing messages in envelopes dlq. Completed {} messages", completedCount);
        } catch (Exception e) {
            log.error("Unable to connect to envelopes dead letter queue", e);
        } finally {
            if (messageReceiver != null) {
                try {
                    log.info("Closing the messageReceiver");
                    messageReceiver.close();
                } catch (ServiceBusException e) {
                    log.error("Error closing dlq connection", e);
                }
            }
        }
        log.info("Finished {} job", "DlqProcessing");
        System.exit(0);
    }


    public static IMessageReceiver getMessageReceiver() throws InterruptedException, ServiceBusException {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;" +
                "SharedAccessKeyName=SendAndListenSharedAccessKey;" +
                "SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";

        try {
            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder
                    (connectionString, "rd-caseworker-topic-sandbox/subscriptions/temporary/$deadletterqueue")
                    , ReceiveMode.PEEKLOCK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Interrupted");
            throw e;
        } catch (ServiceBusException e) {
            log.info("ServiceBus exception" + e.getMessage());
            throw e;
        }
    }

}*/
