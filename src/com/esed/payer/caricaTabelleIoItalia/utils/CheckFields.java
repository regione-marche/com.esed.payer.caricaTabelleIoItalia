package com.esed.payer.caricaTabelleIoItalia.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.seda.payer.core.bean.IoItaliaMessaggio;
import com.seda.payer.core.bean.IoItaliaMessaggioBolzano;

public class CheckFields {

	public static List<IoItaliaMessaggio> controllaFile(File file) {

		List<IoItaliaMessaggio> listaMex = new ArrayList<IoItaliaMessaggio>();
		int posizioneMessaggio = 0;

		Scanner scanner = null;

		try {

			scanner = new Scanner(file);

			while (scanner.hasNext()) {

				if (posizioneMessaggio == 0) {
					scanner.nextLine();
					posizioneMessaggio++;
					continue;
				}

				String linea = scanner.nextLine();
				String[] campi = linea.split("\\|");
				if (campi.length != 11) {
					listaMex.clear();
					System.err.println(
							"============================================================================================");
					System.err.println("FILE SCARTATO: Errore alla riga " + posizioneMessaggio++ + " del file");
					System.err.println("Numero campi diverso da 11.");
					System.err.println(
							"============================================================================================");
					return listaMex;

				} else {

					if (controlloValori(campi)) {

						IoItaliaMessaggio messaggio = new IoItaliaMessaggio();

						messaggio.setIdDominio(campi[0]);
						messaggio.setTipologiaServizio(campi[1]);
						messaggio.setImpostaServizio(campi[2]);
						messaggio.setTimestampParsingFile(
								LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
						messaggio.setPosizione(1);
						messaggio.setOggettoMessaggio(campi[3]);
						messaggio.setCorpoMessaggio(campi[4]);
						messaggio.setCodiceFiscale(campi[5]);

						String[] data = campi[6].split("/");
						messaggio.setDataScadenzaMessaggio(Date.valueOf(LocalDate.of(Integer.valueOf(data[2]),
								Integer.valueOf(data[1]), Integer.valueOf(data[0]))));

						messaggio.setStato("0");

						// FACOLTATIVI
						if (!campi[7].isEmpty())
							messaggio.setImporto(new BigDecimal(campi[7]));
						if (!campi[8].isEmpty())
							messaggio.setAvvisoPagoPa(campi[8]);
						if (!campi[9].isEmpty())
							messaggio.setScadenzaPagamento(campi[9]);
						if (!campi[10].isEmpty())
							messaggio.setEmail(campi[10]);

						listaMex.add(messaggio);
						posizioneMessaggio++;

					} else {

						listaMex.clear();

						System.err.println(
								"============================================================================================");
						System.err.println("FILE SCARTATO: Errore alla riga " + posizioneMessaggio++ + " del file");
						System.err.println("Uno o più campi non corretti");
						System.err.println(
								"============================================================================================");

						return listaMex;

					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		return listaMex;

	}

	public static List<IoItaliaMessaggioBolzano> controllaFileBolzano(File file) {

		List<IoItaliaMessaggioBolzano> listaMex = new ArrayList<IoItaliaMessaggioBolzano>();
		int posizioneMessaggio = 0;

		Scanner scanner = null;

		try {

			scanner = new Scanner(file);

			while (scanner.hasNext()) {

				if (posizioneMessaggio == 0) {
					scanner.nextLine();
					posizioneMessaggio++;
					continue;
				}

				String linea = scanner.nextLine();
				String[] campi = linea.split("\\|");
				if (campi.length != 11) {
					listaMex.clear();
					System.err.println(
							"============================================================================================");
					System.err.println("FILE SCARTATO: Errore alla riga " + posizioneMessaggio++ + " del file");
					System.err.println("Numero campi diverso da 11.");
					System.err.println(
							"============================================================================================");
					return listaMex;

				} else {

					if (controlloValoriBolzano(campi)) {

						IoItaliaMessaggioBolzano messaggio = new IoItaliaMessaggioBolzano();

						messaggio.setIdDominio(campi[0]);
						messaggio.setWsKey1(campi[1]);
						messaggio.setWsKey2(campi[2]);
						messaggio.setTimestampParsingFile(
								LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
						messaggio.setPosizione(1);
						messaggio.setOggettoMessaggio(campi[3]);
						messaggio.setCorpoMessaggio(campi[4]);
						messaggio.setCodiceFiscale(campi[5]);

						String[] data = campi[6].split("/");
						messaggio.setDataScadenzaMessaggio(Date.valueOf(LocalDate.of(Integer.valueOf(data[2]),
								Integer.valueOf(data[1]), Integer.valueOf(data[0]))));

						messaggio.setStato("0");

						// FACOLTATIVI
						if (!campi[7].isEmpty())
							messaggio.setImporto(new BigDecimal(campi[7]));
						if (!campi[8].isEmpty())
							messaggio.setAvvisoPagoPa(campi[8]);
						if (!campi[9].isEmpty())
							messaggio.setScadenzaPagamento(campi[9]);
						if (!campi[10].isEmpty())
							messaggio.setEmail(campi[10]);

						listaMex.add(messaggio);
						posizioneMessaggio++;

					} else {

						listaMex.clear();
						System.err.println(
								"============================================================================================");
						System.err.println("FILE SCARTATO: Errore alla riga " + posizioneMessaggio++ + " del file");
						System.err.println("Uno o più campi non corretti");
						System.err.println(
								"============================================================================================");
						return listaMex;

					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		return listaMex;

	}

	public static boolean controllaCampiUnivoci(List<IoItaliaMessaggio> lista) {

		final String idDominio = lista.get(0).getIdDominio();
		final String tipoServizio = lista.get(0).getTipologiaServizio();
		final String impostaServizio = lista.get(0).getImpostaServizio();

		for (int i = 1; i < lista.size(); i++) {
			String id = lista.get(i).getIdDominio();
			String tipoS = lista.get(i).getTipologiaServizio();
			String imposta = lista.get(i).getImpostaServizio();

			if (!id.equals(idDominio)) {
				return false;
			} else if (!tipoS.equals(tipoServizio)) {
				return false;
			} else if (!imposta.equals(impostaServizio)) {
				return false;
			}

		}

		return true;
	}

	public static boolean controllaCampiUnivociBolzano(List<IoItaliaMessaggioBolzano> lista) {

		final String idDominio = lista.get(0).getIdDominio();

		for (int i = 1; i < lista.size(); i++) {
			String id = lista.get(i).getIdDominio();

			if (!id.equals(idDominio)) {
				return false;
			}

		}

		return true;
	}

	public static boolean controlloValori(String[] campi) {

		boolean fileValido = false;
//		prima controllo i valori obbligatori
		if (campi[0] != null && campi[0].trim().length() <= 16)
			if (campi[1] != null && campi[1].length() > 0 && campi[1].trim().length() <= 3)
					if (campi[3] != null && campi[3].length() >= 10 && campi[3].length() <= 120)
						if (campi[4] != null && campi[4].length() >= 80 && campi[4].length() <= 10000)
							if (campi[5] != null && campi[5].length() == 16)
								if (campi[6] != null && !campi[6].isEmpty()) {
									fileValido = true;

									// controllo i valori facoltativi
									if (!campi[7].isEmpty()) {
										if (campi[7] != null && campi[7].length() > 10)
											fileValido = false;
									}
									if (!campi[8].isEmpty()) {
										if (campi[8] != null && campi[8].length() > 18)
											fileValido = false;
									}
									if (!campi[9].isEmpty()) {
										if (campi[9] != null && campi[9].length() != 10)
											fileValido = false;
									}
									if (!campi[10].isEmpty()) {
										if (campi[10] != null && campi[10].length() > 512)
											fileValido = false;
									}
									if(!campi[2].isEmpty()) {
										if (campi[2] != null && campi[2].length() > 2 && campi[2].trim().length() < 2) {
											fileValido = false;
										}
									}
								}

		return fileValido;
	}

	public static boolean controlloValoriBolzano(String[] campi) {

		boolean fileValido = false;
//		prima controllo i valori obbligatori
		if (campi[0] != null && campi[0].trim().length() <= 16)
			if (campi[1] != null && campi[1].length() > 0)
				if (campi[2] != null && campi[2].length() > 0)
					if (campi[3] != null && campi[3].length() >= 10 && campi[3].length() <= 120)
						if (campi[4] != null && campi[4].length() >= 80 && campi[4].length() <= 10000)
							if (campi[5] != null && campi[5].length() == 16)
								if (campi[6] != null && !campi[6].isEmpty()) {
									fileValido = true;

									// controllo i valori facoltativi
									if (!campi[7].isEmpty()) {
										if (campi[7] != null && campi[7].length() > 10)
											fileValido = false;
									}
									if (!campi[8].isEmpty()) {
										if (campi[8] != null && campi[8].length() > 18)
											fileValido = false;
									}
									if (!campi[9].isEmpty()) {
										if (campi[9] != null && campi[9].length() != 1)
											fileValido = false;
									}
									if (!campi[10].isEmpty()) {
										if (campi[10] != null && campi[10].length() > 512)
											fileValido = false;
									}
								}

		return fileValido;
	}

}
