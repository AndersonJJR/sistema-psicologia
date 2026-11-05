# Registro de Código e Artefatos Sem Uso no Back-End (`no-use.md`)

Este documento cataloga classes, métodos e arquivos estáticos identificados na auditoria do projeto **Sistema de Gestão para Clínicas de Psicologia (`sistema-psicologia`)** que não possuem vínculo operacional com o fluxo ativo da API REST ou foram substituídos pela arquitetura SPA Angular em `sistema-psicologia-interface`.

---

## 1. Classes Java Sem Referência Ativa

### 1.1. `com.anderson.sistemapsicologia.view.ApiResponseView`
- **Caminho:** `src/main/java/com/anderson/sistemapsicologia/view/ApiResponseView.java`
- **Símbolos:** `record ApiResponseView<T>(Instant timestamp, int status, String mensagem, T dados)` e método fábrica `ApiResponseView.ok(String mensagem, T dados)`.
- **Motivo do Não Uso:** Todos os controllers REST (`AutenticacaoController`, `PsicologoController`, `PacienteController`, `AgendamentoController`, `ProntuarioClinicoController`, `RegistroDocumentalController`, `PagamentoController`, `ExportacaoPacienteController` e `AuditoriaController`) retornam diretamente `ResponseEntity<DTO>` ou `ResponseEntity<byte[]>` para manter contratos JSON enxutos e compatíveis com tipagem estrita no Angular e testes automatizados.

---

## 2. Artefatos Estáticos Legados em `src/main/resources/static`

### 2.1. `src/main/resources/static/assets/auth.guard.ts`
- **Caminho:** `src/main/resources/static/assets/auth.guard.ts`
- **Motivo do Não Uso:** Arquivo TypeScript isolado armazenado como recurso estático do Spring Boot (sem pipeline de compilação Node/Angular no back-end). Foi integralmente substituído pelos guardas funcionais reais do Angular 22 em `sistema-psicologia-interface/src/app/core/guards/auth.guard.ts` (`authGuard`, `clinicalRoleGuard` e `psychologistTitularGuard`).

### 2.2. `src/main/resources/static/index.html`
- **Caminho:** `src/main/resources/static/index.html`
- **Motivo do Não Uso:** Página HTML estática demonstrativa servida pelo Spring Boot na raiz `/`. Com a implementação completa do front-end Angular em `sistema-psicologia-interface` consumindo a API `/api/v1/**` via JWT e CORS, a interface oficial passou a residir exclusivamente no projeto front-end.
