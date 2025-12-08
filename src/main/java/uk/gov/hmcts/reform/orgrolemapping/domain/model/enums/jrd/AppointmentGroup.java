package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd;

import java.util.List;
import java.util.stream.Collectors;

public enum AppointmentGroup implements AppointmentEnum {

    ANY_TRIBUNAL_MEMBER("Any Tribunal Member", List.of(
        Appointment.TRIBUNAL_MEMBER,
        Appointment.TRIBUNAL_MEMBER_DENTIST,
        Appointment.TRIBUNAL_MEMBER_DISABILITY,
        Appointment.TRIBUNAL_MEMBER_DRAINAGE,
        Appointment.TRIBUNAL_MEMBER_FARMER,
        Appointment.TRIBUNAL_MEMBER_FINANCIALLY_QUALIFIED,
        Appointment.TRIBUNAL_MEMBER_LANDOWNER,
        Appointment.TRIBUNAL_MEMBER_LAY,
        Appointment.TRIBUNAL_MEMBER_MEDICAL,
        Appointment.TRIBUNAL_MEMBER_OPTOMETRIST,
        Appointment.TRIBUNAL_MEMBER_PHARMACIST,
        Appointment.TRIBUNAL_MEMBER_PROFESSIONAL,
        Appointment.TRIBUNAL_MEMBER_SERVICE,
        Appointment.TRIBUNAL_MEMBER_VALUER,
        Appointment.TRIBUNAL_MEMBER_VALUER_CHAIRMAN
    ));


    private final String name;
    private final List<String> codes;
    private final List<Appointment> members;

    AppointmentGroup(String name, List<Appointment> appointments) {
        this.name = name;
        this.codes = appointments.stream().flatMap(a -> a.getCodes().stream()).collect(Collectors.toList());
        this.members = appointments;
    }

    public String getName() {
        return name;
    }

    public List<String> getCodes() {
        return codes;
    }

    public List<Appointment> getMembers() {
        return members;
    }

}
