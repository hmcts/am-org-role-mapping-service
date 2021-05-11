package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.util.Optional;

@Service
public class PersistenceService {


    private RefreshJobsRepository refreshJobsRepository;


    public PersistenceService(RefreshJobsRepository refreshJobsRepository) {
        this.refreshJobsRepository = refreshJobsRepository;
    }


    public Optional<RefreshJobEntity> fetchRefreshJobById(Long jobId) {

        return refreshJobsRepository.findById(jobId);
    }

    public void persistRefreshJob(RefreshJobEntity refreshJobEntity) {

        refreshJobsRepository.save(refreshJobEntity);

    }


}
