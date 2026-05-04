package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.example.model.PersonalAutorizado;
import org.example.repository.PersonalRepository;
@Service
public class PersonalService {

    @Autowired
    private PersonalRepository repository;

    public PersonalAutorizado guardar(PersonalAutorizado p) {
        return repository.save(p);
    }

    public PersonalAutorizado buscarPorNumero(String numero) {
        if (numero == null || numero.length() < 10) {
            return null;
        }

        String diezDigitos = numero.substring(numero.length() - 10);

        System.out.println("🔍 Buscando en DB personal que termine en: " + diezDigitos);

        return repository.findByUltimosDiez(diezDigitos).orElse(null);
    }

    public List<PersonalAutorizado> obtenerTodos() {
        return repository.findAll();
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}