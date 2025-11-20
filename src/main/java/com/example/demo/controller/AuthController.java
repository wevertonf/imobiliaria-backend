package com.example.demo.controller;

import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.UserModel;
//import com.example.demo.util.SenhaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserServices;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServices service;

    /**
     * Endpoint para login de usuário.
     * Recebe email e senha, verifica no banco e cria sessão.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        String email = loginDTO.getEmail();
        String senha = loginDTO.getSenha();

        if (email == null || senha == null || email.trim().isEmpty() || senha.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email e senha são obrigatórios.");
        }

        // Usar o Service para autenticar (o Service usa o Repository)
        UserModel usuario = service.login(email, senha); // Este método deve existir em UsuarioServices

        // Buscar usuário pelo email
        //UserModel usuario = repositorio.findByEmail(email); // Precisa ter um método no UserRepository

        if (usuario != null) {
            // Verificar senha usando a utilidade
            //if (SenhaUtil.verificarSenha(senha, usuario.getSenha())) {
                // Credenciais válidas - criar sessão
                session.setAttribute("usuarioLogado", usuario); // Armazena o objeto usuário na sessão

                // Retornar dados do usuário (sem senha)
                UserDTO userDTO = new UserDTO(usuario); // Usando o construtor que omite a senha
                return ResponseEntity.ok(userDTO);
            /* } else {
                // Senha inválida
                return ResponseEntity.status(401).body("Credenciais inválidas."); 
            }*/
        } else {
            // Usuário não encontrado
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }
    }

    /**
     * Endpoint para logout.
     * Invalida a sessão atual.
     */
    @PostMapping("/logout") // <-- Mapeamento correto: POST /auth/logout
    public ResponseEntity<String> logout(HttpSession session) { // <-- Recebe a sessão
        try {
            // Verificar se há uma sessão ativa
            if (session != null) {
                // Invalida a sessão, removendo o atributo "usuarioLogado" e qualquer outro
                session.invalidate();
            }
            // Mesmo que a sessão já estivesse inválida, retornar sucesso é comum
            return ResponseEntity.ok("Logout realizado com sucesso.");
        } catch (Exception e) {
            // Tratar qualquer erro inesperado ao invalidar a sessão
            System.err.println("Erro ao fazer logout: " + e.getMessage());
            e.printStackTrace(); // Log detalhado da stack trace
            return ResponseEntity.status(500).body("Erro ao processar logout.");
        }
    }

    /**
     * Endpoint para verificar se o usuário está logado.
     * Útil para o frontend confirmar o status de login ao carregar.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(HttpSession session) {
        UserModel usuario = (UserModel) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            // Retorna dados do usuário logado (sem senha)
            UserDTO userDTO = new UserDTO(usuario);// Usando o construtor que omite a senha e inclui o tipo
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(401).body("Usuário não está logado.");
        }
    }
}