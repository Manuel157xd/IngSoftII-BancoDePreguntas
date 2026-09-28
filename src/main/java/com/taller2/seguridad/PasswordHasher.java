package com.taller2.seguridad;

public interface PasswordHasher {

    String hash(String password);

    boolean verificar(String password, String hash);
}