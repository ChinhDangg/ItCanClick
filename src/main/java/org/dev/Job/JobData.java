package org.dev.Job;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class JobData implements Serializable {
    @Serial @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 1L;
    private final MainJob mainJob;
    private final List<JobData> jobDataList;
}
