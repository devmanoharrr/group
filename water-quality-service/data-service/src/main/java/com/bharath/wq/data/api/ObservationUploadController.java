package com.bharath.wq.data.api;

import com.bharath.wq.data.api.dto.CreateObservationRequest;
import com.bharath.wq.data.api.dto.CreateObservationResult;
import com.bharath.wq.data.service.ImageStorageService;
import com.bharath.wq.data.service.ObservationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/observations")
public class ObservationUploadController {

  private final ObservationService service;
  private final ImageStorageService storage;

  public ObservationUploadController(ObservationService service, ImageStorageService storage) {
    this.service = service;
    this.storage = storage;
  }

  @PostMapping(
      path = "/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CreateObservationResult> createWithUploads(
      @Valid @RequestPart("payload") CreateObservationRequest payload,
      @RequestPart(name = "images", required = false) List<MultipartFile> images,
      UriComponentsBuilder uriBuilder) {

    final var savedPaths = storage.store(images);
    final var merged =
        new CreateObservationRequest(
            payload.citizenId(),
            payload.postcode(),
            payload.temperatureC(),
            payload.pH(),
            payload.alkalinityMgL(),
            payload.turbidityNTU(),
            payload.observations(),
            savedPaths, // override image paths with what we stored
            payload.authority());

    final String id = service.create(merged);
    final URI location = ObservationService.createdLocation(uriBuilder, id);
    return ResponseEntity.created(location).body(new CreateObservationResult(id));
  }
}
