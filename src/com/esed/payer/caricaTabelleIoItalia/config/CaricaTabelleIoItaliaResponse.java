package com.esed.payer.caricaTabelleIoItalia.config;

public class CaricaTabelleIoItaliaResponse {

	private String code;
	private String message;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public CaricaTabelleIoItaliaResponse() {}
	public CaricaTabelleIoItaliaResponse(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	
	public String toString() {
		return "CaricaTabelleIoItaliaResponse [code="+code+
		" ,message="+message+"]";
	}
	
}
