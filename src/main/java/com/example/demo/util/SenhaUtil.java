package com.example.demo.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário para operações de criptografia de senha usando BCrypt.
 */
public class SenhaUtil {

    /**
     * Gera um hash BCrypt para uma senha em texto plano.
     *
     * @param senhaPlana A senha em texto plano.
     * @return O hash BCrypt da senha.
     */
    public static String hashSenha(String senhaPlana) {
        return BCrypt.hashpw(senhaPlana, BCrypt.gensalt());
    }

    /**
     * Verifica se uma senha em texto plano corresponde a um hash BCrypt.
     *
     * @param senhaPlana A senha em texto plano fornecida pelo usuário.
     * @param hashArmazenado O hash BCrypt armazenado no banco de dados.
     * @return true se corresponder, false caso contrário.
     */
    public static boolean verificarSenha(String senhaPlana, String hashArmazenado) {
        //uma verificação de null aqui para evitar erros
        if (hashArmazenado == null) {
            System.err.println("Senha armazenada no banco é nula para verificação.");
            return false;
        }
        return BCrypt.checkpw(senhaPlana, hashArmazenado);
    }
}