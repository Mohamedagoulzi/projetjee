package org.example.projectjee.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private final RestTemplate restTemplate;
    private static final String PYTHON_BACKEND_URL = "http://localhost:8000/search";

    public SemanticSearchService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Long> searchProductIds(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Construit le body pour l'API Python
            Map<String, Object> requestBody = Map.of(
                    "query", query,
                    "n_results", 20);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_BACKEND_URL,
                    requestBody,
                    Map.class);

            if (response.getBody() != null && response.getBody().containsKey("product_ids")) {
                List<Integer> ids = (List<Integer>) response.getBody().get("product_ids");
                return ids.stream()
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'appel au service sémantique Python: " + e.getMessage());
        }

        return Collections.emptyList();
    }
}
