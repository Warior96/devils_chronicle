package it.aulab.aulab_chronicle.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;

@Service
public class CareerRequestServiceImpl implements CareerRequestService {

    @Autowired
    private CareerRequestRepository careerRequestRepository;

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
        careerRequestRepository.save(careerRequest);

    }

    @Override
    public void careerAccept(Long requestId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'careerAccept'");
    }

    @Override
    public CareerRequest find(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'find'");
    }

}
