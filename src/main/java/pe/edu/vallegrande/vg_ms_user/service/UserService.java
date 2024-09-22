package pe.edu.vallegrande.vg_ms_user.service;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class UserService {


    public Flux<ExportedUserRecord> getAllFirebaseUsers() {
        return Mono.fromCallable(() -> {
                    ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
                    return page.getValues();
                }).flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> {
                    // Manejo de errores
                    System.err.println("Error fetching users from Firebase: " + e.getMessage());
                    return Flux.empty();
                });
    }

    public Mono<UserRecord> getUserByUid(String uid) {
        return Mono.fromCallable(() -> FirebaseAuth.getInstance().getUser(uid))
                .onErrorResume(e -> {
                    // Manejo de errores
                    System.err.println("Error fetching user with UID " + uid + " from Firebase: " + e.getMessage());
                    return Mono.empty();
                });
    }

    // Método para filtrar usuarios por un claim 'role' específico
    public Flux<ExportedUserRecord> getUsersByRole(String role) {
        return getAllFirebaseUsers()
                .filter(userRecord -> {
                    Map<String, Object> claims = userRecord.getCustomClaims();
                    return role.equals(claims.get("role"));
                });
    }

}
