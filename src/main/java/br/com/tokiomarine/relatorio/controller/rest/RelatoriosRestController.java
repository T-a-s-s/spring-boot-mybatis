package br.com.tokiomarine.relatorio.controller.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.tokiomarine.relatorio.service.RelatoriosService;

@RestController
@RequestMapping({"/corretores"})
public class RelatoriosRestController {
	
	@Autowired
	RelatoriosService relatorioService;
	
	@RequestMapping(value={"/emissoes/{dataReferencia}"}, method={RequestMethod.GET})
	@ResponseStatus(HttpStatus.OK)
	public Integer updateContatoEspecialCorretor(@PathVariable String dataReferencia)
	{
		try {
			Date dataRef = new SimpleDateFormat("yyyyMMdd").parse(dataReferencia);
			relatorioService.relatorioEmissoesCorretores(dataRef);
		} catch (Exception e){
			return 1;
		}
		return 0;
	}

}
