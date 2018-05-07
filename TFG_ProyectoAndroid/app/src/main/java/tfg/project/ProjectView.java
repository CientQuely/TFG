package tfg.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import tfg.project.presenter.Presenter;
import tfg.project.view.AugmentedReality;
import tfg.project.view.View;
import tfg.proyecto.R;

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
