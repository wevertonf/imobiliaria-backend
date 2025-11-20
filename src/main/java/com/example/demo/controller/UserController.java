package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.ImoveisModel;
import com.example.demo.model.UserModel;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserServices;
import com.example.demo.util.SenhaUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.net.URI;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping(value = "/users")

/*
 * .../users
 * GET /users
 * GET /users/1
 * POST /users
 * PUT /users/1
 * DELETE /users/1
 */

public class UserController {

    @Autowired
    private UserServices service;

    @Autowired
    private UserRepository repositorio;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(HttpSession session) {
        // Verificar se o usuário está logado
        Object usuarioLogado = session.getAttribute("usuarioLogado"); // Nome do atributo da sessão
        if (usuarioLogado == null) {
            // Usuário não está logado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserModel> usuarios = service.getAll();
        List<UserDTO> usuariosDTO = usuarios.stream()// Mapeia para DTOs (sem senha)
                .map(user -> new UserDTO(user))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(usuariosDTO);
    }

    /*
     * @GetMapping("/users-page")
     * public Page<UserDTO> getPosts(Pageable pageable, HttpSession session) {
     * // Verificar se o usuário está logado
     * Object usuarioLogado = session.getAttribute("usuarioLogado"); // Nome do
     * atributo da sessão
     * if (usuarioLogado == null) {
     * // Usuário não está logado
     * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
     * }
     * return service.getAll(pageable).map(UserDTO::new);// Page.map com construtor
     * sem senha
     * }
     */

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int id, HttpSession session) {
        // Verificar se o usuário está logado
        Object usuarioLogado = session.getAttribute("usuarioLogado"); // Nome do atributo da sessão
        if (usuarioLogado == null) {
            // Usuário não está logado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel user = service.find(id);
        if (user != null) {
            UserDTO userDTO = new UserDTO(user);
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);// Retorna DTO sem senha
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody UserDTO dto, HttpSession session) {
        // Verificar se o usuário está logado
        Object usuarioLogado = session.getAttribute("usuarioLogado"); // Nome do atributo da sessão
        if (usuarioLogado == null) {
            // Usuário não está logado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UserModel model = service.update(id, dto);
            if (model != null) {
                UserDTO userDTO = new UserDTO(model);
                return ResponseEntity.status(HttpStatus.OK).body(userDTO);// Retorna DTO sem senha
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDTO dto) {
        try {
            // Verificar se email já existe (opcional, mas recomendado)
            if (repositorio.findByEmail(dto.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
            }

            UserModel model = service.insert(dto);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(model.getId()).toUri();
            return ResponseEntity.created(uri).body(model); // Retorna 201 Created com o objeto criado
        } catch (Exception e) {
            e.printStackTrace(); // Log do erro
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao criar usuário: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id, HttpSession session) {
        // Verificar se o usuário está logado
        Object usuarioLogado = session.getAttribute("usuarioLogado"); // Nome do atributo da sessão
        if (usuarioLogado == null) {
            // Usuário não está logado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para atualizar os dados do perfil do usuário logado.
     * O ID do usuário é obtido da sessão, não da URL ou do corpo.
     */

}
