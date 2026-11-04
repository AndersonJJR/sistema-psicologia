package com.anderson.sistemapsicologia.model.common;

import com.anderson.sistemapsicologia.model.agendamento.Agendamento;
import com.anderson.sistemapsicologia.model.agendamento.AgendamentoRepository;
import com.anderson.sistemapsicologia.model.agendamento.ModalidadeAtendimento;
import com.anderson.sistemapsicologia.model.agendamento.StatusAgendamento;
import com.anderson.sistemapsicologia.model.auditoria.AcaoAuditoria;
import com.anderson.sistemapsicologia.model.auditoria.AuditoriaService;
import com.anderson.sistemapsicologia.model.paciente.Paciente;
import com.anderson.sistemapsicologia.model.paciente.PacienteRepository;
import com.anderson.sistemapsicologia.model.pagamento.Pagamento;
import com.anderson.sistemapsicologia.model.pagamento.PagamentoRepository;
import com.anderson.sistemapsicologia.model.pagamento.StatusSplit;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinico;
import com.anderson.sistemapsicologia.model.prontuario.ProntuarioClinicoRepository;
import com.anderson.sistemapsicologia.model.psicologo.Psicologo;
import com.anderson.sistemapsicologia.model.psicologo.PsicologoRepository;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumental;
import com.anderson.sistemapsicologia.model.registrodocumental.RegistroDocumentalRepository;
import com.anderson.sistemapsicologia.model.usuario.PerfilUsuario;
import com.anderson.sistemapsicologia.model.usuario.Usuario;
import com.anderson.sistemapsicologia.model.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inicializador idempotente de dados clínicos base para homologação e operação integrada Front-end / Back-end.
 */
@Component
@RequiredArgsConstructor
public class ClinicalDataInitializer implements CommandLineRunner {

    private final PsicologoRepository psicologoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProntuarioClinicoRepository prontuarioRepository;
    private final RegistroDocumentalRepository registroDocumentalRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByLogin("dra.juliana@psicosafe.com.br")) {
            return;
        }

        Psicologo draJuliana = psicologoRepository.findAllByAtivoTrue().stream()
                .filter(p -> "123.456.789-00".equals(p.getCpf()) || "06/142980 - SP".equals(p.getCrp()))
                .findFirst()
                .orElseGet(() -> psicologoRepository.save(Psicologo.builder()
                        .nome("Dra. Juliana Mendes Silveira")
                        .cpf("123.456.789-00")
                        .crp("06/142980 - SP")
                        .numeroEpsi("EPSI-SP-2023-88412")
                        .email("dra.juliana@psicosafe.com.br")
                        .telefone("(11) 98765-4321")
                        .build()));

        usuarioRepository.save(Usuario.builder()
                .login("dra.juliana@psicosafe.com.br")
                .senhaHash(passwordEncoder.encode("PsicoSafe@2026"))
                .perfil(PerfilUsuario.PSICOLOGO)
                .idPsicologoVinculado(draJuliana.getIdPsicologo())
                .build());

        Paciente lucas = pacienteRepository.findAllByAtivoTrue().stream()
                .filter(p -> "342.189.058-22".equals(p.getCpf()))
                .findFirst()
                .orElseGet(() -> pacienteRepository.save(Paciente.builder()
                        .nome("Lucas Gabriel Albuquerque Rocha")
                        .cpf("342.189.058-22")
                        .dataNascimento(LocalDate.of(1992, 5, 14))
                        .email("lucas.albuquerque@email.com")
                        .telefone("(11) 97123-4567")
                        .contatoEmergenciaNome("Mariana Albuquerque (Irmã)")
                        .contatoEmergenciaTelefone("(11) 99887-6655")
                        .enderecoLocalSessao("Rua das Camélias, 450, Apto 82, Vila Mariana, São Paulo - SP, CEP 04048-001")
                        .build()));

        Paciente mariana = pacienteRepository.findAllByAtivoTrue().stream()
                .filter(p -> "119.450.882-45".equals(p.getCpf()))
                .findFirst()
                .orElseGet(() -> pacienteRepository.save(Paciente.builder()
                        .nome("Mariana Torres Nogueira")
                        .cpf("119.450.882-45")
                        .dataNascimento(LocalDate.of(1995, 8, 22))
                        .email("mariana.nogueira@email.com")
                        .telefone("(11) 98234-5678")
                        .contatoEmergenciaNome("Roberto Nogueira (Pai)")
                        .contatoEmergenciaTelefone("(11) 99111-2233")
                        .enderecoLocalSessao("Av. Paulista, 1578, Bela Vista, São Paulo - SP")
                        .build()));

        Paciente felipe = pacienteRepository.findAllByAtivoTrue().stream()
                .filter(p -> "504.312.901-82".equals(p.getCpf()))
                .findFirst()
                .orElseGet(() -> pacienteRepository.save(Paciente.builder()
                        .nome("Felipe Silveira Campos")
                        .cpf("504.312.901-82")
                        .dataNascimento(LocalDate.of(1988, 11, 3))
                        .email("felipe.campos@email.com")
                        .telefone("(11) 97654-3210")
                        .contatoEmergenciaNome("Camila Campos (Esposa)")
                        .contatoEmergenciaTelefone("(11) 98877-6655")
                        .enderecoLocalSessao("Rua Oscar Freire, 800, Jardins, São Paulo - SP")
                        .build()));

        Agendamento sessaoLucas = agendamentoRepository.save(Agendamento.builder()
                .psicologo(draJuliana)
                .paciente(lucas)
                .dataHoraSessao(LocalDateTime.of(2024, 10, 24, 9, 0))
                .modalidade(ModalidadeAtendimento.TELECONSULTA)
                .status(StatusAgendamento.AGENDADO)
                .enderecoLocalSessao(lucas.getEnderecoLocalSessao())
                .build());

        Agendamento sessaoMariana = agendamentoRepository.save(Agendamento.builder()
                .psicologo(draJuliana)
                .paciente(mariana)
                .dataHoraSessao(LocalDateTime.of(2024, 10, 24, 11, 0))
                .modalidade(ModalidadeAtendimento.TELECONSULTA)
                .status(StatusAgendamento.REALIZADO)
                .enderecoLocalSessao(mariana.getEnderecoLocalSessao())
                .build());

        Agendamento sessaoFelipe = agendamentoRepository.save(Agendamento.builder()
                .psicologo(draJuliana)
                .paciente(felipe)
                .dataHoraSessao(LocalDateTime.of(2024, 10, 23, 17, 0))
                .modalidade(ModalidadeAtendimento.PRESENCIAL)
                .status(StatusAgendamento.REALIZADO)
                .enderecoLocalSessao("Consultório Sala 1 - Unidade Jardins")
                .build());

        prontuarioRepository.save(ProntuarioClinico.builder()
                .agendamento(sessaoLucas)
                .procedimentosTecnicos("""
                        1. Aplicação do Inventário de Ansiedade de Beck (BAI) - Escore obtido: 18 (Ansiedade Leve/Moderada).
                        2. Revisão do Registro de Pensamentos Disfuncionais (RPD) focado em ruminações ocupacionais no ambiente corporativo.
                        3. Psicoeducação sobre distorções cognitivas: especificamente "catastrofização" e "leitura mental" nas interações com a liderança direta.
                        4. Treinamento de respiração diafragmática pausada (técnica 4-7-8) como recurso de manejo agudo de hiperventilação.""")
                .evolucaoObservavel("""
                        Paciente apresentou-se pontual, asseado, cooperativo e vigil. Humor eutímico com oscilações para afeto ansioso ao rememorar reuniões com gestor executivo. Pensamento com curso e velocidade preservados, sem alterações no conteúdo sensoperceptivo.

                        Relata melhora no padrão do sono (média de 6h30 ininterruptas), após adesão ao protocolo de higiene do sono pactuado na Sessão #12. Demonstrou engajamento ativo na análise do RPD e comprometeu-se com a tarefa comportamental intersessão de autoexposição gradual em apresentação de relatórios.""")
                .dataRegistro(Instant.now())
                .build());

        registroDocumentalRepository.save(RegistroDocumental.builder()
                .agendamento(sessaoLucas)
                .hipotesesProvisorias("""
                        • Hipótese provisória: Esquema desadaptativo precoce de "Padrões Inflexíveis / Postura Crítica", possivelmente ancorado nas exigências paternas durante a graduação acadêmica.
                        • Mecanismo de defesa prevalente: Racionalização intensa como barreira contra o sentimento de inadequação e desamparo ao ser avaliado por pares.
                        • Investigar na próxima sessão se a cobrança do gestor reativa memórias de rejeição escolar.""")
                .notasSupervisao("""
                        • Ponto de atenção na supervisão clínica de terça-feira com Dr. Arnaldo: observei sutil desconforto contratransferencial diante da pressa do paciente em apresentar resultados tangíveis.
                        • Cuidar para não assumir o papel de "cobrador de metas", mantendo o enquadre estritamente terapêutico e acolhedor.
                        • Reavaliar se a dosagem de tarefas intersessão não está alimentando a compulsão por performance.""")
                .dataRegistro(Instant.now())
                .build());

        pagamentoRepository.save(Pagamento.builder()
                .agendamento(sessaoLucas)
                .valorTotal(new BigDecimal("250.00"))
                .taxaClinicaInfraestrutura(new BigDecimal("75.00"))
                .repasseProfissional(new BigDecimal("175.00"))
                .statusSplit(StatusSplit.LIQUIDADO)
                .dataPagamento(Instant.now())
                .build());

        pagamentoRepository.save(Pagamento.builder()
                .agendamento(sessaoMariana)
                .valorTotal(new BigDecimal("280.00"))
                .taxaClinicaInfraestrutura(new BigDecimal("84.00"))
                .repasseProfissional(new BigDecimal("196.00"))
                .statusSplit(StatusSplit.PENDENTE)
                .dataPagamento(Instant.now())
                .build());

        pagamentoRepository.save(Pagamento.builder()
                .agendamento(sessaoFelipe)
                .valorTotal(new BigDecimal("220.00"))
                .taxaClinicaInfraestrutura(new BigDecimal("66.00"))
                .repasseProfissional(new BigDecimal("154.00"))
                .statusSplit(StatusSplit.LIQUIDADO)
                .dataPagamento(Instant.now())
                .build());

        auditoriaService.registrarAcao(
                "dra.juliana@psicosafe.com.br",
                "127.0.0.1",
                AcaoAuditoria.INSERCAO,
                "SEED_INICIAL_CLINICA:" + draJuliana.getIdPsicologo()
        );
    }
}
