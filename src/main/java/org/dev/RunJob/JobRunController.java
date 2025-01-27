package org.dev.RunJob;

import org.dev.JobController.MainJobController;
import org.dev.JobData.JobData;

public interface JobRunController extends MainJobController {

    boolean startJob(JobData jobData);
}
