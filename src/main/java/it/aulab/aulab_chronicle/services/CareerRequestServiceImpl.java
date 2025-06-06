package it.aulab.aulab_chronicle.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class CareerRequestServiceImpl implements CareerRequestService {

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {

        List<Long> allUserIds = careerRequestRepository.findAllUserIds();

        if (!allUserIds.contains(user.getId())) {
            return false;
        }

        List<Long> requests = careerRequestRepository.findByUserId(user.getId());

        return requests.stream().anyMatch(roleId -> roleId.equals(careerRequest.getRole().getId()));

    }

    @Override
    public void save(CareerRequest careerRequest, User user) {

        careerRequest.setUser(user);
        careerRequest.setIsChecked(false);
        careerRequest.setIsAccepted(null);
        careerRequestRepository.save(careerRequest);

        // invio email di richiesta collaborazione all'admin
        emailService.sendSimpleEmail("adminAulabChronicle@admin.com",
                "Richiesta per ruolo: " + careerRequest.getRole().getName(),
                "Richiesta di collaborazione da parte di " + user.getUsername());

    }

    @Override
    public void careerAccept(Long requestId) {

        CareerRequest request = careerRequestRepository.findById(requestId).get();

        User user = request.getUser();
        Role role = request.getRole();

        List<Role> rolesUser = user.getRoles();
        Role newRole = roleRepository.findByName(role.getName());
        rolesUser.add(newRole);

        user.setRoles(rolesUser);
        userRepository.save(user);
        request.setIsChecked(true);
        request.setIsAccepted(true);
        careerRequestRepository.save(request);

        emailService.sendSimpleEmail(user.getEmail(), "Richiesta di collaborazione accettata",
                "Salve, la sua richiesta di collaborazione per il " + role.getName() + " è stata accettata.");

    }

    @Override
    public CareerRequest find(Long id) {

        return careerRequestRepository.findById(id).get();

    }

    @Override
    public void markAsChecked(Long requestId) {
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        if (request.getIsChecked() == false) {
            request.setIsChecked(true);
            careerRequestRepository.save(request);
        }
    }

    @Override
    public void careerReject(Long requestId) {
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        request.setIsChecked(true);
        request.setIsAccepted(false);
        careerRequestRepository.save(request);

        User user = request.getUser();
        Role role = request.getRole();
        emailService.sendSimpleEmail(user.getEmail(), "Richiesta di collaborazione rifiutata",
                "Salve, la sua richiesta di collaborazione per il ruolo di " + role.getName()
                        + " non è stata accettata.");
    }

    @Override
    @Transactional
    public void revokeAndReject(Long requestId) {
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        User user = request.getUser();
        Role roleToRemove = request.getRole();

        user.getRoles().removeIf(role -> role.getId().equals(roleToRemove.getId()));
        userRepository.save(user);

        request.setIsAccepted(false);
        careerRequestRepository.save(request);
    }

    @Override
    public boolean hasPendingRequest(User user, Role role) {
        return careerRequestRepository.existsByUserAndRoleAndIsAcceptedIsNull(user, role);
    }

}
