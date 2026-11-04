# Documentação Arquitetural e Modelagem de Dados — Sistema de Gestão para Clínicas de Psicologia

## 1. Visão Geral e Arquitetura

Plataforma de gestão clínica especializada em psicologia, projetada com foco em resiliência, segurança da informação e conformidade estrita com:
- **Resolução CFP nº 001/2009** (Segregação entre Prontuário Clínico e Registro Documental privativo do psicólogo titular);
- **Resolução CFP de Telessaúde / Sistema e-Psi** (Validação do cadastro `numero_epsi`, contatos de emergência e localização física da sessão remota para gestão de crises);
- **Lei nº 13.787/2018** (Retenção obrigatória de prontuários por 20 anos via *Soft Delete*, proibindo exclusão física);
- **Lei Geral de Proteção de Dados — LGPD (Lei nº 13.709/2018)** (Criptografia em Nível da Aplicação AES-256-GCM, trilha de auditoria *append-only* e portabilidade em lote nos formatos abertos JSON, CSV e PDF/A-1b).

### 1.1. Stack Tecnológico
- **Back-end**: Java 21, Spring Boot 4.1.2, Spring Data JPA (Hibernate), Spring Security + JWT (HS256 stateless), Bean Validation.
- **Front-end / View**: HTML5 + Tailwind CSS + Arquitetura de AuthGuard em TypeScript/Angular e geradores nativos de visualização em JSON, CSV (RFC 4180) e PDF/A-1b (ISO 19005-1).
- **Banco de Dados**: MySQL 8.4 com constraints de integridade relacional e chaves únicas.
- **Infraestrutura**: Orquestração via `docker-compose.yml`.

---

## 2. Arquitetura de Pastas (Padrão MVC + SOLID + DRY)

O código-fonte em `src/main/java/com/anderson/sistemapsicologia` segue estritamente a divisão **Model / View / Controller**, onde o pacote `model` é subdividido por entidade de domínio e cada entidade possui sua subpasta `dtos/` implementada com `record` do Java 21:

```text
src/main/java/com/anderson/sistemapsicologia/
├── SistemaPsicologiaApplication.java
├── controller/
│   ├── AgendamentoController.java
│   ├── AuditoriaController.java
│   ├── AutenticacaoController.java
│   ├── ExportacaoPacienteController.java
│   ├── GlobalExceptionHandler.java
│   ├── PacienteController.java
│   ├── PagamentoController.java
│   ├── ProntuarioClinicoController.java
│   ├── PsicologoController.java
│   ├── RegistroDocumentalController.java
│   └── RequisicaoHttpUtils.java
├── model/
│   ├── common/
│   │   ├── AcessoNegadoClinicoException.java
│   │   ├── AesAttributeConverter.java
│   │   ├── CriptografiaAesService.java
│   │   ├── EntidadeAuditavel.java
│   │   ├── RecursoNaoEncontradoException.java
│   │   └── RegraNegocioException.java
│   ├── agendamento/
│   │   ├── Agendamento.java
│   │   ├── AgendamentoRepository.java
│   │   ├── AgendamentoService.java
│   │   ├── ModalidadeAtendimento.java
│   │   ├── StatusAgendamento.java
│   │   └── dtos/
│   │       ├── AgendamentoRequestDTO.java
│   │       └── AgendamentoResponseDTO.java
│   ├── auditoria/
│   │   ├── AcaoAuditoria.java
│   │   ├── AuditoriaService.java
│   │   ├── LogAuditoria.java
│   │   ├── LogAuditoriaRepository.java
│   │   └── dtos/
│   │       ├── LogAuditoriaCreateDTO.java
│   │       └── LogAuditoriaResponseDTO.java
│   ├── paciente/
│   │   ├── ExportacaoPortabilidadeService.java
│   │   ├── Paciente.java
│   │   ├── PacienteRepository.java
│   │   ├── PacienteService.java
│   │   └── dtos/
│   │       ├── PacienteExportacaoDTO.java
│   │       ├── PacienteRequestDTO.java
│   │       └── PacienteResponseDTO.java
│   ├── pagamento/
│   │   ├── CalculadoraSplitPagamento.java
│   │   ├── Pagamento.java
│   │   ├── PagamentoRepository.java
│   │   ├── PagamentoService.java
│   │   ├── StatusSplit.java
│   │   └── dtos/
│   │       ├── PagamentoRequestDTO.java
│   │       └── PagamentoResponseDTO.java
│   ├── prontuario/
│   │   ├── ProntuarioClinico.java
│   │   ├── ProntuarioClinicoRepository.java
│   │   ├── ProntuarioClinicoService.java
│   │   └── dtos/
│   │       ├── ProntuarioClinicoRequestDTO.java
│   │       └── ProntuarioClinicoResponseDTO.java
│   ├── psicologo/
│   │   ├── Psicologo.java
│   │   ├── PsicologoRepository.java
│   │   ├── PsicologoService.java
│   │   └── dtos/
│   │       ├── PsicologoRequestDTO.java
│   │       └── PsicologoResponseDTO.java
│   ├── registrodocumental/
│   │   ├── RegistroDocumental.java
│   │   ├── RegistroDocumentalRepository.java
│   │   ├── RegistroDocumentalService.java
│   │   └── dtos/
│   │       ├── RegistroDocumentalRequestDTO.java
│   │       └── RegistroDocumentalResponseDTO.java
│   └── usuario/
│       ├── AutenticacaoService.java
│       ├── JwtAuthenticationFilter.java
│       ├── JwtTokenService.java
│       ├── PerfilUsuario.java
│       ├── SecurityConfiguration.java
│       ├── Usuario.java
│       ├── UsuarioRepository.java
│       └── dtos/
│           ├── LoginRequestDTO.java
│           ├── TokenResponseDTO.java
│           ├── UsuarioCreateDTO.java
│           └── UsuarioResponseDTO.java
└── view/
    ├── ApiResponseView.java
    ├── ErroPadraoView.java
    ├── ExportacaoPacienteCsvView.java
    └── ExportacaoPacientePdfAView.java
```

---

## 3. Modelo de Entidade-Relacionamento (MER/DER) Implementado

### 3.1. Segregação de Prontuários (Resolução CFP nº 001/2009)
1. **`PRONTUARIOS_CLINICOS` (`ProntuarioClinico`)**:
   - `id_prontuario` (PK)
   - `id_agendamento` (FK -> `AGENDAMENTOS`)
   - `procedimentos_tecnicos` (`TEXT`)
   - `evolucao_observavel` (`TEXT`)
   - `data_registro` (`TIMESTAMP UTC`)
   - `ativo` (`BOOLEAN`, default `true`) e `deleted_at` (`TIMESTAMP UTC`)
2. **`REGISTROS_DOCUMENTAIS` (`RegistroDocumental`)**:
   - `id_registro_documental` (PK)
   - `id_agendamento` (FK -> `AGENDAMENTOS`)
   - `hipoteses_provisorias` (`TEXT` — Criptografado com AES-256-GCM na aplicação via `@Convert(converter = AesAttributeConverter.class)`)
   - `notas_supervisao` (`TEXT` — Criptografado com AES-256-GCM na aplicação via `@Convert(converter = AesAttributeConverter.class)`)
   - `data_registro` (`TIMESTAMP UTC`)
   - `ativo` (`BOOLEAN`, default `true`) e `deleted_at` (`TIMESTAMP UTC`)
   - **Isolamento de Titularidade**: O `RegistroDocumentalService` confronta o `idPsicologoVinculado` extraído do token JWT com o psicólogo responsável pelo agendamento; outros psicólogos da mesma clínica recebem `403 Forbidden` e a tentativa é registrada em `LOGS_AUDITORIA`.

### 3.2. Telessaúde e Gestão de Crises
1. **`PSICOLOGOS` (`Psicologo`)**:
   - Campos `cpf`, `crp` e `numero_epsi` com constraints `UNIQUE` (`uk_psicologos_cpf`, `uk_psicologos_crp`, `uk_psicologos_numero_epsi`).
2. **`PACIENTES` (`Paciente`) e `AGENDAMENTOS` (`Agendamento`)**:
   - Campos `contato_emergencia_nome`, `contato_emergencia_telefone` e `endereco_local_sessao` obrigatórios para suporte imediato em casos de descompensação psíquica grave durante teleconsultas.

### 3.3. Engenharia Financeira: Split de Pagamentos
- **`PAGAMENTOS` (`Pagamento`)**:
  - Substitui o campo único `valor` por:
    - `valor_total` (`DECIMAL(12,2)`)
    - `taxa_clinica_infraestrutura` (`DECIMAL(12,2)`) — Base para NFS-e da clínica
    - `repasse_profissional` (`DECIMAL(12,2)`) — Base para Carnê-Leão/NFS-e do psicólogo
    - `status_split` (`PENDENTE`, `LIQUIDADO`)
  - Validado por `CalculadoraSplitPagamento` garantindo que `valor_total == taxa_clinica_infraestrutura + repasse_profissional`.

### 3.4. Auditoria e Retenção de Dados (LGPD e Lei nº 13.787/2018)
- **Soft Delete (`EntidadeAuditavel`)**: Todas as entidades principais herdam `ativo` (`default: true`) e `deleted_at` (`Instant`), com bloqueio explícito de `delete` físico nos repositórios.
- **`LOGS_AUDITORIA` (`LogAuditoria`)**: Entidade anotada com `@Immutable` e bloqueio em `@PreUpdate` / `@PreRemove`, armazenando `id_log`, `carimbo_tempo_utc`, `id_usuario_operador`, `endereco_ip`, `acao` (`LEITURA`, `INSERCAO`, `ALTERACAO`, `EXPORTACAO`) e `id_recurso_acessado`.

---

## 4. Diretrizes de Segurança e Endpoints da API REST

| Método | Endpoint | Perfis Permitidos (RBAC) | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Público | Autentica usuário e emite token JWT (HS256) |
| `POST` | `/api/v1/auth/usuarios` | Público / Admin | Cadastra usuário com perfil (`ADMIN`, `PSICOLOGO`, `RECEPCAO`) |
| `GET/POST/DELETE` | `/api/v1/psicologos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Gestão de psicólogos com validação `UNIQUE` em CPF, CRP e e-Psi |
| `GET/POST/PUT/DELETE` | `/api/v1/pacientes` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Gestão demográfica e contatos de emergência (Soft Delete) |
| `GET/POST/DELETE` | `/api/v1/agendamentos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Gestão de sessões presenciais e teleconsultas |
| `GET/POST/DELETE` | `/api/v1/prontuarios` | `PSICOLOGO`, `ADMIN` (**Bloqueado para `RECEPCAO`**) | Prontuários clínicos públicos ao paciente (CFP 001/2009) |
| `GET/POST/DELETE` | `/api/v1/registros-documentais` | Exclusivo `PSICOLOGO` Titular (**Bloqueado para `RECEPCAO` e outros psicólogos**) | Registros privativos criptografados com AES-256-GCM |
| `GET/POST/PATCH` | `/api/v1/pagamentos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Registro e liquidação de Split de Pagamentos |
| `GET` | `/api/v1/exportacao/paciente/{id}` | `PSICOLOGO`, `ADMIN` | Portabilidade LGPD em lote (JSON + CSV + PDF/A-1b Base64) |
| `GET` | `/api/v1/exportacao/paciente/{id}/csv` | `PSICOLOGO`, `ADMIN` | Download direto do dossiê em formato aberto CSV |
| `GET` | `/api/v1/exportacao/paciente/{id}/pdfa` | `PSICOLOGO`, `ADMIN` | Download direto do dossiê em padrão ISO 19005-1 (PDF/A-1b) |
| `GET` | `/api/v1/auditoria` | `PSICOLOGO`, `ADMIN` | Consulta à trilha de auditoria append-only (`LOGS_AUDITORIA`) |
