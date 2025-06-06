package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import it.aulab.aulab_chronicle.services.CareerRequestService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
@RequestMapping("/operations")
public class OperationController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRequestService careerRequestService;

    // rotta get per richiesta collaborazione
    @GetMapping("/career/request")
    public String careerRequestCreate(Model viewModel) {

        viewModel.addAttribute("title", "Submit your request");
        viewModel.addAttribute("careerRequest", new CareerRequest());

        List<Role> roles = roleRepository.findAll();
        // elimino role user da form
        roles.removeIf(e -> e.getName().equals("ROLE_USER"));
        viewModel.addAttribute("roles", roles);

        return "career/requestForm";
    }

    // rotta post per richiesta collaborazione
    @PostMapping("/career/request/save")
    public String careerRequestStore(@ModelAttribute("careerRequest") CareerRequest careerRequest, Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(principal.getName());

        if (careerRequestService.isRoleAlreadyAssigned(user, careerRequest)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are already assigned to this role");
            return "redirect:/";
        }

        careerRequestService.save(careerRequest, user);

        redirectAttributes.addFlashAttribute("successMessage", "Request successfully submitted");

        return "redirect:/";

    }

    // rotta get per visualizzazione dettaglio richiesta
    @GetMapping("career/request/detail/{id}")
    public String careerRequestDetail(@PathVariable("id") Long id, Model viewModel) {

        viewModel.addAttribute("title", "Request Detail");
        viewModel.addAttribute("request", careerRequestService.find(id));

        return "career/requestDetail";
    }

    // rotta post per accettazione richiesta
    @PostMapping("/career/request/accept/{requestId}")
    public String careerReqeustAccept(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {

        careerRequestService.careerAccept(requestId);

        redirectAttributes.addFlashAttribute("successMessage", "Request successfully accepted, role assigned");

        return "redirect:/admin/dashboard";
        
    }
    
    

}
