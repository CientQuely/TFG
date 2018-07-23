package uib.tfg.project.presenter;

import uib.tfg.project.model.Model;
import uib.tfg.project.model.ProjectModel;
import uib.tfg.project.view.View;


public class ProjectPresenter implements Presenter{

    private Model model;
    private View view;

    public ProjectPresenter (View v){
        this.view = v;
        this.model = new ProjectModel(this);
    }
}
