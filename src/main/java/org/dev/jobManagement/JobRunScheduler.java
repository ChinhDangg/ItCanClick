package org.dev.jobManagement;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.concurrent.Task;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.JobController.JobDataController;
import org.dev.RunJob.JobRunController;

public class JobRunScheduler implements NativeKeyListener {
    @Getter
    private boolean isJobRunning = false;
    @Getter
    private JobRunStructure currentJobRunStructure;
    private static Thread runJobThread = null;
    private boolean isKeyListening = false;

    private final String className = this.getClass().getSimpleName();

    public void startJobRun(JobDataController jobDataController) {
        if (jobDataController == null)
            return;
        if (isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Fail to start - as another job is running");
            return;
        }
        registerKeyListener();
        setIsJobRunning(true);
        AppScene.addLog(LogLevel.INFO, className, "Starting to run job");
        Task<Void> runJobTask = getRunJobTask(jobDataController);
        runJobThread = new Thread(runJobTask);
        AppScene.displayCurrentRunJobNode();
        AppScene.loadRunSideMenuHierarchy();
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
                    unregisterKeyListener();
                } catch (Exception e) {
                    AppScene.addLog(LogLevel.ERROR, className, "Error to start run job");
                }
                return null;
            }
        };
    }

    public void stopRunJob() {
        AppScene.addLog(LogLevel.INFO, className, "Stopping running job");
        runJobThread.interrupt();
        setIsJobRunning(false);
        unregisterKeyListener();
    }

    private void setIsJobRunning(boolean isJobRunning) {
        this.isJobRunning = isJobRunning;
        AppScene.setIsJobRunning(isJobRunning);
    }

    private void registerKeyListener() {
        if (isKeyListening)
            return;
        GlobalScreen.addNativeKeyListener(this);
        isKeyListening = true;
        AppScene.addLog(LogLevel.TRACE, className, "Registering key listener");
    }

    private void unregisterKeyListener() {
        if (!isKeyListening)
            return;
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.TRACE, className, "Unregistering key listener");
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            AppScene.addLog(LogLevel.INFO, className, "Hot key clicked to stop running job");
            stopRunJob();
        }
    }
}
