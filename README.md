# Interview Prep Bot

An AI-powered interview preparation chatbot built with Spring Boot and Thymeleaf. Practice technical interviews with real-time AI-generated questions, instant answer evaluation, scoring, and detailed feedback — all without a database.

## Features

- **AI-Generated Questions** — Dynamic questions powered by Groq (llama-3.3-70b-versatile)
- **Answer Evaluation** — Each answer is scored 0–10 with detailed feedback and a model answer
- **Company-Specific Mode** — Filter questions by company (Google, Amazon, Microsoft, Meta, Netflix, Flipkart, TCS, Infosys, Wipro)
- **Difficulty Levels** — Beginner, Intermediate, Advanced
- **10 Categories** — Java, Python, DSA, System Design, Spring Boot, SQL, JavaScript, React, DevOps, Git
- **Session-Based** — No database; all state stored in `HttpSession`
- **Progress Tracking** — Live progress bar and previous Q&A accordion during the interview
- **Summary Report** — Score circle, performance label, and full Q&A breakdown at the end
- **Loading Overlay** — Spinner shown while AI is processing

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Spring Boot 3.x, Java 21, Maven     |
| Frontend   | Thymeleaf, Bootstrap 5, Bootstrap Icons |
| AI API     | Groq API (llama-3.3-70b-versatile)  |
| HTTP Client| Spring `RestClient`                 |
| Deployment | Railway                             |

## Project Structure

```
src/main/java/com/chatbot/
├── ChatBotSbApplication.java       # Main class + @EnableConfigurationProperties
├── controller/
│   ├── InterviewController.java    # All web routes
│   └── GlobalExceptionHandler.java # Error handling
├── service/
│   └── InterviewService.java       # Question generation + answer evaluation logic
├── groq/
│   ├── GroqProperties.java         # Config properties (key, model)
│   ├── GroqClient.java             # HTTP client for Groq API
│   ├── GroqRequest.java            # Request POJO
│   └── GroqResponse.java           # Response POJO
└── model/
    ├── InterviewSession.java       # Session state (category, difficulty, company, Q&A pairs)
    ├── QAPair.java                 # A single question-answer-feedback record
    └── EvaluationResult.java       # Parsed AI evaluation (score, feedback, modelAnswer)

src/main/resources/
├── application.properties
└── templates/
    ├── home.html       # Setup form (category, company, difficulty)
    ├── interview.html  # Live interview page
    ├── summary.html    # Final score and breakdown
    └── error.html      # Friendly error page
```

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- [Groq API key](https://console.groq.com) (free tier available)

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/ChatBotSBApplication.git
   cd ChatBotSBApplication
   ```

2. **Set your Groq API key**

   **Option A — Environment variable (recommended)**
   ```bash
   export GROQ_API_KEY=your_groq_api_key_here
   ```

   **Option B — `application.properties` (local dev only, do not commit)**
   ```properties
   groq.api.key=your_groq_api_key_here
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Open in browser**
   ```
   http://localhost:8080
   ```

## How It Works

### Application Flow

```
GET  /           → Home page (select category, company, difficulty)
POST /start      → Creates InterviewSession, generates Q1, redirects to /interview
GET  /interview  → Shows current question + previous Q&As
POST /answer     → Evaluates answer, saves QAPair, generates next question
POST /end        → Ends interview early, redirects to /summary
GET  /summary    → Displays score circle + full Q&A breakdown
POST /reset      → Clears session, redirects to home
```

### AI Prompt Strategy

**Question Generation**
- General: `"Generate a [difficulty] [category] interview question. Return just the question."`
- Company-specific: `"Generate a [difficulty] [category] interview question that [Company] is known to ask. Return just the question."`
- Avoids repeating previous questions by including them in the prompt context.

**Answer Evaluation**
- Sends question + user answer to Groq with `response_format: json_object`
- Asks for: `score` (0–10), `feedback` (constructive), `modelAnswer` (ideal answer)
- Response is parsed into `EvaluationResult` via Jackson `ObjectMapper`

## Deploying to Railway

1. Push your code to a GitHub repository.

2. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo**.

3. Select your repository.

4. In **Variables**, add:
   ```
   GROQ_API_KEY = your_groq_api_key_here
   ```
   Railway automatically injects `PORT` — the app reads it via `server.port=${PORT:8080}`.

5. Railway will build and deploy automatically. Your app will be live at a `*.railway.app` URL.

## Supported Companies

| Company   | Focus Areas                              |
|-----------|------------------------------------------|
| Google    | DSA, System Design, Problem Solving      |
| Amazon    | Leadership Principles, DSA, System Design |
| Microsoft | DSA, System Design, .NET/Java            |
| Meta      | DSA, System Design, Behavioral           |
| Netflix   | System Design, Java, Distributed Systems |
| Flipkart  | DSA, Java, System Design                 |
| TCS       | Core Java, SQL, Basic DSA               |
| Infosys   | Core Java, Python, Basic Concepts        |
| Wipro     | Core Java, SQL, Basics                  |
| General   | Topic-based, no company filter           |

## Environment Variables

| Variable      | Required | Description                        |
|---------------|----------|------------------------------------|
| `GROQ_API_KEY`| Yes      | Your Groq API key                  |
| `PORT`        | No       | Server port (Railway sets this automatically; defaults to 8080) |

## License

This project is open source and available under the [MIT License](LICENSE).
