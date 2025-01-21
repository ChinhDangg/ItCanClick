package org.dev.JobController;

import org.dev.JobData.JobData;

public interface JobDataController extends MainJobController {

    JobData getSavedData();

    void loadSavedData(JobData jobData);

    void addSavedData(JobData data);

    void removeSavedData(JobDataController jobDataController);
}
