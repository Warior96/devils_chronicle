package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CrudService<ReadDto, Model, Key> {

    List<ReadDto> readAll(); // restituisce tutti gli elementi

    ReadDto read(Key key); // restituisce un elemento per key

    ReadDto create(Model model, Principal principal, MultipartFile file); // crea un nuovo elemento

    ReadDto update(Key key, Model model, MultipartFile file); // aggiorna un elemento

    void delete(Key key);

}
