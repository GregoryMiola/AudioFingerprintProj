package com.tcc.audiofingerprintproj.model;

public class Musica {
	private int _id;
	private String _idTitulo = "";
	private String _titulo = "";
	private String _artista = "";
	private double _score;
	
	public Musica(int id, String idTitulo, String titulo, String artista, double score) {
		this._id = id;
		this._idTitulo = idTitulo;
		this._titulo = titulo;
		this._artista = artista;
		this._score = score;
	}

	public Musica() {
		
	}

	public int getId() {
		return _id;
	}
	
	public void setId(int id) {
		this._id = id;
	}
	
	public String getIdTitulo() {
		return _idTitulo;
	}
	public void setIdTitulo(String idTitulo) {
		this._idTitulo = idTitulo;
	}
	public String getTitulo() {
		return _titulo;
	}
	public void setTitulo(String titulo) {
		this._titulo = titulo;
	}
	public String getArtista() {
		return _artista;
	}
	public void setArtista(String artista) {
		this._artista = artista;
	}
	public double getScore() {
		return _score;
	}
	public void setScore(double score) {
		this._score = score;
	}
	
	@Override
	public String toString() {		
		return getTitulo();
	}
}
