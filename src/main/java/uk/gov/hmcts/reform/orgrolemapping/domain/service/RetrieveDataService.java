package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

@Service
public class RetrieveDataService {
    //1. Fetching multiple case-worker user details from CRD
    //2. Fetching multiple judicial user details from JRD
    private final CRDFeignClient crdFeignClient;

    public RetrieveDataService(CRDFeignClient crdFeignClient) {
        this.crdFeignClient = crdFeignClient;
    }


}
