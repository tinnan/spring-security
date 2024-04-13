Learning Spring security from
1. [Securing a Web Application](https://spring.io/guides/gs/securing-web)
2. [Securing Spring Boot API With API Key and Secret](https://www.baeldung.com/spring-boot-api-key-secret)
3. [Multiple apps access to single H2 database](https://www.baeldung.com/spring-boot-access-h2-database-multiple-apps)
4. [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap)

### About the application
- Has 2 security filter chain.
  - First, for securing request path `/app/**` which is for REST API. This filter chain is protected by API key 
    authenticator which also look up authenticated user's authorities from embedded LDAP server.
  - Second, the other is for securing other request path, which is for Web MVC requests. This filter chain use 
    user/password authentication against an embedded LDAP server.
- Use H2 In-Memory database for storing basic users, roles, API keys.
- H2 console enabled at path `/h2-console`. Connect to URL `jdbc:h2:tcp://localhost:9090/mem:mydb` Username: `sa` 
  Password: `<empty>`
- App starts with default user `ben` with password `benspassword` and its dummy API key.