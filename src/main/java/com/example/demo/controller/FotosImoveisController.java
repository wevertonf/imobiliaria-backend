package com.example.demo.controller;

import com.example.demo.dto.FotosImoveisDTO;
import com.example.demo.model.FotosImoveisModel;
import com.example.demo.model.ImoveisModel;
import com.example.demo.services.FotosImoveisServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // Para converter String JSON em Objeto

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.print.attribute.standard.Media;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/fotos-imoveis")
public class FotosImoveisController {

    @Autowired
    private FotosImoveisServices service;

    @Autowired
    private ObjectMapper objectMapper; // Injeta o ObjectMapper do Spring para desserializar JSON

    // Injeta o caminho base para salvar os arquivos a partir do
    // application.properties
    // Exemplo em application.properties: app.upload.dir=./imoveis/fotos
    @Value("${app.upload.dir:./uploads}") // Valor padrão se não estiver no .properties
    private String uploadDir;

    /**
     * Salva uma nova foto de imóvel via upload multipart/form-data.
     *
     * @param arquivo O arquivo da foto.
     * @param dados   Uma string JSON contendo os dados da foto (ex: imovelId).
     * @return ResponseEntity indicando sucesso ou falha.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> salvar(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart("dados") String dados) {

        try {
            // 1. Converter a String de dados para um objeto FotosImoveisDTO
            FotosImoveisDTO dto = objectMapper.readValue(dados, FotosImoveisDTO.class);

            // 1.1. Validar se imovelId foi fornecido
            if (dto.getImovelId() == null) {
                return ResponseEntity.badRequest().body("ID do imóvel é obrigatório nos dados.");
            }

            // 2. Salvar o arquivo em um diretório
            // Caminho completo: {uploadDir}/{imovelId}/fotos/{nome_arquivo_unico}
            String subDirPath = dto.getImovelId().toString() + File.separator + "fotos";
            Path dirPath = Paths.get(uploadDir, subDirPath);

            // Cria os diretórios se não existirem
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Gerar nome único para o arquivo
            String nomeArquivo = UUID.randomUUID() + "-" + arquivo.getOriginalFilename();
            Path filePath = dirPath.resolve(nomeArquivo);

            // Transferir o arquivo para o local desejado
            arquivo.transferTo(filePath);

            // Calcular o caminho relativo ou absoluto para salvar no banco
            // Aqui vamos salvar o caminho relativo ao uploadDir para portabilidade
            String caminhoRelativo = Paths.get(subDirPath, nomeArquivo).toString();
            // Para URL completa, poderia ser:
            // http://seudominio.com/uploads/{caminhoRelativo}
            // Mas para simplicidade, vamos salvar o caminho relativo no banco.

            // 3. Criar um modelo FotosImoveisModel a partir do DTO e do caminho do arquivo
            // salvo
            FotosImoveisModel model = service.createFromDtoAndPath(dto, caminhoRelativo, nomeArquivo);

            // 4. Salvar o modelo no banco de dados usando o serviço
            // (O serviço já salvou dentro de createFromDtoAndPath, mas podemos retornar o
            // objeto salvo)
            // Se o service.createFromDtoAndPath não salvar, chame service.insert(model)
            // aqui.

            // 5. Retornar uma resposta adequada
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/fotos-imoveis/{id}") // Ou outro path para acessar a foto
                    .buildAndExpand(model.getId())
                    .toUri();
            return ResponseEntity.created(uri).body("Foto salva com sucesso. ID: " + model.getId());

        } catch (IOException e) {
            // Erros relacionados a IO (disco, permissões, etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar o arquivo: " + e.getMessage());
        } catch (RuntimeException e) {
            // Erros de negócio ou validação do serviço
            return ResponseEntity.badRequest().body("Erro ao processar a requisição: " + e.getMessage());
        } catch (Exception e) {
            // Qualquer outro erro inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<FotosImoveisModel>> getAll() {
        List<FotosImoveisModel> lista = service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    @GetMapping("/fotos-page")
    public Page<FotosImoveisModel> getPosts(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FotosImoveisModel> getById(@PathVariable Integer id) {
        FotosImoveisModel model = service.find(id);
        if (model != null) {
            return ResponseEntity.status(HttpStatus.OK).body(model);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/imoveis/{id}") // pegar fotos pelo id do imovel
    public List<FotosImoveisModel> getFotosByImovelId(@PathVariable Integer id) {
        List<FotosImoveisModel> fotos = service.getAll().stream()
                .filter(foto -> foto.getImovel() != null && foto.getImovel().getId().equals(id))
                .toList();
        return fotos;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody FotosImoveisDTO dto) {
        FotosImoveisModel model = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(model.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }


    @PutMapping("/{id}") // Mapeia PUT para /fotos-imoveis/{id}
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody FotosImoveisDTO dto) {
        try {
            // 1. Chamar o serviço para atualizar o modelo com base no DTO
            FotosImoveisModel modelAtualizado = service.update(id, dto);

            if (modelAtualizado != null) {
                return ResponseEntity.ok(modelAtualizado);

                // Retornar uma mensagem simples (mais comum para updates)
                // return ResponseEntity.ok("Foto com ID " + id + " atualizada com sucesso.");
            } else {
                return ResponseEntity.notFound().build();// ¬ID, status 404
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar: " + e.getMessage());// Tratar erros de validação
                                                                                            // ou negócio (ex: imovelId
                                                                                            // inválido)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());// Tratar
                                                                                                                   // outros
                                                                                                                   // erros
                                                                                                                   // inesperados
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            boolean deleted = service.delete(id);
            if (deleted) {
                // 204 No Content é o código mais apropriado para deleção bem-sucedida
                return ResponseEntity.noContent().build();
            } else {
                // 404 Not Found se o ID não existir
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            // Erro retornado pelo serviço (ex: falha ao excluir arquivo)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir a foto: " + e.getMessage());
        }
    }
}