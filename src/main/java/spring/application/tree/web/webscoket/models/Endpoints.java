package spring.application.tree.web.webscoket.models;

import lombok.Getter;

@Getter
public enum Endpoints {
    PRODUCT("/topic/product"),
    ORDER("/topic/order"),
    CUSTOMER("/topic/customer"),
    WORKER("/topic/worker");

    private final String endpointPrefix;

    Endpoints(String endpointPrefix) {
        this.endpointPrefix = endpointPrefix;
    }
}
