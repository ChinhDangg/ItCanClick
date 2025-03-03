package org.dev.jobManagement;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;

import java.util.*;

public class JobReferenceHolder {

    HashMap<JobData, HashSet<JobStructure>> holder = new HashMap<>();

    public void addNewJobReference(JobData refData, JobStructure beingRef) {
        if (!holder.containsKey(refData)) {
            HashSet<JobStructure> set = new HashSet<>();
            set.add(beingRef);
            refData.setRef(true);
            holder.put(refData, set);
        }
        beingRef.markLabelAsRef();
    }

    public void addKnownJobReference(JobData refData, JobStructure knownRefJobStructure) {
        if (!refData.isRef())
            return;
        if (holder.containsKey(refData))
            holder.get(refData).add(knownRefJobStructure);
        else {
            HashSet<JobStructure> set = new HashSet<>();
            set.add(knownRefJobStructure);
            holder.put(refData, set);
        }
        knownRefJobStructure.markLabelAsRef();
    }

    public void removeJobReference(JobData refData, JobStructure toRemove) {
        HashSet<JobStructure> set = holder.get(refData);
        if (set == null)
            return;
        set.remove(toRemove);
        if (set.size() == 1) {
            for(JobStructure ref : set)
                ref.unmarkLabelAsRef();
            holder.remove(refData);
        }
        else if (set.isEmpty())
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Should not be empty");
    }
}
