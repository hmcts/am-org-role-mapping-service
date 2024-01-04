package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

@Service
public class AccessTypeService {


    private final AccessTypesRepository accessTypesRepository;

    public AccessTypeService(AccessTypesRepository accessTypesRepository) {
        this.accessTypesRepository = accessTypesRepository;
    }

    public AccessTypes getVersion(Long version) {
        return accessTypesRepository.findByVersion(version);
    }

    public AccessTypes getAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc();
    }
}
