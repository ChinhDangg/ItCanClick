package org.dev.RunJob;

import org.dev.JobController.MainJobController;
import org.dev.Job.JobData;
import org.dev.jobManagement.JobRunStructure;

public interface JobRunController<T> extends MainJobController {

    T startJob(JobData jobData);

    void setJobRunStructure(JobRunStructure jobRunStructure);
}
