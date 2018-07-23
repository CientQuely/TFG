package uib.tfg.project.model;

import uib.tfg.project.presenter.Presenter;

/**
 * Created by Micky on 15/03/2018.
 */

public class ProjectModel implements Model{

    private Presenter presenter;

    public ProjectModel(Presenter p){
        this.presenter = p;
    }
}
