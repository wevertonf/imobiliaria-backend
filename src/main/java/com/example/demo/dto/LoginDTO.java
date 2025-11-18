package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para receber dados de login (email e senha).
 */
@Getter
@Setter
public class LoginDTO {
    private String email;
    private String senha;
}