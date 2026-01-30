package com.example.cinema.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import com.example.cinema.model.Film;
import com.example.cinema.service.FilmService;
import com.example.cinema.service.GenreService;

@Controller
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final GenreService genreService;

    public FilmController(FilmService filmService, GenreService genreService) {
        this.filmService = filmService;
        this.genreService = genreService;
    }

    // Afficher la liste des films
    @GetMapping
    public String listFilms(Model model) {
        model.addAttribute("films", filmService.findAll());
        
        return "films/list"; // /WEB-INF/views/films/list.jsp
    }

    // Formulaire création film
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("film", new Film());
         model.addAttribute("genres", genreService.findAll());
        return "films/form"; // /WEB-INF/views/films/form.jsp
    }

    // Sauvegarder film (POST)
@PostMapping("/save")
public String saveFilm(@ModelAttribute Film film,
                       @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

    if (!imageFile.isEmpty()) {
        // Chemin vers src/main/resources/static/images
        Path uploadPath = Paths.get(new File("src/main/resources/static/images").getAbsolutePath());

        // Crée le dossier si nécessaire
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Vérifier l'extension (optionnel mais recommandé)
        String originalFilename = imageFile.getOriginalFilename();
        if (!originalFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw new IllegalArgumentException("Seules les images sont autorisées");
        }

        // Générer un nom unique
        String filename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        // Sauvegarder le fichier
        imageFile.transferTo(filePath.toFile());

        // Enregistrer le nom du fichier dans l'entité
        film.setImage(filename);
    }

    filmService.save(film);
    return "redirect:/films";
}


    // Formulaire édition film
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Film film = filmService.findById(id)
                .orElseThrow(() -> new RuntimeException("Film not found"));
        model.addAttribute("film", film);
         model.addAttribute("genres", genreService.findAll());
        return "films/form";
    }

    // Supprimer film
    @GetMapping("/delete/{id}")
    public String deleteFilm(@PathVariable Long id) {
        filmService.deleteById(id);
        return "redirect:/films";
    }

    @GetMapping("/fiche/{id}")
public String ficheFilm(@PathVariable Long id, Model model) {
    Film film = filmService.findById(id)
            .orElseThrow(() -> new RuntimeException("Film non trouvé"));
    model.addAttribute("film", film);
    return "films/fiche";
}
}
