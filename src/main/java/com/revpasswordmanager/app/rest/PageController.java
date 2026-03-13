package com.revpasswordmanager.app.rest;

import com.revpasswordmanager.app.dto.UserRequestDTO;
import com.revpasswordmanager.app.dto.UserResponseDTO;
import com.revpasswordmanager.app.dto.SecurityQuestionDTO;
import com.revpasswordmanager.app.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;


@Controller
public class PageController {

    @Autowired
    private UserService userService;



    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            session.invalidate();
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }


    @PostMapping("/web/login")
    public String doLogin(@RequestParam String username,
            @RequestParam String masterPassword,
            HttpSession session, RedirectAttributes redirect) {
        try {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setUsername(username);
            dto.setMasterPassword(masterPassword);

            UserResponseDTO user = userService.login(dto);


            if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
                session.setAttribute("pending2FAUser", user);
                userService.generate2FACode(user.getUserId());
                return "redirect:/2fa";
            }

            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/2fa")
    public String twoFactorPage(HttpSession session) {
        if (session.getAttribute("pending2FAUser") == null) {
            return "redirect:/login";
        }
        return "2fa";
    }

    @PostMapping("/web/verify-2fa")
    public String verify2FA(@RequestParam String code,
            HttpSession session, RedirectAttributes redirect) {
        try {
            UserResponseDTO user = (UserResponseDTO) session.getAttribute("pending2FAUser");
            if (user == null)
                return "redirect:/login";

            userService.validate2FACode(user.getUserId(), code);
            session.removeAttribute("pending2FAUser");
            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/2fa";
        }
    }

    @PostMapping("/web/register")
    public String doRegister(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String masterPassword,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam String q1, @RequestParam String a1,
            @RequestParam String q2, @RequestParam String a2,
            @RequestParam String q3, @RequestParam String a3,
            RedirectAttributes redirect) {
        try {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setMasterPassword(masterPassword);
            dto.setFirstName(firstName);
            dto.setLastName(lastName);

            UserResponseDTO registeredUser = userService.register(dto);

            SecurityQuestionDTO sq1 = new SecurityQuestionDTO();
            sq1.setQuestion(q1);
            sq1.setAnswer(a1);

            SecurityQuestionDTO sq2 = new SecurityQuestionDTO();
            sq2.setQuestion(q2);
            sq2.setAnswer(a2);

            SecurityQuestionDTO sq3 = new SecurityQuestionDTO();
            sq3.setQuestion(q3);
            sq3.setAnswer(a3);

            List<SecurityQuestionDTO> sqList = new ArrayList<>();
            sqList.add(sq1);
            sqList.add(sq2);
            sqList.add(sq3);

            userService.saveSecurityQuestions(registeredUser.getUserId(), sqList);
            redirect.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/recovery")
    public String recoveryPage() {
        return "recovery";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }



    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        UserResponseDTO user = getLoggedInUser(session);
        if (user == null)
            return "redirect:/login";
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/vault")
    public String vault(HttpSession session, Model model) {
        UserResponseDTO user = getLoggedInUser(session);
        if (user == null)
            return "redirect:/login";
        model.addAttribute("user", user);
        return "vault";
    }

    @GetMapping("/generator")
    public String generator(HttpSession session, Model model) {
        UserResponseDTO user = getLoggedInUser(session);
        if (user == null)
            return "redirect:/login";
        model.addAttribute("user", user);
        return "generator";
    }

    @GetMapping("/security")
    public String security(HttpSession session, Model model) {
        UserResponseDTO user = getLoggedInUser(session);
        if (user == null)
            return "redirect:/login";
        model.addAttribute("user", user);
        return "security";
    }



    private UserResponseDTO getLoggedInUser(HttpSession session) {
        return (UserResponseDTO) session.getAttribute("loggedInUser");
    }
}
