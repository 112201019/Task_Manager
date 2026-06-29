package com.projects.task_manager.controller;


import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.service.UserServiceInterface;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserServiceInterface usersService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    //this login is handled now by the spring securityy//
//    @PostMapping("/login")
//    public String loggingIn(@RequestParam("loginIdentifier") String loginIdentifier, @RequestParam("password") String password, HttpSession session, Model model){
//        Boolean b = usersService.authenticateUser(loginIdentifier, passwordEncoder.encode(password));
//        UserDto user = usersService.fetchUser(loginIdentifier);
//
//        if(b){
//            session.setAttribute("userId", user.getUserId());
//            return "redirect:/task/getall";
//        }
//        return "login";
//    }


    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register-submit")
    public String registerUserFromForm(@ModelAttribute AddUserRequestDto requstdto, HttpSession session) {
        usersService.createUser(requstdto);
        return "redirect:/login";
    }

    @PostMapping("/delete")
    public String DeleteUser(@AuthenticationPrincipal Users currentUser){
        usersService.deleteUser(currentUser.getUserId());
        return "redirect:/";
    }


    @PostMapping("/edit")
    public String editUser(@AuthenticationPrincipal Users currentUser, @RequestParam String username, @RequestParam String email){
        Long userId = currentUser.getUserId();
        EditUserDto newEdit = new EditUserDto(userId, username, email);
        usersService.editUser(newEdit);
        return "redirect:/task/getall";
    }

    @PostMapping("/logout")
    public String logOut(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}