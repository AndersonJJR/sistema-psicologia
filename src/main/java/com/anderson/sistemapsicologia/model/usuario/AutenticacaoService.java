package com.anderson.sistemapsicologia.model.usuario;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.usuario.dtos.LoginRequestDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.TokenResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.UsuarioCreateDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioCreateDTO dto, String enderecoIp) {
        if (usuarioRepository.existsByLogin(dto.login())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o login informado.");
        }
        if (dto.perfil() == PerfilUsuario.PSICOLOGO && dto.idPsicologoVinculado() == null) {
            throw new RegraNegocioException("Usuários com perfil PSICOLOGO devem possuir um idPsicologoVinculado.");
        }

        Usuario usuario = Usuario.builder()
                .login(dto.login())
                .senhaHash(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .idPsicologoVinculado(dto.idPsicologoVinculado())
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrarAcao(dto.login(), enderecoIp, AcaoAuditoria.INSERCAO, "USUARIO:" + salvo.getIdUsuario());
        return UsuarioResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public TokenResponseDTO autenticar(LoginRequestDTO dto, String enderecoIp) {
        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(dto.login())
                .orElseThrow(() -> new AcessoNegadoClinicoException("Credenciais inválidas ou usuário inativo."));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new AcessoNegadoClinicoException("Credenciais inválidas ou usuário inativo.");
        }

        String token = jwtTokenService.gerarToken(usuario);
        Instant expiraEm = jwtTokenService.calcularExpiracao();
        auditoriaService.registrarAcao(usuario.getLogin(), enderecoIp, AcaoAuditoria.LEITURA, "AUTH:LOGIN");

        return new TokenResponseDTO(
                token,
                "Bearer",
                usuario.getLogin(),
                usuario.getPerfil(),
                usuario.getIdPsicologoVinculado(),
                expiraEm
        );
    }

    public String obterLoginOperadorAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "SISTEMA";
        }
        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario.getLogin();
        }
        return auth.getName();
    }

    public Optional<UUID> obterIdPsicologoAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof Usuario usuario) {
            return Optional.ofNullable(usuario.getIdPsicologoVinculado());
        }
        return usuarioRepository.findByLoginAndAtivoTrue(auth.getName())
                .map(Usuario::getIdPsicologoVinculado);
    }
}
