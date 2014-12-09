package com.tcc.audiofingerprintproj;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tcc.audiofingerprintproj.controller.DatabaseHandler;
import com.tcc.audiofingerprintproj.model.Musica;

public class MusicActivity extends Activity {
	DatabaseHandler db;
	String id = ""; 
	String cifraclub = "";

	TextView txtIdMusica;
	TextView txtArtista;
	TextView txtTitulo;
	EditText editUrl;
	Button btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music);
				
		editUrl = (EditText) findViewById(R.id.cipher);
		btn = (Button) findViewById(R.id.searchCipher);
		cifraclub = getResources().getString(R.string.urlSite);
		
		btn.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            	openUrl();
            }
        });
		
		if(getIntent().hasExtra("ID_MUSICA")){
			Bundle extras = getIntent().getExtras();
			id = extras.getString("ID_MUSICA");
			
			db = new DatabaseHandler(this);
			Musica m = db.Get_Music(Integer.parseInt(id));
			
			txtIdMusica = (TextView)findViewById(R.id.idMusica);
			txtArtista = (TextView)findViewById(R.id.artistName);
			txtTitulo = (TextView)findViewById(R.id.musicTitle);
			
			txtIdMusica.setText(m.getIdTitulo());
			txtArtista.setText(m.getArtista());
			txtTitulo.setText(m.getTitulo());
			
			editUrl.setText(makeUrlCipher().toLowerCase());
		}	
	}
	
	protected void openUrl() {
    	Intent i = new Intent(Intent.ACTION_VIEW);
    	i.setData(Uri.parse(editUrl.getText().toString()));
    	startActivity(i);
	}

	private String makeUrlCipher() {
		String url = "";
		url = cifraclub.concat(makeArtistParam().concat(makeSongParam()));
		return url;
	}

	private String makeSongParam() {
		String song = txtTitulo.getText().toString();
		int i;
		for(i = 0; i < song.length(); i++) {
	        char ch = song.charAt(i);
	        if (ch == '(')
	            break;
	    }
		
		song = replaceWhiteSpace(replaceSpecialChars(song.substring(0, i).trim()));
		return song.concat("/");
	}
	
	private String makeArtistParam() {
		String artist = txtArtista.getText().toString();
		artist = replaceWhiteSpace(replaceSpecialChars(artist));
		return artist.concat("/");
	}
	
	private String replaceSpecialChars(String str) {
		return str.replaceAll("[|?*<\":>+\\[\\]/']", " ");
	}
	
	private String replaceWhiteSpace(String str) {
		return str.replace(" ", "-");
	}
	
	
}
