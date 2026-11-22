package com.ojodchakra.agricultor.parcela;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelaRepository extends JpaRepository<Parcela, String> {

    List<Parcela> findAllByOrderByNombreAsc();
}