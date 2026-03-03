package com.chatbot.controller;

import com.chatbot.model.EvaluationResult;
import com.chatbot.model.InterviewSession;
import com.chatbot.model.QAPair;
import com.chatbot.service.InterviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class InterviewController {

    private static final String SESSION_KEY = "interviewSession";

    private final InterviewService interviewService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/start")
    public String startInterview(@RequestParam String category,
                                  @RequestParam String difficulty,
                                  @RequestParam(defaultValue = "General") String company,
                                  HttpSession session) {
        InterviewSession interviewSession = new InterviewSession();
        interviewSession.setCategory(category);
        interviewSession.setDifficulty(difficulty);
        interviewSession.setCompany(company);

        String firstQuestion = interviewService.generateQuestion(category, difficulty, company, interviewSession.getCompletedPairs());
        interviewSession.setCurrentQuestion(firstQuestion);

        session.setAttribute(SESSION_KEY, interviewSession);
        return "redirect:/interview";
    }

    @GetMapping("/interview")
    public String interview(HttpSession session, Model model) {
        InterviewSession interviewSession = (InterviewSession) session.getAttribute(SESSION_KEY);

        if (interviewSession == null) {
            return "redirect:/";
        }
        if (interviewSession.isCompleted()) {
            return "redirect:/summary";
        }

        model.addAttribute("interviewSession", interviewSession);
        model.addAttribute("maxQuestions", InterviewSession.MAX_QUESTIONS);
        return "interview";
    }

    @PostMapping("/answer")
    public String submitAnswer(@RequestParam String answer, HttpSession session) {
        InterviewSession interviewSession = (InterviewSession) session.getAttribute(SESSION_KEY);

        if (interviewSession == null) {
            return "redirect:/";
        }

        EvaluationResult evaluation = interviewService.evaluateAnswer(
                interviewSession.getCategory(),
                interviewSession.getDifficulty(),
                interviewSession.getCurrentQuestion(),
                answer
        );

        QAPair pair = new QAPair(
                interviewSession.getCurrentQuestionNumber(),
                interviewSession.getCurrentQuestion(),
                answer,
                evaluation.getScore(),
                evaluation.getFeedback(),
                evaluation.getModelAnswer()
        );
        interviewSession.getCompletedPairs().add(pair);

        if (interviewSession.getCurrentQuestionNumber() >= InterviewSession.MAX_QUESTIONS) {
            interviewSession.setCurrentQuestionNumber(InterviewSession.MAX_QUESTIONS + 1);
            session.setAttribute(SESSION_KEY, interviewSession);
            return "redirect:/summary";
        }

        interviewSession.setCurrentQuestionNumber(interviewSession.getCurrentQuestionNumber() + 1);
        String nextQuestion = interviewService.generateQuestion(
                interviewSession.getCategory(),
                interviewSession.getDifficulty(),
                interviewSession.getCompany(),
                interviewSession.getCompletedPairs()
        );
        interviewSession.setCurrentQuestion(nextQuestion);

        session.setAttribute(SESSION_KEY, interviewSession);
        return "redirect:/interview";
    }

    @PostMapping("/end")
    public String endInterview(HttpSession session) {
        InterviewSession interviewSession = (InterviewSession) session.getAttribute(SESSION_KEY);
        if (interviewSession != null) {
            interviewSession.setCurrentQuestionNumber(InterviewSession.MAX_QUESTIONS + 1);
            session.setAttribute(SESSION_KEY, interviewSession);
        }
        return "redirect:/summary";
    }

    @GetMapping("/summary")
    public String summary(HttpSession session, Model model) {
        InterviewSession interviewSession = (InterviewSession) session.getAttribute(SESSION_KEY);

        if (interviewSession == null || interviewSession.getCompletedPairs().isEmpty()) {
            return "redirect:/";
        }

        double avgScore = interviewSession.getCompletedPairs().stream()
                .mapToInt(QAPair::getScore)
                .average()
                .orElse(0);

        model.addAttribute("interviewSession", interviewSession);
        model.addAttribute("avgScore", String.format("%.1f", avgScore));
        model.addAttribute("avgScoreRaw", avgScore);
        return "summary";
    }

    @PostMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/";
    }
}
