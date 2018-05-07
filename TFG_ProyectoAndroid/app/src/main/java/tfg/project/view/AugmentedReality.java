package tfg.project.view;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;

import tfg.proyecto.R;
import tfg.project.presenter.*;

public class AugmentedReality extends Activity implements View{

   Presenter presentador;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Escondemos el título
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Escondemos la barra de notificaciones
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_augmented_reality);

        presentador = new ProjectPresenter(this);
    }


}
