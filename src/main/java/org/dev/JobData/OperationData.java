package org.dev.JobData;

import lombok.Getter;
import lombok.Setter;
import org.dev.Job.MainJob;
import org.dev.Job.Operation;

import java.io.Serial;
import java.util.List;

@Getter @Setter
public class OperationData implements JobData {
    @Serial
    private static final long serialVersionUID = 1L;
    private MainJob operation;
    private List<JobData> taskGroupDataList;
}
