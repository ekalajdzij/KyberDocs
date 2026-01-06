package com.kyberdocs.docs.beneficiaries;

import com.kyberdocs.docs.beneficiaries.dto.AddBeneficiaryRequestDto;
import com.kyberdocs.docs.beneficiaries.dto.BeneficiaryDto;
import com.kyberdocs.docs.users.User;
import com.kyberdocs.docs.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserService userService;

    public BeneficiaryController(BeneficiaryService beneficiaryService, UserService userService) {
        this.beneficiaryService = beneficiaryService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryDto>> getMyBeneficiaries(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(beneficiaryService.getBeneficiariesForUser(user));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<BeneficiaryDto>> getAllSystemBeneficiaries() {
        return ResponseEntity.ok(beneficiaryService.getAllSystemBeneficiaries());
    }

    @PostMapping
    public ResponseEntity<BeneficiaryDto> addBeneficiary(@AuthenticationPrincipal UserDetails userDetails,
                                                         @RequestBody AddBeneficiaryRequestDto request) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                beneficiaryService.addBeneficiary(user, request.getDocumentId(), request.getTargetUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBeneficiary(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long id) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        beneficiaryService.deleteBeneficiary(id, user);
        return ResponseEntity.ok("Beneficiary removed successfully");
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<BeneficiaryDto>> getFilesSharedWithMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(beneficiaryService.getSharedWithMe(user));
    }
}