package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

@Service
public class PersistenceAccessTypeService {

    private final AccessTypesRepository accessTypesRepository;

    public PersistenceAccessTypeService(AccessTypesRepository accessTypesRepository) {
        this.accessTypesRepository = accessTypesRepository;
    }


    public AccessTypes getAccessType(String json) {
        return accessTypesRepository.findByAccessType(json);
    }

    public AccessTypes getVersion(Long version) {
        return accessTypesRepository.findByVersion(version);
    }
}
