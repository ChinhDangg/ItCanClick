package org.dev.JobController;

import javafx.concurrent.Task;
import org.dev.JobData.JobData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;

public interface JobDataController extends MainJobController {

    boolean isSet();

    String getName();

    JobData getSavedData();

    void loadSavedData(JobData jobData);

    void addSavedData(JobData data);

    void removeSavedData(JobStructure jobStructure);

    void moveSavedDataUp(JobStructure jobStructure);

    void moveSavedDataDown(JobStructure jobStructure);

    JobRunController getRunJob();
}
