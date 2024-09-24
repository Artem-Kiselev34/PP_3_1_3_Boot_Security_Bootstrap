package ru.kiselev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kiselev.repository.RoleRepository;
import ru.kiselev.model.User;
import ru.kiselev.service.UserService;


import javax.validation.Valid;
import java.security.Principal;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listAllUsers(Model model, Principal principal) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("currentUserEmail", principal.getName());
        model.addAttribute("currentUserRoles", userService.findByEmail(principal.getName()).getAuthorities());
        User currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("user", currentUser);
        return "users";
    }

    @GetMapping("/user")
    public String getUserById(@RequestParam(value = "id") Long id, Model model) {
        model.addAttribute("user", userService.findUserById(id));
        return "user";
    }


    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "add";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "add"; // Возвращаемся к форме, если есть ошибки
        }
        try {
            userService.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "error.user", "Учетная запись для этого Email уже существует.");
            model.addAttribute("roles", roleRepository.findAll());
            return "add"; // Возвращаемся к форме, если есть ошибка уникальности
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit")
    public String editUserForm(Long id, Model model) {
        model.addAttribute("user", userService.findUserById(id));
        model.addAttribute("roles", roleRepository.findAll());
        return "edit";
    }

    @PostMapping("/edit")
    public String editeUser(Long id, @Valid @ModelAttribute("user") User user, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "edit";
        }
        try {
            userService.updateUser(id, user);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("email", "error.user", "Учетная запись для Email уже существует.");
            model.addAttribute("roles", roleRepository.findAll());
            return "edit";
        }
        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin";
    }


    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }
}
