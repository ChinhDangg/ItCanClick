package org.dev.jobManagement;

import javafx.application.Platform;
import javafx.concurrent.Task;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.JobController.JobDataController;
import org.dev.RunJob.JobRunController;

public class JobRunScheduler {
    @Getter
    private boolean isJobRunning = false;
    @Getter
    private JobRunStructure currentJobRunStructure;
    private static Thread runJobThread = null;

    private final String className = this.getClass().getSimpleName();

    public void startJobRun(JobDataController jobDataController) {
        if (jobDataController == null)
            return;
        if (isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Fail to start - as another job is running");
            return;
        }
        setIsJobRunning(true);
        AppScene.addLog(LogLevel.INFO, className, "Starting to run job");
        Task<Void> runJobTask = getRunJobTask(jobDataController);
        runJobThread = new Thread(runJobTask);
        Platform.runLater(AppScene::displayCurrentRunJobNode);
        Platform.runLater(AppScene::loadRunSideMenuHierarchy);
        runJobThread.start();
    }

    private Task<Void> getRunJobTask(JobDataController jobDataController) {
        JobRunController<Object> jobRunController = jobDataController.getRunJob();
        currentJobRunStructure = new JobRunStructure(null, null, jobRunController, jobDataController.getName());
        jobRunController.setJobRunStructure(currentJobRunStructure);
        JobData jobData = jobDataController.getSavedData();
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    currentJobRunStructure.getCurrentController().startJob(jobData);
                    AppScene.addLog(LogLevel.INFO, className, "Finished running job");
                    setIsJobRunning(false);
                } catch (Exception e) {
                    AppScene.addLog(LogLevel.ERROR, className, "Error to start run job");
                }
                return null;
            }
        };
    }

    public void stopRunJob() {
        if (!isJobRunning) {
            return;
        }
        AppScene.addLog(LogLevel.INFO, className, "Stopping running job");
        runJobThread.interrupt();
        setIsJobRunning(false);
    }

    private void setIsJobRunning(boolean isJobRunning) {
        this.isJobRunning = isJobRunning;
        AppScene.setIsJobRunning(isJobRunning);
    }
}
