package com.atmmachine.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePinRequest {
    private String oldPin;
    private String newPin;
}
