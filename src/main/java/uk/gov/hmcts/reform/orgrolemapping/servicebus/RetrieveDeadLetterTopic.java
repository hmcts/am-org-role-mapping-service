package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RetrieveDeadLetterTopic {

    static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        deleteMessagesInEnvelopesDlq();
        log.info("clients registered.....");
    }

    static void registerMessageHandlerOnClient(SubscriptionClient receiveClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        log.info("registerMessageHandlerOnClient.....");
        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("onMessageAsync.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                log.info("body.....{}", body);
                Integer users = null;
                try {
                    users = mapper.readValue(body.get(0), Integer.class);
                } catch (IOException e) {
                    try {
                        receiveClient.abandon(message.getLockToken());
                    } catch (InterruptedException | ServiceBusException ex) {
                        ex.printStackTrace();
                    }
                    throw e;
                }
                System.out.printf(
                        "\n\t\t\t\t%s Message received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                                "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ User Id = %s]\n",
                        receiveClient.getEntityPath(),
                        message.getMessageId(),
                        message.getSequenceNumber(),
                        message.getEnqueuedTimeUtc(),
                        message.getExpiresAtUtc(),
                        message.getContentType(),
                        "",
                        "");

                System.out.printf("Message consumed successfully..... ");
                return receiveClient.completeAsync(message.getLockToken());
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                System.out.printf(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                messageHandler,new MessageHandlerOptions(1,
                        false, Duration.ofMinutes(1), Duration.ofMinutes(1)), executorService);

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
                /*if (canBeCompleted(message)) {
                    logMessage(message);*/
                //DO not log user ID's
                log.info("Message is : " + mapper.readValue(message.getMessageBody().getBinaryData().get(0), String.class));
                log.info("Message logging complete");
                messageReceiver.complete(message.getLockToken());
                completedCount++;
                log.info(
                        "Completed message from envelopes dlq. messageId: {} Current time: {}",
                        message.getMessageId(),
                        Instant.now()
                );
               /* } else {
                    // just continue, lock on the current msg will expire automatically
                    log.info("Leaving message on dlq, ttl has not passed yet. Message id: {}", message.getMessageId());
                }*/
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
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";

        try {
            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder
                    (connectionString, "rd-caseworker-topic-sandbox/subscriptions/temporary/$deadletterqueue"), ReceiveMode.PEEKLOCK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Interrupted");
            throw e;
        } catch (ServiceBusException e) {
            log.info("ServiceBus exception" + e.getMessage());
            throw e;
        }
    }

}
