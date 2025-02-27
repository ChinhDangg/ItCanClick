package org.dev.JobController;

import org.dev.Job.JobData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;

public interface JobDataController extends MainJobController {

    boolean isSet();

    String getName();

    JobData getSavedData();

    JobData getSavedDataByReference();

    void loadSavedData(JobData jobData);

    JobStructure addSavedData(JobData data);

    void removeSavedData(JobDataController jobDataController);

    void moveSavedDataUp(JobDataController jobDataController);

    void moveSavedDataDown(JobDataController jobDataController);

    JobRunController<Object> getRunJob();
}
