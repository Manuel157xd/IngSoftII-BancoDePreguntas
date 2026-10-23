package com.taller2.seguridad;

import com.taller2.negocio.model.Usuario;

public interface CredentialAuthenticator {
    Usuario autenticar(String login, String password);
}
