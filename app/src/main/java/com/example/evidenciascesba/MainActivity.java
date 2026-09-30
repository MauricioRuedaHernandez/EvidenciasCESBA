package com.example.evidenciascesba;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.core.content.FileProvider;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // COMPONENTES DE LA INTERFAZ
    private ImageView imgFoto;
    private EditText edtTitulo;
    private EditText edtDescripcion;

    private Button btnTomarFoto;
    private Button btnGuardar;

    private TextView txtEstado;

    // VARIABLE PARA SABER SI EXISTE UNA FOTO
    private Bitmap fotografiaBitmap = null;
    private String fechaHoraCaptura = "";

    private TextView txtContador;
    private Button btnAnterior;
    private Button btnSiguiente;
    private Button btnCompartir;

    // VARIABLES PARA HISTORIAL Y CONTADOR
    private ArrayList<Bitmap> historialFotos = new ArrayList<>();
    private ArrayList<String> historialFechas = new ArrayList<>();
    private int indiceActual = -1; // Rastrea qué foto estamos viendo
    private int contadorFotos = 0;

    /*
     * ActivityResultLauncher
     *
     * Se encarga de recibir el resultado
     * después de utilizar la cámara.
     */
    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(

                    new ActivityResultContracts.StartActivityForResult(),

                    resultado -> {

                        /*
                         * Verificamos si la cámara terminó
                         * correctamente.
                         */
                        if (resultado.getResultCode() == RESULT_OK) {

                            Intent datos = resultado.getData();

                            if (datos != null &&
                                    datos.getExtras() != null) {

                                /*
                                 * Recuperamos la miniatura
                                 * generada por la cámara.
                                 */
                                fotografiaBitmap =
                                        (Bitmap) datos
                                                .getExtras()
                                                .get("data");

                                // OBTENER FECHA Y HORA ACTUAL
                                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                fechaHoraCaptura = formato.format(new Date());

                                historialFotos.add(fotografiaBitmap);
                                historialFechas.add(fechaHoraCaptura);
                                contadorFotos++;
                                indiceActual = historialFotos.size() - 1; // Apuntar a la foto más reciente

                                // MOSTRAR DATOS EN LA INTERFAZ
                                txtEstado.setText("Capturada el: " + fechaHoraCaptura);
                                txtContador.setText("Fotos tomadas: " + contadorFotos);

                                /*
                                 * Mostramos la fotografía
                                 * en el ImageView.
                                 */
                                imgFoto.setImageBitmap(
                                        fotografiaBitmap
                                );

                                txtEstado.setText(
                                        "Fotografía tomada correctamente"
                                );

                                Toast.makeText(
                                        MainActivity.this,
                                        "Fotografía recibida",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {

                            txtEstado.setText(
                                    "La fotografía fue cancelada"
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Operación cancelada",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // CONECTAR JAVA CON XML

        imgFoto =findViewById(R.id.imgFoto);
        edtTitulo =findViewById(R.id.edtTitulo);
        edtDescripcion =findViewById(R.id.edtDescripcion);
        btnTomarFoto =findViewById(R.id.btnTomarFoto);
        btnGuardar =findViewById(R.id.btnGuardar);
        txtEstado =findViewById(R.id.txtEstado);
        txtContador = findViewById(R.id.txtContador);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnCompartir = findViewById(R.id.btnCompartir);

        /*
         * BOTONES DE HISTORIAL Y COMPARTIR
         */
        btnAnterior.setOnClickListener(v -> mostrarFotoAnterior());
        btnSiguiente.setOnClickListener(v -> mostrarFotoSiguiente());
        btnCompartir.setOnClickListener(v -> compartirFoto());

        /*
         * BOTÓN TOMAR FOTOGRAFÍA
         */
        btnTomarFoto.setOnClickListener(v -> {

            abrirCamara();

        });


        /*
         * BOTÓN GUARDAR
         */
        btnGuardar.setOnClickListener(v -> {

            guardarEvidencia();

        });
    }


    /*
     * MÉTODO PARA ABRIR LA CÁMARA
     */
    private void abrirCamara() {

        /*
         * Creamos un Intent.
         *
         * ACTION_IMAGE_CAPTURE indica que queremos
         * utilizar una aplicación capaz de
         * tomar fotografías.
         */
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        /*
         * Verificamos que exista una aplicación
         * capaz de utilizar este Intent.
         */
        if (intentCamara.resolveActivity(
                getPackageManager()) != null) {

            launcherCamara.launch(
                    intentCamara
            );

        } else {

            Toast.makeText(
                    this,
                    "No se encontró una aplicación de cámara",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    /*
     * MÉTODO PARA VALIDAR LA EVIDENCIA
     */
    private void guardarEvidencia() {

        String titulo =
                edtTitulo
                        .getText()
                        .toString()
                        .trim();

        String descripcion =
                edtDescripcion
                        .getText()
                        .toString()
                        .trim();


        // VALIDAR TÍTULO

        if (titulo.isEmpty()) {

            edtTitulo.setError(
                    "Escribe un título"
            );

            edtTitulo.requestFocus();

            return;
        }


        // VALIDAR DESCRIPCIÓN

        if (descripcion.isEmpty()) {

            edtDescripcion.setError(
                    "Escribe una descripción"
            );

            edtDescripcion.requestFocus();

            return;
        }


        // VALIDAR FOTOGRAFÍA

        if (fotografiaBitmap == null) {

            Toast.makeText(
                    this,
                    "Primero debes tomar una fotografía",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        /*
         * TODAS LAS VALIDACIONES
         * FUERON CORRECTAS.
         */

        txtEstado.setText(
                "Evidencia registrada correctamente"
        );


        Toast.makeText(
                this,
                "Evidencia guardada",
                Toast.LENGTH_LONG
        ).show();
    }

    private void mostrarFotoAnterior() {
        if (indiceActual > 0) {
            indiceActual--;
            actualizarVistaHistorial();
        } else {
            Toast.makeText(this, "No hay fotos anteriores", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarFotoSiguiente() {
        if (indiceActual < historialFotos.size() - 1) {
            indiceActual++;
            actualizarVistaHistorial();
        } else {
            Toast.makeText(this, "Esta es la foto más reciente", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarVistaHistorial() {
        if (indiceActual >= 0 && indiceActual < historialFotos.size()) {
            fotografiaBitmap = historialFotos.get(indiceActual);
            fechaHoraCaptura = historialFechas.get(indiceActual);

            imgFoto.setImageBitmap(fotografiaBitmap);
            txtEstado.setText("Capturada el: " + fechaHoraCaptura);
        }
    }

    /*
     * MÉTODO PARA COMPARTIR LA FOTO ACTUAL
     */
    private void compartirFoto() {
        if (fotografiaBitmap == null) {
            Toast.makeText(this, "No hay ninguna foto para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 1. Crear un archivo temporal en la memoria caché
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "evidencia.png");
            FileOutputStream stream = new FileOutputStream(file);
            fotografiaBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // 2. Obtener la URI segura usando FileProvider
            Uri uriImagen = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            // 3. Lanzar la ventana de compartir de Android
            Intent intentCompartir = new Intent(Intent.ACTION_SEND);
            intentCompartir.setType("image/png");
            intentCompartir.putExtra(Intent.EXTRA_STREAM, uriImagen);
            intentCompartir.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intentCompartir, "Compartir evidencia vía..."));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}