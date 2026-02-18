package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd;

import java.util.List;

public enum Appointment implements AppointmentEnum {

    // NB: for codes see:
    //  https://tools.hmcts.net/confluence/display/DTSAM/Judicial+Reconciliation#JudicialReconciliation-AppointmentCodes

    PRESIDENT_ET_SCOTLAND("President, Employment Tribunals (Scotland)", List.of("153")),
    PRESIDENT_OF_TRIBUNAL("President of Tribunal", List.of("65")),
    MAGISTRATE("Magistrate", List.of("1")),
    REGIONAL_EMPLOYMENT_JUDGE("Regional Employment Judge", List.of("71")),

    // NB: Tribunal members also form a group, see AppointmentGroup.ANY_TRIBUNAL_MEMBER
    TRIBUNAL_MEMBER("Tribunal Member", List.of("85")),
    TRIBUNAL_MEMBER_DENTIST("Tribunal Member Dentist", List.of("28")),
    TRIBUNAL_MEMBER_DISABILITY("Tribunal Member Disability", List.of("44")),
    TRIBUNAL_MEMBER_DRAINAGE("Tribunal Member Drainage", List.of("47")),
    TRIBUNAL_MEMBER_FARMER("Tribunal Member Farmer", List.of("49")),
    TRIBUNAL_MEMBER_FINANCIALLY_QUALIFIED("Tribunal Member Financially Qualified", List.of("50")),
    TRIBUNAL_MEMBER_LANDOWNER("Tribunal Member Landowner", List.of("54")),
    TRIBUNAL_MEMBER_LAY("Tribunal Member Lay", List.of("55")),
    TRIBUNAL_MEMBER_MEDICAL("Tribunal Member Medical", List.of("58")),
    TRIBUNAL_MEMBER_OPTOMETRIST("Tribunal Member Optometrist", List.of("60")),
    TRIBUNAL_MEMBER_PHARMACIST("Tribunal Member Pharmacist", List.of("63")),
    TRIBUNAL_MEMBER_PROFESSIONAL("Tribunal Member Professional", List.of("66")),
    TRIBUNAL_MEMBER_SERVICE("Tribunal Member Service", List.of("81")),
    TRIBUNAL_MEMBER_VALUER("Tribunal Member Valuer", List.of("88")),
    TRIBUNAL_MEMBER_VALUER_CHAIRMAN("Tribunal Member Valuer Chairman", List.of("89")),

    VICE_PRESIDENT("Vice President", List.of("91")),
    VICE_PRESIDENT_ET_SCOTLAND("Vice-President, Employment Tribunal (Scotland)", List.of("213"));


    private final String name;
    private final List<String> codes; // support for multiple codes in future (i.e. different spellings for same role)

    Appointment(String name, List<String> codes) {
        this.name = name;
        this.codes = codes;
    }

    public String getName() {
        return name;
    }

    public List<String> getCodes() {
        return codes;
    }

}
