
package com.example.employeemanager.controller;

import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // READ: Display form and all employees
    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute(
                "employees",
                employeeService.getAllEmployees());

        model.addAttribute("employee", new Employee());

        return "employees";
    }

    // CREATE: Save a new employee
    @PostMapping("/employees")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute(
                    "employees",
                    employeeService.getAllEmployees());
            return "employees";
        }

        if (employeeService.emailExists(employee.getEmail())) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered");

            model.addAttribute(
                    "employees",
                    employeeService.getAllEmployees());

            return "employees";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Employee saved successfully!");

        return "redirect:/employees";
    }

    // UPDATE: Display the edit form
    @GetMapping("/employees/{id}/edit")
    public String editEmployee(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        var employee = employeeService.getEmployeeById(id);

        if (employee.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found");
            return "redirect:/employees";
        }

        model.addAttribute("employee", employee.get());
        return "edit-employee";
    }

    // UPDATE: Save the edited employee
    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        var existing = employeeService.getEmployeeById(id);

        if (existing.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found");
            return "redirect:/employees";
        }

        if (result.hasErrors()) {
            return "edit-employee";
        }

        employee.setId(id);

        if (employeeService.emailExists(employee.getEmail())
                && !employee.getEmail().equalsIgnoreCase(
                        existing.get().getEmail())) {

            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered");

            return "edit-employee";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Employee updated successfully!");

        return "redirect:/employees";
    }

    // DELETE: Remove an employee
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (employeeService.getEmployeeById(id).isPresent()) {
            employeeService.deleteEmployee(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Employee deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found");
        }

        return "redirect:/employees";
    }
}
