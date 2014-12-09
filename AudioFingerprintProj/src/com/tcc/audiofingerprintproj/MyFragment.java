package com.tcc.audiofingerprintproj;
 
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.audiofingerprintproj.controller.DatabaseHandler;
import com.tcc.audiofingerprintproj.model.Musica;

import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;
 
public class MyFragment extends Fragment implements AudioFingerprinterListener
{
	String Toast_msg = "";
	DatabaseHandler db;
	boolean recording;
	AudioFingerprinter fingerprinter;
	TextView status;
	Button btn;
	MyFragmentList lst;
	
	public MyFragment(MyFragmentList frag){
		lst = frag;		
	}
	
	public MyFragment(){
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.myfragment_layout, container, false);
    	db = new DatabaseHandler(getActivity());
    	btn = (Button) v.findViewById(R.id.recordButton);
        status = (TextView) v.findViewById(R.id.status);
        btn.setOnClickListener(onClick);
    	
    	return v;
    }
    
    private OnClickListener onClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			getActivity().runOnUiThread(new Runnable() {
                public void run() {
                	if(recording) {            		 
            			fingerprinter.stop();        			
                	}
                	else {            		
                		if(fingerprinter == null)
                			fingerprinter = new AudioFingerprinter(MyFragment.this);
                		
                		fingerprinter.fingerprint(15);
                	}
                }
			});
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
		db = new DatabaseHandler(getActivity());
		Musica m = new Musica();
		m.setIdTitulo(table.get("id"));
		m.setArtista(table.get("artist_name"));
		m.setTitulo(table.get("title"));
		m.setScore(Double.parseDouble(table.get("score")));
		
		db.Add_Music(m);
		lst.atualizaLista(m);
		status.setText("Aguardando...");
			
		Intent i = new Intent(getActivity(), MusicActivity.class);
		i.putExtra("ID_MUSICA", String.valueOf(lst.getContador()));
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
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}
}