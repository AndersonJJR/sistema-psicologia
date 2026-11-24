# PsicoSafe API — Sistema de Gestão para Clínicas de Psicologia & Telepsicologia

API RESTful de alta confiabilidade para gestão de clínicas de psicologia, teleatendimento, prontuário eletrônico segregado, engenharia financeira com split de pagamentos e portabilidade criptográfica sob medida para normas brasileiras.

---

## 1. Conformidade Regulatória & Segurança da Informação

- **Resolução CFP nº 001/2009 (Segregação de Prontuário e Registro Documental):**
  - **Prontuário Clínico (`PRONTUARIOS_CLINICOS`):** Evolução observável e procedimentos técnicos acessíveis ao paciente e à equipe clínica autorizada (`PSICOLOGO`, `ADMIN`), bloqueado para `RECEPCAO`. Conta com assinatura digital SHA-256 que torna o registro imutável.
  - **Registro Documental Privativo (`REGISTROS_DOCUMENTAIS`):** Hipóteses provisórias e notas de supervisão criptografadas em nível de aplicação (**AES-256-GCM**) e isoladas estritamente pelo psicólogo titular autenticado no token JWT (`403 Forbidden` e log de auditoria para qualquer outro profissional).
- **Resolução CFP nº 011/2018 (Telessaúde e Gestão de Crises):**
  - Validação obrigatória de contatos de emergência e endereço físico da sessão remota para acionamento rápido de suporte em intercorrências clínicas.
- **Lei nº 13.787/2018 (Guarda e Retenção por 20 Anos):**
  - Exclusão exclusivamente lógica (*Soft Delete* via `EntidadeAuditavel` com `ativo` e `deleted_at`), proibindo remoção física de registros clínicos.
- **LGPD — Lei nº 13.709/2018 (Auditoria & Portabilidade):**
  - Trilha de auditoria imutável *append-only* (`@Immutable` em `LOGS_AUDITORIA`).
  - Exportação de portabilidade em lote nos padrões abertos **JSON**, **CSV (RFC 4180)** e **PDF/A-1b (ISO 19005-1)**.
- **Prevenção contra IDOR e Path Traversal:**
  - Todas as entidades utilizam identificadores **UUID (`BINARY(16)`)**.
  - Upload seguro de avatares (`UploadService`) com validação de extensão, limite de 10 MB e normalização de diretório.

---

## 2. Stack Tecnológico

- **Linguagem & Runtime:** Java 21 (Records, Pattern Matching)
- **Framework:** Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA / Hibernate, Spring Security, Bean Validation)
- **Autenticação:** JWT Stateless (HMAC-SHA256) + BCrypt
- **Criptografia de Dados Sensíveis:** Application-Level Encryption (ALE) com `AES/GCM/NoPadding` (256 bits)
- **Banco de Dados:** MySQL 8.4
- **Containerização:** Docker (Multi-Stage Build) & Docker Compose (Orquestração Full-Stack com o Front-End Angular)

---

## 3. Principais Endpoints REST (`/api/v1`)

| Método | Endpoint | Perfis (RBAC) | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Público | Autentica usuário e emite token JWT com perfil e vínculo profissional |
| `POST` | `/api/v1/auth/registrar-psicologo` | Público | Credenciamento rápido de psicólogo titular e geração de sessão JWT |
| `POST` | `/api/v1/auth/usuarios` | Público / Admin | Criação de usuários por perfil (`ADMIN`, `PSICOLOGO`, `RECEPCAO`) |
| `GET/POST/PUT/DELETE` | `/api/v1/psicologos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Gestão de psicólogos, CRP e abordagem clínica |
| `GET/POST/PUT/PATCH/DELETE` | `/api/v1/pacientes` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Cadastro demográfico, protocolo de emergência CFP e Soft Delete |
| `GET/POST/PATCH/DELETE` | `/api/v1/agendamentos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Agenda presencial/teleconsulta, recorrência semanal, alerta de emergência e split automático |
| `GET` | `/api/v1/dashboard/resumo` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Indicadores consolidados da página inicial (repasse líquido, agenda do dia, assiduidade e pendências) |
| `GET/POST/DELETE` | `/api/v1/prontuarios` | `PSICOLOGO`, `ADMIN` | Prontuário clínico formal e assinatura digital SHA-256 (`/agendamento/{id}/assinar`) |
| `GET/POST/DELETE` | `/api/v1/registros-documentais` | `PSICOLOGO` (Titular) | Registro documental privativo criptografado com AES-256-GCM |
| `GET/POST/PATCH` | `/api/v1/pagamentos` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Split financeiro (30% Clínica / 70% Psicólogo), CSV Carnê-Leão e XML NFS-e |
| `GET` | `/api/v1/exportacao/paciente/{id}` | `PSICOLOGO`, `ADMIN` | Dossiê de portabilidade LGPD em JSON, CSV (`/csv`) e PDF/A-1b (`/pdfa`) |
| `GET` | `/api/v1/auditoria` | `PSICOLOGO`, `ADMIN` | Consulta à trilha imutável de auditoria (`LOGS_AUDITORIA`) |
| `GET/POST` | `/api/v1/uploads/**` | `RECEPCAO`, `PSICOLOGO`, `ADMIN` | Upload e exibição segura de fotos de perfil de psicólogos e pacientes |

> Para detalhes de modelagem MER/DER e regras de domínio, consulte [documentation.md](file:///home/anderson/IdeaProjects/sistema-psicologia/documentation.md).

---

## 4. Execução com Docker Compose (Back-End + Front-End + MySQL)

O arquivo [docker-compose.yml](file:///home/anderson/IdeaProjects/sistema-psicologia/docker-compose.yml) sobe automaticamente toda a infraestrutura:
1. **`mysql-db` (MySQL 8.4)** na porta `3306` com healthcheck ativo;
2. **`sistema-psicologia-api` (Spring Boot Java 21)** na porta `8080`;
3. **`sistema-psicologia-frontend` (Angular 22 + Nginx Reverse Proxy)** localizado em `/home/anderson/IdeaProjects/sistema-psicologia-interface`, exposto na porta `4200`.

```bash
docker compose up --build -d
```

- **Interface Web (Front-End Angular):** `http://localhost:4200`
- **API REST (Back-End Spring Boot):** `http://localhost:8080`

---

## 5. Execução Local e Testes Automatizados

### Pré-requisitos
- Java 21+
- Maven 3.9+ (ou `./mvnw` incluso)
- MySQL 8.4+

### Variáveis de Ambiente (`.env` opcional)
A aplicação importa automaticamente um arquivo `.env` na raiz (`optional:file:.env[.properties]`) ou utiliza variáveis de ambiente do sistema:
```properties
DATASOURCE_URL=jdbc:mysql://localhost:3306/sistema_psicologia?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=root
APP_AES_KEY=9f8c7b6a5e4d3c2b1a0f9e8d7c6b5a43
JWT_SECRET=4a7b9c2e5f8a1d3c6e9f2b5a8d4c1e7f3a6b9c2e5f8a1d3c6e9f2b5a8d4c1e7f
JWT_EXPIRATION_MS=86400000
```

### Comandos Maven
```bash
# Executar a suíte de testes de conformidade e segurança
./mvnw test

# Iniciar o servidor Spring Boot localmente
./mvnw spring-boot:run
```
