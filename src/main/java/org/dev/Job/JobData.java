package org.dev.Job;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobData implements Serializable {
    @Serial @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 1L;
    private MainJob mainJob;
    private List<JobData> jobDataList;
}
