package org.dev.Operation;

import org.dev.Operation.Data.AppData;

public interface DataController extends MainJobController{

    AppData getSavedData();

    void loadSavedData(AppData appData);

    void addSavedData(AppData data);

    void removeSavedData(DataController dataController);
}
