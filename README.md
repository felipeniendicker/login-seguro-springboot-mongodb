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
- Spring Session Data MongoDB
- Maven

## Estado atual

O projeto está na fase de preparação da estrutura inicial. A autenticação
herdada está preservada temporariamente para migração em uma etapa posterior.
Cadastro, login e logout ainda não foram implementados na nova arquitetura.

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
