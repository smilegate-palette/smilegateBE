package org.example.smilegate.user.service;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("이미 가입된 이메일입니다");
    }
}

