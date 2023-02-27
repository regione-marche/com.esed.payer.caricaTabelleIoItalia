package com.esed.payer.caricaTabelleIoItalia.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public enum PropertiesPath {

	directoryIoItaliaCSV, directoryIoItaliaCSVValidi, directoryIoItaliaCSVScartati, datasourceJDBCUrl,
	datasourceJDBCDriver, datasourceJDBCUser, datasourceJDBCPassword, datasourceSchema, templateName, emailSender,
	recap;

	private static ResourceBundle rb;

	/**
	 * @param args - Gli argomenti da passare come variabile al messaggio.
	 * @return il messaggio formattato
	 */
	public String format(Object... args) {
		synchronized (PropertiesPath.class) {
			if (rb == null)
				rb = ResourceBundle.getBundle(PropertiesPath.class.getName());
			return MessageFormat.format(rb.getString(name()), args);
		}
	}

}
