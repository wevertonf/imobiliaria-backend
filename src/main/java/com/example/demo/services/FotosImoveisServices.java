package com.example.demo.services;

import com.example.demo.dto.FotosImoveisDTO;
import com.example.demo.model.FotosImoveisModel;
import com.example.demo.model.ImoveisModel;
import com.example.demo.repository.FotosImoveisRepository;
import com.example.demo.repository.ImoveisRepository; // Para buscar imóvel
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;// Para limpar nomes de arquivos
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FotosImoveisServices {

    @Autowired
    private FotosImoveisRepository repositorio;

    @Autowired
    private ImoveisRepository imoveisRepository; // Para buscar imóvel

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public List<FotosImoveisModel> getAll() {
        return repositorio.findAll();
    }

    public Page<FotosImoveisModel> getAll(Pageable pageable) {
        Page<FotosImoveisModel> list = repositorio.findAll(pageable);
        return list;
    }

    public FotosImoveisModel find(Integer id) {
        Optional<FotosImoveisModel> model = repositorio.findById(id);
        return model.orElse(null);
    }

    public FotosImoveisModel insert(FotosImoveisModel model) {
        // Verificar se o imóvel existe
        if (model.getImovel() != null) {
            ImoveisModel imovel = imoveisRepository.findById(model.getImovel().getId()).orElse(null);
            if (imovel == null)
                throw new RuntimeException("Imóvel não encontrado");
            model.setImovel(imovel);
        }
        return repositorio.save(model);
    }

    public FotosImoveisModel insert(FotosImoveisDTO dto) {
        FotosImoveisModel model = new FotosImoveisModel();
        model.setNome_arquivo(dto.getNome_arquivo());
        model.setCaminho(dto.getCaminho());
        model.setCapa(dto.getCapa());
        model.setOrdem(dto.getOrdem());

        // Relacionamento com imóvel
        if (dto.getImovelId() != null) {
            ImoveisModel imovel = imoveisRepository.findById(dto.getImovelId()).orElse(null);
            if (imovel == null)
                throw new RuntimeException("Imóvel não encontrado");
            model.setImovel(imovel);
        }

        return repositorio.save(model);
    }

    // --- MÉTODO UPDATE ATUALIZADO (recebe Model) ---
    public FotosImoveisModel update(FotosImoveisModel model) {
        try {
            if (find(model.getId()) != null) {
                return repositorio.save(model);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Atualiza os dados de uma foto de imóvel existente com base em um DTO.
     *
     * @param id  O ID da foto a ser atualizada.
     * @param dto O DTO com os novos dados.
     * @return O modelo atualizado, ou null se não encontrado.
     * @throws RuntimeException Se houver erro de validação (ex: imovelId inválido).
     */
    public FotosImoveisModel update(Integer id, FotosImoveisDTO dto) {
        // 1. Buscar o modelo existente pelo ID
        Optional<FotosImoveisModel> optionalModel = repositorio.findById(id);
        if (optionalModel.isPresent()) {
            FotosImoveisModel model = optionalModel.get();

            // 2. Atualizar os campos do modelo com os dados do DTO
            // Somente atualize os campos que fazem sentido e estão no DTO
            if (dto.getCapa() != null) {
                model.setCapa(dto.getCapa());
            }
            if (dto.getOrdem() != null) {
                model.setOrdem(dto.getOrdem());
            }
            if (dto.getNome_arquivo() != null) {
                model.setNome_arquivo(dto.getNome_arquivo());
            }
            // Se quiser permitir mudar o imóvel associado:
            if (dto.getImovelId() != null) {
                // Verificar se o novo imóvel existe
                ImoveisModel novoImovel = imoveisRepository.findById(dto.getImovelId()).orElse(null);
                if (novoImovel == null) {
                    throw new RuntimeException("Imóvel com ID " + dto.getImovelId() + " não encontrado.");
                }
                model.setImovel(novoImovel);
            }

            // 3. Salvar o modelo atualizado no banco de dados
            return repositorio.save(model);
        } else {
            // 4. Se não encontrado, retornar null (o controller trata)
            return null;
        }
        // Obs: Não capturamos exceções genéricas, deixando o controller tratar,ou lançar RuntimeException para erros específicos de negócio.
    }

    /* public boolean delete(Integer id) {
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
    } */

    public boolean delete(Integer id) {
        try {
            // 1. Buscar a foto pelo ID para obter o caminho do arquivo
            Optional<FotosImoveisModel> optionalFoto = repositorio.findById(id);
            if (optionalFoto.isPresent()) {
                FotosImoveisModel foto = optionalFoto.get();

                // 2. Construir o caminho completo do arquivo
                // Assumindo que 'foto.getCaminho()' armazena o caminho relativo ao uploadDir
                Path caminhoArquivo = Paths.get(uploadDir, foto.getCaminho()).normalize();

                // 3. Excluir o arquivo do sistema de arquivos
                // Verifica se o arquivo existe antes de tentar excluí-lo
                if (Files.exists(caminhoArquivo)) {
                    Files.delete(caminhoArquivo);
                    // System.out.println("Arquivo excluído: " + caminhoArquivo.toAbsolutePath());
                } else {
                    // Opcional: Logar um aviso se o arquivo não for encontrado
                    System.out.println("Aviso: Arquivo não encontrado para exclusão: " + caminhoArquivo.toAbsolutePath());
                    // Dependendo da sua política, você pode optar por lançar uma exceção ou continuar
                }

                // 4. Excluir o registro do banco de dados
                repositorio.deleteById(id);
                return true;
            } else {
                // Foto com o ID fornecido não foi encontrada
                return false;
            }
        } catch (Exception e) {
            // Captura qualquer exceção (IO, banco de dados, etc.)
            System.err.println("Erro ao excluir foto com ID " + id + ": " + e.getMessage());
            // Você pode escolher relançar a exceção ou retornar false
            // return false; // Ou lançar uma RuntimeException personalizada
            throw new RuntimeException("Falha ao excluir a foto: " + e.getMessage(), e);
        }
    }

    // --- MÉTODO PARA BUSCAR POR IMÓVEL (usado no controller) ---
    public List<FotosImoveisModel> findByImovelId(Integer imovelId) {
        return repositorio.findByImovelId(imovelId);
    }

    // --- NOVO MÉTODO PARA UPLOAD ---
    /**
     * Cria um FotosImoveisModel a partir de um DTO e do caminho do arquivo salvo.
     * Assume que o DTO contém o imovelId.
     *
     * @param dto             O DTO com os dados.
     * @param caminhoRelativo O caminho relativo onde o arquivo foi salvo.
     * @param nomeArquivo     O nome do arquivo salvo.
     * @return O modelo salvo no banco.
     * @throws RuntimeException Se o imóvel não for encontrado.
     */
    public FotosImoveisModel createFromDtoAndPath(FotosImoveisDTO dto, String caminhoRelativo, String nomeArquivo) {
        // 1. Buscar o imóvel associado
        ImoveisModel imovel = imoveisRepository.findById(dto.getImovelId())
                .orElseThrow(() -> new RuntimeException("Imóvel com ID " + dto.getImovelId() + " não encontrado."));

        // 2. Criar o modelo FotosImoveisModel
        FotosImoveisModel model = new FotosImoveisModel();
        model.setNome_arquivo(nomeArquivo);
        model.setCaminho(caminhoRelativo); // Ou uma URL completa se desejar
        model.setCapa(false); // Valor padrão, pode vir do DTO
        model.setOrdem(0); // Valor padrão, pode vir do DTO

        // Associar ao imóvel
        model.setImovel(imovel);

        // 3. Salvar o modelo no banco de dados
        return repositorio.save(model);
    }

}