package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario,Long>{

   // Usuario findByUsername(String username);

   Usuario findByUsername(String username);

   Usuario findByCorreo(String correo);

   @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.permisos")
   List<Usuario> findAllWithPermisos();

   @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.permisos WHERE u.correo = :correo")
   Usuario findByCorreoWithPermisos(@Param("correo") String correo);

}