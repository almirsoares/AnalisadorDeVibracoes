package br.com.poli.departamentoeletronica.analisadordevibracoes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Sensor accelerometer;
    SensorManager sensorManager;

    private Button salvar;

    private int conta;

    private int STOREGE_PERMISSON_CODE = 1;

    private String texto="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            pedirPermissao();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private float sensorX;
    private float sensorY;
    private float sensorZ;
    private float sensorXAnt=0;
    private float sensorYAnt=0;
    private float sensorZAnt=0;
    private float primeirosX = 0;
    private float primeirosY = 0;
    private float primeirosZ = 0;

    private long tempoInicio, tempoPassado;

    private String str;
    private float tempoDecorrido;
    private float tempoDecorridoAnt;

    String quebraLinha = System.getProperty("line.separator");

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor accelerometer;
        SensorManager sensorManager;

        ProgressBar carregando= (ProgressBar) findViewById(R.id.carregandoPBId);
        TextView tx = (TextView) findViewById(R.id.tVAceXId);
        TextView ty = (TextView) findViewById(R.id.tVAceYId);
        TextView tz = (TextView) findViewById(R.id.tVAceZId);

        sensorX = event.values[0];
        sensorY = event.values[1];
        sensorZ = event.values[2];

        if (conta < 1) {
            tempoInicio = System.currentTimeMillis();
            primeirosX = sensorX;
            primeirosY = sensorY;
            primeirosZ = sensorZ;

        }else if(conta <100){
            primeirosX += sensorX;
            primeirosY += sensorY;
            primeirosZ += sensorZ;

            carregando.setProgress(conta);
        }
        else if(conta==100){
            tempoPassado = System.currentTimeMillis() - tempoInicio;
            str = Long.toString(tempoPassado);
            tempoDecorridoAnt = Float.valueOf(str).floatValue();

            primeirosX = primeirosX/100;
            primeirosY = primeirosY/100;
            primeirosZ = primeirosZ/100;

            TextView aceleracao = (TextView) findViewById(R.id.textViewAceleracaoId);
            aceleracao.setText("Aceleração");

            TextView velocidade = (TextView) findViewById(R.id.textViewVelocidadeId);
            velocidade.setText("Velocidade");

            tx.setVisibility(View.VISIBLE);
            ty.setVisibility(View.VISIBLE);
            tz.setVisibility(View.VISIBLE);
            salvar.setVisibility(View.VISIBLE);
            carregando.setVisibility(View.INVISIBLE);
        }else{

            sensorX -= primeirosX;
            sensorY -= primeirosY;
            sensorZ -= primeirosZ;

            tx.setText("X : " + formatarFloat(sensorX));
            ty.setText("Y : " + formatarFloat(sensorY));
            tz.setText("Z : " + formatarFloat(sensorZ));

            tempoPassado = System.currentTimeMillis() - tempoInicio;

            str = Long.toString(tempoPassado);
            tempoDecorrido = Float.valueOf(str).floatValue();


            calculaVelocidade((tempoDecorrido-tempoDecorridoAnt)/1000 , sensorXAnt, sensorYAnt, sensorZAnt, sensorX, sensorY, sensorZ);

            texto += tempoDecorrido/1000 + ";" + formatarFloat(sensorX) + ";" + formatarFloat(sensorY) + ";" + formatarFloat(sensorZ)
                    + ";" + formatarFloat(velocidadeX) + ";" + formatarFloat(velocidadeY) + ";" + formatarFloat(velocidadeZ)
                    +  quebraLinha;
            tempoDecorridoAnt = tempoDecorrido;
        }
        conta++;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float velocidadeX = 0;
    private float velocidadeY = 0;
    private float velocidadeZ = 0;


    public void calculaVelocidade(float tempo, float xAnt, float yAnt, float zAnt, float xAtu, float yAtu, float zAtu){

        TextView velX = (TextView) findViewById(R.id.tVVelXId);
        velX.setVisibility(View.VISIBLE);

        TextView velY = (TextView) findViewById(R.id.tVVelYId);
        velY.setVisibility(View.VISIBLE);

        TextView velZ = (TextView) findViewById(R.id.tVVelZId);
        velZ.setVisibility(View.VISIBLE);

        velocidadeX += (xAtu + xAnt) * tempo / 2;
        velocidadeY += (yAtu + yAnt) * tempo / 2;
        velocidadeZ += (zAtu + zAnt) * tempo / 2;

        velX.setText("X : " + formatarFloat(velocidadeX));
        velY.setText("Y : " + formatarFloat(velocidadeY));
        velZ.setText("Z : " + formatarFloat(velocidadeZ));
    }

    public String formatarFloat(float numero) {
        String retorno = "";
        DecimalFormat formatter = new DecimalFormat("0.000");
        try {
            numero *=1000;
            retorno = formatter.format(numero);
        } catch (Exception ex) {
            System.err.println("Erro ao formatar numero: " + ex);
        }
        return retorno;
    }

    private File diretorio;
    private String nomeDiretorio = "acelerometro";
    private String diretorioApp;
    private String nomeArquivo = "acelerometro";

    private void salvaTXT(String texto){

        Date data = new Date();

        diretorioApp = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/"+nomeDiretorio+"/";

        diretorio = new File(diretorioApp);
        diretorio.mkdirs();

        String textoCompleto = "TEMPO (segundos);AceleraçãoX(mm/s²);AceleraçãoY(mm/s²);AceleraçãoZ(mm/s²); " +
                "VelecidadeX(mm/s²); VelecidadeY(mm/s²); VelecidadeZ(mm/s²)" + quebraLinha + texto;


        //Quando o File() tem um parâmetro ele cria um diretório.
//Quando tem dois ele cria um arquivo no diretório onde é informado.
        File fileExt = new File(diretorioApp, nomeArquivo + " "+ data +".txt");

        fileExt.getParentFile().mkdirs();//Cria o arquivo

        FileOutputStream fosExt = null;//Abre o arquivo

        try {
            fosExt = new FileOutputStream(fileExt);
            fosExt.write(textoCompleto.getBytes());//Escreve no arquivo
            fosExt.close();//Obrigatoriamente fecha
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STOREGE_PERMISSON_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissao garantida", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Permissão não concedida", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pedirPermissao(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            new AlertDialog.Builder(this)
                    .setTitle("Permissão")
                    .setMessage("Voce permite salvar dados na memoria interna do dispositivo?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STOREGE_PERMISSON_CODE);
                        }
                    }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

        } else{
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STOREGE_PERMISSON_CODE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        final Toast toast = new Toast(this);

        salvar = (Button) findViewById(R.id.botaoSalvarId);
        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvaTXT(texto);
                toast.makeText(MainActivity.this, "As informações foram salvas!", Toast.LENGTH_SHORT).show();
            }
        });
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    protected void onPause() {
        sensorManager.unregisterListener((SensorEventListener) this);
        super.onPause();
    }


}
