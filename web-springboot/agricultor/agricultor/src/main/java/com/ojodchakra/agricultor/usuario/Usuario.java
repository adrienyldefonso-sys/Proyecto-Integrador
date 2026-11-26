package com.ojodchakra.agricultor.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Usuario de Django (tabla monitoreo_usuario). Spring también puede crear usuarios,
 * así que mapea todas las columnas NOT NULL que Django exige.
 */
@Entity
@Table(name = "monitoreo_usuario")
public class Usuario {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "password", nullable = false, length = 128)
    private String contrasena;

    @Column(name = "is_superuser", nullable = false)
    private boolean superusuario;

    @Column(name = "username", nullable = false, length = 150, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false, length = 150)
    private String nombre;

    @Column(name = "last_name", nullable = false, length = 150)
    private String apellido;

    @Column(name = "email", nullable = false, length = 254)
    private String correo;

    @Column(name = "is_staff", nullable = false)
    private boolean staff;

    @Column(name = "is_active", nullable = false)
    private boolean activo;

    @Column(name = "date_joined", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    protected Usuario() {
        // Lo exige JPA
    }

    /** Usado por las pruebas existentes: solo importan el id y si está activo. */
    public Usuario(String id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    /** Usuario nuevo creado desde la app móvil. La contraseña llega ya cifrada. */
    public Usuario(String id, String username, String contrasenaCifrada, String nombre, String telefono) {
        this.id = id;
        this.username = username;
        this.contrasena = contrasenaCifrada;
        this.nombre = nombre;
        this.apellido = "";
        this.correo = "";
        this.telefono = telefono;
        this.superusuario = false;
        this.staff = false;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now(ZoneOffset.UTC);
        this.rol = "AGRICULTOR";
    }

    public String getId() { return id; }
    public boolean isActivo() { return activo; }
    public String getUsername() { return username; }
    public String getContrasena() { return contrasena; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
}
