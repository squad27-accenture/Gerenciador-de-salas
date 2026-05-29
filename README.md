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
- **Spring Mail** (recuperação de senha via e-mail)
- **SpringDoc OpenAPI 3.0.3** (Swagger UI)

---

## 📁 Estrutura do Projeto

```
src/
├── controller/
│   ├── AuthController.java
│   ├── PasswordResetController.java
│   ├── ReservaController.java
│   ├── SalaController.java
│   └── UsuarioController.java
├── domain/
│   ├── Assento.java
│   ├── EquipamentosAssento.java
│   ├── EquipamentosSala.java
│   ├── PasswordResetToken.java
│   ├── RefreshToken.java
│   ├── Reserva.java
│   ├── Role.java
│   ├── Sala.java
│   ├── StatusReserva.java
│   ├── StatusSala.java
│   └── Usuario.java
├── dto/
│   ├── AssentoReponseDTO.java
│   ├── AuthorizationDTO.java
│   ├── LoginResponse.java
│   ├── RedefinirSenhaDTO.java
│   ├── RegisterDTO.java
│   ├── ReservaDTO.java
│   ├── ReservaGrupoDTO.java
│   ├── SalaDTO.java
│   ├── SolicitarRecuperacaoDTO.java
│   ├── UsuarioDTO.java
│   └── UsuarioResponseDTO.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repositories/
│   ├── AssentoRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── RefreshTokenRepository.java
│   ├── ReservaRepository.java
│   ├── SalaRepository.java
│   └── UsuarioRepository.java
├── security/
│   ├── SecurityConfig.java
│   ├── SecurityFilter.java
│   └── TokenService.java
└── services/
    ├── AuthorizationService.java
    ├── PasswordResetService.java
    ├── RefreshTokenService.java
    ├── ReservaService.java
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
| POST | `/auth/recuperar-senha` | Solicita código de recuperação de senha por e-mail | ❌ |
| POST | `/auth/redefinir-senha` | Redefine a senha usando o código recebido | ❌ |

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

**Solicitar recuperação de senha — Body:**
```json
{
  "email": "usuario@empresa.com"
}
```

**Redefinir senha — Body:**
```json
{
  "email": "usuario@empresa.com",
  "codigo": "123456",
  "novaSenha": "novaSenha123"
}
```

---

### 🏠 Salas — `/salas`

| Método | Rota | Descrição | Role necessária |
|--------|------|-----------|----------------|
| GET | `/salas/ListarSala` | Lista todas as salas | Autenticado |
| POST | `/salas/CadastrarSala` | Cadastra nova sala | ADMIN |
| PUT | `/salas/AtualizarSala?id={id}` | Atualiza sala por ID | Autenticado |
| DELETE | `/salas/DeletarSala?id={id}` | Deleta sala por ID | ADMIN |
| GET | `/salas/{id}/assentos` | Lista os assentos de uma sala | Autenticado |
| GET | `/salas/ocupados` | Retorna assentos ocupados em um intervalo de tempo | Autenticado |

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

**Buscar assentos ocupados — Query Params:**
```
salaId=1&dataReserva=2025-06-01&horarioInicio=09:00&horarioFim=11:00
```

---

### 📅 Reservas — `/reserva`

| Método | Rota | Descrição | Autenticação |
|--------|------|-----------|-------------|
| POST | `/reserva/realizarReserva` | Realiza reserva de um assento | ✅ |
| POST | `/reserva/reservaGrupo` | Realiza reserva de múltiplos assentos para um grupo | ✅ |
| PUT | `/reserva/{id}/cancelar` | Cancela uma reserva individual | ✅ |
| PUT | `/reserva/grupo/{codigoGrupo}/cancelar` | Cancela todas as reservas de um grupo | ✅ |

**Realizar reserva — Body:**
```json
{
  "horarioInicio": "09:00",
  "horarioFim": "11:00",
  "dataReserva": "2025-06-01",
  "posicaoAssento": 3,
  "salaId": 1
}
```

**Reserva em grupo — Body:**
```json
{
  "horarioInicio": "09:00",
  "horarioFim": "11:00",
  "dataReserva": "2025-06-01",
  "salaId": 1,
  "posicoesAssentos": [1, 2, 3]
}
```

**Status de reserva disponíveis:** `EmANDAMENTO`, `FINALIZADA`, `CANCELADA`

---

### 👤 Usuários — `/usuarios`

| Método | Rota | Descrição | Role necessária |
|--------|------|-----------|----------------|
| GET | `/usuarios/listarUsuarios` | Lista todos os usuários | Autenticado |
| GET | `/usuarios/buscar?id={id}` | Busca dados de um usuário por ID | Autenticado |
| PUT | `/usuarios/atualizarConta` | Atualiza dados do usuário autenticado | Autenticado |
| DELETE | `/usuarios/DeletarUsuario?id={id}` | Deleta usuário por ID | ADMIN |
| DELETE | `/usuarios/deletarConta` | Deleta a própria conta do usuário autenticado | Autenticado |

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

O projeto utiliza **PostgreSQL** como banco de dados e **Flyway** para controle de migrations. As migrations ficam em `src/main/resources/db/migration/`.

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

### Tabela `assentos`
| Campo | Tipo | Restrição |
|-------|------|-----------|
| id | SERIAL | PRIMARY KEY |
| sala_id | INT | NOT NULL, FK → salas(id) |
| posicao | INT | NOT NULL |
| equipamento_assento | TEXT | — |

**Equipamentos de assento disponíveis:** `Computador`, `ApenasMonitor`, `Tela_4k`

### Tabela `reservas`
| Campo | Tipo | Restrição |
|-------|------|-----------|
| id | SERIAL | PRIMARY KEY |
| horario_inicio | TIME | NOT NULL |
| horario_fim | TIME | NOT NULL |
| data_reserva | DATE | NOT NULL |
| posicao | INT | NOT NULL |
| sala_id | INT | NOT NULL, FK → salas(id) |
| usuario_id | INT | FK → usuarios(id) |
| status_reserva | TEXT | NOT NULL |
| codigo_grupo | VARCHAR(100) | — |

---

## ⚙️ Configuração

Configure as variáveis no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gerenciadordesala
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

api.security.token.secret=${JWT_SECRET:my-secret-key}

spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${API_KEY}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 📖 Documentação (Swagger)

Com a aplicação rodando, acesse a documentação interativa em:

```
http://localhost:8080/swagger-ui.html
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

- [x] **Reserva de salas e assentos** — agendamento por dia e horário
- [x] **Consulta por disponibilidade** — filtro por dia, horário e sala
- [x] **Gestão de reservas** — cancelamento individual e em grupo
- [x] **Recuperação de senha** — envio de código por e-mail
- [ ] **Recomendação inteligente com IA** — sugestão da melhor sala com base nos critérios do usuário
- [ ] **Extração automática de layout** — leitura da planta da sala para mapear assentos automaticamente

---

👥 Time
<table>
  <tr>
    <td align="center">
      <a href="https://github.com/bernas0610">
        <img src="https://github.com/bernas0610.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>Bernardo Mendes</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/eujotag">
        <img src="https://github.com/eujotag.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>João Guilherme</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/andersonsanto09">
        <img src="https://github.com/andersonsanto09.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>Anderson Ferreira </b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/GabryelPaivaDev">
        <img src="https://github.com/GabryelPaivaDev.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>Gabryel Paiva</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/gustavouus">
        <img src="https://github.com/gustavouus.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>Luiz Gustavo</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/PauloCesar-12">
        <img src="https://github.com/PauloCesar-12.png" width="80px" style="border-radius: 50%"/><br/>
        <sub><b>Paulo César</b></sub>
      </a>
    </td>
  </tr>
</table>
