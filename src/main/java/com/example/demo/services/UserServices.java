package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.UserModel;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SenhaUtil;

// Se usar o Spring Security PasswordEncoder, injete-o:
// import org.springframework.security.crypto.password.PasswordEncoder;
// @Autowired private PasswordEncoder passwordEncoder;

@Service
public class UserServices {

    @Autowired
    UserRepository repositorio;

    // P/ PasswordEncoder do Spring Security:
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    // HTTP -> Controller -> Service (getAll()) -> Repository -> Model -> Banco de
    // Dados
    // Banco de Dados -> Model -> Repository -> Service -> Controller -> HTTP
    public List<UserModel> getAll() {
        List<UserModel> lista = repositorio.findAll();
        return lista;
    }

    public Page<UserModel> getAll(Pageable pageable) {
        Page<UserModel> list = repositorio.findAll(pageable);
        return list;
    }

    public UserModel find(Integer id) {
        Optional<UserModel> model = repositorio.findById(id);
        return model.orElse(null);
    }

    /*
     * public UserModel insert(UserModel user) {
     * return repositorio.save(user);
     * 
     * }
     */

    public UserModel insert(UserDTO dto) {
        // Verificar se email já existe
        UserModel usuarioExistente = repositorio.findByEmail(dto.getEmail());
        if (usuarioExistente != null) {
            throw new RuntimeException("Email já cadastrado.");
        }

        UserModel model = new UserModel();
        model.setNome(dto.getNome());
        model.setEmail(dto.getEmail());
        model.setTipo(dto.getTipo());

        // Criptografar a senha antes de salvar
        String senhaCriptografada = null;
        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            senhaCriptografada = SenhaUtil.hashSenha(dto.getSenha());// Usando utilitário standalone

        }
        model.setSenha(senhaCriptografada); // Pode ser null se dto.getSenha() for vazia

        return repositorio.save(model);
    }

    public UserModel update(Integer id, UserDTO dto) {
        Optional<UserModel> optionalModel = repositorio.findById(id);
        if (optionalModel.isPresent()) {
            UserModel model = optionalModel.get();

            // Atualizar campos básicos
            model.setNome(dto.getNome());
            model.setEmail(dto.getEmail());
            model.setTipo(dto.getTipo());

            // Atualizar senha SOMENTE se uma nova foi fornecida
            if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
                String senhaCriptografada = SenhaUtil.hashSenha(dto.getSenha());
                // Ou com Spring Security: passwordEncoder.encode(dto.getSenha());
                model.setSenha(senhaCriptografada);
            }
            // Se dto.getSenha() for null/vazia, a senha existente é mantida.

            return repositorio.save(model);
        } else {
            return null; // Ou lançar exceção
        }
    }

    public boolean delete(Integer id) {
        try {
            if (find(id) != null) {
                repositorio.deleteById(id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método para autenticar um usuário.
     * 
     * @param email O email fornecido pelo usuário.
     * @param senha A senha fornecida pelo usuário (em texto plano).
     * @return O objeto UserModel se as credenciais forem válidas, ou null se
     *         inválidas ou usuário não encontrado.
     */
    public UserModel login(String email, String senha) {
        // 1. Buscar usuário pelo email
        UserModel usuario = repositorio.findByEmail(email); // Precisa ter um método no UserRepository

        // 2. Verificar se o usuário existe
        if (usuario != null) {
            // 3. Verificar se a senha está correta usando o utilitário
            if (SenhaUtil.verificarSenha(senha, usuario.getSenha())) {
                // 4. Senha correta, retornar o usuário
                return usuario;
            }
            // 5. Senha incorreta
        }
        // 6. Usuário não encontrado ou senha incorreta
        return null;
    }
}
