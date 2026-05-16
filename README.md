# 🏢 Gerenciador de Salas

API desenvolvida para resolver a dificuldade de coordenar o uso de salas e assentos em empresas no modelo híbrido. Permite consultar disponibilidade de espaços, realizar reservas e conta com IA para recomendar a melhor opção conforme a necessidade do usuário.

---

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.0.5**
- **Spring Security** com autenticação JWT
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL**
- **Flyway** (migrations)
- **Lombok**
- **Auth0 Java JWT 4.5.2**

---

## 📁 Estrutura do Projeto

```
src/
├── controller/
│   ├── AuthController.java
│   ├── SalaController.java
│   └── UsuarioController.java
├── domain/
│   ├── Sala.java
│   ├── Usuario.java
│   ├── Role.java
│   └── StatusSala.java
├── dto/
│   ├── AuthorizationDTO.java
│   ├── LoginResponse.java
│   ├── RegisterDTO.java
│   ├── SalaDTO.java
│   └── UsuarioDTO.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repositories/
│   ├── SalaRepository.java
│   └── UsuarioRepository.java
├── security/
│   ├── SecurityConfig.java
│   ├── SecurityFilter.java
│   └── TokenService.java
└── services/
    ├── AuthorizationService.java
    ├── SalaService.java
    └── UsuarioService.java
```

---

## 🔐 Autenticação

A API utiliza autenticação via **JWT (Bearer Token)**. Para acessar os endpoints protegidos, é necessário fazer login e incluir o token no header `Authorization`.

### Roles disponíveis

| Role | Permissões |
|------|-----------|
| `ADMIN` | Acesso total (criar, editar, deletar salas e usuários) |
| `TECHLEADER` | Acesso a funcionalidades de liderança |
| `USER` | Consulta e reserva de salas |

---

## 📋 Endpoints

### 🔑 Auth — `/auth`

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|-------------|
| POST | `/auth/login` | Login e geração de token JWT | ❌ |
| POST | `/auth/cadastro` | Cadastro de novo usuário | ❌ |

**Login — Body:**
```json
{
  "email": "admin@empresa.com",
  "senha": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### 🏠 Salas — `/salas`

| Método | Rota | Descrição | Role necessária |
|--------|------|-----------|----------------|
| GET | `/salas` | Lista todas as salas | Autenticado |
| POST | `/salas` | Cadastra nova sala | ADMIN |
| PUT | `/salas?id={id}` | Atualiza sala por ID | Autenticado |
| DELETE | `/salas?id={id}` | Deleta sala por ID | ADMIN |

**Cadastrar/Atualizar sala — Body:**
```json
{
  "nome": "Sala de Reunião A",
  "capacidade": 10,
  "statusSala": "DISPONIVEL",
  "local": "Bloco A - 2º Andar"
}
```

**Status disponíveis:** `DISPONIVEL`, `INDISPONIVEL`, `MANUTENCAO`

---

### 👤 Usuários — `/usuarios`

| Método | Rota | Descrição | Role necessária |
|--------|------|-----------|----------------|
| GET | `/usuarios` | Lista todos os usuários | Autenticado |
| PUT | `/usuarios?id={id}` | Atualiza usuário por ID | Autenticado |
| DELETE | `/usuarios?id={id}` | Deleta usuário por ID | Autenticado |

**Atualizar usuário — Body:**
```json
{
  "email": "novo@empresa.com",
  "senha": "novasenha123",
  "username": "novousername",
  "role": "USER"
}
```

**Roles disponíveis:** `ADMIN`, `TECHLEADER`, `USER`

---

## 🗄️ Banco de Dados

O projeto utiliza **Flyway** para controle de migrations. As migrations ficam em `src/main/resources/db/migration/`.

### Tabela `usuarios`
| Campo | Tipo | Restrição |
|-------|------|-----------|
| id | SERIAL | PRIMARY KEY |
| email | TEXT | NOT NULL, UNIQUE |
| senha | TEXT | NOT NULL |
| username | TEXT | NOT NULL, UNIQUE |
| role | TEXT | NOT NULL |

### Tabela `salas`
| Campo | Tipo | Restrição |
|-------|------|-----------|
| id | SERIAL | PRIMARY KEY |
| nome | VARCHAR(130) | NOT NULL |
| capacidade | INT | NOT NULL |
| status | TEXT | NOT NULL |
| local | TEXT | NOT NULL |

---

## ⚙️ Configuração

Configure as variáveis no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gerenciador_salas
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

api.security.token.secret=seu_secret_jwt
```

---

## ▶️ Como Rodar

```bash
# Clone o repositório
git clone https://github.com/squad27-accenture/Gerenciador-de-salas.git

# Entre na pasta
cd Gerenciador-de-salas

# Execute com Maven
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 🔮 Funcionalidades Previstas

As funcionalidades abaixo fazem parte do escopo do projeto e serão implementadas nas próximas sprints:

- [ ] **Reserva de salas e assentos** — agendamento por dia e horário
- [ ] **Consulta por disponibilidade** — filtro por dia, horário, lotação, local e tipo
- [ ] **Recomendação inteligente com IA** — sugestão da melhor sala com base nos critérios do usuário
- [ ] **Extração automática de layout** — leitura da planta da sala para mapear assentos automaticamente
- [ ] **Gestão de reservas** — visualização, edição e cancelamento de reservas

---

## 👥 Time

Desenvolvido pelo **Squad 27** — Accenture.
