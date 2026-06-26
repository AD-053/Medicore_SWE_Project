package service.userservice.userservice.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import service.userservice.userservice.context.UserContext;
import service.userservice.userservice.model.Appointment;
import service.userservice.userservice.model.DoctorProfile;
import service.userservice.userservice.model.Prescription;
import java.util.List;
import java.util.stream.Collectors;
import service.userservice.userservice.repository.AppointmentRepository;
import service.userservice.userservice.repository.DoctorProfileRepository;
import service.userservice.userservice.repository.PrescriptionRepository;

@RestController 
@RequestMapping("/api/v1/patient")
public class PatientController {
    
    @Autowired private AppointmentRepository apptRepo;
    @Autowired private PrescriptionRepository prescRepo;
    @Autowired private DoctorProfileRepository docRepo;

    /**
     * Helper method to validate if the user is actually a patient.
     */
    private boolean isNotPatient() {
        return !"patient".equalsIgnoreCase(UserContext.getRole());
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> bookAppointment(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Role Validation
            if (isNotPatient()) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden: Only patients can book appointments."));
            }

            // 2. Validate payload exists
            if (!payload.containsKey("doctor_id") || payload.get("doctor_id") == null) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Bad Request: 'doctor_id' is required."));
            }

            String doctorId = String.valueOf(payload.get("doctor_id"));
            
            // 3. Create Appointment
            String doctorNameInput = payload.get("doctor_name") != null
                    ? String.valueOf(payload.get("doctor_name"))
                    : null;

            Appointment appt = Appointment.builder()
                    .patientId(UserContext.getUserId())
                    .patientName(UserContext.getName())
                    .patientPhone(UserContext.getPhone())
                    .doctorId(doctorId)
                    .doctorName(doctorNameInput)
                    .date(payload.get("date") != null ? String.valueOf(payload.get("date")) : "N/A")
                    .symptoms(payload.get("symptoms") != null ? String.valueOf(payload.get("symptoms")) : "N/A")
                    .transactionId(payload.get("transaction_id") != null ? String.valueOf(payload.get("transaction_id")) : "N/A")
                    .isComplete(false)
                    .build();
            
            apptRepo.save(appt);

            // 4. Create Prescription Shell
            Prescription presc = Prescription.builder()
                    .patientId(appt.getPatientId())
                    .doctorId(appt.getDoctorId())
                    .symptoms(appt.getSymptoms())
                    .transactionId(appt.getTransactionId())
                    .build();
                    
            prescRepo.save(presc);

            // 5. Fetch Doctor Profile
            DoctorProfile docProfile = docRepo.findById(doctorId).orElse(new DoctorProfile());
            String doctorName = doctorNameInput != null
                    ? doctorNameInput
                    : (docProfile.getName() != null ? docProfile.getName() : "Unknown");

            // 6. Return Success Response
            return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Appointment booked successfully.",
                "data", Map.of(
                    "prescriptionID", presc.getPrescriptionId(),
                    "patient_id", appt.getPatientId(),
                    "doctor_name", doctorName,
                    "doctor_info", Map.of(
                            "name", doctorName,
                            "doctorId", doctorId,
                            "specialization", docProfile.getSpecialization() != null ? docProfile.getSpecialization() : ""
                    ),
                    "location", docProfile.getLocation() != null ? docProfile.getLocation() : "",
                    "date", appt.getDate(),
                    "serial_no", appt.getSerialNo(),
                    "symptoms", appt.getSymptoms()
                )
            ));

        } catch (Exception e) {
            // IF IT CRASHES NOW, POSTMAN WILL TELL YOU EXACTLY WHY!
            e.printStackTrace(); // This prints the error to your IDE console
            return ResponseEntity.status(500).body(Map.of(
                "success", false, 
                "message", "Server Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<?> getPrescriptions() {
        if (isNotPatient()) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden"));
        // Hide empty "appointment stub" rows — a prescription only counts for the patient
        // once the doctor has actually written something (description OR at least one medicine).
        List<Prescription> filled = prescRepo.findByPatientId(UserContext.getUserId()).stream()
                .filter(p -> (p.getDescription() != null && !p.getDescription().isBlank())
                          || (p.getMedicineDetails() != null && !p.getMedicineDetails().isEmpty()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", filled));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments() {
        if (isNotPatient()) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Forbidden: Only patients can view their appointments."
            ));
        }

        List<Appointment> mine = apptRepo.findByPatientId(UserContext.getUserId());

        // Enrich each appointment with the doctor's name, specialization and location
        // so the patient-side My Appointments table can render without extra calls.
        List<Map<String, Object>> data = mine.stream()
                .sorted((a, b) -> {
                    String ad = a.getDate() != null ? a.getDate() : "";
                    String bd = b.getDate() != null ? b.getDate() : "";
                    return bd.compareTo(ad); // newest date first
                })
                .map(appt -> {
                    DoctorProfile doc = docRepo.findById(appt.getDoctorId()).orElse(new DoctorProfile());
                    String doctorName = appt.getDoctorName() != null
                            ? appt.getDoctorName()
                            : (doc.getName() != null ? doc.getName() : "Unknown");
                    String specialization = doc.getSpecialization() != null ? doc.getSpecialization() : "";
                    String location = doc.getLocation() != null ? doc.getLocation() : "";
                    String status = Boolean.TRUE.equals(appt.getIsComplete()) ? "Completed" : "Booked";

                    Map<String, Object> doctorInfo = new java.util.LinkedHashMap<>();
                    doctorInfo.put("name", doctorName);
                    doctorInfo.put("specialization", specialization);

                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("appointmentId", appt.getSerialNo());
                    row.put("doctor_info", doctorInfo);
                    row.put("specialization", specialization);
                    row.put("date", appt.getDate());
                    row.put("serial_no", appt.getSerialNo());
                    row.put("location", location);
                    row.put("status", status);
                    row.put("symptoms", appt.getSymptoms());
                    return row;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", data
        ));
    }

    @GetMapping("/prescriptions/doctor/{doctorId}")
    public ResponseEntity<?> getPrescriptionsByDoctor(@PathVariable String doctorId) {
        if (isNotPatient()) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden"));
        // Same filter as /prescriptions: drop appointment stubs.
        List<Prescription> filled = prescRepo.findByPatientIdAndDoctorId(UserContext.getUserId(), doctorId).stream()
                .filter(p -> (p.getDescription() != null && !p.getDescription().isBlank())
                          || (p.getMedicineDetails() != null && !p.getMedicineDetails().isEmpty()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", filled));
    }
}