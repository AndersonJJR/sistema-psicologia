package com.anderson.sistemapsicologia;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoRepository;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoService;
import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.common.AcessoNegadoClinicoException;
import com.anderson.sistemapsicologia.model.common.AesAttributeConverter;
import com.anderson.sistemapsicologia.model.common.CriptografiaAesService;
import com.anderson.sistemapsicologia.model.common.RegraNegocioException;
import com.anderson.sistemapsicologia.model.paciente.ExportacaoPortabilidadeService;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteService;
import com.anderson.sistemapsicologia.model.paciente.dtos.PacienteExportacaoDTO;
import com.anderson.sistemapsicologia.model.pagamento.CalculadoraSplitPagamento;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoRepository;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        Psicologo psicologoTitular = Psicologo.builder().idPsicologo(idPsicologoTitular).nome("Dra. Ana").numeroEpsi("EPSI-001").build();
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
                .numeroEpsi("EPSI-2026-99")
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
}
