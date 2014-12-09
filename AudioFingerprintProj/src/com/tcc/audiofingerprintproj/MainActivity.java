package com.tcc.audiofingerprintproj;

import java.util.Hashtable;
import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.audiofingerprintproj.controller.DatabaseHandler;
import com.tcc.audiofingerprintproj.model.Musica;

import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements AudioFingerprinterListener 
{	
	DatabaseHandler db;
	List<Musica> listaMusicas = null;
	AdaptadorMusica adaptador = null;
	String Toast_msg = "";
	
	boolean recording;
	AudioFingerprinter fingerprinter;
	TextView status;
	Button btn;
		
    @Override       
    public void onCreate(Bundle savedInstanceState) 
    {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btn = (Button) findViewById(R.id.recordButton);
        
        status = (TextView) findViewById(R.id.status);
        btn.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                // Perform action on click
            	if(recording)
            	{            		 
        			fingerprinter.stop();        			
            	}
            	else
            	{            		
            		if(fingerprinter == null)
            			fingerprinter = new AudioFingerprinter(MainActivity.this);
            		
            		fingerprinter.fingerprint(15);
            	}
            }
        });
        
        db = new DatabaseHandler(this);
        listaMusicas = db.Get_Musics();
        
        ListView lista = (ListView) findViewById(R.id.listaMusicas);		
		adaptador = new AdaptadorMusica();
		lista.setAdapter(adaptador);
		lista.setOnItemClickListener(onListClick);
		
        TabSpec descritor = getTabHost().newTabSpec("tag1");
        descritor.setContent(R.id.capturaMusicas);
        descritor.setIndicator("Captura", getResources().getDrawable(R.drawable.busca));
        getTabHost().addTab(descritor);
         
        descritor = getTabHost().newTabSpec("tag2");
        descritor.setContent(R.id.listaMusicas);
        descritor.setIndicator("Lista de Músicas", getResources().getDrawable(R.drawable.lista));
        getTabHost().addTab(descritor);
         
        getTabHost().setCurrentTab(0);        
    }
    
    private OnItemClickListener onListClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			
			Intent i = new Intent(MainActivity.this, MusicActivity.class);
			i.putExtra("ID_MUSICA", String.valueOf(listaMusicas.get(position).getId()));
			startActivity(i);
		}
	};

	public void didFinishListening() 
	{					
		btn.setText("Capturar");
		status.setText("Aguardando...");
		
		recording = false;
	}
	
	public void didFinishListeningPass()
	{}

	public void willStartListening() 
	{
		status.setText("Capturando...");
		btn.setText("Parar");
		recording = true;
	}

	public void willStartListeningPass() 
	{}

	public void didGenerateFingerprintCode(String code) 
	{
		status.setText("Buscando informações para o código:\n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFindMatchForCode(final Hashtable<String, String> table, String code) 
	{
		db = new DatabaseHandler(this);
		Musica m = new Musica();
		m.setIdTitulo(table.get("id"));
		m.setArtista(table.get("artist_name"));
		m.setTitulo(table.get("title"));
		m.setScore(Double.parseDouble(table.get("score")));
		
		db.Add_Music(m);
		adaptador.add(m);
		status.setText("Aguardando...");
			
		Intent i = new Intent(MainActivity.this, MusicActivity.class);
		i.putExtra("ID_MUSICA", String.valueOf(adaptador.getCount()));
		startActivity(i);
	}

	public void didNotFindMatchForCode(String code) 
	{
		Toast_msg = "Nenhuma informação retornada para o código: \n" + code.substring(0, Math.min(50, code.length()));
		Show_Toast(Toast_msg);
	}

	public void didFailWithException(Exception e) 
	{
		status.setText("Aguardando...");
		Toast_msg = "Error: " + e;
		Show_Toast(Toast_msg);
	}
	
	public void Show_Toast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
	
	class AdaptadorMusica extends ArrayAdapter<Musica> {
		
		public AdaptadorMusica() {
			super(MainActivity.this, R.layout.linha, listaMusicas);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View linha = convertView;
			ArmazenadorMusica armazenador = null;
			
			if (linha == null) {
				LayoutInflater inflater = getLayoutInflater();
				linha = inflater.inflate(R.layout.linha, parent, false);
				armazenador = new ArmazenadorMusica(linha);
				linha.setTag(armazenador);
			} else {
				armazenador = (ArmazenadorMusica) linha.getTag();
			}
			
			armazenador.popularFormulario(listaMusicas.get(position));
			
			return linha;
		}
	}
	
	static class ArmazenadorMusica {
		private TextView titulo = null;
		private TextView artista = null;
		private ImageView icone = null;
		
		ArmazenadorMusica(View linha) {
			titulo = (TextView) linha.findViewById(R.id.titulo);
			artista = (TextView) linha.findViewById(R.id.artista);
			icone = (ImageView) linha.findViewById(R.id.icone);
		}
		
		void popularFormulario(Musica m) {
			titulo.setText(m.getTitulo());
			artista.setText(m.getArtista());
			icone.setImageResource(R.drawable.musica);
		}
	}
}