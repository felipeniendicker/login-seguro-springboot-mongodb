# Login Seguro

Sistema acadêmico de cadastro, autenticação e autorização desenvolvido para a
disciplina de Programação para Internet. O projeto utiliza senhas protegidas
com BCrypt, sessões HTTP persistidas no MongoDB e controle de acesso baseado em
três perfis.

Repositório público:
[felipeniendicker/login-seguro-springboot-mongodb](https://github.com/felipeniendicker/login-seguro-springboot-mongodb)

## Objetivo

Demonstrar um fluxo seguro e simples de cadastro, login e logout com Spring
Security. A aplicação oferece interface web com Thymeleaf e endpoints REST,
mantendo usuários e sessões no MongoDB Atlas sem armazenar credenciais no
código-fonte.

## Funcionalidades

- Cadastro público com validação de nome, e-mail e senha.
- Normalização e unicidade do e-mail.
- Hash de senha com BCrypt.
- Login e logout com sessões HTTP.
- Persistência das sessões no MongoDB.
- Proteção CSRF nos formulários e endpoints de alteração.
- Autorização com os perfis `USUARIO`, `MODERADOR` e `ADMIN`.
- Interface responsiva com Thymeleaf e CSS externo.
- APIs que não retornam senha nem hash.

## Tecnologias utilizadas

- Java 17.
- Spring Boot 4.1.1.
- Spring Security.
- Spring Data MongoDB.
- MongoDB Spring Session 4.0.0.
- MongoDB Atlas.
- Thymeleaf.
- Jakarta Validation.
- Maven Wrapper.
- JUnit, Mockito e Spring Security Test.

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/com/atividade/login/
│   │   ├── config/       # Spring Security e sessões MongoDB
│   │   ├── controller/   # APIs REST e páginas Thymeleaf
│   │   ├── dto/          # Dados de entrada e saída
│   │   ├── exception/    # Exceções da aplicação
│   │   ├── model/        # Documento Usuario
│   │   ├── repository/   # Acesso ao MongoDB
│   │   └── service/      # Cadastro e carregamento de usuários
│   └── resources/
│       ├── static/css/   # Estilos e variáveis de tema
│       ├── templates/    # Páginas e fragmentos Thymeleaf
│       └── application.properties
└── test/java/            # Testes unitários e de interface web
docs/
└── documentacao.md       # Documentação acadêmica para exportação em PDF
```

## Requisitos de instalação

- JDK 17 disponível no `PATH`.
- Windows PowerShell.
- Acesso a um cluster MongoDB Atlas.
- Usuário de banco restrito ao banco `login_seguro`.
- Endereço IP autorizado no controle de acesso de rede do Atlas.
- Git, caso o projeto seja clonado do GitHub.

O Maven não precisa ser instalado globalmente, pois o projeto inclui o Maven
Wrapper. Confira o Java instalado:

```powershell
java -version
```

## Obtenção do projeto

```powershell
git clone https://github.com/felipeniendicker/login-seguro-springboot-mongodb.git
Set-Location login-seguro-springboot-mongodb
```

## Configuração segura do MongoDB Atlas

A conexão é lida exclusivamente da variável de ambiente `MONGODB_URI`:

```properties
spring.mongodb.uri=${MONGODB_URI}
```

Use um usuário exclusivo da aplicação com permissão apenas sobre o banco
`login_seguro`. Não utilize uma conta administrativa do cluster. Copie a URI
fornecida pelo Atlas, selecione explicitamente o banco `login_seguro` e codifique
caracteres especiais da senha para URL.

Configure a variável somente na sessão atual do PowerShell:

```powershell
$env:MONGODB_URI = "mongodb+srv://<USUARIO>:<SENHA_CODIFICADA>@<CLUSTER>.mongodb.net/login_seguro?retryWrites=true&w=majority"
```

Verifique apenas a existência da variável, sem imprimir seu conteúdo:

```powershell
Test-Path Env:MONGODB_URI
```

O arquivo `.env.example` é somente uma referência e não é carregado
automaticamente pelo Spring Boot. Arquivos `.env` reais são ignorados pelo Git.
Nunca grave a URI verdadeira no README, no `application.properties` ou em
arquivos versionados.

## Cookie seguro em HTTPS

A variável `SESSION_COOKIE_SECURE` controla o atributo `Secure` do cookie de
sessão. Para desenvolvimento local por HTTP:

```powershell
$env:SESSION_COOKIE_SECURE = "false"
```

Para produção servida exclusivamente por HTTPS:

```powershell
$env:SESSION_COOKIE_SECURE = "true"
```

O cookie `SESSION` também utiliza `HttpOnly` e `SameSite=Lax`.

## Execução local no Windows

Depois de configurar as variáveis na mesma janela do PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## Interface web

| Página | URL | Acesso |
|---|---|---|
| Login | `http://localhost:8080/login` | Público |
| Cadastro | `http://localhost:8080/cadastro` | Público |
| Painel do usuário | `/painel/usuario` | Todos os perfis |
| Painel do moderador | `/painel/moderador` | `MODERADOR` e `ADMIN` |
| Painel administrativo | `/painel/admin` | `ADMIN` |
| Acesso negado | `/acesso-negado` | Exibido em tentativa sem permissão |

Após o login, `/painel` redireciona o usuário ao painel correspondente ao seu
perfil. O logout é enviado por formulário `POST` com token CSRF.

## Perfis de acesso

- `USUARIO`: perfil atribuído automaticamente pelo cadastro público. Acessa o
  painel de usuário e `/api/auth/me`.
- `MODERADOR`: possui os acessos de `USUARIO` e acessa o painel de moderação.
- `ADMIN`: possui os acessos anteriores e acessa o painel administrativo.

O formulário e a API de cadastro não recebem perfil. Portanto, uma requisição
pública não pode criar `MODERADOR` ou `ADMIN`.

## Persistência de usuários e sessões

Os usuários são documentos da coleção `usuarios`. O e-mail possui índice único
e as senhas são armazenadas somente como hash BCrypt.

O MongoDB Spring Session substitui a sessão em memória por documentos na
coleção `sessoes`. A sessão expira após 30 minutos de inatividade. O navegador
recebe apenas o identificador no cookie `SESSION`; os dados de autenticação
permanecem no servidor. O logout invalida a sessão e impede sua reutilização.

## Execução dos testes

A suíte local abaixo não acessa o Atlas e utiliza mocks para repositórios:

```powershell
.\mvnw.cmd clean "-Dtest=AuthSessionControllerTest,PerfilAcessoControllerTest,SessionConfigTest,UsuarioControllerTest,UsuarioServiceTest,WebInterfaceControllerTest" test
```

O teste `AtlasConnectionIntegrationTest` é desabilitado por padrão. Ele somente
deve ser executado de forma explícita, em uma sessão que já possua
`MONGODB_URI`, pois realiza inserção e remoção de um documento fictício na
coleção exclusiva `teste_conexao_atlas`:

```powershell
.\mvnw.cmd "-Datlas.integration.enabled=true" "-Dtest=AtlasConnectionIntegrationTest" test
```

## Gitflow

O repositório utiliza o seguinte fluxo:

- `main`: versão estável destinada à entrega.
- `develop`: integração das funcionalidades concluídas.
- `feature/*`: uma branch por etapa, criada a partir de `develop`.

As funcionalidades são registradas em commits separados, enviadas em suas
branches e integradas a `develop` com merge `--no-ff`. A integração final em
`main` deve ocorrer apenas quando a versão estiver revisada para entrega.

Branches utilizadas incluem `feature/persistencia-mongodb`,
`feature/conexao-atlas`, `feature/cadastro-usuarios`, `feature/login-sessoes`,
`feature/perfis-acesso`, `feature/interface-thymeleaf` e
`feature/documentacao-entrega`.

## Documentação acadêmica

O texto-base para geração do PDF está em
[`docs/documentacao.md`](docs/documentacao.md). Antes da exportação, preencha os
campos de identificação da instituição e aplique o modelo exigido pelo curso.
