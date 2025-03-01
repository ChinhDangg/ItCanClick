package org.dev.jobManagement;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JobReferenceHolder {

    HashMap<JobData, List<JobStructure>> holder = new HashMap<>();

    public void addJobReference(JobData refData, JobStructure beingRef, JobStructure newRef) {
        if (holder.containsKey(refData))
            holder.get(refData).add(newRef);
        else {
            List<JobStructure> list = new ArrayList<>();
            list.add(beingRef);
            list.add(newRef);
            refData.setRef(true);
            holder.put(refData, list);
        }
        beingRef.markLabelAsRef();
        newRef.markLabelAsRef();
    }

    public void removeJohReference(JobData refData, JobStructure toRemove) {
        List<JobStructure> list = holder.get(refData);
        if (list == null)
            return;
        list.remove(toRemove);
        if (list.size() == 1) {
            list.getFirst().unmarkLabelAsRef();
            holder.remove(refData);
        }
        else if (list.isEmpty())
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Should not be empty");
    }
}
