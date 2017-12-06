package br.com.tokiomarine.relatorio.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DadosEmissaoDTO {
	
	private Integer codCorretor;
	private String nomeCorretor;
	private Integer codPedidoCotacao;
	private Integer codLocal;
	private Integer codRamo;
	private Integer codApolice;
	private Integer codEndosso;
	private String dadosApolice;
	private String dadosEndosso;
	private Date dataEmissao;
	private String nomeSegurado;
	private Date dataEnvioEmail;
	private Integer codDocumento;
	private String desricaoDocumento;
	private String urlDocstore;

}
