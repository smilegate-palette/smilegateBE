package org.example.smilegate.user.service;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super("이미 중복된 닉네임입니다");
    }
}

