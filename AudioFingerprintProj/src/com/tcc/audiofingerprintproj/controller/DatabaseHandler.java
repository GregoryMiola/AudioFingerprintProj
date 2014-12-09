package com.tcc.audiofingerprintproj.controller;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tcc.audiofingerprintproj.model.Musica;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	// Versão do banco
	private static final int DATABASE_VERSION = 1;
	
	// Nome do banco
	private static final String DATABASE_NAME = "musicasReconhecidas";
	
	// Nome da tabela Música
	private static final String TABLE_MUSICS = "musicas";

	// Nomes das colunas na tabela Músicas
	private static final String KEY_ID = "id";
	private static final String KEY_ID_TITLE = "id_titulo";
	private static final String KEY_TITLE = "titulo";
	private static final String KEY_ARTIST = "artista";
	private static final String KEY_SCORE = "score";
	private final ArrayList<Musica> music_list = new ArrayList<Musica>();
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MUSICS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_ID_TITLE + " TEXT,"
				+ KEY_TITLE + " TEXT," + KEY_ARTIST + " TEXT," 
				+ KEY_SCORE + " DECIMAL" + ")";
		
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Dropa a tabela antiga caso exista
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSICS);

		// Cria a tabela novamente
		onCreate(db);
	}

	/**
	* Todas operações CRUD(Create, Read, Update, Delete) 
	*/
	
	// Adiciona novas músicas
	public void Add_Music(Musica music) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(KEY_ID_TITLE, music.getIdTitulo()); // ID do Titulo
		values.put(KEY_TITLE, music.getTitulo()); // Titulo
		values.put(KEY_ARTIST, music.getArtista()); // Artista
		values.put(KEY_SCORE, music.getScore()); // Score
		
		db.insert(TABLE_MUSICS, null, values);
		db.close(); // Fecha conexão
	}
	
	// Retorna uma música
	public Musica Get_Music(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_MUSICS, new String[] { KEY_ID, KEY_ID_TITLE, KEY_TITLE, KEY_ARTIST, KEY_SCORE }, 
				KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		Musica music = new Musica(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), 
				cursor.getString(3), Double.parseDouble(cursor.getString(4)));

		cursor.close();
		db.close();
		return music;
	}
	
	// Retorna todas músicas
	public ArrayList<Musica> Get_Musics() {
		try {
			music_list.clear();
			String selectQuery = "SELECT * FROM " + TABLE_MUSICS;
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor.moveToFirst()) {
				do {
					Musica music = new Musica();
					music.setId(Integer.parseInt(cursor.getString(0)));
					music.setIdTitulo(cursor.getString(1));
					music.setTitulo(cursor.getString(2));
					music.setArtista(cursor.getString(3));
					music.setScore(Double.parseDouble(cursor.getString(4)));

					music_list.add(music);
				} 
				while (cursor.moveToNext());
			}

			cursor.close();
			db.close();
			return music_list;
		} catch (Exception e) {
		
			Log.e("all_musics", "" + e);
		}
		return music_list;
	}
	
	// Atualiza uma música
	public int Update_Music(Musica music) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put(KEY_ID_TITLE, music.getIdTitulo()); // ID do Titulo
		values.put(KEY_TITLE, music.getTitulo()); // Titulo
		values.put(KEY_ARTIST, music.getArtista()); // Artista
		values.put(KEY_SCORE, music.getScore()); // Score
		
		return db.update(TABLE_MUSICS, values, KEY_ID + " = ?",
		new String[] { String.valueOf(music.getId()) });
	}
	
	// Deleta uma música
	public void Delete_Music(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MUSICS, KEY_ID + " = ?",
		new String[] { String.valueOf(id) });
		db.close();
	}
	
	// Contador de músicas registradas
	public int Get_Total_Musics() {
		String countQuery = "SELECT * FROM " + TABLE_MUSICS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();
	}
}
