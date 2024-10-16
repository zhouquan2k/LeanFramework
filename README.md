- # LeanDDD
  
  Welcome to **LeanDDD**! This project aims to create a simple yet powerful foundational framework for Domain-Driven Design (DDD). Our guiding principle is to make it easy to start, flexible to grow, and true to the core ideas of DDD. If you’re looking for a straightforward way to implement DDD concepts without excessive complexity, LeanDDD is for you!  
- ## Purpose
  
  The purpose of LeanDDD is to provide a **simple starting point** that is aligned with DDD principles, particularly in isolating business logic from technical infrastructure. Most existing DDD frameworks I have seen are kind of overly complex, so LeanDDD aims to fill the gap by making things as simple as possible for:  
  
	1. **Medium to Small-Scale Applications**: Suitable for department-level applications rather than large-scale enterprise apps.
	2. **Complex Business Logic**: Ideal when business needs are intricate and keep growing and DDD methods can help manage complexity as your system evolves.
	3. **Flexible Growth**: Start small, keep it simple, and add complexity only when necessary.

- ## Tech-Stack
- **Java 11**, **Spring**, **MyBatis**, **Maven** (no surprise)
- **Vue 2**, **Element-UI** (will upgrade soon)
- ## LeanDDD Features
- **DDD Principles**: Separation between **business logic** and **technical infrastructure**. The goal is to ensure that business concepts are well isolated from underlying technologies.
- **Keep It Simple**: We emphasize starting with the simplest approach and only adopting more complex solutions as problems evolve. This leads to a more natural growth in the design.
- **Unified Metadata with Meta-Annotations**: We use domain-level annotations that are implementation-independent, keeping your domain layer clean and free from infrastructure details. This means your domain code depends only on Java, Lombok, and LeanDDD annotations, and we transform them to implementation-specific annotations as needed.
- **Framework & Solutions**: LeanDDD includes a foundational framework and solutions to common patterns, which are documented to help you tackle typical challenges.
- **Team Development & AI-Assistant Friendly**: LeanDDD can also serve as a guide for team development and as a reference for AI-assisted coding training.
- **Monolithic/Microservice**: you can create app with LeanDDD from a monolithic application instead of microservices at the beginning. However, you can easily extend it into smaller services if your application needs to grow, keeping maintenance simple.
- ## Core Functionalities
- ### Features
- **RBAC (Role-Based Access Control)**: Manage users, roles, and permissions across different organizations.
- **Workflow Integration**: User roles and workflows are synchronized, facilitating more effective workflow management.
- ### Development
- **DDD Practice**:
	- **Business and Technology Separation**: , we’re all about keeping business logic and technical details in their own lanes. That’s why our core model layer is implementation-independent—it focuses purely on the business side, true to DDD principles. Here, you’ll find Aggregates and Entities crafted using clean, object-oriented techniques. no dependency on any specific tech. We rely on Services to coordinate these domain objects and Repositories to handle their persistence
	- **Bounded Contexts**: Define bounded contexts to divide the domain into cohesive, independently managed parts. This ensures that different parts of the domain don’t interfere with each other and can evolve separately. we use modules or packages to represent bounded contexts.
	- **Domain Events**: Model significant events in the domain to decouple different  Bounded Contexts and facilitate eventual consistency.
	- **CQRS Support**: Distinguish between Command and Query responsibilities explicitly, either via different services or methods.
- **Rapid Development**: Automatically synchronize and update the database schema based on your entity metadata.
- **Logging**: Automatically log service calls into an audit table or log file.
- **Transaction Management**: Changes made during a single service call are part of one database transaction, including any resulting events.
- **Declarative Permission Checks**: Check if a subject has the required permissions on a resource declaratively.
- ## Components
- ### LeanFramework
  LeanFramework should be used as a Maven module dependency for each application process. It includes the foundational components needed by all services and modules.  
	- **Common**: Contains general metadata definitions and implementation-agnostic annotations, enums, and interfaces used across modules.
	- **Interface**: Defines interfaces for common features such as security management, logging, and workflow.
	- **Data**: Implements data persistence logic.
	- **Spring**: Spring-specific integrations.
	- **Test**: Provides infrastructure for testing.
	- **WebApp-parent**: Parent POM for your application.
- ### LeanModules
  LeanModules can either run as separate processes in a microservice architecture or be included as Maven modules in other applications, supporting both monolithic and microservice approaches.  
	- **System**: RBAC and security management.
	- **File**: File upload and download functionality.
	- **BPM**: Business Process Management (workflow), integrating with Flowable.
- ### Demo Application (WIP)
- A knowledge base application as a demonstration project (TODO).
- ## Application Practice (WIP)
- **API**: Public service interfaces and Value Objects (VOs) for other modules to use.
- **Model**: Contains DDD-compliant service implementations and domain classes (technical-agnostic layer).
- **Infra**: Contains MyBatis interfaces and XML, as well as MapStruct mapping interfaces (technical layer).
  
  *Package structure and main components are still under construction.*  
- ## Roadmap & Contributions
  This project is still a work in progress. We’ll be continuously expanding documentation and usage examples. If you’re interested in contributing, we’d love your help!  
	- demo application
	- more docs and examples
-
- Feel free to raise issues, suggest features, or submit pull requests.
- ## License
  [MIT License](LICENSE)  
  
---
  
  Thanks for checking out LeanDDD! We hope this framework makes your DDD journey simpler and more effective. Get started, stay simple, and grow only as needed!