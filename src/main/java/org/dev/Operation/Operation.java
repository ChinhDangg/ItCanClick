package org.dev.Operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Operation implements MainJob, Serializable {

    private String operationName = "Operation Name";

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Operation getDeepCopied() {
        return new Operation(operationName);
    }
}
