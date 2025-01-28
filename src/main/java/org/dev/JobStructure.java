package org.dev;

import org.dev.JobController.JobDataController;

import java.util.List;

public class JobStructure {

    private JobDataController displayParentController;
    private JobDataController parentController;
    private JobDataController currentController;

    private List<JobStructure> subStructures;

}
