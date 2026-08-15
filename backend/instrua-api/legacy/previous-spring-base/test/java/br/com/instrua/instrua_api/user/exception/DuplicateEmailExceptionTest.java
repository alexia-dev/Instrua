package br.com.instrua.instrua_api.user.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateEmailExceptionTest {

    @Test
    void shouldExposeExpectedMessage() {
        DuplicateEmailException exception = new DuplicateEmailException();

        assertEquals("Já existe um usuário cadastrado com este e-mail.", exception.getMessage());
    }
}
