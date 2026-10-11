package com.campusfind.backend.api;

import com.campusfind.backend.claim.Claim;
import com.campusfind.backend.claim.ClaimRepository;
import com.campusfind.backend.item.Item;
import com.campusfind.backend.item.ItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;

    public ApiController(ItemRepository itemRepository, ClaimRepository claimRepository) {
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "CampusFind Backend");
    }

    @GetMapping("/items")
    public List<Item> getItems(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {
        return itemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(item -> item.getStatus().equals("OPEN"))
                .filter(item -> type == null || type.isBlank()
                        || type.equalsIgnoreCase("ALL") || item.getType().equalsIgnoreCase(type))
                .filter(item -> category == null || category.isBlank()
                        || category.equalsIgnoreCase("ALL") || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> q == null || q.isBlank()
                        || contains(item.getTitle(), q)
                        || contains(item.getDescription(), q)
                        || contains(item.getLocation(), q)
                        || contains(item.getCategory(), q))
                .toList();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Item createItem(@Valid @RequestBody CreateItemRequest request) {
        Item item = new Item();
        item.setTitle(request.title().trim());
        item.setType(request.type().trim().toUpperCase(Locale.ROOT));
        item.setCategory(request.category().trim());
        item.setDescription(request.description().trim());
        item.setLocation(request.location().trim());
        item.setContactName(request.contactName().trim());
        item.setContactEmail(request.contactEmail().trim().toLowerCase(Locale.ROOT));

        if (!item.getType().equals("LOST") && !item.getType().equals("FOUND")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type must be LOST or FOUND.");
        }
        return itemRepository.save(item);
    }

    @PostMapping("/items/{itemId}/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public Claim createClaim(@PathVariable Long itemId, @Valid @RequestBody CreateClaimRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
        if (!"OPEN".equals(item.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This report is no longer open for claims.");
        }

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimantName(request.claimantName().trim());
        claim.setClaimantEmail(request.claimantEmail().trim().toLowerCase(Locale.ROOT));
        claim.setMessage(request.message().trim());
        return claimRepository.save(claim);
    }

    @GetMapping("/items/{itemId}/claims")
    public List<Claim> getClaims(@PathVariable Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found.");
        }
        return claimRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().filter(claim -> claim.getItem().getId().equals(itemId)).toList();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    public record CreateItemRequest(
            @NotBlank @Size(max = 100) String title,
            @NotBlank @Size(max = 12) String type,
            @NotBlank @Size(max = 60) String category,
            @NotBlank @Size(max = 1500) String description,
            @NotBlank @Size(max = 120) String location,
            @NotBlank @Size(max = 80) String contactName,
            @NotBlank @Email @Size(max = 160) String contactEmail) {}

    public record CreateClaimRequest(
            @NotBlank @Size(max = 80) String claimantName,
            @NotBlank @Email @Size(max = 160) String claimantEmail,
            @NotBlank @Size(max = 1000) String message) {}
}