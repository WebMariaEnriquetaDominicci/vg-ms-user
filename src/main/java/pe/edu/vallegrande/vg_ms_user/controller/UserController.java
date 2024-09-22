package pe.edu.vallegrande.vg_ms_user.controller;

import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.firebase.auth.ExportedUserRecord;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vg_ms_user.dto.TokenValidationResponse;
import pe.edu.vallegrande.vg_ms_user.service.FirebaseJWT;
import pe.edu.vallegrande.vg_ms_user.dto.LoginRequest;
import pe.edu.vallegrande.vg_ms_user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/firebase-users")
public class UserController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final UserService userService;

    private final FirebaseJWT firebaseJwt;

    @Autowired
    public UserController(UserService userService, FirebaseJWT firebaseJwt) {
        this.firebaseJwt = firebaseJwt;
        this.userService = userService;
    }


    @GetMapping("/all")
    public Flux<ExportedUserRecord> getAllUsers() {
        return userService.getAllFirebaseUsers();
    }

    @GetMapping("/{uid}")
    public Mono<UserRecord> getUserByUid(@PathVariable String uid) {
        return userService.getUserByUid(uid);
    }

    @GetMapping("/claims")
    public Flux<ExportedUserRecord> getUsersByRole(@RequestParam String role) {
        return userService.getUsersByRole(role);
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestBody LoginRequest loginRequest) {
        String apiKey = "AIzaSyAuAO14fRSFNVRf_YnSVUV_9k8fhMlIIIo"; // Sustituye con tu API Key de Firebase

        return webClientBuilder.build()
                .post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey)
                .bodyValue(Map.of(
                        "email", loginRequest.getEmail(),
                        "password", loginRequest.getPassword(),
                        "returnSecureToken", true
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Obtener el token JWT de la respuesta de Firebase
                    String idToken = (String) response.get("idToken");
                    return idToken;
                });
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return Mono.fromCallable(() -> {
                try {
                    FirebaseToken decodedToken = firebaseJwt.verifyToken(token);
                    String role = firebaseJwt.getRoleFromToken(decodedToken);
                    return ResponseEntity.ok(new TokenValidationResponse(true, role));
                } catch (Exception e) {
                    // Maneja errores de validación aquí
                }
                return ResponseEntity.ok(new TokenValidationResponse(false, null));
            });
        }
        return Mono.just(ResponseEntity.ok(new TokenValidationResponse(false, null)));
    }



}





