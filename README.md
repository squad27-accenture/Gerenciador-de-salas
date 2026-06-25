# 🏢 Gerenciador de Salas — RoomFlow

Sistema completo para gerenciamento de salas, assentos, reservas individuais e reservas em grupo em ambientes corporativos híbridos.

O projeto permite que usuários consultem salas disponíveis, visualizem assentos, realizem reservas, acompanhem histórico, participem de grupos, recebam convites e usem uma análise inteligente para encontrar as melhores salas e assentos conforme perfil dos integrantes do grupo.

---

## 🚀 Tecnologias Utilizadas

### Backend

* **Java 21**
* **Spring Boot**
* **Spring Security**
* **JWT Bearer Token**
* **Spring Data JPA**
* **Hibernate**
* **PostgreSQL**
* **Flyway**
* **Lombok**
* **Auth0 Java JWT**
* **SpringDoc OpenAPI / Swagger**
* **Spring Mail** para recuperação de senha

### Frontend

* **HTML**
* **CSS**
* **JavaScript puro**
* Dashboard administrativo
* Telas de salas, reservas, calendário, grupos, usuários, IA, relatórios e configurações
* Geração de PDF no relatório com `html2canvas` e `jsPDF`

---

## 🎯 Objetivo do Projeto

O RoomFlow resolve a dificuldade de coordenar o uso de salas e assentos em empresas com modelo híbrido.

A aplicação permite:

* Cadastrar e gerenciar salas
* Visualizar salas por capacidade, localidade e status
* Consultar assentos e equipamentos de cada sala
* Reservar assentos individualmente
* Cancelar reservas
* Visualizar histórico de reservas
* Gerenciar grupos de usuários
* Convidar integrantes por e-mail
* Aceitar ou recusar convites
* Recomendar salas e assentos com IA/análise inteligente
* Gerar relatórios visuais e exportar PDF

---

## 👥 Perfis de Acesso

| Perfil       | Permissões principais                                                                                         |
| ------------ | ------------------------------------------------------------------------------------------------------------- |
| `ADMIN`      | Acesso total ao sistema. Pode gerenciar salas, usuários, grupos, reservas, relatórios e configurações gerais. |
| `TECHLEADER` | Pode gerenciar seu próprio grupo, convidar usuários por e-mail e usar a IA para reservas em grupo.            |
| `USER`       | Pode visualizar salas, reservas, calendário, grupos em que participa e convites recebidos.                    |

---

## 🔐 Autenticação

A API utiliza autenticação via **JWT Bearer Token**.

Após o login, o frontend salva o token e envia nas requisições protegidas:

```http
Authorization: Bearer SEU_TOKEN_AQUI
```

### Login

```http
POST /api/v1/auth/login
```

Body:

```json
{
  "email": "admin@teste.com",
  "senha": "123456"
}
```

Resposta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Cadastro

```http
POST /api/v1/auth/cadastro
```

Body:

```json
{
  "email": "usuario@empresa.com",
  "senha": "123456",
  "username": "Nome do Usuário",
  "role": "USER",
  "tipoFuncionario": "PROGRAMADOR"
}
```

---

## 🧑‍💼 Tipos de Funcionário

O sistema usa o tipo de funcionário para melhorar recomendações de assentos e salas.

Tipos disponíveis:

```txt
PROGRAMADOR
DESIGNER
QA
SUPORTE
GESTOR
OUTRO
```

Exemplo de uso:

* `PROGRAMADOR` pode priorizar assentos com computador
* `DESIGNER` pode priorizar monitor 4K
* `QA` pode priorizar setup completo
* `GESTOR` pode priorizar sala de reunião ou assentos próximos

---

## 🏠 Salas

As salas possuem dados de localização, capacidade e estrutura.

Campos principais:

```txt
id
nome
capacidade
local
cidade
estado
andar
bloco
statusSala
raioProximidade
```

### Endpoints de Salas

| Método   | Endpoint                              | Descrição                                     | Acesso      |
| -------- | ------------------------------------- | --------------------------------------------- | ----------- |
| `GET`    | `/api/v1/salas`                       | Lista todas as salas                          | Autenticado |
| `GET`    | `/api/v1/salas/{id}`                  | Busca uma sala por ID                         | Autenticado |
| `POST`   | `/api/v1/salas`                       | Cadastra uma nova sala                        | ADMIN       |
| `PUT`    | `/api/v1/salas/{id}`                  | Atualiza uma sala                             | ADMIN       |
| `DELETE` | `/api/v1/salas/{id}`                  | Remove uma sala                               | ADMIN       |
| `GET`    | `/api/v1/salas/ocupados`              | Retorna assentos ocupados em uma data/horário | Autenticado |
| `POST`   | `/api/v1/salas/{id}/layout/upload`    | Envia imagem/layout da sala                   | ADMIN       |
| `GET`    | `/api/v1/salas/{id}/layout-preview`   | Visualiza layout processado                   | ADMIN       |
| `PUT`    | `/api/v1/salas/{id}/layout`           | Aprova layout da sala                         | ADMIN       |
| `POST`   | `/api/v1/salas/{id}/layout/resultado` | Recebe resultado do agente de layout          | ADMIN       |

### Exemplo de cadastro de sala

```json
{
  "nome": "Sala Olimpo",
  "capacidade": 12,
  "local": "Prédio Principal",
  "cidade": "São Paulo",
  "estado": "SP",
  "andar": "5",
  "bloco": "A",
  "statusSala": "DISPONIVEL",
  "raioProximidade": 5.0
}
```

---

## 💺 Assentos e Equipamentos

Cada sala pode possuir diversos assentos. Cada assento pode ter equipamentos diferentes.

Exemplos de equipamentos:

```txt
COMPUTADOR
MONITOR
MONITOR_4K
HEADSET
WEBCAM
MESA_ADAPTADA
OUTRO
```

A tela de salas permite abrir uma sala e visualizar os assentos como um mapa, parecido com cinema, mostrando:

* Posição do assento
* Disponibilidade
* Equipamentos
* Botão de reservar
* Status ocupado/livre

---

## 📅 Reservas

O sistema permite reservas individuais e reservas em grupo.

### Endpoints de Reservas

| Método | Endpoint                                        | Descrição                                     | Acesso      |
| ------ | ----------------------------------------------- | --------------------------------------------- | ----------- |
| `POST` | `/api/v1/reservas`                              | Realiza reserva individual                    | Autenticado |
| `POST` | `/api/v1/reservas/confirmar-opcao`              | Confirma opção gerada pela IA                 | Autenticado |
| `PUT`  | `/api/v1/reservas/{id}/cancelar`                | Cancela reserva individual                    | Autenticado |
| `PUT`  | `/api/v1/reservas/grupo/{codigoGrupo}/cancelar` | Cancela reservas de um grupo                  | Autenticado |
| `GET`  | `/api/v1/reservas/historico`                    | Lista histórico de reservas do usuário logado | Autenticado |
| `GET`  | `/api/v1/reservas/ocupacao`                     | Relatório de ocupação por sala/período        | ADMIN       |

### Reserva individual

```json
{
  "horarioInicio": "10:00",
  "horarioFim": "12:00",
  "dataReserva": "2026-06-08",
  "posicaoAssento": 3,
  "salaId": 1
}
```

### Cancelar reserva

```http
PUT /api/v1/reservas/{id}/cancelar?motivo=Cancelada pelo usuário
```

---

## 👥 Grupos

A funcionalidade de grupos permite que Tech Leaders e Admins organizem equipes para reservas em conjunto.

Regras principais:

* `ADMIN` pode visualizar e alterar todos os grupos
* `TECHLEADER` só vê e gerencia o próprio grupo
* `USER` apenas visualiza o grupo em que está
* Usuário comum não altera grupo
* Integrantes entram por convite
* Convites podem ser aceitos ou recusados
* Integrantes ficam ocultos no card e aparecem ao clicar em **Ver integrantes**

### Endpoints de Grupos

| Método   | Endpoint                               | Descrição                                  | Acesso                 |
| -------- | -------------------------------------- | ------------------------------------------ | ---------------------- |
| `POST`   | `/api/v1/grupos`                       | Cria grupo                                 | ADMIN / TECHLEADER     |
| `GET`    | `/api/v1/grupos`                       | Lista grupos conforme permissão do usuário | Autenticado            |
| `GET`    | `/api/v1/grupos/{id}`                  | Busca grupo por ID                         | Autenticado            |
| `PUT`    | `/api/v1/grupos/{id}`                  | Edita grupo                                | ADMIN / líder do grupo |
| `DELETE` | `/api/v1/grupos/{id}`                  | Remove grupo                               | ADMIN / líder do grupo |
| `POST`   | `/api/v1/grupos/{id}/convites`         | Envia convite por e-mail                   | ADMIN / líder do grupo |
| `GET`    | `/api/v1/grupos/convites/me`           | Lista meus convites                        | Autenticado            |
| `POST`   | `/api/v1/grupos/convites/{id}/aceitar` | Aceita convite                             | Autenticado            |
| `POST`   | `/api/v1/grupos/convites/{id}/recusar` | Recusa convite                             | Autenticado            |

### Criar grupo

```json
{
  "nome": "Squad Backend",
  "descricao": "Equipe responsável pelo backend",
  "liderId": 1,
  "usuariosIds": [2, 3, 4]
}
```

### Convidar usuário

```http
POST /api/v1/grupos/{id}/convites
```

```json
{
  "email": "dev@empresa.com"
}
```

---

## 🤖 IA / Análise Inteligente

A IA do sistema funciona como um analisador de opções de reserva.

Ela considera:

* Grupo selecionado
* Quantidade de integrantes
* Tipo de funcionário de cada integrante
* Data
* Horário inicial
* Horário final
* Critério de proximidade
* Equipamentos dos assentos
* Capacidade da sala
* Disponibilidade dos assentos
* Compatibilidade mínima recomendada

Critérios de proximidade:

```txt
OBRIGATORIO
PREFERENCIAL
NENHUM
```

Exemplo de fluxo:

1. Usuário escolhe grupo
2. Escolhe data e horário
3. Define se os assentos precisam estar próximos
4. IA analisa salas e assentos disponíveis
5. Sistema retorna opções com compatibilidade em porcentagem
6. Usuário confirma uma opção
7. Backend cria as reservas em grupo

### Endpoint de opções da IA

```http
POST /api/v1/ia/opcoes
```

Exemplo de body:

```json
{
  "grupoId": 1,
  "dataReserva": "2026-06-08",
  "horarioInicio": "10:00",
  "horarioFim": "12:00",
  "criterioProximidade": "PREFERENCIAL"
}
```

### Confirmar opção da IA

```http
POST /api/v1/reservas/confirmar-opcao
```

```json
{
  "grupoId": 1,
  "salaId": 2,
  "dataReserva": "2026-06-08",
  "horarioInicio": "10:00",
  "horarioFim": "12:00",
  "posicoesAssentos": [1, 2, 3, 4]
}
```

---

## 📊 Dashboard

O dashboard administrativo apresenta dados reais do sistema.

Cards principais:

* Reservas de hoje
* Salas cadastradas
* Reservas ativas
* Usuários ativos

Gráficos e blocos:

* Distribuição das reservas por status
* Salas mais usadas
* Reservas de hoje
* Salas cadastradas

O dashboard consome os endpoints:

```txt
GET /api/v1/salas
GET /api/v1/reservas/historico
GET /api/v1/usuarios/listarUsuarios
GET /api/v1/grupos
```

---

## 🗓️ Calendário

A tela de calendário mostra as reservas em formato mensal.

Funcionalidades:

* Navegação entre meses
* Contador de reservas por dia
* Lista lateral de reservas do dia selecionado
* Detalhes da reserva
* Cancelamento de reserva pelo calendário

Fonte de dados:

```http
GET /api/v1/reservas/historico
```

---

## 📈 Relatórios

A tela de relatórios exibe métricas consolidadas das reservas.

Métricas:

* Total de reservas
* Horas reservadas
* Sala mais usada
* Cancelamentos
* Distribuição por sala
* Ranking de uso
* Pico de uso por horário

Também há exportação de relatório em PDF.

Tecnologias usadas para PDF:

```txt
html2canvas
jsPDF
```

---

## ⚙️ Configurações

A tela de configurações permite:

* Visualizar perfil
* Alterar nome/e-mail
* Escolher tipo de funcionário
* Alterar tema
* Sair da conta

O tipo de funcionário atualizado impacta as recomendações da IA.

Endpoint:

```http
PUT /api/v1/usuarios/me/tipo-funcionario
```

Body:

```json
{
  "tipoFuncionario": "DESIGNER"
}
```

---

## 👤 Usuários

### Endpoints de Usuários

| Método   | Endpoint                               | Descrição                    | Acesso      |
| -------- | -------------------------------------- | ---------------------------- | ----------- |
| `GET`    | `/api/v1/usuarios`                     | Lista usuários               | ADMIN       |
| `GET`    | `/api/v1/usuarios/listarUsuarios`      | Lista usuários               | ADMIN       |
| `GET`    | `/api/v1/usuarios/meuPerfil`           | Perfil do usuário logado     | Autenticado |
| `PUT`    | `/api/v1/usuarios`                     | Atualiza usuário autenticado | Autenticado |
| `PUT`    | `/api/v1/usuarios/me/tipo-funcionario` | Atualiza tipo de funcionário | Autenticado |
| `DELETE` | `/api/v1/usuarios/{id}`                | Remove usuário por ID        | ADMIN       |
| `DELETE` | `/api/v1/usuarios/deletarConta`        | Remove a própria conta       | Autenticado |

---

## 🗄️ Banco de Dados

O projeto usa PostgreSQL com Flyway.

### Principais tabelas

```txt
usuarios
salas
assentos
reservas
grupos
grupo_usuarios
convites_grupo
refresh_tokens
password_reset_tokens
```

### Usuários

Campos principais:

```txt
id
email
senha
username
role
tipo_funcionario
deletado
```

### Salas

Campos principais:

```txt
id
nome
capacidade
status
local
cidade
estado
andar
bloco
raio_proximidade
```

### Assentos

Campos principais:

```txt
id
sala_id
posicao
equipamento_assento
coordenada_x
coordenada_y
ativo
```

### Reservas

Campos principais:

```txt
id
horario_inicio
horario_fim
data_reserva
posicao_assento
sala_id
usuario_id
status_reserva
codigo_grupo
```

### Grupos

Campos principais:

```txt
id
nome
descricao
lider_id
```

### Convites de grupo

Campos principais:

```txt
id
grupo_id
email_convidado
status
criado_em
respondido_em
```

---

## 🔧 Configuração

Exemplo de `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gerenciadordesala
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate

spring.flyway.enabled=true

api.security.token.secret=${JWT_SECRET:my-secret-key}

spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${API_KEY}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 📖 Swagger

Com a aplicação rodando, acesse:

```txt
http://localhost:8080/swagger-ui.html
```

ou:

```txt
http://localhost:8080/swagger-ui/index.html
```

---

## ▶️ Como Rodar o Backend

```bash
git clone https://github.com/squad27-accenture/Gerenciador-de-salas.git

cd Gerenciador-de-salas

./mvnw spring-boot:run
```

API disponível em:

```txt
http://localhost:8080
```

Base da API:

```txt
http://localhost:8080/api/v1
```

---

## ▶️ Como Rodar o Frontend

O frontend está em HTML, CSS e JavaScript puro.

Recomenda-se abrir com Live Server ou servidor local.

Exemplo com VS Code:

```txt
1. Abrir a pasta do frontend
2. Instalar extensão Live Server
3. Abrir index.html com Live Server
```

O frontend consome:

```txt
http://localhost:8080/api/v1
```

---

## 🧪 Fluxo de Teste Recomendado

1. Criar usuário admin
2. Fazer login
3. Cadastrar salas
4. Cadastrar/visualizar assentos
5. Criar usuários comuns
6. Criar grupo
7. Convidar usuários por e-mail
8. Aceitar convite
9. Criar reserva individual
10. Testar IA para reserva em grupo
11. Confirmar opção sugerida
12. Ver reservas no calendário
13. Ver dados no dashboard
14. Exportar relatório em PDF

---

## 👥 Time

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
        <sub><b>Anderson Ferreira</b></sub>
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
