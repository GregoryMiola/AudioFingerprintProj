package com.tcc.audiofingerprintproj;
 
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tcc.audiofingerprintproj.controller.DatabaseHandler;
import com.tcc.audiofingerprintproj.model.Musica;
 
public class MyFragmentList extends Fragment
{
    DatabaseHandler db;
	List<Musica> listaMusicas = null;
	AdaptadorMusica adaptador = null;
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
     }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.myfragmentlist_layout, container, false);
    	db = new DatabaseHandler(getActivity());
        listaMusicas = db.Get_Musics();
        ListView lista = (ListView) v.findViewById(R.id.listaMusicas);	
        adaptador = new AdaptadorMusica();
		lista.setAdapter(adaptador);
		lista.setOnItemClickListener(onListClick);
        
    	return v;
    }
	
	private OnItemClickListener onListClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			
			Intent i = new Intent(getActivity(), MusicActivity.class);
			i.putExtra("ID_MUSICA", String.valueOf(listaMusicas.get(position).getId()));
			startActivity(i);
		}
	};
	
	public void atualizaLista(Musica m){
		adaptador.add(m);
	}
	
	public int getContador(){
		return adaptador.getCount();
	}
 
	class AdaptadorMusica extends ArrayAdapter<Musica> {
		
		public AdaptadorMusica() {
			super(getActivity(), R.layout.linha, listaMusicas);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View linha = convertView;
			ArmazenadorMusica armazenador = null;
			
			if (linha == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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