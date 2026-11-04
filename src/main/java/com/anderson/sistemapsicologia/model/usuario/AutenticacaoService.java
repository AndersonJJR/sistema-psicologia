package com.anderson.sistemapsicologia.model.usuario;

import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoRepository;
import com.anderson.sistemapsicologia.model.usuario.dtos.LoginRequestDTO;
import com.anderson.sistemapsicologia.model.usuario.dtos.RegistroPsicologoAuthRequestDTO;
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
    private final PsicologoRepository psicologoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public TokenResponseDTO registrarPsicologo(RegistroPsicologoAuthRequestDTO dto, String enderecoIp) {
        String emailLogin = dto.email().trim().toLowerCase();
        if (usuarioRepository.existsByLogin(emailLogin)) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail informado.");
        }

        String crpNormalizado = dto.crp().trim();
        if (psicologoRepository.existsByCrp(crpNormalizado)) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o registro CRP informado.");
        }

        String digitosCrp = crpNormalizado.replaceAll("[^0-9]", "");
        if (digitosCrp.isBlank()) {
            digitosCrp = String.valueOf(Math.abs(emailLogin.hashCode()) % 900000 + 100000);
        }

        String cpfNormalizado = dto.cpf() != null && !dto.cpf().isBlank()
                ? dto.cpf().trim()
                : gerarCpfDeterministico(digitosCrp, emailLogin);

        if (psicologoRepository.existsByCpf(cpfNormalizado)) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o CPF informado.");
        }

        String numeroEpsiNormalizado = dto.numeroEpsi() != null && !dto.numeroEpsi().isBlank()
                ? dto.numeroEpsi().trim()
                : "EPSI-SP-2026-" + digitosCrp;

        if (psicologoRepository.existsByNumeroEpsi(numeroEpsiNormalizado)) {
            throw new RegraNegocioException("Já existe psicólogo cadastrado com o número e-Psi informado.");
        }

        String telefoneNormalizado = dto.telefone() != null && !dto.telefone().isBlank()
                ? dto.telefone().trim()
                : "(11) 98765-4321";

        Psicologo psicologo = Psicologo.builder()
                .nome(dto.nome().trim())
                .cpf(cpfNormalizado)
                .crp(crpNormalizado)
                .numeroEpsi(numeroEpsiNormalizado)
                .email(emailLogin)
                .telefone(telefoneNormalizado)
                .build();

        Psicologo psicologoSalvo = psicologoRepository.save(psicologo);

        Usuario usuario = Usuario.builder()
                .login(emailLogin)
                .senhaHash(passwordEncoder.encode(dto.senha()))
                .perfil(PerfilUsuario.PSICOLOGO)
                .idPsicologoVinculado(psicologoSalvo.getIdPsicologo())
                .build();

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        auditoriaService.registrarAcao(
                emailLogin,
                enderecoIp,
                AcaoAuditoria.INSERCAO,
                "CREDENCIAMENTO_EPSI:" + psicologoSalvo.getIdPsicologo()
        );

        String token = jwtTokenService.gerarToken(usuarioSalvo);
        Instant expiraEm = jwtTokenService.calcularExpiracao();

        return new TokenResponseDTO(
                token,
                "Bearer",
                usuarioSalvo.getLogin(),
                usuarioSalvo.getPerfil(),
                usuarioSalvo.getIdPsicologoVinculado(),
                expiraEm
        );
    }

    private String gerarCpfDeterministico(String digitosCrp, String email) {
        String base = (digitosCrp + Math.abs(email.hashCode()) + "00000000000").replaceAll("[^0-9]", "");
        String n = base.substring(0, 11);
        return n.substring(0, 3) + "." + n.substring(3, 6) + "." + n.substring(6, 9) + "-" + n.substring(9, 11);
    }

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
