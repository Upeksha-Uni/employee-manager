
package com.example.employeemanager.controller;

import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ================= WEB PAGE CONTROLLERS =================

    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute(
            "employees", employeeService.getAllEmployees()
        );

        model.addAttribute("employee", new Employee());

        return "employees";
    }

    // Add employee from web page
    @PostMapping("/employees")
    public String addEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!result.hasErrors()
                && employeeService.emailExists(employee.getEmail())) {
            result.rejectValue(
                "email", "duplicate",
                "An employee with this email already exists"
            );
        }

        if (result.hasErrors()) {
            model.addAttribute(
                "employees", employeeService.getAllEmployees()
            );
            return "employees";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
            "successMessage", "Employee added successfully"
        );

        return "redirect:/employees";
    }

    // Show edit page
    @GetMapping("/employees/{id}/edit")
    public String editEmployee(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return employeeService.getEmployeeById(id)
            .map(employee -> {
                model.addAttribute("employee", employee);
                return "edit-employee";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found"
                );
                return "redirect:/employees";
            });
    }

    // Update employee from web page
    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        Employee existing = employeeService.getEmployeeById(id)
            .orElse(null);

        if (existing == null) {
            redirectAttributes.addFlashAttribute(
                "errorMessage", "Employee not found"
            );
            return "redirect:/employees";
        }

        if (!result.hasErrors()
                && employeeService.emailExistsForOtherEmployee(
                    employee.getEmail(), id)) {
            result.rejectValue(
                "email", "duplicate",
                "Another employee already uses this email"
            );
        }

        if (result.hasErrors()) {
            employee.setId(id);
            model.addAttribute("employee", employee);
            return "edit-employee";
        }

        employee.setId(id);
        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
            "successMessage", "Employee updated successfully"
        );

        return "redirect:/employees";
    }

    // Delete employee from web page
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (employeeService.getEmployeeById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "errorMessage", "Employee not found"
            );
            return "redirect:/employees";
        }

        employeeService.deleteEmployee(id);

        redirectAttributes.addFlashAttribute(
            "successMessage", "Employee deleted successfully"
        );

        return "redirect:/employees";
    }


    // ================= REST API CONTROLLERS =================

    // GET: Retrieve all employees
    @GetMapping("/api/employees")
    @ResponseBody
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // GET: Retrieve one employee
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Long id) {

        return employeeService.getEmployeeById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Create an employee
    @PostMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<?> createEmployee(
            @Valid @RequestBody Employee employee) {

        if (employeeService.emailExists(employee.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("An employee with this email already exists");
        }

        Employee saved = employeeService.saveEmployee(employee);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT: Update an employee
    @PutMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee) {

        Employee existing = employeeService.getEmployeeById(id)
            .orElse(null);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (employeeService.emailExistsForOtherEmployee(
                employee.getEmail(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Another employee already uses this email");
        }

        employee.setId(id);

        Employee updated = employeeService.saveEmployee(employee);

        return ResponseEntity.ok(updated);
    }

    // DELETE: Remove an employee
    @DeleteMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteEmployeeApi(
            @PathVariable Long id) {

        if (employeeService.getEmployeeById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }
}
