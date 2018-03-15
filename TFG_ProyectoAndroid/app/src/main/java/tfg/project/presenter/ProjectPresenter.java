package tfg.project.presenter;

import tfg.project.model.Model;
import tfg.project.model.ProjectModel;
import tfg.project.view.View;


public class ProjectPresenter implements Presenter{

    private Model model;
    private View view;

    public ProjectPresenter (View v){
        this.view = v;
        this.model = new ProjectModel(this);
    }
}
