package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "imoveis")
@Getter
@Setter
public class ImoveisModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @DecimalMin(value = "0.0", inclusive = true, message = "Preço de venda deve ser zero ou positivo")
    private BigDecimal preco_venda;

    @DecimalMin(value = "0.0", inclusive = true, message = "Preço de aluguel deve ser zero ou positivo")
    private BigDecimal preco_aluguel;

    @NotNull(message = "Finalidade é obrigatória")
    @Enumerated(EnumType.STRING)
    private Finalidade finalidade;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Min(value = 0, message = "Dormitórios deve ser zero ou positivo")
    private Integer dormitorios;

    @Min(value = 0, message = "Banheiros deve ser zero ou positivo")
    private Integer banheiros;

    @Min(value = 0, message = "Garagem deve ser zero ou positivo")
    private Integer garagem;

    @DecimalMin(value = "0.0", inclusive = true, message = "Área total deve ser zero ou positiva")
    private BigDecimal area_total;

    @DecimalMin(value = "0.0", inclusive = true, message = "Área construída deve ser zero ou positiva")
    private BigDecimal area_construida;

    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    private String complemento;

    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @Column(columnDefinition = "text")
    private String caracteristicas;

    private Boolean destaque = false;

    // Relacionamento com Tipos de Imóveis (muitos imóveis para um tipo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_imovel_id")
    @JsonBackReference("tipo-imovel") //
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TiposImoveisModel tipoImovel;

    // Relacionamento com Bairros (muitos imóveis para um bairro)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bairro_id")
    @JsonBackReference("bairro-imovel") // <--- Adicione aqui (value opcional para diferenciar)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Mantém
    private BairrosModel bairro;

    // Relacionamento com Usuários (muitos imóveis para um usuário)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    //@JsonIgnore
    @JsonBackReference("usuario-imovel")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel usuario;
    // Getter para serializar APENAS o ID do usuário associado
    @JsonProperty("usuarioId") // <--- Nome do campo no JSON será "usuarioId"
    public Integer getUsuarioIdFromRelationship() {
        return this.usuario != null ? this.usuario.getId() : null;
    }

    // Relacionamento com Fotos (um imóvel pode ter muitas fotos)
    // Se FotosImoveisModel tiver @JsonBackReference em "imovel", então este precisa de @JsonManagedReference
    @OneToMany(mappedBy = "imovel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference // <--- Adicione se FotosImoveisModel.imovel tem @JsonBackReference
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    @JsonIgnore // <-- Mais simples: ignora a lista de fotos na serialização
    private List<FotosImoveisModel> fotos;

    public ImoveisModel() {}

    public ImoveisModel(Integer id, String titulo, String descricao, BigDecimal preco_venda, BigDecimal preco_aluguel,
                        Finalidade finalidade, Status status, Integer dormitorios, Integer banheiros, Integer garagem,
                        BigDecimal area_total, BigDecimal area_construida, String endereco, String numero,
                        String complemento, String cep, String caracteristicas, Boolean destaque) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco_venda = preco_venda;
        this.preco_aluguel = preco_aluguel;
        this.finalidade = finalidade;
        this.status = status;
        this.dormitorios = dormitorios;
        this.banheiros = banheiros;
        this.garagem = garagem;
        this.area_total = area_total;
        this.area_construida = area_construida;
        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cep = cep;
        this.caracteristicas = caracteristicas;
        this.destaque = destaque;
    }

    // Enums
    public enum Finalidade {
        VENDA, ALUGUEL, VENDA_E_ALUGUEL
    }

    public enum Status {
        DISPONIVEL, ALUGADO, VENDIDO, PENDENTE
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ImoveisModel other = (ImoveisModel) obj;
        return id != null && id.equals(other.id);
    }
}