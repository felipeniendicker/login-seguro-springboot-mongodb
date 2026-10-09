# Login Seguro

Projeto acadêmico da disciplina de Programação para Internet.

## Objetivo

Desenvolver um sistema de autenticação e autorização com cadastro, login,
logout e três perfis de acesso. As senhas serão protegidas com BCrypt, e os
usuários e as sessões serão armazenados no MongoDB Atlas.

## Tecnologias previstas

- Java 17
- Spring Boot
- Spring Security
- Thymeleaf
- MongoDB Atlas
- MongoDB Spring Session
- Maven

## Perfis de acesso

- `USUARIO`: acessa `/api/usuario/painel` e `/api/auth/me`.
- `MODERADOR`: possui os acessos de `USUARIO` e acessa
  `/api/moderador/painel`.
- `ADMIN`: possui os acessos de `MODERADOR` e acessa `/api/admin/painel`.

O cadastro público sempre cria usuários com o perfil `USUARIO`. Perfis
privilegiados não podem ser escolhidos pela requisição de cadastro.

## Estado atual

O projeto possui cadastro público com perfil `USUARIO`, autenticação por sessão,
logout, proteção CSRF e controle de acesso para três perfis. Usuários e sessões
são persistidos no MongoDB.

## Configuração local do MongoDB Atlas

A aplicação lê a conexão pela variável de ambiente `MONGODB_URI`. O banco
utilizado é `login_seguro`, com um usuário da aplicação que deve ter permissões
restritas somente a esse banco, como `login_seguro_app`.

Na sessão atual do Windows PowerShell, configure a variável substituindo os
campos entre `< >` pelos seus valores locais:

```powershell
$env:MONGODB_URI = "mongodb+srv://login_seguro_app:<SENHA_CODIFICADA>@<SEU_CLUSTER>.mongodb.net/login_seguro?retryWrites=true&w=majority"
```

Se a senha possuir caracteres especiais, utilize a versão codificada para URL.
Para confirmar a existência da variável sem exibir seu conteúdo:

```powershell
Test-Path Env:MONGODB_URI
```

A variável permanece disponível somente na sessão atual do PowerShell. Nunca
grave a URI real no `application.properties`, no `.env.example` ou no Git.
