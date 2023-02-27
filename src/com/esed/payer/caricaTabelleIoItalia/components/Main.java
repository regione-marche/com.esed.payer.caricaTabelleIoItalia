package com.esed.payer.caricaTabelleIoItalia.components;

import org.apache.log4j.Logger;

import com.esed.payer.caricaTabelleIoItalia.config.CaricaTabelleIoItaliaResponse;
import com.seda.bap.components.core.spi.ClassPrinting;

public class Main {

	public static void main(String[] args) {

		if (args == null || args.length <= 0) {
			System.err.println("Usage:  com.esed.payer.caricaTabelleIoItalia.components.Main [-f <fileConfigurazione>] [-c <codiceUtente>]");
			System.err.println("Options:"); 
			System.err.println("-f: File di configurazione del batch");
			System.err.println("-c: codice utente");  		
			System.exit(1);
		} else {
			try {
				String [] params = parseArgs(args);
				if (params == null) {
					System.exit(1);
				}
				CaricaTabelleIoItaliaCore core = new CaricaTabelleIoItaliaCore();
				ClassPrinting classPrinting = null;
				Logger logger = null;
				String jobId = "";
				
				CaricaTabelleIoItaliaResponse res;
				res= core.run(params, null, null, classPrinting, logger, jobId);
				if (!res.getCode().equals("00")) {
					System.exit(1);
				} else {
					System.exit(0);
				}
			} catch (Exception e) {
				System.err.println("Errore nella procedura: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}	
	}
	

	public static String[] parseArgs(String[] args) {
		String[] params = null;
		String sParam = null;
		String codUtente = null;
		String pathConfig = null;
		boolean ret = true;
		boolean bOk = true;
		int f=0;
		int c=0;
		for (int i = 0; i < args.length && bOk; i++) {
			if (args[i].length() > 1 && (args[i].charAt(0) == '-')) {
				if (i == args.length - 1) {
					ret = false;
					break;
				}
				switch (args[i].toLowerCase().charAt(1)) {
					case 'f':
						f++;
						sParam = args[++i];
						if ( null == sParam || 0 >= sParam.trim().length() ) {
							System.err.println("parametro -f mancante. Parametro obbligatorio");
							ret = bOk = false;
						} else{
							pathConfig = sParam;
						}
						break;
					case 'c':
						c++;
						sParam = args[++i];
						if ( null == sParam || 0 >= sParam.trim().length() ) {
							System.err.println("parametro -c mancante. Parametro obbligatorio");
							ret = bOk = false;
						} else{
							codUtente = sParam;
						}
						break;
					default:
						ret = false;
					break;
				}
			}
		}
		
		if(f==0){
			System.err.println("Il parametro -f è obbligatorio");
			ret=false;
		}
		if(c==0){
			System.err.println("Il parametro -c è obbligatorio");
			ret=false;
		}
		
		if (ret) {
			params = new String[]{ 
					"CONFIGPATH      " + pathConfig,
					"CUTECUTE      " + codUtente
					};
		}
		
		return params;
	}


}
