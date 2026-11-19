package com.anderson.sistemapsicologia;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoRepository;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoRequestDTO;
import com.anderson.sistemapsicologia.model.agendamento.dtos.AgendamentoResponseDTO;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.AesAttributeConverter;
import com.anderson.sistemapsicologia.model.common.CriptografiaAesService;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.dashboard.DashboardService;
import com.anderson.sistemapsicologia.model.dashboard.dtos.DashboardResumoResponseDTO;
import com.anderson.sistemapsicologia.model.paciente.ExportacaoPortabilidadeService;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteRepository;
import com.anderson.sistemapsicologia.model.paciente.PacienteService;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteExportacaoDTO;
import com.anderson.sistemapsicologia.model.pagamento.CalculadoraSplitPagamento;
import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoRepository;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoService;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoRequestDTO;
import com.anderson.sistemapsicologia.model.prontuario.dtos.ProntuarioClinicoResponseDTO;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoRepository;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoService;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumental;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumentalRepository;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumentalService;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalRequestDTO;
import com.anderson.sistemapsicologia.model.registrodocumental.dtos.RegistroDocumentalResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.AutenticacaoService;
import com.anderson.sistemapsicologia.model.usuario.JwtTokenService;
import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;
import com.anderson.sistemapsicologia.model.usuario.Usuario;
import com.anderson.sistemapsicologia.view.ExportacaoPacienteCsvView;
import com.anderson.sistemapsicologia.view.ExportacaoPacientePdfAView;
import com.anderson.sistemapsicologia.model.upload.UploadService;
import com.anderson.sistemapsicologia.model.upload.dtos.UploadResponseDTO;
import com.anderson.sistemapsicologia.model.usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SistemaPsicologiaConformidadeTests {

    @Test
    @DisplayName("1. Criptografia em Nível da Aplicação (ALE AES-256-GCM): cifra antes de persistir e decifra ao ler")
    void deveCriptografarEDescriptografarComAes256Gcm() {
        AesAttributeConverter converter = new AesAttributeConverter();
        String segredoClinico = "Hipótese diagnóstica: Transtorno de Ansiedade Generalizada (CID F41.1)";

        String colunaBanco = converter.convertToDatabaseColumn(segredoClinico);

        assertNotNull(colunaBanco);
        assertTrue(colunaBanco.startsWith("ENC:AES256:"));
        assertNotEquals(segredoClinico, colunaBanco);
        assertFalse(colunaBanco.contains("Ansiedade"));

        String textoRecuperado = converter.convertToEntityAttribute(colunaBanco);
        assertEquals(segredoClinico, textoRecuperado);
    }

    @Test
    @DisplayName("2. Segregação CFP 001/2009: apenas o psicólogo titular (JWT) acessa REGISTROS_DOCUMENTAIS")
    void deveBloquearOutroPsicologoDaMesmaClinicaEmRegistrosDocumentais() {
        RegistroDocumentalRepository repository = Mockito.mock(RegistroDocumentalRepository.class);
        AgendamentoService agendamentoService = Mockito.mock(AgendamentoService.class);
        AuditoriaService auditoriaService = Mockito.mock(AuditoriaService.class);
        AutenticacaoService autenticacaoService = Mockito.mock(AutenticacaoService.class);

        RegistroDocumentalService service = new RegistroDocumentalService(
                repository, agendamentoService, auditoriaService, autenticacaoService
        );

        UUID idPsicologoTitular = UUID.randomUUID();
        UUID idPaciente = UUID.randomUUID();
        UUID idAgendamento = UUID.randomUUID();
        UUID idRegistro = UUID.randomUUID();
        UUID idOutroPsicologo = UUID.randomUUID();

        Psicologo psicologoTitular = Psicologo.builder().idPsicologo(idPsicologoTitular).nome("Dra. Ana").build();
        Paciente paciente = Paciente.builder().idPaciente(idPaciente).nome("Carlos").build();
        Agendamento agendamento = Agendamento.builder()
                .idAgendamento(idAgendamento)
                .psicologo(psicologoTitular)
                .paciente(paciente)
                .build();

        RegistroDocumental registroExistente = RegistroDocumental.builder()
                .idRegistroDocumental(idRegistro)
                .agendamento(agendamento)
                .hipotesesProvisorias("Hipótese sigilosa")
                .notasSupervisao("Nota de supervisão")
                .dataRegistro(Instant.now())
                .build();

        when(repository.findByIdRegistroDocumentalAndAtivoTrue(idRegistro)).thenReturn(Optional.of(registroExistente));

        // Psicólogo (outro profissional da mesma clínica) tenta ler o registro do Psicólogo titular
        when(autenticacaoService.obterIdPsicologoAutenticado()).thenReturn(Optional.of(idOutroPsicologo));
        when(autenticacaoService.obterLoginOperadorAtual()).thenReturn("psicologo.outro@clinica.com");

        assertThrows(
                AcessoNegadoClinicoException.class,
                () -> service.buscarPorId(idRegistro, "127.0.0.1")
        );

        // Psicólogo titular consegue ler normalmente
        when(autenticacaoService.obterIdPsicologoAutenticado()).thenReturn(Optional.of(idPsicologoTitular));
        when(autenticacaoService.obterLoginOperadorAtual()).thenReturn("dra.ana@clinica.com");

        RegistroDocumentalResponseDTO resposta = service.buscarPorId(idRegistro, "127.0.0.1");
        assertEquals(idPsicologoTitular, resposta.idPsicologoTitular());
        assertEquals("Hipótese sigilosa", resposta.hipotesesProvisorias());
    }

    @Test
    @DisplayName("3. Engenharia Financeira: Split de Pagamentos previne bitributação e valida soma exata")
    void deveCalcularEValidarSplitDePagamentos() {
        CalculadoraSplitPagamento calculadora = new CalculadoraSplitPagamento();

        CalculadoraSplitPagamento.ResultadoSplit resultado = calculadora.processarSplit(
                new BigDecimal("200.00"),
                new BigDecimal("60.00"),
                new BigDecimal("140.00")
        );

        assertEquals(new BigDecimal("200.00"), resultado.valorTotal());
        assertEquals(new BigDecimal("60.00"), resultado.taxaClinicaInfraestrutura());
        assertEquals(new BigDecimal("140.00"), resultado.repasseProfissional());

        // Deve rejeitar split inconsistente
        assertThrows(
                RegraNegocioException.class,
                () -> calculadora.processarSplit(
                        new BigDecimal("200.00"),
                        new BigDecimal("60.00"),
                        new BigDecimal("150.00")
                )
        );
    }

    @Test
    @DisplayName("4. Segurança JWT: gera e valida token com perfil RBAC e vínculo de psicólogo titular")
    void deveGerarEValidarTokenJwtComClaimsRbac() {
        JwtTokenService jwtService = new JwtTokenService(
                "c2lzdGVtYS1wc2ljb2xvZ2lhLWp3dC1zZWNyZXQta2V5LTI1Ni1iaXRzLXNlY3VyZQ==",
                3600000L
        );

        UUID idUsuario = UUID.randomUUID();
        UUID idPsicologoVinculado = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
                .idUsuario(idUsuario)
                .login("psicologa.titular")
                .perfil(PerfilUsuario.PSICOLOGO)
                .idPsicologoVinculado(idPsicologoVinculado)
                .senhaHash("hash")
                .build();

        String token = jwtService.gerarToken(usuario);
        Optional<JwtTokenService.ClaimsJwt> claims = jwtService.validarToken(token);

        assertTrue(claims.isPresent());
        assertEquals("psicologa.titular", claims.get().login());
        assertEquals(PerfilUsuario.PSICOLOGO, claims.get().perfil());
        assertEquals(idPsicologoVinculado, claims.get().idPsicologoVinculado());
        assertEquals(idUsuario, claims.get().idUsuario());
    }

    @Test
    @DisplayName("5. Retenção de 20 anos (Lei 13.787/2018): Soft Delete preserva registro e registra deleted_at")
    void deveExecutarSoftDeletePreservandoDadosClinicos() {
        Paciente paciente = Paciente.builder()
                .idPaciente(UUID.randomUUID())
                .nome("Mariana Silva")
                .cpf("123.456.789-00")
                .contatoEmergenciaNome("João Silva")
                .contatoEmergenciaTelefone("(11) 99999-0000")
                .enderecoLocalSessao("Rua das Flores, 100 - São Paulo/SP")
                .build();

        assertTrue(paciente.isAtivo());
        paciente.realizarSoftDelete();

        assertFalse(paciente.isAtivo());
        assertNotNull(paciente.getDeletedAt());
    }

    @Test
    @DisplayName("6. Portabilidade em Lote LGPD: gera JSON, CSV aberto e binário PDF/A-1b válido")
    void deveExportarDossieEmFormatoAbertoJsonCsvEPdfA() {
        PacienteService pacienteService = Mockito.mock(PacienteService.class);
        AgendamentoRepository agendamentoRepo = Mockito.mock(AgendamentoRepository.class);
        ProntuarioClinicoRepository prontuarioRepo = Mockito.mock(ProntuarioClinicoRepository.class);
        PagamentoRepository pagamentoRepo = Mockito.mock(PagamentoRepository.class);
        AuditoriaService auditoriaService = Mockito.mock(AuditoriaService.class);
        AutenticacaoService autenticacaoService = Mockito.mock(AutenticacaoService.class);

        ExportacaoPortabilidadeService exportacaoService = new ExportacaoPortabilidadeService(
                pacienteService,
                agendamentoRepo,
                prontuarioRepo,
                pagamentoRepo,
                new ExportacaoPacienteCsvView(),
                new ExportacaoPacientePdfAView(),
                auditoriaService,
                autenticacaoService
        );

        UUID idPaciente = UUID.randomUUID();
        UUID idPsicologo = UUID.randomUUID();
        UUID idAgendamento = UUID.randomUUID();
        UUID idProntuario = UUID.randomUUID();

        Paciente paciente = Paciente.builder()
                .idPaciente(idPaciente)
                .nome("Paciente Portabilidade LGPD")
                .cpf("111.222.333-44")
                .dataNascimento(LocalDate.of(1992, 5, 14))
                .email("paciente@email.com")
                .telefone("11988887777")
                .contatoEmergenciaNome("Maria Emergência")
                .contatoEmergenciaTelefone("11977776666")
                .enderecoLocalSessao("Av. Paulista, 1000 - São Paulo/SP")
                .build();

        Psicologo psicologo = Psicologo.builder()
                .idPsicologo(idPsicologo)
                .nome("Dr. Roberto")
                .crp("06/12345")
                .build();

        Agendamento agendamento = Agendamento.builder()
                .idAgendamento(idAgendamento)
                .paciente(paciente)
                .psicologo(psicologo)
                .dataHoraSessao(LocalDateTime.of(2026, 10, 15, 14, 0))
                .modalidade(ModalidadeAtendimento.TELECONSULTA)
                .status(StatusAgendamento.REALIZADO)
                .enderecoLocalSessao("Av. Paulista, 1000 - São Paulo/SP")
                .build();

        ProntuarioClinico prontuario = ProntuarioClinico.builder()
                .idProntuario(idProntuario)
                .agendamento(agendamento)
                .procedimentosTecnicos("Escuta clínica e reestruturação cognitiva")
                .evolucaoObservavel("Paciente apresentou redução significativa de sintomas ansiosos")
                .dataRegistro(Instant.now())
                .build();

        when(pacienteService.buscarEntidadePorId(idPaciente)).thenReturn(paciente);
        when(agendamentoRepo.findAllByPacienteIdPacienteOrderByDataHoraSessaoDesc(idPaciente)).thenReturn(List.of(agendamento));
        when(prontuarioRepo.findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataRegistroDesc(idPaciente)).thenReturn(List.of(prontuario));
        when(pagamentoRepo.findAllByAgendamentoPacienteIdPacienteAndAtivoTrueOrderByDataPagamentoDesc(idPaciente)).thenReturn(List.of());
        when(autenticacaoService.obterLoginOperadorAtual()).thenReturn("dr.roberto");

        PacienteExportacaoDTO exportacao = exportacaoService.exportarDossiePaciente(idPaciente, "127.0.0.1");

        assertNotNull(exportacao);
        assertEquals("Paciente Portabilidade LGPD", exportacao.dadosDemograficos().nome());
        assertTrue(exportacao.conteudoCsvAberto().contains("PRONTUARIO_CLINICO"));
        assertTrue(exportacao.conteudoCsvAberto().contains("reestruturação cognitiva"));

        byte[] pdfBytes = Base64.getDecoder().decode(exportacao.documentoPdfABase64());
        String pdfTexto = new String(pdfBytes, StandardCharsets.UTF_8);
        assertTrue(pdfTexto.startsWith("%PDF-1.4"));
        assertTrue(pdfTexto.contains("<pdfaid:part>1</pdfaid:part>"));
        assertTrue(pdfTexto.contains("<pdfaid:conformance>B</pdfaid:conformance>"));
    }

    @Test
    @DisplayName("7. Assinatura Digital SHA-256 e Imutabilidade do Prontuário Clínico (CFP 001/2009)")
    void deveAssinarProntuarioDigitalmenteEBloquearEdicoesPosteriores() {
        ProntuarioClinicoRepository prontuarioRepo = Mockito.mock(ProntuarioClinicoRepository.class);
        RegistroDocumentalRepository registroRepo = Mockito.mock(RegistroDocumentalRepository.class);
        AgendamentoService agendamentoService = Mockito.mock(AgendamentoService.class);
        AuditoriaService auditoriaService = Mockito.mock(AuditoriaService.class);
        AutenticacaoService autenticacaoService = Mockito.mock(AutenticacaoService.class);

        ProntuarioClinicoService service = new ProntuarioClinicoService(
                prontuarioRepo,
                registroRepo,
                agendamentoService,
                auditoriaService,
                autenticacaoService
        );

        UUID idAgendamento = UUID.randomUUID();
        Psicologo psicologo = Psicologo.builder().idPsicologo(UUID.randomUUID()).nome("Dra. Juliana").crp("06/142980").build();
        Paciente paciente = Paciente.builder().idPaciente(UUID.randomUUID()).nome("Lucas").build();
        Agendamento agendamento = Agendamento.builder()
                .idAgendamento(idAgendamento)
                .psicologo(psicologo)
                .paciente(paciente)
                .build();

        ProntuarioClinico rascunho = ProntuarioClinico.builder()
                .idProntuario(UUID.randomUUID())
                .agendamento(agendamento)
                .procedimentosTecnicos("Procedimentos iniciais")
                .evolucaoObservavel("Evolução inicial")
                .dataRegistro(Instant.now())
                .assinado(false)
                .build();

        when(prontuarioRepo.findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)).thenReturn(Optional.of(rascunho));
        when(prontuarioRepo.save(any(ProntuarioClinico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(registroRepo.findByAgendamentoIdAgendamentoAndAtivoTrue(idAgendamento)).thenReturn(Optional.empty());
        when(autenticacaoService.obterLoginOperadorAtual()).thenReturn("dra.juliana@psicosafe.com.br");

        ProntuarioClinicoResponseDTO assinado = service.assinarPorAgendamento(idAgendamento, "127.0.0.1");
        assertTrue(assinado.assinado());
        assertNotNull(assinado.hashAssinatura());
        assertTrue(assinado.hashAssinatura().startsWith("SHA-256 • "));

        // Tentativa de alterar prontuário já assinado deve falhar
        assertThrows(
                RegraNegocioException.class,
                () -> service.criar(
                        new ProntuarioClinicoRequestDTO(idAgendamento, "Tentativa de edição", "Evolução alterada"),
                        "127.0.0.1"
                )
        );
    }

    @Test
    @DisplayName("8. Upload de arquivos de imagem e vinculação às entidades no banco de dados")
    void deveSalvarUploadDeImagemEVincularEntidades() {
        PsicologoRepository psicologoRepo = Mockito.mock(PsicologoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        UsuarioRepository usuarioRepo = Mockito.mock(UsuarioRepository.class);

        UploadService uploadService = new UploadService(psicologoRepo, pacienteRepo, usuarioRepo);

        UUID idPsicologo = UUID.randomUUID();
        Psicologo psicologo = Psicologo.builder()
                .idPsicologo(idPsicologo)
                .nome("Dra. Juliana Mendes Silveira")
                .cpf("123.456.789-00")
                .crp("06/142980")
                .email("dra.juliana@psicosafe.com.br")
                .telefone("(11) 98765-4321")
                .build();

        Usuario usuario = Usuario.builder()
                .idUsuario(UUID.randomUUID())
                .login("dra.juliana@psicosafe.com.br")
                .idPsicologoVinculado(idPsicologo)
                .build();

        when(psicologoRepo.findById(idPsicologo)).thenReturn(Optional.of(psicologo));
        when(psicologoRepo.save(any(Psicologo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepo.findByIdPsicologoVinculado(idPsicologo)).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "perfil.png",
                "image/png",
                "conteudo-de-teste-imagem".getBytes(StandardCharsets.UTF_8)
        );

        UploadResponseDTO resposta = uploadService.salvarAvatarPsicologo(idPsicologo, arquivo);

        assertNotNull(resposta);
        assertNotNull(resposta.url());
        assertTrue(resposta.url().startsWith("/api/v1/uploads/psicologos/"));
        assertEquals(resposta.url(), psicologo.getAvatarUrl());
        assertEquals(resposta.url(), usuario.getAvatarUrl());
    }

    @Test
    @DisplayName("9. Agendamento com duração, recorrência semanal e geração automática de split 30%/70%")
    void deveCriarAgendamentoComRecorrenciaESplitAutomatico() {
        AgendamentoRepository agendamentoRepo = Mockito.mock(AgendamentoRepository.class);
        PsicologoService psicologoService = Mockito.mock(PsicologoService.class);
        PacienteService pacienteService = Mockito.mock(PacienteService.class);
        PagamentoRepository pagamentoRepo = Mockito.mock(PagamentoRepository.class);
        CalculadoraSplitPagamento calculadoraSplit = new CalculadoraSplitPagamento();
        AuditoriaService auditoriaService = Mockito.mock(AuditoriaService.class);
        AutenticacaoService autenticacaoService = Mockito.mock(AutenticacaoService.class);

        AgendamentoService service = new AgendamentoService(
                agendamentoRepo,
                psicologoService,
                pacienteService,
                pagamentoRepo,
                calculadoraSplit,
                auditoriaService,
                autenticacaoService
        );

        UUID idPsicologo = UUID.randomUUID();
        UUID idPaciente = UUID.randomUUID();
        Psicologo psicologo = Psicologo.builder().idPsicologo(idPsicologo).nome("Dra. Juliana").build();
        Paciente paciente = Paciente.builder()
                .idPaciente(idPaciente)
                .nome("Mariana Duarte Farias")
                .enderecoLocalSessao("Rua das Flores, 100")
                .build();

        when(psicologoService.buscarEntidadeAtiva(idPsicologo)).thenReturn(psicologo);
        when(pacienteService.buscarEntidadeAtiva(idPaciente)).thenReturn(paciente);
        when(agendamentoRepo.existsByPsicologoIdPsicologoAndDataHoraSessaoAndAtivoTrue(any(), any())).thenReturn(false);
        when(agendamentoRepo.save(any(Agendamento.class))).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setIdAgendamento(UUID.randomUUID());
            return a;
        });
        when(pagamentoRepo.save(any(Pagamento.class))).thenAnswer(inv -> {
            Pagamento p = inv.getArgument(0);
            p.setIdPagamento(UUID.randomUUID());
            return p;
        });
        when(autenticacaoService.obterLoginOperadorAtual()).thenReturn("dra.juliana@psicosafe.com.br");

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(
                idPsicologo,
                idPaciente,
                LocalDateTime.of(2026, 11, 20, 16, 0),
                ModalidadeAtendimento.TELECONSULTA,
                StatusAgendamento.AGENDADO,
                null,
                50,
                "Sessão Individual Ordinária",
                true,
                3,
                new BigDecimal("250.00"),
                true
        );

        AgendamentoResponseDTO resposta = service.criar(dto, "127.0.0.1");

        assertNotNull(resposta.idAgendamento());
        assertEquals(50, resposta.duracaoMinutos());
        assertEquals("Sessão Individual Ordinária", resposta.tipoSessao());
        assertTrue(resposta.enviarLembrete24h());
        Mockito.verify(agendamentoRepo, Mockito.times(3)).save(any(Agendamento.class));
        Mockito.verify(pagamentoRepo, Mockito.times(3)).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("10. Dashboard Clínico e Financeiro: consolida métricas do dia, assiduidade, pendências e repasse mensal")
    void deveConsolidarResumoDoDashboardClinicoEFinanceiro() {
        AgendamentoRepository agendamentoRepo = Mockito.mock(AgendamentoRepository.class);
        PagamentoRepository pagamentoRepo = Mockito.mock(PagamentoRepository.class);
        ProntuarioClinicoRepository prontuarioRepo = Mockito.mock(ProntuarioClinicoRepository.class);
        PacienteRepository pacienteRepo = Mockito.mock(PacienteRepository.class);
        AutenticacaoService autenticacaoService = Mockito.mock(AutenticacaoService.class);

        DashboardService dashboardService = new DashboardService(
                agendamentoRepo,
                pagamentoRepo,
                prontuarioRepo,
                pacienteRepo,
                autenticacaoService
        );

        UUID idPsicologo = UUID.randomUUID();
        Psicologo psicologo = Psicologo.builder()
                .idPsicologo(idPsicologo)
                .nome("Dra. Juliana Mendes")
                .abordagemClinica("tcc")
                .build();
        Paciente paciente = Paciente.builder()
                .idPaciente(UUID.randomUUID())
                .nome("Lucas Gabriel")
                .dataNascimento(LocalDate.of(1994, 5, 10))
                .enderecoLocalSessao("Rua A, 10")
                .build();

        Agendamento agendamentoHoje = Agendamento.builder()
                .idAgendamento(UUID.randomUUID())
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHoraSessao(LocalDateTime.now().plusMinutes(20))
                .modalidade(ModalidadeAtendimento.TELECONSULTA)
                .status(StatusAgendamento.AGENDADO)
                .duracaoMinutos(50)
                .tipoSessao("Sessão Individual Ordinária")
                .enderecoLocalSessao("Rua A, 10")
                .build();

        Agendamento agendamentoRealizado = Agendamento.builder()
                .idAgendamento(UUID.randomUUID())
                .psicologo(psicologo)
                .paciente(paciente)
                .dataHoraSessao(LocalDateTime.now().minusHours(2))
                .modalidade(ModalidadeAtendimento.PRESENCIAL)
                .status(StatusAgendamento.REALIZADO)
                .duracaoMinutos(50)
                .tipoSessao("Sessão Individual Ordinária")
                .enderecoLocalSessao("Sala 1")
                .build();

        Pagamento pagamento = Pagamento.builder()
                .idPagamento(UUID.randomUUID())
                .agendamento(agendamentoRealizado)
                .valorTotal(new BigDecimal("250.00"))
                .taxaClinicaInfraestrutura(new BigDecimal("75.00"))
                .repasseProfissional(new BigDecimal("175.00"))
                .meioPagamento("PIX")
                .dataPagamento(Instant.now())
                .build();

        ProntuarioClinico prontuarioRascunho = ProntuarioClinico.builder()
                .idProntuario(UUID.randomUUID())
                .agendamento(agendamentoRealizado)
                .procedimentosTecnicos("TCC")
                .evolucaoObservavel("Em andamento")
                .dataRegistro(Instant.now())
                .assinado(false)
                .build();

        when(autenticacaoService.obterIdPsicologoAutenticado()).thenReturn(Optional.of(idPsicologo));
        when(agendamentoRepo.findAllByAtivoTrueOrderByDataHoraSessaoDesc())
                .thenReturn(List.of(agendamentoHoje, agendamentoRealizado));
        when(pagamentoRepo.findAllByAtivoTrueOrderByDataPagamentoDesc()).thenReturn(List.of(pagamento));
        when(prontuarioRepo.findAllByAtivoTrueOrderByDataRegistroDesc()).thenReturn(List.of(prontuarioRascunho));
        when(pacienteRepo.findAllByAtivoTrue()).thenReturn(List.of(paciente));

        DashboardResumoResponseDTO resumo = dashboardService.obterResumo();

        assertEquals(new BigDecimal("175.00"), resumo.repasseLiquidoMes());
        assertEquals(new BigDecimal("250.00"), resumo.valorBrutoMes());
        assertEquals(2L, resumo.totalConsultasHoje());
        assertEquals(1L, resumo.totalTelessaudeHoje());
        assertEquals(1L, resumo.totalPresenciaisHoje());
        assertNotNull(resumo.proximaConsulta());
        assertEquals(100.0, resumo.taxaAssiduidade());
        assertEquals(1L, resumo.pacientesAtivos());
        assertEquals(1L, resumo.prontuariosEmRascunho());
        assertEquals(2, resumo.agendamentosHoje().size());
        assertEquals(1, resumo.atendimentosRecentes().size());
        assertEquals("RASCUNHO", resumo.atendimentosRecentes().get(0).prontuarioStatus());
    }
}
