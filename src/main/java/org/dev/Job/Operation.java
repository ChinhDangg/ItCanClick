package org.dev.Job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class Operation implements MainJob, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String operationName = "Operation Name";

    @Override
    public Operation clone() {
        return new Operation(operationName);
    }
}
