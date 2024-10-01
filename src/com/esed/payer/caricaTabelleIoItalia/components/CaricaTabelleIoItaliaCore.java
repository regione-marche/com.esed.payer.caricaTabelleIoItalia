package com.esed.payer.caricaTabelleIoItalia.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;

import com.esed.payer.caricaTabelleIoItalia.config.CaricaTabelleIoItaliaContext;
import com.esed.payer.caricaTabelleIoItalia.config.CaricaTabelleIoItaliaResponse;
import com.esed.payer.caricaTabelleIoItalia.utils.CheckFields;
import com.esed.payer.caricaTabelleIoItalia.utils.EMailSender;
import com.seda.bap.components.core.spi.ClassPrinting;
import com.seda.bap.components.core.spi.PrintCodes;
import com.seda.commons.properties.PropertiesLoader;
import com.seda.data.dao.DAOHelper;
import com.seda.data.datasource.DataSourceFactoryImpl;
import com.seda.emailsender.webservices.dati.EMailSenderResponse;
import com.seda.payer.core.bean.ConfigUtenteTipoServizioEnte;
import com.seda.payer.core.bean.IoItaliaConfigurazione;
import com.seda.payer.core.bean.IoItaliaMessaggio;
import com.seda.payer.core.bean.IoItaliaMessaggioBolzano;
import com.seda.payer.core.dao.ArchivioCarichiDao;
import com.seda.payer.core.dao.ConfigUtenteTipoServizioEnteDao;
import com.seda.payer.core.dao.IoItaliaDao;
import com.seda.payer.core.exception.DaoException;
import com.sun.rowset.WebRowSetImpl;

@SuppressWarnings("restriction")
public class CaricaTabelleIoItaliaCore {

	private static String PRINT_SYSOUT = "SYSOUT";
	Calendar cal = Calendar.getInstance();

	DataSource datasource;
	private ClassPrinting classPrinting;
	String schema;
	String jobId;
	private Connection connection;
	String lineSeparator = "============================================================================================";
	private String cutecute;
	private String rootAccettati;
	private String path;
	private String templateName;
	private String rootScartati;
	private int caricati = 0;
	private Map<Long, Integer> listConfig = new HashMap<Long, Integer>();
	private CaricaTabelleIoItaliaContext context;
	//inizio LP 20241001 - PAGONET-604
	private ArchivioCarichiDao archivioCarichiDao =  null;
	private ConfigUtenteTipoServizioEnteDao configUtenteTipoServizioEnteDao = null;
	private IoItaliaDao ioItaliaDao = null;;
	//fine LP 20241001 - PAGONET-604

	public CaricaTabelleIoItaliaResponse run(String[] params, DataSource datasource, String schema,
			ClassPrinting classPrinting, Logger logger, String jobId) {

		this.datasource = datasource;
		this.schema = schema;
		this.jobId = jobId;
		this.classPrinting = classPrinting;

		CaricaTabelleIoItaliaResponse res = new CaricaTabelleIoItaliaResponse();
		res.setCode("00");
		res.setMessage("Elaborazione completata con successo");

		try {
			preProcess(params);
			processMessaggi(params);
			postProcess(classPrinting);
		} catch (Exception e) {
			e.printStackTrace();
			printRow(PRINT_SYSOUT, "Elaborazione completata con errori " + e);
			printRow(PRINT_SYSOUT, lineSeparator);
			res.setCode("30");
			res.setMessage("Operazione terminata con errori ");
		//inizio LP 20241001 - PAGONET-604
		} finally {
			if(archivioCarichiDao !=  null) {
				archivioCarichiDao.destroy();
				archivioCarichiDao = null;
			}
			if(configUtenteTipoServizioEnteDao !=  null) {
				configUtenteTipoServizioEnteDao.destroy();
				configUtenteTipoServizioEnteDao = null;
			}
			if(ioItaliaDao !=  null) {
				ioItaliaDao.destroy();
				ioItaliaDao = null;
			}
		//fine LP 20241001 - PAGONET-604
		}
		return res;
	}

	private void postProcess(ClassPrinting classPrinting2) {

	}

	private void processMessaggi(String[] params) throws Exception {
		File folder = new File(path);
		printRow(PRINT_SYSOUT, lineSeparator);
		printRow(PRINT_SYSOUT, "Recupero files da:" + path);
		try {
			int counter = 1;
			for (File file : folder.listFiles()) {
				printRow(PRINT_SYSOUT, lineSeparator);
				printRow(PRINT_SYSOUT, "Lavoro file n°" + counter++);
				List<IoItaliaMessaggio> listaMex = new ArrayList<IoItaliaMessaggio>();
				List<IoItaliaMessaggioBolzano> listaMexBolzano = new ArrayList<IoItaliaMessaggioBolzano>();
				if (templateName.equalsIgnoreCase("aosta")) {
					listaMex = CheckFields.controllaFile(file);
				} else {
					listaMexBolzano = CheckFields.controllaFileBolzano(file);
				}
				if (!listaMex.isEmpty() || !listaMexBolzano.isEmpty()) {
					if (templateName.equalsIgnoreCase("aosta")) {
						listaMexBolzano = null;
						if (CheckFields.controllaCampiUnivoci(listaMex)) {
							String codiceUtente = cutecute;
							String tipoServizio = listaMex.get(0).getTipologiaServizio();
							String impostaServizio = listaMex.get(0).getImpostaServizio();
							String chiaveEnte = getChiaveEnte(listaMex.get(0).getIdDominio());
							//inizio LP 20241001 - PAGONET-604
							//IoItaliaDao italiadao = new IoItaliaDao(connection, schema);
							IoItaliaDao italiadao = ioItaliaDao;
							//fine LP 20241001 - PAGONET-604
							String codSocieta2 = getCodiceSocieta(codiceUtente, chiaveEnte, tipoServizio);
							printRow(PRINT_SYSOUT, "codSocieta2: " + codSocieta2 + "; codiceUtente: " + codiceUtente + "; chiaveEnte: " + chiaveEnte + "; tipoServizio: " + tipoServizio + "; impostaServizio: " + impostaServizio);
							//inizio LP 20240907 - PAGONET-604
							//IoItaliaConfigurazione conf2 = italiadao.selectConfigurazione(codSocieta2, codiceUtente,
							//		chiaveEnte, tipoServizio, impostaServizio);
							IoItaliaConfigurazione conf2 = italiadao.selectConfigurazioneTailBis(false, codSocieta2, codiceUtente,
									chiaveEnte, tipoServizio, impostaServizio, false);
							//fine LP 20240907 - PAGONET-604
							if (conf2 != null) {
								//inizio LP 20240907 - PAGONET-604
								//long idFornitura2 = italiadao.insertFornitura(codSocieta2, codiceUtente, chiaveEnte,
								//		tipoServizio, impostaServizio,
								//		"CSV-" + FilenameUtils.getBaseName(file.getName()));
								long idFornitura2 = italiadao.insertFornituraTail(false, codSocieta2, codiceUtente, chiaveEnte,
										tipoServizio, impostaServizio,
										"CSV-" + FilenameUtils.getBaseName(file.getName()));
								//fine LP 20240907 - PAGONET-604
								printRow(PRINT_SYSOUT, "listaMex.size: " + listaMex.size());
								for (IoItaliaMessaggio ioItaliamessaggio : listaMex) {
									ioItaliamessaggio.setCutecute(codiceUtente);
									ioItaliamessaggio.setIdFornitura(idFornitura2);
									printRow(PRINT_SYSOUT, "idFornitura2: " + idFornitura2);
									try {
										//inizio LP 20240907 - PAGONET-604
										//italiadao.insertMessaggio(ioItaliamessaggio);
										italiadao.insertMessaggioTail(false, ioItaliamessaggio);
										//fine LP 20240907 - PAGONET-604
									} catch(Exception e) {e.printStackTrace();}
									caricati++;
								}
								listConfig.put(conf2.getIdConfigurazione(), caricati);
								caricati = 0;
								printRow(PRINT_SYSOUT, lineSeparator);
								printRow(PRINT_SYSOUT, "Messaggi caricati correttamente.");
								File fileValido = new File(rootAccettati, file.getName());
								FileUtils.copyFile(file, fileValido);
							} else {
								System.err.println("ERRORE: Configurazione non trovata.");
								File filescartato = new File(rootScartati, file.getName());
								FileUtils.copyFile(file, filescartato);
							}
							file.delete();
						} else {
							try {
								File filescartato = new File(rootScartati, file.getName());
								FileUtils.copyFile(file, filescartato);
								System.err.println("FILE SCARTATO: campi univoci non validi");
								file.delete();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else if (templateName.equalsIgnoreCase("bolzano")) {
						listaMex = null;
						if (CheckFields.controllaCampiUnivociBolzano(listaMexBolzano)) {
							String codiceUtente = cutecute;
							String chiaveEnte = getChiaveEnte(listaMexBolzano.get(0).getIdDominio());
							//inizio LP 20241001 - PAGONET-604
							//IoItaliaDao italiadao = new IoItaliaDao(connection, schema);
							IoItaliaDao italiadao = ioItaliaDao;
							//fine LP 20241001 - PAGONET-604
//							String codSoc = italiadao.selectCodiceSocieta(codiceUtente, chiaveEnte, "", "");
//							YLM PG22XX06 INI
							//inizio LP 20240907 - PAGONET-604
							//IoItaliaConfigurazione conf2 = italiadao.selectConfigurazioneTail(listaMexBolzano.get(0).getWsKey1(), true);
							IoItaliaConfigurazione conf2 = italiadao.selectConfigurazioneTailBis(false, listaMexBolzano.get(0).getWsKey1(), true);
							//fine LP 20240907 - PAGONET-604
//							YLM PG22XX06 FINE
							if (conf2 != null) {
								//inizio LP 20240907 - PAGONET-604
								//long idFornitura2 = italiadao.insertFornitura(conf2.getCodiceSocieta(), codiceUtente,
								//		chiaveEnte, conf2.getTipologiaServizio(), conf2.getImpostaServizio(),
								//		"CSV-" + FilenameUtils.getBaseName(file.getName()));
								long idFornitura2 = italiadao.insertFornituraTail(false, conf2.getCodiceSocieta(), codiceUtente,
										chiaveEnte, conf2.getTipologiaServizio(), conf2.getImpostaServizio(),
										"CSV-" + FilenameUtils.getBaseName(file.getName()));
								//fine LP 20240907 - PAGONET-604
								for (IoItaliaMessaggioBolzano ioItaliamessaggio : listaMexBolzano) {
									ioItaliamessaggio.setTipologiaServizio(conf2.getTipologiaServizio());
									ioItaliamessaggio.setCutecute(codiceUtente);
									ioItaliamessaggio.setIdFornitura(idFornitura2);
									//inizio LP 20240907 - PAGONET-604
									//italiadao.insertMessaggio(ioItaliamessaggio);
									italiadao.insertMessaggioTail(false, ioItaliamessaggio);
									//fine LP 20240907 - PAGONET-604
									caricati++;
								}
								listConfig.put(conf2.getIdConfigurazione(), caricati);
								caricati = 0;
								printRow(PRINT_SYSOUT, lineSeparator);
								printRow(PRINT_SYSOUT, "Messaggi caricati correttamente.");
								File fileValido = new File(rootAccettati, file.getName());
								FileUtils.copyFile(file, fileValido);
							} else {
								System.err.println("ERRORE: Configurazione non trovata.");
								File filescartato = new File(rootScartati, file.getName());
								FileUtils.copyFile(file, filescartato);
							}
							file.delete();
						} else {
							try {
								File filescartato = new File(rootScartati, file.getName());
								FileUtils.copyFile(file, filescartato);
								System.err.println("FILE SCARTATO: Campi univoci non validi.");
								file.delete();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					try {
						File filescartato = new File(rootScartati, file.getName());
						FileUtils.copyFile(file, filescartato);
						file.delete();
						printRow(PRINT_SYSOUT, lineSeparator);
						printRow(PRINT_SYSOUT, "File scartato.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			printRow(PRINT_SYSOUT, "Elaborazione completata con successo ");
			printRow(PRINT_SYSOUT, lineSeparator);
			if (listConfig != null) {
				invioMailReport(templateName);
			} else {
				System.out.println("Non è stato inserito alcun messaggio.");
			}
			connection.commit();
			connection.close();
			connection = null; //LP 20241001 - PAGONET-604
		}
	}

	private void preProcess(String[] params) throws Exception {
		context = new CaricaTabelleIoItaliaContext();
		context.loadSchedeBap(params);
		Properties config = null;

		String fileConf = context.getParameter("CONFIGPATH");
		cutecute = context.getParameter("CUTECUTE");

		try {
			config = PropertiesLoader.load(fileConf);
		} catch (FileNotFoundException e) {
			printRow(PRINT_SYSOUT, "File di configurazione " + fileConf + " non trovato");
			throw new Exception();
		} catch (IOException e) {
			printRow(PRINT_SYSOUT, "Errore file di configurazione " + fileConf + " " + e);
			throw new Exception();
		}
		context.setConfig(config);

		printRow(PRINT_SYSOUT, "Configurazione esterna caricata da " + fileConf);
		printRow(PRINT_SYSOUT, "CUTECUTE = " + cutecute);

		if (context.getDatasourceJDBCDriver(cutecute) == null) {
			printRow(PRINT_SYSOUT, "JDBCDriver non configurato");
			throw new Exception();
		}
		if (context.getDatasourceJDBCUrl(cutecute) == null) {
			printRow(PRINT_SYSOUT, "JDBCUrl non configurato");
			throw new Exception();
		}
		if (context.getDatasourceJDBCUser(cutecute) == null) {
			printRow(PRINT_SYSOUT, "JDBCUSer non configurato");
			throw new Exception();
		}
		if (context.getDatasourceJDBCPassword(cutecute) == null) {
			printRow(PRINT_SYSOUT, "JDBCPassword non configurato");
			throw new Exception();
		}
		if (context.getDatasourceSchema(cutecute) == null) {
			printRow(PRINT_SYSOUT, "Datasource Schema non configurato");
			throw new Exception();
		}

		if (this.datasource == null) {
			// Recupero da file di configurazione
			DataSourceFactoryImpl dataSourceFactory = new DataSourceFactoryImpl();
			Properties dsProperties = new Properties();
			dsProperties.put(DAOHelper.JDBC_DRIVER, context.getDatasourceJDBCDriver(cutecute));
			dsProperties.put(DAOHelper.JDBC_URL, context.getDatasourceJDBCUrl(cutecute));
			dsProperties.put(DAOHelper.JDBC_USER, context.getDatasourceJDBCUser(cutecute));
			dsProperties.put(DAOHelper.JDBC_PASSWORD, context.getDatasourceJDBCPassword(cutecute));
			dsProperties.put("autocommit", "false");
			dataSourceFactory.setProperties(dsProperties);
			this.datasource = dataSourceFactory.getDataSource();
		}
		if (schema == null) {
			// Recupero da file di configurazione
			this.schema = context.getDatasourceSchema(cutecute);
		}
		connection = this.datasource.getConnection();
		connection.setAutoCommit(false);
		templateName = context.getTemplate(cutecute);
		path = context.getDirectoryIoItaliaCSV(templateName);
		rootAccettati = context.getDirectoryIoItaliaCSVValidi(templateName);
		rootScartati = context.getDirectoryIoItaliaCSVScartati(templateName);
		//inizio LP 20241001 - PAGONET-604
		archivioCarichiDao =  new ArchivioCarichiDao(connection, schema);
		configUtenteTipoServizioEnteDao = new ConfigUtenteTipoServizioEnteDao(connection, schema);
		ioItaliaDao = new IoItaliaDao(connection, schema);
		//fine LP 20241001 - PAGONET-604
	}

	public void printRow(String printer, String row) {
		System.out.println(row);
		if (classPrinting != null)
			try {
				classPrinting.print(printer, row);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	public void printRow(String printer, String row, PrintCodes printCodes) {
		System.out.println(row);
		if (classPrinting != null)
			try {
				classPrinting.print(printer, row, printCodes);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	protected String getChiaveEnte(String idDominio) throws Exception {
		// codiceEnte da 10
		String chiaveEnte = null;
		//inizio LP 20241001 - PAGONET-604
		//ArchivioCarichiDao archivioCarichiDao = new ArchivioCarichiDao(connection, this.schema);
		//chiaveEnte = archivioCarichiDao.getKeyEnteEC(idDominio);
		chiaveEnte = archivioCarichiDao.getKeyEnteECBatch(false, true, idDominio);
		//fine LP 20241001 - PAGONET-604
		return chiaveEnte;
	}

	protected String getCodiceSocieta(String codiceUtente, String chiaveEnte, String tipologiaServizio) throws DaoException, SQLException, IOException {
		String codiceSocieta = null;
		//inizio LP 20241001 - PAGONET-604
		//ConfigUtenteTipoServizioEnteDao dao = new ConfigUtenteTipoServizioEnteDao(connection, this.schema);
		//fine LP 20241001 - PAGONET-604
		ConfigUtenteTipoServizioEnte configUtenteTipoServizioEnte = new ConfigUtenteTipoServizioEnte();
		configUtenteTipoServizioEnte.getEnte().getUser().getCompany().setCompanyCode("");
		configUtenteTipoServizioEnte.getEnte().getUser().setUserCode(codiceUtente);
		configUtenteTipoServizioEnte.getEnte().getAnagEnte().setChiaveEnte(chiaveEnte);
		configUtenteTipoServizioEnte.getTipoServizio().setCodiceTipologiaServizio(tipologiaServizio);
		//inizio LP 20240907 - PAGONET-604
		//dao.doRowSets(configUtenteTipoServizioEnte, "", "", "", "");
		//String xml = dao.getWebRowSetXml(ConfigUtenteTipoServizioEnteDao.IDX_DOLIST_LISTA);
		//inizio LP 20241001 - PAGONET-604
		//dao.doRowSets(false, configUtenteTipoServizioEnte, "", "", "", "");
		//String xml = dao.getWebRowSetXml(ConfigUtenteTipoServizioEnteDao.IDX_DOLIST_LISTA);
		configUtenteTipoServizioEnteDao.doRowSets(false, configUtenteTipoServizioEnte, "", "", "", "");
		String xml = configUtenteTipoServizioEnteDao.getWebRowSetXml(ConfigUtenteTipoServizioEnteDao.IDX_DOLIST_LISTA);
		//fine LP 20241001 - PAGONET-604
		try (WebRowSetImpl wrs = new WebRowSetImpl()) {
			wrs.readXml(new StringInputStream(xml, "UTF-8"));
			if (wrs.next()) {
				codiceSocieta = wrs.getString("CFE_CSOCCSOC");
			}
		}
		return codiceSocieta;
	}
	
	public void invioMailReport(String templateName) throws DaoException {

		// Recupero le email e i report per idDominio
		for (Map.Entry<Long, Integer> entry : listConfig.entrySet()) {
			//inizio LP 20241001 - PAGONET-604
			//IoItaliaDao dao = new IoItaliaDao(connection, schema);
			IoItaliaDao dao = ioItaliaDao;
			//inizio LP 20241001 - PAGONET-604
			
//			YLM PG22XX06 INI 
			IoItaliaConfigurazione config = new IoItaliaConfigurazione();
			if (templateName.equalsIgnoreCase("aosta")){
				//inizio LP 20240907 - PAGONET-604
				//config = dao.selectConfigurazione(entry.getKey());
				config = dao.selectConfigurazioneTailBis(false, entry.getKey(), false);
				//fine LP 20240907 - PAGONET-604
			} else {
				//inizio LP 20240907 - PAGONET-604
				//config= dao.selectConfigurazioneTail(entry.getKey(), true);
				config = dao.selectConfigurazioneTailBis(false, entry.getKey(), true);
				//fine LP 20240907 - PAGONET-604
			}
//			YLM PG22XX06 FINE
			
			// String emailAdmin = inviaMessaggiAppIOContext.getEmailAdminReport(idDominio);
			String emailAdmin = config.getEmail();

//				 System.out.println("emailAdmin: "+emailAdmin);

			if (emailAdmin == null || emailAdmin.equals("")) {
				System.out.println(
						"Non è stato possibile inviare la mail di riepilogo all'amministratore: la mail non è configurata per l'ente "
								+ config.getCodiceEnte());
			} else {
				Integer numCaricamenti = entry.getValue();
				StringBuffer bodyEmail = new StringBuffer();
				// String descrizioneEnte = inviaMessaggiAppIOBL.getDescrizioneEnte(cutecute,
				// csoccsoc, config.getIdDominio());
				String descrizioneEnte = config.getDescrizioneEnte();
				bodyEmail.append("<br>Ente:");
				bodyEmail.append("<br>" + config.getIdDominio() + " - " + descrizioneEnte);
				bodyEmail.append("<br>Tipologia servizio:");
				bodyEmail.append("<br>" + config.getTipologiaServizio() + " - " + config.getDescrizioneTipologiaServizio());
				bodyEmail.append("<br><br>ELABORAZIONE CONCLUSA CON IL SEGUENTE RISULTATO");
				bodyEmail.append("<br>Caricamenti effettuati: " + numCaricamenti);
			}
		}
	}

	public boolean sendMail(String emailAdmin, StringBuffer bodyEmail, String idDominio) {
		// System.out.println("bodyEmail: "+bodyEmail);
		EMailSenderResponse emsRes = null;
		try {
			String endPoint = context.getWsEmailSenderEndpointURL();
			String oggetto = context.getOggettoEmailReport();

			// System.out.println("endPoint: "+endPoint);
			// System.out.println("oggetto: "+oggetto);

			if (endPoint == null || endPoint.equals("") || oggetto == null || oggetto.equals("")) {
				System.out.println(
						"Non è stato possibile inviare la mail di riepilogo all'amministratore dell'ente: la mail non è configurata per l'ente "
								+ idDominio);
			} else {
				EMailSender emailSender = new EMailSender(endPoint);
				emsRes = emailSender.sendEMail(emailAdmin, "", "", oggetto, bodyEmail.toString(), "", cutecute);
			}
		} catch (Exception e) {
			try {
				throw new Exception("errore nella creazione dell'email", e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		if (emsRes == null)
			return false;
		else
			return emsRes.getValue().equalsIgnoreCase("OK");
	}

}
