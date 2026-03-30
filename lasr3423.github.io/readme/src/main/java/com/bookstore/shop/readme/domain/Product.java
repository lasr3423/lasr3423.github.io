package com.bookstore.shop.readme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity @Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity{

}
