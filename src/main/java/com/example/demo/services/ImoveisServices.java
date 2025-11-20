package com.example.demo.services;

import com.example.demo.dto.ImoveisDTO;
import com.example.demo.dto.ImoveisListDTO;
import com.example.demo.model.ImoveisModel;
import com.example.demo.model.TiposImoveisModel;
import com.example.demo.model.BairrosModel;
import com.example.demo.model.UserModel;
import com.example.demo.repository.ImoveisRepository;
import com.example.demo.repository.TiposImoveisRepository;
import com.example.demo.repository.BairrosRepository;
import com.example.demo.repository.UserRepository; // Você já tem isso
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImoveisServices {

    @Autowired
    private ImoveisRepository repositorio;

    @Autowired
    private TiposImoveisRepository tiposImoveisRepository;

    @Autowired
    private BairrosRepository bairrosRepository;

    @Autowired
    private UserRepository usuariosRepository; // Para buscar o usuário

    /* public List<ImoveisModel> getAll() {
        return repositorio.findAll();
    }

    public Page<ImoveisModel> getAll(Pageable pageable) {
        Page<ImoveisModel> list = repositorio.findAll(pageable);
        return list;
    } */

    // --- MÉTODO GET ALL ATUALIZADO ---
    public List<ImoveisListDTO> getAll() {
        List<ImoveisModel> lista = repositorio.findAll();
        // Converter a lista de Model para DTO
        return lista.stream()
                .map(imovel -> new ImoveisListDTO(imovel)) // Usando o construtor que popula os dados dos relacionamentos
                .collect(Collectors.toList());
    }

    public Page<ImoveisListDTO> getAll(Pageable pageable) {
        Page<ImoveisModel> pageModel = repositorio.findAll(pageable);
        // Converter a página de Model para DTO
        return pageModel.map(imovel -> new ImoveisListDTO(imovel)); // Usando o construtor que popula os dados dos relacionamentos
    }

    public ImoveisModel find(Integer id) {
        Optional<ImoveisModel> model = repositorio.findById(id);
        return model.orElse(null);
    }

    public ImoveisModel insert(ImoveisModel model) {
        // Verificar se as entidades relacionadas existem
        if (model.getTipoImovel() != null) {
            TiposImoveisModel tipo = tiposImoveisRepository.findById(model.getTipoImovel().getId()).orElse(null);
            if (tipo == null) throw new RuntimeException("Tipo de imóvel não encontrado");
            model.setTipoImovel(tipo);
        }
        if (model.getBairro() != null) {
            BairrosModel bairro = bairrosRepository.findById(model.getBairro().getId()).orElse(null);
            if (bairro == null) throw new RuntimeException("Bairro não encontrado");
            model.setBairro(bairro);
        }
        if (model.getUsuario() != null) {
            UserModel usuario = usuariosRepository.findById(model.getUsuario().getId()).orElse(null);
            if (usuario == null) throw new RuntimeException("Usuário não encontrado");
            model.setUsuario(usuario);
        }
        return repositorio.save(model);
    }

    /* public ImoveisModel insert(ImoveisDTO dto) {
        ImoveisModel model = new ImoveisModel();
        model.setTitulo(dto.getTitulo());
        model.setDescricao(dto.getDescricao());
        model.setPreco_venda(dto.getPreco_venda());
        model.setPreco_aluguel(dto.getPreco_aluguel());
        model.setFinalidade(dto.getFinalidade());
        model.setStatus(dto.getStatus());
        model.setDormitorios(dto.getDormitorios());
        model.setBanheiros(dto.getBanheiros());
        model.setGaragem(dto.getGaragem());
        model.setArea_total(dto.getArea_total());
        model.setArea_construida(dto.getArea_construida());
        model.setEndereco(dto.getEndereco());
        model.setNumero(dto.getNumero());
        model.setComplemento(dto.getComplemento());
        model.setCep(dto.getCep());
        model.setCaracteristicas(dto.getCaracteristicas());
        model.setDestaque(dto.getDestaque());

        // Relacionamentos
        if (dto.getTipoImovelId() != null) {
            TiposImoveisModel tipo = tiposImoveisRepository.findById(dto.getTipoImovelId()).orElse(null);
            if (tipo == null) throw new RuntimeException("Tipo de imóvel não encontrado");
            model.setTipoImovel(tipo);
        }
        if (dto.getBairroId() != null) {
            BairrosModel bairro = bairrosRepository.findById(dto.getBairroId()).orElse(null);
            if (bairro == null) throw new RuntimeException("Bairro não encontrado");
            model.setBairro(bairro);
        }
        if (dto.getUsuarioId() != null) {
            UserModel usuario = usuariosRepository.findById(dto.getUsuarioId()).orElse(null);
            if (usuario == null) throw new RuntimeException("Usuário não encontrado");
            model.setUsuario(usuario);
        }

        return repositorio.save(model);
    } */ 
 
    public ImoveisModel update(ImoveisModel model) {
        try {
            if (find(model.getId()) != null) {
                // Verificar relacionamentos como no insert
                if (model.getTipoImovel() != null) {
                    TiposImoveisModel tipo = tiposImoveisRepository.findById(model.getTipoImovel().getId()).orElse(null);
                    if (tipo == null) throw new RuntimeException("Tipo de imóvel não encontrado");
                    model.setTipoImovel(tipo);
                }
                if (model.getBairro() != null) {
                    BairrosModel bairro = bairrosRepository.findById(model.getBairro().getId()).orElse(null);
                    if (bairro == null) throw new RuntimeException("Bairro não encontrado");
                    model.setBairro(bairro);
                }
                if (model.getUsuario() != null) {
                    UserModel usuario = usuariosRepository.findById(model.getUsuario().getId()).orElse(null);
                    if (usuario == null) throw new RuntimeException("Usuário não encontrado");
                    model.setUsuario(usuario);
                }
                return repositorio.save(model);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // --- MÉTODO INSERT USANDO DTO (NOVO) ---
    public ImoveisModel insert(ImoveisDTO dto) {
        ImoveisModel model = new ImoveisModel();

        // Copia os dados básicos do DTO para o Model
        copyDtoToModel(dto, model);

        // Salva o model no banco
        return repositorio.save(model);
    }

    // --- MÉTODO UPDATE USANDO DTO (NOVO) ---
    public ImoveisModel update(Integer id, ImoveisDTO dto) {
        Optional<ImoveisModel> optionalModel = repositorio.findById(id);
        if (optionalModel.isPresent()) {
            ImoveisModel model = optionalModel.get();

            // Copia os dados do DTO para o Model existente
            copyDtoToModel(dto, model);

            // Salva as alterações
            return repositorio.save(model);
        } else {
            return null; // Ou lançar exceção
        }
    }

    // --- MÉTODO AUXILIAR PARA COPIAR DADOS DO DTO PARA O MODEL ---
    private void copyDtoToModel(ImoveisDTO dto, ImoveisModel model) {
        model.setTitulo(dto.getTitulo());
        model.setDescricao(dto.getDescricao());
        model.setPreco_venda(dto.getPreco_venda());
        model.setPreco_aluguel(dto.getPreco_aluguel());
        model.setFinalidade(dto.getFinalidade());
        model.setStatus(dto.getStatus());
        model.setDormitorios(dto.getDormitorios());
        model.setBanheiros(dto.getBanheiros());
        model.setGaragem(dto.getGaragem());
        model.setArea_total(dto.getArea_total());
        model.setArea_construida(dto.getArea_construida());
        model.setEndereco(dto.getEndereco());
        model.setNumero(dto.getNumero());
        model.setComplemento(dto.getComplemento());
        model.setCep(dto.getCep());
        model.setCaracteristicas(dto.getCaracteristicas());
        model.setDestaque(dto.getDestaque());

        // --- Tratamento dos Relacionamentos ---
        // Busca e associa o Tipo de Imóvel
        if (dto.getTipoImovelId() != null) {
            TiposImoveisModel tipo = tiposImoveisRepository.findById(dto.getTipoImovelId()).orElse(null);
            if (tipo == null) {
                throw new RuntimeException("Tipo de Imóvel com ID " + dto.getTipoImovelId() + " não encontrado.");
            }
            model.setTipoImovel(tipo);
        } else {
             // Se for update e o ID for null, mantém o existente?
             // Ou define como null? Depende da regra de negócio.
             // Vamos assumir que, se for null, mantém o existente (no update).
             // Se for create e for null, o banco pode reclamar se for NOT NULL.
             // Para simplificar, vamos deixar passar e deixar o banco validar.
        }

        // Busca e associa o Bairro
        if (dto.getBairroId() != null) {
            BairrosModel bairro = bairrosRepository.findById(dto.getBairroId()).orElse(null);
            if (bairro == null) {
                throw new RuntimeException("Bairro com ID " + dto.getBairroId() + " não encontrado.");
            }
            model.setBairro(bairro);
        }

        // Busca e associa o Usuário
        if (dto.getUsuarioId() != null) {
            UserModel usuario = usuariosRepository.findById(dto.getUsuarioId()).orElse(null);
            if (usuario == null) {
                throw new RuntimeException("Usuário com ID " + dto.getUsuarioId() + " não encontrado.");
            }
            model.setUsuario(usuario);
        }
    }

    public List<ImoveisModel> buscarPorUsuarioId(Integer usuarioId) {
        return repositorio.findByUsuarioId(usuarioId); // Este método deve existir no Repository
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
}