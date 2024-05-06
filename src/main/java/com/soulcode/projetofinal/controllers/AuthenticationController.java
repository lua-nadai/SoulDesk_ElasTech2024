package com.soulcode.projetofinal.controllers;

import com.soulcode.projetofinal.models.Person;
import com.soulcode.projetofinal.repositories.PersonRepository;
import com.soulcode.projetofinal.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    AuthenticationService authenticationService;

    @RequestMapping(value = "/register-user", method = RequestMethod.POST)
    public String save(@RequestParam String name, @RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword, @RequestParam int typeId, Model model) {

        if (authenticationService.checkIfEmailExists(email)) {
            model.addAttribute("error", "This email is already in use. Please choose another one.");
            return "register-user";
        }

        if (!authenticationService.confirmPassword(password, confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register-user";
        }

        authenticationService.registerNewUser(name, email, password, typeId);

        return "redirect:/login-user";
    }

    @RequestMapping(value = "/login-user", method = RequestMethod.POST)
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpServletRequest request) {
        // Search in the database if the person already exists using the email
        Person user = personRepository.findByEmail(email);

        // Check if the user exists and if the password is correct
        if (user != null && user.getPassword().equals(password)) {
            int userType = user.getType().getId();

            // Create a session and set the user as logged in, storing the logged-in user's information in the cache
            HttpSession session = request.getSession();
            session.setAttribute("loggedInUser", user);

            // Redirect to the appropriate page based on the user type (technician or client)
            if (userType == 1) {
                return "redirect:/user-page?name=" + user.getName();
            } else {
                return "redirect:/technician-page?name=" + user.getName();
            }
        } else if (user != null && user.getEmail().equals(email)) {
            model.addAttribute("error", "Incorrect password");
        } else {
            model.addAttribute("error", "Incorrect email and password");
        }

        return "login-user";
    }
}
