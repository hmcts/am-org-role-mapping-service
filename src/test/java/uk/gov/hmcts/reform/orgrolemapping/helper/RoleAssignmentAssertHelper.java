package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.poi.util.StringUtil;
//import org.eclipse.jetty.util.StringUtil;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleAssignmentAssertHelper {

    public static class MultiRegion {

        /**
         * Stage 1: Assert the Role Assignment count matches the expected role list after adjusting for multi-region.
         *
         * @param roleAssignments         - list of all role assignments being verified.
         * @param expectedRoleList        - list of all roles that should be returned.
         * @param expectMultiRegion       - flag to say if we expect to be in a multi region scenario.
         * @param rolesThatRequireRegions - list of all the role names that need a region attribute.
         * @param multiRegionList         - list of regions that form the multi-region set.
         */
        public static void assertRoleAssignmentCount(List<RoleAssignment> roleAssignments,
                                                     List<String> expectedRoleList,
                                                     boolean expectMultiRegion,
                                                     List<String> rolesThatRequireRegions,
                                                     List<String> multiRegionList) {

            int expectedRoleAssignmentCount = calculateExpectedRoleAssignmentCount(
                    expectedRoleList,
                    expectMultiRegion,
                    rolesThatRequireRegions,
                    multiRegionList.size()
            );

            assertFalse(roleAssignments.isEmpty());
            assertEquals(expectedRoleAssignmentCount, roleAssignments.size());
            List<String> actualRoleNames = roleAssignments.stream()
                    .map(RoleAssignment::getRoleName).distinct().toList();
            assertEquals(expectedRoleList.size(), actualRoleNames.size());
            assertTrue(actualRoleNames.containsAll(expectedRoleList));
        }


        /**
         * Stage 2: Assert the region is present when required and add value to map to validate later. (see stage 3)
         *
         * @param roleAssignment       - role assignment to review.
         * @param roleNameToRegionsMap - map of all role names that need region and a value to hold all regions found.
         */
        public static void assertRegionStatusAndUpdateRoleToRegionMap(RoleAssignment roleAssignment,
                                                                      Map<String, List<String>> roleNameToRegionsMap) {
            // check region status and add to map
            if (roleNameToRegionsMap.containsKey(roleAssignment.getRoleName())) {
                assertNotNull(roleAssignment.getAttributes().get("region"));
                roleNameToRegionsMap.get(roleAssignment.getRoleName())
                        .add(roleAssignment.getAttributes().get("region").asText());
            } else {
                assertNull(roleAssignment.getAttributes().get("region"));
            }
        }


        /**
         * Stage 3: Assert roleName to region map has been populated as expected. (see stage 2)
         *
         * @param roleNameToRegionsMap - map of all role names that need region and a value to hold all regions found.
         * @param expectedRoleList     - list of all roles that should be returned.
         * @param expectMultiRegion    - flag to say if we expect to be in a multi region scenario.
         * @param multiRegionList      - list of regions that form the multi-region set.
         * @param fallbackRegion       - fallback region to expect if not using multi-region list.
         * @param bookedRoleRegion     - region for booked role
         */
        public static void assertRoleNameToRegionsMapIsAsExpected(Map<String, List<String>> roleNameToRegionsMap,
                                                                  List<String> expectedRoleList,
                                                                  boolean expectMultiRegion,
                                                                  List<String> multiRegionList,
                                                                  String fallbackRegion,
                                                                  String bookedRoleRegion) {
            // verify regions add to map
            roleNameToRegionsMap.forEach((roleName, regions) -> {
                if (expectedRoleList.contains(roleName)) {
                    if (expectMultiRegion) {
                        assertEquals(multiRegionList.size(), regions.size());
                        assertTrue(regions.containsAll(multiRegionList));
                    } else {
                        assertEquals(1, regions.size());
                        // check if booked role: i.e. is judge and has a fallback booking region
                        if ("judge".equals(roleName) && StringUtil.isNotBlank(bookedRoleRegion)) {
                            assertTrue(regions.contains(bookedRoleRegion));
                        } else {
                            assertTrue(regions.contains(fallbackRegion));
                        }
                    }
                } else {
                    assertTrue(regions.isEmpty()); // i.e. roleName not in play
                }
            });
        }


        /**
         * Create a map of all role names that need region attribute and a value to hold all regions found.
         *
         * @param rolesNeedingRegions - list of all the role names that need a region attribute
         * @return map of roleNames each with a list ready store regions found
         */
        public static Map<String, List<String>> buildRoleNameToRegionsMap(List<String> rolesNeedingRegions) {
            // create map for all CTSC roleNames that need regions
            Map<String, List<String>> roleNameToRegionsMap = new HashMap<>();

            rolesNeedingRegions.forEach(roleName -> roleNameToRegionsMap.put(roleName, new ArrayList<>()));

            return roleNameToRegionsMap;
        }


        /**
         * Calculate the expected role assignment count by expanding the list of expected role assignments
         * to account for multi-region scenario.
         *
         * @param expectedRoleList        - list of all roles that should be returned.
         * @param expectMultiRegion       - flag to say if we expect to be in a multi region scenario.
         * @param rolesThatRequireRegions - list of all the role names that need region attribute.
         * @param multiRegionCount        - count of regions that form the multi-region list.
         * @return count of expected role assignments generated.
         */
        private static int calculateExpectedRoleAssignmentCount(List<String> expectedRoleList,
                                                                boolean expectMultiRegion,
                                                                List<String> rolesThatRequireRegions,
                                                                int multiRegionCount) {

            int expectedRoleAssignmentCount = expectedRoleList.size(); // default. i.e. 1-to-1
            if (expectMultiRegion) {
                expectedRoleAssignmentCount = 0; // reset
                for (String roleName : expectedRoleList) {
                    // if expecting region
                    if (rolesThatRequireRegions.contains(roleName)) {
                        expectedRoleAssignmentCount = expectedRoleAssignmentCount + multiRegionCount;
                    } else {
                        expectedRoleAssignmentCount++; // just increment by 1
                    }
                }
            }

            return expectedRoleAssignmentCount;
        }

    }

}
