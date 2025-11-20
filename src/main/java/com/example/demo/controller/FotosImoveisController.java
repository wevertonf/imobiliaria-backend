package com.example.demo.controller;

import com.example.demo.dto.FotosImoveisDTO;
import com.example.demo.model.FotosImoveisModel;
import com.example.demo.model.ImoveisModel;
import com.example.demo.model.UserModel;
import com.example.demo.repository.ImoveisRepository;
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

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/fotos-imoveis")
public class FotosImoveisController {

    @Autowired
    private FotosImoveisServices service;

    @Autowired
    private ImoveisRepository imoveisRepository; // Para buscar o imóvel associado à foto

    @Autowired
    private ObjectMapper objectMapper; // Injeta o ObjectMapper do Spring para desserializar JSON

    // Injeta o caminho base para salvar os arquivos a partir do
    // application.properties
    @Value("${app.upload.dir:./uploads}") // Valor padrão se não estiver no .properties
    private String uploadDir;

    /**
     * Endpoint para upload de nova foto de imóvel via multipart/form-data.
     * Requer que o usuário esteja logado e que o imóvel pertença a ele (ou seja
     * admin).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> salvar(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart("dados") String dados,
            HttpSession session) {

        // 1. Verificar se o usuário está logado
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        if (usuarioLogadoObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não está logado.");
        }
        UserModel usuarioLogado = (UserModel) usuarioLogadoObj; // Assume que você salvou o objeto UserModel na sessão

        try {
            // 2. Converter a String de dados JSON para um objeto FotosImoveisDTO
            FotosImoveisDTO dto = objectMapper.readValue(dados, FotosImoveisDTO.class);

            // 3. Validar se imovelId foi fornecido
            if (dto.getImovelId() == null) {
                return ResponseEntity.badRequest().body("ID do imóvel é obrigatório nos dados.");
            }

            // 4. Buscar o imóvel associado para verificar permissão
            ImoveisModel imovel = imoveisRepository.findById(dto.getImovelId()).orElse(null);
            if (imovel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Imóvel não encontrado.");
            }

            // 5. Verificar permissão: somente admin ou proprietário do imóvel pode
            // adicionar foto
            if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                    !imovel.getUsuario().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Você não tem permissão para adicionar fotos a este imóvel.");
            }

            // 6. Salvar o arquivo no sistema de arquivos
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

            // 7. Criar um modelo FotosImoveisModel a partir do DTO e do caminho do arquivo
            // salvo
            // O serviço deve receber o DTO e o caminho, e associar ao imóvel encontrado
            // acima
            FotosImoveisModel model = new FotosImoveisModel();
            model.setNome_arquivo(nomeArquivo);
            model.setCaminho(caminhoRelativo);
            model.setCapa(dto.getCapa() != null ? dto.getCapa() : false);
            model.setOrdem(dto.getOrdem() != null ? dto.getOrdem() : 0);
            model.setImovel(imovel); // Associa o imóvel já validado

            // 8. Salvar o modelo no banco de dados usando o serviço
            FotosImoveisModel modelSalvo = service.insert(model);

            // 9. Retornar resposta de sucesso
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replacePath("/fotos-imoveis/{id}") // Ou outro path para acessar a foto
                    .buildAndExpand(modelSalvo.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(modelSalvo); // Retorna o objeto criado, não uma mensagem

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { // <--- ESPECÍFICO PRIMEIRO
            // Erro ao processar o JSON dos dados
            return ResponseEntity.badRequest().body("JSON de dados inválido: " + e.getMessage());
        } catch (IOException e) { // <--- GENÉRICO DEPOIS
            // Erros relacionados a IO (disco, permissões, etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar o arquivo no servidor: " + e.getMessage());
        } catch (RuntimeException e) { // <--- Outras exceções específicas do negócio
            // Erros de negócio ou validação do serviço (já tratados acima)
            return ResponseEntity.badRequest().body("Erro ao processar a requisição: " + e.getMessage());
        } catch (Exception e) { // <--- GENÉRICA por último
            // Qualquer outro erro inesperado
            e.printStackTrace(); // Log importante para debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor ao processar upload: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar todas as fotos (GET /fotos-imoveis)
     * Acesso: Qualquer um (visitante, logado)
     * OBS: Pode ser útil para admins, mas talvez não para visitantes (pense na
     * privacidade)
     */
    @GetMapping
    public ResponseEntity<List<FotosImoveisModel>> getAll() {
        List<FotosImoveisModel> lista = service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    /**
     * Endpoint para paginação de fotos (GET /fotos-imoveis/fotos-page)
     * Acesso: Qualquer um (visitante, logado)
     */
    @GetMapping("/fotos-page")
    public Page<FotosImoveisModel> getPosts(Pageable pageable) {
        return service.getAll(pageable);
    }

    /**
     * Endpoint para buscar foto por ID (GET /fotos-imoveis/{id})
     * Acesso: Qualquer um (visitante, logado)
     */
    @GetMapping("/{id}")
    public ResponseEntity<FotosImoveisModel> getById(@PathVariable Integer id) {
        FotosImoveisModel model = service.find(id);
        if (model != null) {
            return ResponseEntity.status(HttpStatus.OK).body(model);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Endpoint para listar fotos por ID do imóvel (GET /fotos-imoveis/imoveis/{id})
     * Acesso: Qualquer um (visitante, logado)
     */
    @GetMapping("/imoveis/{id}")
    public ResponseEntity<List<FotosImoveisModel>> getFotosByImovelId(@PathVariable Integer id) {
        // Buscar fotos pelo ID do imóvel
        List<FotosImoveisModel> fotos = service.findByImovelId(id); // Este método deve existir no service
        // Ou usar o método do repositório:
        // service.getAll().stream().filter(...).toList();
        // Mas é melhor ter um método específico no service/repo.
        return ResponseEntity.ok(fotos);
    }

    /**
     * Endpoint para criar uma foto via JSON (POST /fotos-imoveis)
     * Acesso: Somente admin (não faz sentido criar foto sem upload de arquivo para
     * o corretor)
     * OBS: Geralmente não é usado para upload de arquivos reais, mas pode ser útil
     * para placeholders.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody FotosImoveisDTO dto, HttpSession session) {
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        if (usuarioLogadoObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel usuarioLogado = (UserModel) usuarioLogadoObj;

        // Somente admin pode criar via JSON (sem upload de arquivo)
        if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Verificar se o imóvel associado existe e obter o objeto
        if (dto.getImovelId() != null) {
            ImoveisModel imovel = imoveisRepository.findById(dto.getImovelId()).orElse(null);
            if (imovel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            // Converter DTO para Model e associar o imóvel
            FotosImoveisModel model = new FotosImoveisModel();
            model.setNome_arquivo(dto.getNome_arquivo());
            model.setCaminho(dto.getCaminho());
            model.setCapa(dto.getCapa());
            model.setOrdem(dto.getOrdem());
            model.setImovel(imovel);

            FotosImoveisModel modelSalvo = service.insert(model);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(modelSalvo.getId()).toUri();
            return ResponseEntity.created(uri).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para atualizar dados de uma foto (PUT /fotos-imoveis/{id})
     * Acesso: Somente admin ou proprietário do imóvel associado à foto
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody FotosImoveisDTO dto, HttpSession session) {
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        if (usuarioLogadoObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel usuarioLogado = (UserModel) usuarioLogadoObj;

        try {
            // Buscar a foto existente
            FotosImoveisModel fotoExistente = service.find(id);
            if (fotoExistente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Verificar permissão: admin ou proprietário do imóvel associado à foto
            ImoveisModel imovelDaFoto = fotoExistente.getImovel();
            if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                    !imovelDaFoto.getUsuario().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Atualizar os campos permitidos com base no DTO
            // NÃO atualizamos o nome_arquivo ou caminho (o arquivo físico não muda)
            fotoExistente.setCapa(dto.getCapa());
            fotoExistente.setOrdem(dto.getOrdem());

            // Se o DTO tiver imovelId e o usuário for ADMIN, permite mudar o imóvel
            // associado
            if (dto.getImovelId() != null && usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN)) {
                ImoveisModel novoImovel = imoveisRepository.findById(dto.getImovelId()).orElse(null);
                if (novoImovel != null) {
                    fotoExistente.setImovel(novoImovel);
                } else {
                    return ResponseEntity.badRequest().body("Novo imóvel não encontrado.");
                }
            }

            // Salvar as alterações
            FotosImoveisModel modelAtualizado = service.update(fotoExistente);

            if (modelAtualizado != null) {
                return ResponseEntity.ok(modelAtualizado);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + e.getMessage());
        }
    }

    /**
     * Endpoint para excluir uma foto (DELETE /fotos-imoveis/{id})
     * Acesso: Somente admin ou proprietário do imóvel associado à foto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        Object usuarioLogadoObj = session.getAttribute("usuarioLogado");
        if (usuarioLogadoObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserModel usuarioLogado = (UserModel) usuarioLogadoObj;

        try {
            // Buscar a foto existente
            FotosImoveisModel foto = service.find(id);
            if (foto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Verificar permissão: admin ou proprietário do imóvel associado à foto
            ImoveisModel imovelDaFoto = foto.getImovel();
            if (!usuarioLogado.getTipo().equals(UserModel.Tipo.ADMIN) &&
                    !imovelDaFoto.getUsuario().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Excluir arquivo físico (opcional, mas recomendado)
            Path filePath = Paths.get(uploadDir, foto.getCaminho()).normalize();
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                System.err.println("Erro ao excluir arquivo físico: " + e.getMessage());
                // Mesmo com erro no arquivo, tentamos excluir o registro do banco
                // Poderia retornar um erro específico, mas por enquanto apenas logamos.
            }

            // Excluir registro do banco
            boolean deleted = service.delete(id);

            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir a foto: " + e.getMessage());
        }
    }
}