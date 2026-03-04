package es.marcha.backend.core.shared.utils;

public class Validations {

    public static boolean validateEmail(String email) {
        if (email == null || email.equals(""))
            return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static String createUsername(String name, String surname) {
        String username = name.substring(0, 3).concat(surname.substring(0, 3)).toLowerCase();
        System.out.println(username);
        return username;
    }

    /**
     * @deprecated Las contraseñas se verifican con
     *             {@code PasswordEncoder.matches()} (BCrypt).
     *             Este método solo se mantiene por compatibilidad; no debe usarse
     *             directamente.
     */
    @Deprecated
    public static boolean comparePasswords(String fromCredentials, String fromUser) {
        return fromCredentials.equals(fromUser);
    }
}
