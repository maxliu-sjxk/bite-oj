package com.bite.common.rabbitmq.config;


import com.bite.common.core.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean("ojWorkExchange")
    public DirectExchange ojWorkExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstants.OJ_WORK_EXCHANGE).build();
    }

    @Bean("ojWorkQueue")
    public Queue ojWorkQueue() {
        return QueueBuilder.durable(RabbitMQConstants.OJ_WORK_QUEUE).build();
    }

    @Bean("ojWorkBinding")
    public Binding ojWorkBinding(@Qualifier("ojWorkQueue") Queue queue,
                                 @Qualifier("ojWorkExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.OJ_WORK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
