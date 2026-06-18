package com.example.demo.endpoint.rest.controller.health;

import com.example.demo.PojaGenerated;
import com.example.demo.db.DatabaseConfig;
import java.sql.Connection;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class PingController {

  public static final ResponseEntity<String> OK = new ResponseEntity<>("OK", HttpStatus.OK);
  public static final ResponseEntity<String> KO =
      new ResponseEntity<>("KO", HttpStatus.INTERNAL_SERVER_ERROR);

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @GetMapping("/ping-db")
  public ResponseEntity<String> pingDb() {
    try (Connection conn = DatabaseConfig.getConnection()) {
      return ResponseEntity.ok("Connexion Neon OK !");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
    }
  }
}
