package service.userservice.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.userservice.userservice.model.Medicine;
import service.userservice.userservice.repository.MedicineRepository;
import java.util.Map;

@RestController @RequestMapping("/api/v1/pharmacist/medicines")
public class PharmacistController {
    @Autowired private MedicineRepository medRepo;

    @GetMapping
    public ResponseEntity<?> getAllMedicines() { 
        return ResponseEntity.ok(Map.of("success", true, "data", medRepo.findAll())); 
    }

    @PostMapping
    public ResponseEntity<?> addMedicine(@RequestBody Medicine medicine) {
        return ResponseEntity.status(201).body(Map.of(
            "success", true, 
            "message", "Medicine added successfully to catalog.", 
            "data", medRepo.save(medicine)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicineStock(@PathVariable String id, @RequestBody Medicine payload) {
        return medRepo.findById(id).map(m -> {
            if (payload.getPrice() != null) m.setPrice(payload.getPrice());
            if (payload.getQuantity() != null) m.setQuantity(payload.getQuantity());
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Medicine inventory details altered successfully.", 
                "data", medRepo.save(m)
            ));
        }).orElse(ResponseEntity.status(404).body(Map.of("success", false, "message", "Medicine not found.")));
    }
}