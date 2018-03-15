package tfg.project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tfg.project.presenter.Presenter;
import tfg.project.presenter.ProjectPresenter;
import tfg.project.view.View;
import tfg.proyecto.R;

public class ProjectView extends AppCompatActivity implements View {

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyecto);

        presenter = new ProjectPresenter(this);
    }
}
