package uib.tfg.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import uib.tfg.project.presenter.Presenter;
import uib.tfg.project.view.AugmentedReality;
import uib.tfg.project.view.View;

public class ProjectView extends Activity {

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Escondemos el título
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Escondemos la barra de notificaciones
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.menu);
    }

    public void onStartClick(android.view.View view) {

        Intent ar = new Intent(this, AugmentedReality.class);
        startActivity(ar);

    }
}
