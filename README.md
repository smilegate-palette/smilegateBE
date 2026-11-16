# 스마일 게이트 팔레트 커뮤니티 페이지

스마일 게이트 팔레트 커뮤니티 백엔드 설명 문서입니다.


## Tech stack
**language : JAVA Spring boot** 
**DB : supabase**
**language : JAVA Spring boot**  
**server : OCI (Oracle Cloud  Infrastructure)**  


## CI / CD


## Oracle Cloud - Supabase - GitHub Actions CI/CD Workflow

## 1. CI/CD Architecture Flowchart

```mermaid
flowchart TD
    A["Developer"] -->|Git Push| B["GitHub Repository"]

    B --> C["GitHub Actions"]
    
    subgraph CI["Continuous Integration"]
        C --> D["Checkout Source Code"]
        D --> E["Install Dependencies"]
        E --> F["Lint & Unit Test"]
        F --> G{"Test Passed?"}
        G -->|Yes| H["Build Application"]
        G -->|No| I["Build Failed"]
    end

    subgraph CD["Continuous Deployment"]
        H --> J["Build Docker Image"]
        J --> K["Push Docker Image"]
        K --> L["Connect to Oracle Cloud"]
        L --> M["Deploy Application"]
        M --> N["Restart Container"]
        N --> O["Health Check"]
    end

    O --> P{"Deployment Success?"}
    P -->|Yes| Q["Production Running"]
    P -->|No| R["Rollback"]

    subgraph SUPABASE["Supabase Backend"]
        S["PostgreSQL Database"]
        T["Authentication"]
        U["Storage"]
    end

    Q <-->|API Connection| S
    Q <-->|Auth API| T
    Q <-->|Storage API| U

    style Q fill:#90EE90,color:#000
    style I fill:#FFCCCC,color:#000
    style R fill:#FFCCCC,color:#000
```

## 2. Infrastructure Architecture

```mermaid
flowchart LR
    DEV["Developer"]

    subgraph GITHUB["GitHub"]
        REPO["Repository"]
        ACTION["GitHub Actions"]
    end

    subgraph ORACLE["Oracle Cloud"]
        VM["Compute Instance"]
        DOCKER["Docker"]
        APP["Application Server"]
    end

    subgraph SUPABASE["Supabase"]
        DB["PostgreSQL"]
        AUTH["Authentication"]
        STORAGE["Storage"]
    end

    DEV -->|Git Push| REPO
    REPO --> ACTION
    ACTION -->|Deploy via SSH| VM
    VM --> DOCKER
    DOCKER --> APP

    APP <-->|Database API| DB
    APP <-->|Auth API| AUTH
    APP <-->|Storage API| STORAGE
```

## 3. Deployment Process

```mermaid
sequenceDiagram
    participant DEV as Developer
    participant GH as GitHub
    participant GA as GitHub Actions
    participant OCI as Oracle Cloud
    participant SB as Supabase

    DEV->>GH: Git Push
    GH->>GA: Trigger CI/CD
    GA->>GA: Install Dependencies
    GA->>GA: Run Tests

    alt Tests Passed
        GA->>GA: Build Docker Image
        GA->>OCI: Deploy Application
        OCI->>OCI: Restart Container
        OCI->>OCI: Health Check
        OCI->>SB: Connect Database
        SB-->>OCI: API Response
        OCI-->>GA: Deployment Success
        GA-->>DEV: Success Notification
    else Tests Failed
        GA-->>DEV: Failure Notification
    end
```
