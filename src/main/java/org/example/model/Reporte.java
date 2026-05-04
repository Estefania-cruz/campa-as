package org.example.model;

import javax.persistence.*;
@Entity
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

}