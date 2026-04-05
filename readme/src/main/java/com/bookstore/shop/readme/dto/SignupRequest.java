package com.bookstore.shop.readme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "^01[016789][0-9]{7,8}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,
        @NotBlank String address,
        Boolean marketingAgreed
) {
}
