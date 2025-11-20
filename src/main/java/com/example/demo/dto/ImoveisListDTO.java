package com.example.demo.dto;

import com.example.demo.model.ImoveisModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para exibir informações de imóveis na LISTAGEM.
 * Inclui nomes dos relacionamentos para facilitar a exibição.
 */
@Getter
@Setter
public class ImoveisListDTO {

    private Integer id;
    private String titulo;
    private String descricao;
    private BigDecimal preco_venda;
    private BigDecimal preco_aluguel;
    private ImoveisModel.Finalidade finalidade;
    private ImoveisModel.Status status;
    private Integer dormitorios;
    private Integer banheiros;
    private Integer garagem;
    private BigDecimal area_total;
    private BigDecimal area_construida;
    private String endereco;
    private String numero;
    private String complemento;
    private String cep;
    private String caracteristicas;
    private Boolean destaque;

    // Campos de relacionamento (apenas nomes ou IDs, não objetos completos)
    private Integer id_tipo_imovel;  // ID do tipo de imóvel
    private String nome_tipo_imovel; // Nome do tipo de imóvel
    private Integer id_bairro;       // ID do bairro
    private String nome_bairro;      // Nome do bairro
    private String cidade_bairro;    // Cidade do bairro
    private String estado_bairro;    // Estado do bairro
    private Integer id_usuario;      // ID do proprietário
    private String nome_usuario;     // Nome do proprietário
    private String email_usuario;    // Email do proprietário

    public ImoveisListDTO() {}

    // Construtor para criar o DTO a partir do Model e dos dados dos relacionamentos
    public ImoveisListDTO(ImoveisModel imovel) {
        this.id = imovel.getId();
        this.titulo = imovel.getTitulo();
        this.descricao = imovel.getDescricao();
        this.preco_venda = imovel.getPreco_venda();
        this.preco_aluguel = imovel.getPreco_aluguel();
        this.finalidade = imovel.getFinalidade();
        this.status = imovel.getStatus();
        this.dormitorios = imovel.getDormitorios();
        this.banheiros = imovel.getBanheiros();
        this.garagem = imovel.getGaragem();
        this.area_total = imovel.getArea_total();
        this.area_construida = imovel.getArea_construida();
        this.endereco = imovel.getEndereco();
        this.numero = imovel.getNumero();
        this.complemento = imovel.getComplemento();
        this.cep = imovel.getCep();
        this.caracteristicas = imovel.getCaracteristicas();
        this.destaque = imovel.getDestaque();

        // Preencher dados dos relacionamentos
        if (imovel.getTipoImovel() != null) {
            this.id_tipo_imovel = imovel.getTipoImovel().getId();
            this.nome_tipo_imovel = imovel.getTipoImovel().getNome();
        }
        if (imovel.getBairro() != null) {
            this.id_bairro = imovel.getBairro().getId();
            this.nome_bairro = imovel.getBairro().getNome();
            this.cidade_bairro = imovel.getBairro().getCidade();
            this.estado_bairro = imovel.getBairro().getEstado();
        }
        if (imovel.getUsuario() != null) {
            this.id_usuario = imovel.getUsuario().getId();
            this.nome_usuario = imovel.getUsuario().getNome();
            this.email_usuario = imovel.getUsuario().getEmail();
        }
    }
}