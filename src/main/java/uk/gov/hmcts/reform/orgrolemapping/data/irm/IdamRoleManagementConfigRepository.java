package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdamRoleManagementConfigRepository extends
        JpaRepository<IdamRoleManagementConfigEntity, IdamRoleManagementConfigEntity.CompositeKey> {

    @Query(value = """
        select irmc.*
        from idam_role_management_config irmc, flag_config fc
        where irmc.allow_delete_flag = fc.flag_name
        and fc.status = true
        and fc.env = :env
        """, nativeQuery = true)
    List<IdamRoleManagementConfigEntity> findAllForDeletion(String env);
}
