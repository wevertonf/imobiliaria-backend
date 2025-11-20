package com.example.demo.controller;

import com.example.demo.dto.ImoveisDTO;
import com.example.demo.dto.ImoveisListDTO;
import com.example.demo.model.ImoveisModel;
import com.example.demo.model.TiposImoveisModel;
import com.example.demo.model.UserModel;
import com.example.demo.services.ImoveisServices;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/imoveis")
public class ImoveisController {

    @Autowired
    private ImoveisServices service;

    /* @GetMapping
    public ResponseEntity<List<ImoveisListDTO>> getAll() { // Retorna DTOs
        List<ImoveisListDTO> lista = service.getAll(); // Chama o serviço que retorna DTOs
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    } */

    @GetMapping("/imoveis-page")
    public Page<ImoveisListDTO> getPosts(Pageable pageable) { // Retorna Page de DTOs
        return service.getAll(pageable); // Chama o serviço que retorna Page de DTOs
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImoveisListDTO> getById(@PathVariable Integer id) {
        ImoveisModel model = service.find(id);
        if (model != null) {
            ImoveisListDTO dto = new ImoveisListDTO(model);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // --- MÉTODO GET ALL CORRIGIDO ---
    @GetMapping // GET /imoveis
    public ResponseEntity<List<ImoveisListDTO>> getAllImoveis(HttpSession session) { // <-- Tipo de retorno é DTO
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        // Se o usuário não estiver logado, ainda pode ver a lista (se quiser restringir, adicione verificação)
        // if (usuarioLogadoObj == null) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // }

        // Buscar todos os imóveis como DTOs (o service já converte)
        List<ImoveisListDTO> dtos = service.getAll(); // <-- ✅ Chamando o método correto que retorna DTOs!

        // NÃO precisa converter aqui no controller, o service já fez
        return ResponseEntity.ok(dtos); // <-- Retorna diretamente a lista de DTOs
    }

    @GetMapping("/meus") // GET /imoveis/meus
    public ResponseEntity<List<ImoveisListDTO>> getMeusImoveis(HttpSession session) {
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        if (usuarioLogadoObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel usuarioLogado = (UserModel) usuarioLogadoObj;

        // Chama o service para buscar imóveis do usuário logado
        List<ImoveisModel> meusImoveis = service.buscarPorUsuarioId(usuarioLogado.getId());

        // Converter para DTOs antes de retornar
        List<ImoveisListDTO> dtos = meusImoveis.stream()
                .map(ImoveisListDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos); // Retorna a lista de DTOs
    }

    // --- MÉTODO CREATE ATUALIZADO PARA USAR DTO ---

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ImoveisDTO dto, HttpSession session) {
        UserModel usuarioLogado = (UserModel) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verificar se o imóvel está sendo associado ao usuário logado (ou se é admin)
        if (dto.getUsuarioId() != null) {
            if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                    !dto.getUsuarioId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }
        } else {
            // Se não for enviado usuarioId, assume que é para o usuário logado
            dto.setUsuarioId(usuarioLogado.getId());
        }

        // Chamar o service para salvar
        ImoveisModel model = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(model.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImoveisModel> update(@PathVariable Integer id, @RequestBody ImoveisDTO dto,
            HttpSession session) {
        UserModel usuarioLogado = (UserModel) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ImoveisModel imovelExistente = service.find(id);
        if (imovelExistente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Verificar permissão: Admin pode editar qualquer um, Corretor só o próprio
        if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                !imovelExistente.getUsuario().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ImoveisModel model = service.update(id, dto);
        if (model != null) {
            return ResponseEntity.status(HttpStatus.OK).body(model);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, HttpSession session) {
        UserModel usuarioLogado = (UserModel) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ImoveisModel imovel = service.find(id);
        if (imovel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Verificar permissão: Admin pode excluir qualquer um, Corretor só o próprio
        if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                !imovel.getUsuario().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}