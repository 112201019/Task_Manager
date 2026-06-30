package com.projects.task_manager.controller;


import com.projects.task_manager.dto.AddUserRequestDto;
import com.projects.task_manager.dto.EditUserDto;
import com.projects.task_manager.dto.UserDto;
import com.projects.task_manager.entity.Users;
import com.projects.task_manager.service.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceInterface usersService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody AddUserRequestDto requestDto) {
        UserDto newUser = usersService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser(@AuthenticationPrincipal Users currentUser) {
        UserDto user = usersService.fetchUser(currentUser.getUserId());
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/edit")
    public ResponseEntity<Void>  editUser(@AuthenticationPrincipal Users currentUser, @RequestBody EditUserDto editRequest){
        editRequest.setUserId(currentUser.getUserId());
        usersService.editUser(editRequest);
        return ResponseEntity.ok().build();
    }

    //this login is handled now by the spring securityy//
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Users currentUser){
        usersService.deleteUser(currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

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
//    @GetMapping("/register")
//    public String register() {
//        return "register";
//    }
//
//    @PostMapping("/register-submit")
//    public String registerUserFromForm(@ModelAttribute AddUserRequestDto requstdto, HttpSession session) {
//        usersService.createUser(requstdto);

//        return "redirect:/login";

//    }


//    @PostMapping("/logout")
//    public String logOut(HttpSession session) {
//        session.invalidate();
//        return "redirect:/";
//    }
}