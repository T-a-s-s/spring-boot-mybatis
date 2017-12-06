package br.com.tokiomarine.relatorio.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import com.sun.mail.util.BASE64EncoderStream;

import br.com.tokiomarine.relatorio.dao.OracleDao;
import br.com.tokiomarine.relatorio.dto.CorretorDTO;
import br.com.tokiomarine.relatorio.dto.DadosEmissaoDTO;
import br.com.tokiomarine.relatorio.service.RelatoriosService;
import lombok.Cleanup;

@Service
public class RelatoriosServiceImpl implements RelatoriosService {

	private static final Logger log = LoggerFactory.getLogger(RelatoriosServiceImpl.class);
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
	private static final String COD_MODELO_GNT_RELATORIO = "17";

	@Override
	public void relatorioEmissoesCorretores(Date DataRef) {

		try {
			@Cleanup
			final OracleDao oracle = new OracleDao("PLT");
			Date lastWorkingDay = getLastWorkingDate(oracle, DataRef);
			List<CorretorDTO> corretores = oracle.selectList("CorretoresMapper.listCorretoresRelatorio", lastWorkingDay);
			if(corretores != null && !corretores.isEmpty()) {
				
				String servidorDigital = oracle.selectOne("CorretoresMapper.getServidorDigital");
				String urlServidorBu = oracle.selectOne("CorretoresMapper.getUrlServidorBU");
				String filePath = System.getProperty("jboss.server.log.dir");
				
				if(urlServidorBu != null) {
					
					CSVWriter csvWriter;
					for(CorretorDTO corretor : corretores) {
						
						StringBuilder stb = new StringBuilder()
							.append(urlServidorBu)
						    .append("codigoCorretor/")
						    .append(corretor.getCodCorretor());
						
						RestTemplate restTemplate = new RestTemplate();
						ResponseEntity<String> response = restTemplate.getForEntity(stb.toString(), String.class);
						JsonParser jsonParser = new JsonParser();
						JsonArray jsonArray = (JsonArray) jsonParser.parse(response.getBody());
						StringBuilder emailsDestinatarios = new StringBuilder();
						
						for (int i = 0; i < jsonArray.size(); i++) { 
							JsonObject json = (JsonObject) jsonArray.get(i);
							emailsDestinatarios.append(json.get("EMAIL").toString());
							emailsDestinatarios.append(";");
						}
						
//						if(emailsDestinatarios.length() > 0) {
							
						csvWriter = new CSVWriter(new FileWriter(filePath + "/relatorio-emissoes.csv"), ';', '"', "\n");
					
						Map<String, Object> parametros = new HashMap<String, Object>();
						parametros.put("dataEmissao", lastWorkingDay);
						parametros.put("codCorretor", corretor.getCodCorretor());
						List<DadosEmissaoDTO> dadosEmissoes = oracle.selectList("CorretoresMapper.listDadosEmissoes", parametros); 
						
						if(dadosEmissoes != null && !dadosEmissoes.isEmpty()) {
							
							csvWriter.writeNext(new String[] { "Corretor", 
															   "Nm. Corretor", 
															   "Pedido Cotação", 
															   "Apolice", 
															   "Endosso", 
															   "Dt. Emissão", 
															   "Segurado", 
															   "Destinatários", 
															   "Dt. Envio Email", 
															   "Documento", 
															   "Ds. Documento", 
															   "Link" });
							
							for(DadosEmissaoDTO emissao : dadosEmissoes) {
								csvWriter.writeNext(new String[] { emissao.getCodCorretor().toString(), 
																   emissao.getNomeCorretor(), 
																   emissao.getCodPedidoCotacao() != null ? emissao.getCodPedidoCotacao().toString() : "", 
																   emissao.getDadosApolice(), 
																   emissao.getDadosEndosso() != null ? emissao.getDadosEndosso() : "", 
																   formatter.format(emissao.getDataEmissao()), 
																   emissao.getNomeSegurado(), 
																   emailsDestinatarios.toString(), 
																   formatter.format(emissao.getDataEnvioEmail()), 
																   emissao.getCodDocumento().toString(), 
																   emissao.getDesricaoDocumento(), 
																   emissao.getUrlDocstore()});
							}
							
						}
						csvWriter.close();
						File file = new java.io.File(filePath + "/relatorio-emissoes.csv");
						byte[] bytes = FileUtils.readFileToByteArray(file); 
						if(bytes.length > 0) {
							byte[] encoded = BASE64EncoderStream.encode(bytes);
							String encodedString = new String(encoded);
							
							JsonArray jsonParametros = new JsonArray();
							JsonObject parametroUm = new JsonObject();
							parametroUm.addProperty("nomeParametro","NOME_CORRETOR");
							parametroUm.addProperty("valorParametro", corretor.getNomeCorretor());
							JsonObject parametroDois = new JsonObject();
							parametroDois.addProperty("nomeParametro","DT_EMISSAO");
							parametroDois.addProperty("valorParametro", formatter.format(lastWorkingDay));
							jsonParametros.add(parametroUm);
							jsonParametros.add(parametroDois);
							
							JsonArray jsonAnexos = new JsonArray();
							JsonObject jsonParamAnexo = new JsonObject();
							jsonParamAnexo.addProperty("nome","relatorio_emissoes.csv");
							jsonParamAnexo.addProperty("fileBase64", encodedString);
							jsonAnexos.add(jsonParamAnexo);
							
							JsonArray jsonDestinatarios = new JsonArray();
							JsonObject jsonDestinatario = new JsonObject();
							jsonDestinatario.addProperty("destino","mirian.horita@tokiomarine.com.br");
							jsonDestinatario.addProperty("cpfCnpj", "000");
							jsonDestinatarios.add(jsonDestinatario);
							
							JsonObject jsonDestinatario2 = new JsonObject();
							jsonDestinatario2.addProperty("destino","deal.tomas@tokiomarine.com.br");
							jsonDestinatario2.addProperty("cpfCnpj", "000");
							jsonDestinatarios.add(jsonDestinatario2);
							
//								for (int i = 0; i < jsonArray.size(); i++) { //TODO descomentar aqui
//									JsonObject json = (JsonObject) jsonArray.get(i);
//									JsonObject jsonDest = new JsonObject();
//									jsonDest.addProperty("destino", json.get("EMAIL").toString());
//									jsonDest.addProperty("cpfCnpj", "000");
//									jsonDestinatarios.add(jsonDest);
//								}
//								
							List<String> emailsFuncionarios = oracle.selectList("CorretoresMapper.getEmailsFuncionarios", corretor.getCodFuncionario());
//								if(emailsFuncionarios != null && !emailsFuncionarios.isEmpty()) {
//									for(String emailFuncionario : emailsFuncionarios) {
//										JsonObject jsonDest = new JsonObject();
//										jsonDest.addProperty("destino", emailFuncionario);
//										jsonDest.addProperty("cpfCnpj", "000");
//										jsonDestinatarios.add(jsonDest);
//									}
//								}
							
							
							JsonObject jsonEmail = new JsonObject();
							jsonEmail.addProperty("codigoModelo", COD_MODELO_GNT_RELATORIO);
							jsonEmail.add("destinatariosDetalhes", jsonDestinatarios);
							jsonEmail.add("parametros", jsonParametros);
							jsonEmail.add("anexos", jsonAnexos);
							
							System.out.println(jsonEmail.toString());
							
							HttpHeaders headers = new HttpHeaders();
							headers.setContentType(MediaType.APPLICATION_JSON);

							HttpEntity<String> entity = new HttpEntity<String>(jsonEmail.toString(), headers);
							
							ResponseEntity<String> responseEmail = restTemplate.postForEntity(servidorDigital + "/documentos-rest/comunicacao/agendamento?envia=S", entity, String.class);
							System.out.println(responseEmail.getBody());
						}
					}
				}
			}
		} catch (PersistenceException pe) {
			log.error(pe.getMessage());
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}

	}

	private Date getLastWorkingDate(OracleDao oracle, Date dataRef) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataRef);
		do{
		     calendar.add(Calendar.DATE, -1); //Dia útil anterior à data de referência
		   } while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
		           || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
		           || isHoliday(oracle, calendar));

		return calendar.getTime();
	}

	private boolean isHoliday(OracleDao oracle, Calendar calendar) {
		String holidayName = oracle.selectOne("CorretoresMapper.getFeriado", calendar.getTime());
		if(holidayName != null && !"".equals(holidayName)) {
			return true;
		}
		return false;
	}

}
