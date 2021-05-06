package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.util.List;

@Service
public class PersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceService.class);

    private RefreshJobsRepository refreshJobsRepository;


    public PersistenceService(RefreshJobsRepository refreshJobsRepository) {
        this.refreshJobsRepository = refreshJobsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RefreshJobEntity> retrieveRefreshJobs(String actorId) {

        return refreshJobsRepository.findByRefreshJobStatus(actorId);
    }


}
