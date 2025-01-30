package org.dev.JobData;

import lombok.Getter;
import lombok.Setter;
import org.dev.Job.Action.Action;
import org.dev.Job.MainJob;

import java.io.Serial;
import java.util.List;

@Getter @Setter
public class ActionData implements JobData {
    @Serial
    private static final long serialVersionUID = 1L;
    private MainJob action;
    private List<JobData> entryConditionList;
    private List<JobData> exitConditionList;
}
