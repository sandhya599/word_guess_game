package com.example.word_guess_game;


import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    private WordRepository wordRepository;
    @GetMapping("/dashboard")
    public String showForm(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        String winmessage = (String) session.getAttribute("win_message");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("levels", new String[]{"Easy", "Medium", "Hard"});
            model.addAttribute("selectedLevel", "");
            model.addAttribute("user", user);
            model.addAttribute("message",winmessage);
            return "word-form";
        } else {
            return "redirect:/";
        }

    }
    @PostMapping("/word")
    public String getWord(@ModelAttribute("selectedLevel") String selectedLevel,
                          Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        session.setAttribute("win_message", null);
        if (user != null) {
            Word word = wordRepository.findRandomWordByLevel(selectedLevel);
            model.addAttribute("word", word);
            session.setAttribute("word", word);
            model.addAttribute("user", user);
            // return "wordDetails";
            return "redirect:/showWord";
        } else {
            return "redirect:/";
        }

    }
    @GetMapping("/showWord")
    public String showWord(HttpSession session, Model model) {


        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");
            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());
            model.addAttribute("user", user);
            return "word-input";
        } else {
            return "redirect:/";
        }
    }
    @PostMapping("/getWord")
    public String login(@RequestParam String word, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user != null) {
            Word wordarray = (Word) session.getAttribute("word");

            if (wordarray == null) {
                model.addAttribute("message", "No word found in the session. Please try again.");
                return "word-input"; // Return the same page if the word is not found in session
            }

            // Retrieve the number of tries from the session, or initialize to 0
            Integer tries = (Integer) session.getAttribute("tries");
            if (tries == null) {
                tries = 0; // Initialize to 0 if no previous tries
            }

            model.addAttribute("GivenHints", wordarray.getHints());
            model.addAttribute("GivenImage", wordarray.getImage());

            if (word != null && wordarray.getWordName().toLowerCase().equals(word.toLowerCase())) {
                // If the guess is correct
                model.addAttribute("message", "Congratulations, You win!");
                model.addAttribute("user", user);

                // Reset tries on success
                session.setAttribute("tries", 0);

                // Update user score
                Integer score = Math.toIntExact(user.getScore() + 10);

                // Redirect to score update page
                return "redirect:/Score/" + user.getId() + "/" + score;
            } else {
                // Increment the number of tries
                tries++;
                session.setAttribute("tries", tries);

                if (tries >= 3) {
                    // If user has exceeded the maximum tries, show a message and reset the tries
                    model.addAttribute("message", "Sorry!! You've used all your tries. The correct word was: " + wordarray.getWordName());
                    session.setAttribute("tries", 0); // Reset tries after 3 attempts
                } else {
                    // Allow the user to try again
                    model.addAttribute("message", "Sorry!! Incorrect guess. You have " + (3 - tries) + " tries left.");
                }

                model.addAttribute("user", user);
                return "word-input"; // Return to the same input form with the error message and tries left
            }

        } else {
            // If the user is not logged in
            return "redirect:/";
        }
    }

}

