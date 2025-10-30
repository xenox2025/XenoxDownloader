package cu.toohuman.xenoxdownloader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    
    private EditText urlInput;
    private Button downloadVideoBtn;
    private Button downloadAudioBtn;
    private TextView statusText;
    private TextView progressText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        urlInput = findViewById(R.id.urlInput);
        downloadVideoBtn = findViewById(R.id.downloadVideoBtn);
        downloadAudioBtn = findViewById(R.id.downloadAudioBtn);
        statusText = findViewById(R.id.statusText);
        progressText = findViewById(R.id.progressText);
        
        downloadVideoBtn.setOnClickListener(v -> downloadVideo());
        downloadAudioBtn.setOnClickListener(v -> downloadAudio());
    }
    
    private void downloadVideo() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusText.setText("Descargando video 480p...");
        progressText.setText("Iniciando descarga...");
        
        new DownloadTask("video", url).execute();
    }
    
    private void downloadAudio() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusText.setText("Descargando audio MP3...");
        progressText.setText("Iniciando descarga...");
        
        new DownloadTask("audio", url).execute();
    }
    
    private class DownloadTask extends AsyncTask<Void, String, Boolean> {
        private String type;
        private String url;
        
        DownloadTask(String type, String url) {
            this.type = type;
            this.url = url;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String command;
                if (type.equals("video")) {
                    command = "yt-dlp -f 'bestvideo[height<=480]+bestaudio/best[height<=480]' " +
                             "--merge-output-format mp4 " +
                             "--external-downloader aria2c " +
                             "--external-downloader-args '-x 5 -k 1M' " +
                             "-o '/sdcard/Download/%(title)s.%(ext)s' " + url;
                } else {
                    command = "yt-dlp -x --audio-format mp3 " +
                             "--external-downloader aria2c " +
                             "--external-downloader-args '-x 5 -k 1M' " +
                             "-o '/sdcard/Download/%(title)s.%(ext)s' " + url;
                }
                
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                
                while ((line = reader.readLine()) != null) {
                    publishProgress(line);
                }
                
                int exitCode = process.waitFor();
                return exitCode == 0;
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
            progressText.setText(values[0]);
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                statusText.setText("✓ Descarga completada");
                progressText.setText("Archivo guardado en /sdcard/Download/");
                Toast.makeText(MainActivity.this, "Descarga exitosa", Toast.LENGTH_LONG).show();
            } else {
                statusText.setText("✗ Error en la descarga");
                progressText.setText("Intenta de nuevo");
                Toast.makeText(MainActivity.this, "Error al descargar", Toast.LENGTH_LONG).show();
            }
        }
    }
}
