package com.esed.payer.caricaTabelleIoItalia.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class CaricaTabelleIoItaliaContext {

	private Properties config;
	private Logger logger;
	protected HashMap<String, List<String>> parameters = new HashMap<String, List<String>>();

	public CaricaTabelleIoItaliaContext() {
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public Properties getProperties() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public int addParameter(String name, String value) {
		if (!this.parameters.containsKey(name)) {
			this.parameters.put(name, new LinkedList<String>());
		}
		this.parameters.get(name).add(value); // Aggiunge un valore alla lista delle ripetizioni
		return this.parameters.get(name).size();

	}

	public String getParameter(String name) {
		if (parameters.containsKey(name))
			return (String) parameters.get(name).get(0);
		else
			return "";
	}

	public void loadSchedeBap(String[] params) {
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("\\s+");
			if (p[0].equals("END")) {
				if (p[1].trim().equals("")) {
					addParameter(p[0].trim(), "");
				} else {
					addParameter(p[0].trim(), p[1].trim());
				}
			} else {
				addParameter(p[0].trim(), p[1].trim());// Nome parametro - valore(Aggiunge Lista di valori per schede
														// con ripetizione)
			}
		}
	}

	public String formatDate(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public String formattaData(java.util.Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public String getDatasourceJDBCDriver(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCDriver.format(codiceUtente));
	}

	public String getDatasourceJDBCUrl(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCUrl.format(codiceUtente));
	}

	public String getDatasourceJDBCUser(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCUser.format(codiceUtente));
	}

	public String getDatasourceJDBCPassword(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceJDBCPassword.format(codiceUtente));
	}

	public String getDatasourceSchema(String codiceUtente) {
		return config.getProperty(PropertiesPath.datasourceSchema.format(codiceUtente));
	}

	public String getDirectoryIoItaliaCSV(String template) {
		return config.getProperty(PropertiesPath.directoryIoItaliaCSV.format(template));
	}

	public String getDirectoryIoItaliaCSVValidi(String template) {
		return config.getProperty(PropertiesPath.directoryIoItaliaCSVValidi.format(template));
	}

	public String getDirectoryIoItaliaCSVScartati(String template) {
		return config.getProperty(PropertiesPath.directoryIoItaliaCSVScartati.format(template));
	}

	public String getTemplate(String cutecute) {
		return config.getProperty(PropertiesPath.templateName.format(cutecute));
	}

	public String getWsEmailSenderEndpointURL() {
		return config.getProperty(PropertiesPath.emailSender.format());
	}

	public String getOggettoEmailReport() {
		return config.getProperty(PropertiesPath.recap.format());
	}
}
