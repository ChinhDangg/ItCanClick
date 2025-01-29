package org.dev.JobController;

import javafx.concurrent.Task;
import org.dev.JobData.JobData;
import org.dev.RunJob.JobRunController;

public interface JobDataController extends MainJobController {

    boolean isSet();

    JobData getSavedData();

    void loadSavedData(JobData jobData);

    void addSavedData(JobData data);

    void removeSavedData(JobDataController jobDataController);

    void moveSavedDataUp(JobDataController jobDataController);

    void moveSavedDataDown(JobDataController jobDataController);

    JobRunController getRunJob();
}
