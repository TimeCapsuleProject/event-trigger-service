package br.com.timecapsuleproject.timeCapsuleTriggerService.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RabbitConnectionVerifier implements ApplicationRunner {

    private final ConnectionFactory connectionFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking RabbitMQ connectivity (simple check)");
        try (var conn = connectionFactory.createConnection()) {
            if (conn == null || !conn.isOpen()) {
                throw new IllegalStateException("Cannot connect to RabbitMQ: connection is null or closed");
            }
            log.info("RabbitMQ connection OK");
        } catch (Exception ex) {
            log.error("RabbitMQ connectivity check failed: {}", ex.getMessage());
            throw new IllegalStateException("Failed to connect to RabbitMQ", ex);
        }
    }
}
