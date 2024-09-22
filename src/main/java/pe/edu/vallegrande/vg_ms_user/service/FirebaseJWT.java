package pe.edu.vallegrande.vg_ms_user.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseJWT {

    public FirebaseToken verifyToken(String token) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(token);
    }

    public String getRoleFromToken(FirebaseToken token) {
        // Implementa la lógica para extraer el rol desde el token
        return (String) token.getClaims().get("role");
    }
}
