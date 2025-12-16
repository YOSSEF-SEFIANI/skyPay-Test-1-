# 🏦 Skypay Banking Service

![Java](https://img.shields.io/badge/Java-21-orange? style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-brightgreen?style=flat-square&logo=spring)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=flat-square&logo=apache-maven)
![Tests](https://img.shields.io/badge/Tests-9%20passed-success?style=flat-square)
![License](https://img.shields.io/badge/License-Educational-lightgrey?style=flat-square)

## 📋 Description

Service bancaire minimaliste implémentant les opérations de base :  **dépôt**, **retrait** et **relevé de compte**.

Développé dans le cadre du test technique Skypay, ce projet démontre la maîtrise de Java 21, Spring Boot, TDD et des bonnes pratiques de développement.

---

## 🎯 Fonctionnalités

- ✅ **Dépôt d'argent** avec validation
- ✅ **Retrait d'argent** avec gestion des fonds insuffisants
- ✅ **Relevé de compte** en ordre chronologique inversé
- ✅ **Gestion d'exceptions** métier
- ✅ **Tests unitaires** complets (100% couverture)

---

## 🚀 Quick Start

### Prérequis

- Java 21+
- Maven 3.9+

### Installation

```bash
# Cloner
git clone https://github.com/votre-repo/skypay-banking. git
cd skypay-banking

# Compiler
mvn clean install

# Tester
mvn test

# Exécuter
mvn spring-boot:run
```

---

## 📦 Technologies

| Stack | Version |
|-------|---------|
| Java | 21 |
| Spring Boot | 3.4.6 |
| JUnit 5 | 5.10.x |
| AssertJ | 3.24.x |
| Lombok | 1.18.x |

---

## 🏗️ Architecture

```
src/main/java/com/skypay/bank/
├── domain/
│   ├── Account.java           # Entité compte
│   └── Transaction.java       # Record transaction (Java 21)
├── service/
│   ├── AccountService.java    # Interface
│   └── impl/
│       └── AccountServiceImpl.java
├── exception/
│   └── InsufficientFundsException. java
└── config/
    └── LocalDateConverter.java
```

**Principes appliqués** :  SOLID, Clean Code, Domain-Driven Design

---

## 📊 Exemple d'Utilisation
### code 
```java
AccountService service = new AccountServiceImpl();

service.deposit(1000);   // Dépôt de 1000
service.deposit(2000);   // Dépôt de 2000
service.withdraw(500);   // Retrait de 500

service.printStatement(); // Affiche le relevé
```

**Output** :

```
Date       || Amount || Balance
15/12/2025 || -500   || 2500
15/12/2025 || 2000   || 3000
15/12/2025 || 1000   || 1000
```

---

## 🧪 Tests

**9 tests unitaires** couvrant tous les cas :

```bash
# Tous les tests
mvn test

# Test spécifique
mvn test -Dtest=AccountServiceTest#shouldMatchSpecification

# Avec rapport de couverture
mvn test jacoco:report
```

**Résultat** :

```
Tests run: 9, Failures:  0, Errors: 0, Skipped: 0
✅ BUILD SUCCESS
```

---

## 🛠️ Commandes Utiles

```bash
# Compilation
mvn clean compile

# Tests
mvn test

# Package JAR
mvn package

# Lancer l'application
mvn spring-boot:run

# Vérifier les dépendances obsolètes
mvn versions:display-dependency-updates
```

---

## 📁 Structure du Projet

```
skypay/
├── pom.xml
├── README.md
├── TESTS.md
└── src/
    ├── main/
    │   ├── java/com/skypay/
    │   │   ├── SkypayApplication.java
    │   │   └── bank/
    │   │       ├── model/
    │   │       ├── service/
    │   │       └── exception/
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/skypay/bank/
            └── service/
                └── AccountServiceTest.java
```

---

## ✨ Points Forts

| Aspect | Implémentation |
|--------|----------------|
| **Java 21** | Records, Pattern Matching ready |
| **Tests** | TDD avec JUnit 5 + AssertJ |
| **Clean Code** | SOLID, DRY, nommage explicite |
| **Architecture** | Separation of Concerns |
| **Sécurité** | Validation, CVE corrigés |
| **Documentation** | Complète et claire |

---

## 🚀 Améliorations Futures

- [ ] Persistance JPA/Hibernate
- [ ] API REST avec Spring Web
- [ ] Multi-comptes utilisateur
- [ ] Authentification OAuth2
- [ ] Audit trail
- [ ] Monitoring (Actuator, Prometheus)

---

## 📝 Spécification (PDF)

**Scénario d'acceptation** :

```gherkin
Given a client makes a deposit of 1000 on 10-01-2012
And a deposit of 2000 on 13-01-2012
And a withdrawal of 500 on 14-01-2012
When they print their bank statement
Then they would see: 

Date       || Amount || Balance
14/01/2012 || -500   || 2500
13/01/2012 || 2000   || 3000
10/01/2012 || 1000   || 1000
```

✅ **Conformité 100%**

---

## 🎉 Conclusion

Ce projet démontre :

✅ Maîtrise de **Java 21** et **Spring Boot 3.x**  
✅ Pratique du **Test-Driven Development**  
✅ Application des **principes SOLID**  
✅ Code **clean, testé et documenté**

**BUILD SUCCESS** 🚀

---

*pour le test technique Skypay*