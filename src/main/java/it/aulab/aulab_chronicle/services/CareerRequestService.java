package it.aulab.aulab_chronicle.services;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.User;

public interface CareerRequestService {

    boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest);

    void save(CareerRequest careerRequest, User user);

    void careerAccept(Long requestId);

    CareerRequest find(Long id);

}
