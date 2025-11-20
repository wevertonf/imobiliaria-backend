package com.example.demo.dto;

import com.example.demo.model.UserModel;

import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private int id;
    private String nome;
    private String email;
    private String senha; // NUNCA inclua senha em DTOs de saída
    @NotNull(message = "Tipo é obrigatório")
    //@Pattern(regexp = "^(ADMIN|CORRETOR)$", message = "Tipo deve ser ADMIN ou CORRETOR") // Validação simples com regex
    private UserModel.Tipo tipo; // Inclua o tipo no DTO

    public UserDTO(UserModel userModel) {
        this.id = userModel.getId();
        this.nome = userModel.getNome();
        this.email = userModel.getEmail();
        this.tipo = userModel.getTipo();   
    }
}
