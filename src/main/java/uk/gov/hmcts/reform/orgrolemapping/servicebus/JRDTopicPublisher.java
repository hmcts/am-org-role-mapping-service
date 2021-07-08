package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.UnprocessableEntityException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class JRDTopicPublisher extends CRDMessagingConfiguration {

    @Autowired
    private ServiceBusSenderClient serviceBusSenderClient;

    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 3)
    )
    public void sendMessage(@NotNull String userIds) {
        ServiceBusTransactionContext transactionContext = null;

        try {
            transactionContext = serviceBusSenderClient.createTransaction();
            publishMessageToTopic(userIds, serviceBusSenderClient, transactionContext);
        } catch (Exception exception) {
            if (Objects.nonNull(serviceBusSenderClient) && Objects.nonNull(transactionContext)) {
                log.info("Could not publish the messages to ASB");
                serviceBusSenderClient.rollbackTransaction(transactionContext);
            }
            throw new UnprocessableEntityException(Constants.ASB_PUBLISH_ERROR);
        }
        serviceBusSenderClient.commitTransaction(transactionContext);
        log.info("Message published to service bus topic");
    }

    private void publishMessageToTopic(String userIds,
                                       ServiceBusSenderClient serviceBusSenderClient,
                                       ServiceBusTransactionContext transactionContext) {
        log.info("Started publishing to topic:: {}", topic);
        ServiceBusMessageBatch messageBatch = serviceBusSenderClient.createMessageBatch();
        List<ServiceBusMessage> serviceBusMessages = new ArrayList<>();
        log.info("UserIds is " + userIds);
        serviceBusMessages.add(new ServiceBusMessage(userIds));

        for (ServiceBusMessage message : serviceBusMessages) {
            if (messageBatch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            serviceBusSenderClient.sendMessages(messageBatch, transactionContext);

            // create a new batch
            messageBatch = serviceBusSenderClient.createMessageBatch();

            // Add that message that we couldn't before.
            if (!messageBatch.tryAddMessage(message)) {
                log.error("Message is too large for an empty batch. Skipping. Max size: {}",
                        messageBatch.getMaxSizeInBytes());
            }
        }

        if (messageBatch.getCount() > 0) {
            serviceBusSenderClient.sendMessages(messageBatch, transactionContext);
            log.info("Sent a batch of messages to the topic ::{}", topic);
        }
    }
}

