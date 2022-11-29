package spring.application.tree.web.webscoket.models;

import lombok.Getter;

@Getter
public enum Endpoints {
    ;

    private final String endpointPrefix;

    Endpoints(String endpointPrefix) {
        this.endpointPrefix = endpointPrefix;
    }
}
