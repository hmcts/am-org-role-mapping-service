package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

@Service
public class PersistenceAccessTypeService {

    private AccessTypesRepository accessTypesRepository;

    public PersistenceAccessTypeService(AccessTypesRepository accessTypesRepository) {
        this.accessTypesRepository = accessTypesRepository;
    }


    public String fetchAccessTypeByVersion(Long version) {
        return accessTypesRepository.findByAccessTypeVersion(version);
    }

}
