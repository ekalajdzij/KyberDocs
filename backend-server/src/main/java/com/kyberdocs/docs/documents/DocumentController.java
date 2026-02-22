package com.kyberdocs.docs.documents;

import com.kyberdocs.docs.documents.dto.DocumentSummaryDto;
import com.kyberdocs.docs.users.User;
import com.kyberdocs.docs.users.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "cookieAuth")
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummaryDto>> getAllMyDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updateHeartbeat(user);

        return ResponseEntity.ok(documentService.getAllDocuments(user));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestParam("file") MultipartFile file) {
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userService.updateHeartbeat(user);
            Document savedDoc = documentService.uploadDocument(user, file);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedDoc.getId());
            response.put("message", "Document uploaded successfully.");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable Long id) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updateHeartbeat(user);

        byte[] data = documentService.downloadAndDecryptDocument(id, user);
        String filename = documentService.getFilename(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable Long id) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updateHeartbeat(user);

        documentService.deleteDocument(id, user);
        return ResponseEntity.ok("Document deleted successfully");
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<DocumentSummaryDto>> getAllSystemDocuments() {
        return ResponseEntity.ok(documentService.getAllSystemDocuments());
    }

}