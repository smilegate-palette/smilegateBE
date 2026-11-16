# 스마일 게이트 팔레트 커뮤니티 페이지

스마일 게이트 팔레트 커뮤니티 백엔드 설명 문서입니다.


**Domain: https://www.smilegate-palette.com/** 


## CI / CD


## Oracle Cloud - Supabase - GitHub Actions CI/CD Workflow


## CI/CD Architecture

```mermaid
flowchart TD
    A["👨‍💻 Developer"] -->|Git Push| B["GitHub Repository"]

    B --> C["GitHub Actions"]

    subgraph CI["🔵 Continuous Integration (CI)"]
        C --> D["Checkout Source Code"]
        D --> E["Setup Java Environment"]
        E --> F["Install Dependencies"]
        F --> G["Run Tests"]
        G --> H{"Test Passed?"}
        H -->|No| I["❌ Build Failed"]
        H -->|Yes| J["Build Spring Boot JAR"]
    end

    subgraph CD["🟢 Continuous Deployment (CD)"]
        J --> K["Connect to OCI via SSH"]
        K --> L["Transfer JAR File"]
        L --> M["Deploy JAR on OCI Server"]
        M --> N["Restart Spring Boot Application"]
        N --> O["Health Check"]
    end

    O --> P{"Deployment Success?"}
    P -->|Yes| Q["✅ Production Running"]
    P -->|No| R["❌ Deployment Failed / Rollback"]

    subgraph SUPABASE["🟩 Supabase Backend"]
        S[("PostgreSQL Database")]
    end

    Q <-->|Database Connection| S

    style Q fill:#90EE90,color:#000,stroke:#228B22,stroke-width:2px
    style I fill:#FFB6B6,color:#000
    style R fill:#FFB6B6,color:#000
    style S fill:#3ECF8E,color:#000,stroke:#228B22
```

## Technology Stack

| Component | Technology | Description |
|---|---|---|
| Source Control | GitHub | Source code management |
| CI/CD | GitHub Actions | Automated build and deployment |
| Backend | Spring Boot | Java backend application |
| Build Tool | Gradle  | Dependency management and JAR build |
| Cloud Server | Oracle Cloud Infrastructure (OCI) | Production server |
| Deployment | SSH | Secure JAR file transfer |
| Database | Supabase PostgreSQL | Cloud database |

## Deployment Process

1. Developer pushes code to GitHub.
2. GitHub Actions starts the CI pipeline.
3. Java environment and dependencies are configured.
4. Automated tests are executed.
5. Spring Boot application is packaged into a JAR
