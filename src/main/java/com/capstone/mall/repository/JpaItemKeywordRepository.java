package com.capstone.mall.repository;

import com.capstone.mall.model.itemKeyword.ItemKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaItemKeywordRepository extends JpaRepository<ItemKeyword, Long> {

}
